package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.model.SortType;
import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.gps.OnGpsListener;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.model.GpsInfo;
import com.jieli.lib.dv.control.model.MediaInfo;
import com.jieli.lib.dv.control.player.MovWrapper;
import com.jieli.lib.dv.control.player.OnBufferingListener;
import com.jieli.lib.dv.control.player.OnPlaybackListener;
import com.jieli.lib.dv.control.player.OnProgressListener;
import com.jieli.lib.dv.control.player.PlaybackStream;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.baidu.utils.BitmapUtil;
import com.jieli.stream.dv.gdxxx.baidu.utils.MapUtil;
import com.jieli.stream.dv.gdxxx.bean.DeviceSettingInfo;
import com.jieli.stream.dv.gdxxx.interfaces.OnDownloadListener;
import com.jieli.stream.dv.gdxxx.task.SDPServer;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IMediaController;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IRenderView;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IjkVideoView;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.TimeFormate;
import com.jieli.stream.dv.gdxxx.util.VideoManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.view.animation.Animation.RELATIVE_TO_SELF;
import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;
import static com.jieli.stream.dv.gdxxx.util.IConstant.AUDIO_CHANNEL;
import static com.jieli.stream.dv.gdxxx.util.IConstant.AUDIO_FORMAT;
import static com.jieli.stream.dv.gdxxx.util.IConstant.DEV_STA_MODE;
import static com.jieli.stream.dv.gdxxx.util.IConstant.DIR_DOWNLOAD;
import static com.jieli.stream.dv.gdxxx.util.IConstant.RTS_TCP_PORT;

/**
 * Created by 陈森华 on 2017/8/7.
 * 功能：用一句话描述
 */

public class PlaybackDialog extends BaseDialogFragment implements View.OnClickListener, OnDownloadListener, OnGpsListener {
    private String tag = getClass().getSimpleName();
    public final static String VIDEO_PATH = "video_path";
    public final static String VIDEO_CREATE_TIME = "video_create_time";
    public final static String VIDEO_OFFSET = "video_offset";
    private final static int TAG_REFRESH_VIDEO_TIME = 1;
    private final static int MSG_CLEAR_TRACK_POINT = 0X101;
    private final static int REFRESH_VIDEO_TIME_INTERVAL = 500;

    private IjkVideoView mVideoView;
    private ImageView mCancelPlayback;
    private ImageButton mFastForward;
    private ImageButton stopFastForwardIbtn;
    private ImageButton mMovRecordBtn;
    private boolean isIJKPlayerOpen;

    private ImageButton mPlayPause;
    private SDPServer mServer;
    private final int mTCPPort = 5678;
    private VideoManager mVideoManager;
    private PlaybackStream mStreamPlayer;

    private int fastForwardLevel;

    private a mApplication;
    private TextView mTimeTextView;
    private LinearLayout widgetParent;
    private ProgressBar mBufferingProg;
    private ProgressBar mPlaybackProg;
    private ImageView mShrinkButton;
    private ImageView mExpandButton;
    private MapView mapView;
    private boolean isUseMap = false;

    /**
     * 轨迹点集合
     */
    private List<LatLng> trackPoints = new ArrayList<>();
    /**
     * 地图工具
     */
    private MapUtil mapUtil = null;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg != null) {
                switch (msg.what) {
                    case TAG_REFRESH_VIDEO_TIME: {
                        if (mStreamPlayer != null) {
                            long time = mStreamPlayer.getCurrentPosition();
                            if (time > 10000) {
                                Date date = new Date(time);
                                SimpleDateFormat timeFormat = TimeFormate.yyyyMMddHHmmss;
                                mTimeTextView.setText(timeFormat.format(date));
                            }
                            mHandler.sendEmptyMessageDelayed(TAG_REFRESH_VIDEO_TIME, REFRESH_VIDEO_TIME_INTERVAL);
                        }
                        break;
                    }
                    case MSG_CLEAR_TRACK_POINT:
                        if (trackPoints != null) trackPoints.clear();
                        if (mapUtil != null) mapUtil.clearMap();
                        break;
                }
            }
            return false;
        }
    });


    private int[] fastForwardRes = {R.mipmap.ic_fast_forward_1, R.mipmap.ic_fast_forward_2, R.mipmap.ic_fast_forward_4,
            R.mipmap.ic_fast_forward_8, R.mipmap.ic_fast_forward_16, R.mipmap.ic_fast_forward_32, R.mipmap.ic_fast_forward_64};


    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window == null) return;
        //去除动画
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.windowAnimations = 0;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setAttributes(windowParams);
        //边距置0
        window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getDialog().getWindow().getDecorView().setPadding(0, 0, 0, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_play_back_layout, container, false);
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        mVideoView = (IjkVideoView) view.findViewById(R.id.video_view);
        mPlayPause = (ImageButton) view.findViewById(R.id.play_pause);
        mCancelPlayback = (ImageView) view.findViewById(R.id.cancel_playback);
        mFastForward = (ImageButton) view.findViewById(R.id.fast_forward);
        mMovRecordBtn = (ImageButton) view.findViewById(R.id.mov_record_btn);
        stopFastForwardIbtn = (ImageButton) view.findViewById(R.id.stop_fast_forward_btn);
        mTimeTextView = (TextView) view.findViewById(R.id.play_back_time_tv);
        widgetParent = (LinearLayout) view.findViewById(R.id.playback_widget_parent);
        mBufferingProg = (ProgressBar) view.findViewById(R.id.rts_buffering);
        mPlaybackProg = (ProgressBar) view.findViewById(R.id.playback_progress);
        mShrinkButton = (ImageView) view.findViewById(R.id.shrink_button);
        mExpandButton = (ImageView) view.findViewById(R.id.expand_button);
        mapView = (MapView) view.findViewById(R.id.track_query_mapView);

        mVideoView.setMediaController(iMediaController);
        mVideoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        mPlayPause.setOnClickListener(this);
        mCancelPlayback.setOnClickListener(this);
        stopFastForwardIbtn.setOnClickListener(this);
        mFastForward.setOnClickListener(this);
        mMovRecordBtn.setOnClickListener(this);
        mShrinkButton.setOnClickListener(this);
        mExpandButton.setOnClickListener(this);

        mApplication = (a) getActivity().getApplication();
        fastForwardLevel = 0;

        if (DEV_STA_MODE == mApplication.getSearchMode()) {
            Dbug.w(tag, "Current mode is STA");
            mExpandButton.setVisibility(View.VISIBLE);
            mapUtil = MapUtil.getInstance();
            mapUtil.init(mapView);
            BitmapUtil.init();
        } else {
            mExpandButton.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapUtil != null) mapUtil.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapUtil != null) mapUtil.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Dbug.e(tag, "onDestroyView.........................");
        if (mVideoManager != null) {
            if (VideoManager.isDownloading()) {
                stopRecordMov();
            }
            mVideoManager.release();
            mVideoManager = null;
        }

        if (mStreamPlayer != null) {
            mStreamPlayer.unregisterPlayerListener(mPlayerListener);
            mStreamPlayer.release();
        }
        stopMediaPlayer();
        ClientManager.getClient().unregisterNotifyListener(mOnNotifyListener);
        if (mServer != null) {
            mServer.stopRunning();
            mServer = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mapUtil != null) {
            mapUtil.clear();
            BitmapUtil.clear();
        }
        trackPoints.clear();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Dbug.d(tag, "onActivityCreated.............:");
        if (getDialog() == null || getDialog().getWindow() == null) return;
        final WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        getDialog().getWindow().setAttributes(params);

        mStreamPlayer = new PlaybackStream();
        mStreamPlayer.registerPlayerListener(mPlayerListener);
        mStreamPlayer.setOnProgressListener(onProgressListener);
        if (DEV_STA_MODE == mApplication.getSearchMode()) mStreamPlayer.setOnGpsListener(this);

        ClientManager.getClient().registerNotifyListener(mOnNotifyListener);
        Bundle args = getArguments();
        if (args != null) {
            String path = args.getString(VIDEO_PATH);
            int offset = args.getInt(VIDEO_OFFSET, 0);
            ClientManager.getClient().tryToStartPlayback(path, offset, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code != SEND_SUCCESS) {
                        Dbug.e(tag, "Send failed");
                    }
                }
            });
            //Dbug.i(tag,"offset="+offset + ", path=" + path);
            Date date = new Date(args.getLong(VIDEO_CREATE_TIME));
            SimpleDateFormat dateFormat = TimeFormate.yyyyMMddHHmmss;
            mTimeTextView.setText(dateFormat.format(date));
        }
        mStreamPlayer.setOnBufferingListener(onBufferingListener);
    }

    private final OnBufferingListener onBufferingListener = new OnBufferingListener() {
        @Override
        public void onBuffering(int state) {
            if (state== PlaybackStream.BufferingState.START) {
                mBufferingProg.setVisibility(View.VISIBLE);
            } else {
                mBufferingProg.setVisibility(View.GONE);
            }
        }
    };


    private OnNotifyListener mOnNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                return;
            }
            //Dbug.e(tag, "topic=" + data.getTopic());
            switch (data.getTopic()) {
                case Topic.PLAYBACK:
                    if (mStreamPlayer == null) {
                        mStreamPlayer = new PlaybackStream();
                        mStreamPlayer.registerPlayerListener(mPlayerListener);
                    }
                    Dbug.i(tag, "playback data " + data);
                    mStreamPlayer.create(RTS_TCP_PORT, ClientManager.getClient().getConnectedIP());
                    break;

                case Topic.PLAYBACK_FAST_FORWARD:
                    if (data.getParams() == null) {
                        Dbug.e(tag, "PLAYBACK_FAST_FORWARD: data params is null");
                    } else {
                        fastForwardLevel = Integer.valueOf(data.getParams().get(TopicKey.LEVEL));
                        if (fastForwardLevel < fastForwardRes.length) {
                            mFastForward.setImageResource(fastForwardRes[fastForwardLevel]);
                        }
                    }
                    break;
            }
        }
    };

    private void initPlayer(String videoPath) {
        if (mVideoView != null && !TextUtils.isEmpty(videoPath)) {
            mServer = new SDPServer(mTCPPort);
            MediaInfo mediaInfo = mStreamPlayer.getCurrentMediaInfo();
            if (mediaInfo != null) {
                mServer.setFrameRate(mediaInfo.getFrameRate());
                mServer.setSampleRate(mediaInfo.getSampleRate());
            }
            mServer.start();
            final Uri uri = Uri.parse(videoPath);
            // init player
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            isIJKPlayerOpen = true;
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        } else {
            Dbug.e(tag, "init player fail");
        }
    }

    private void stopMediaPlayer() {
        //mVideoView.clearSurfaceCanvas();
        mVideoView.stopPlayback();
        mVideoView.release(true);
        if (isIJKPlayerOpen)
            IjkMediaPlayer.native_profileEnd();
        isIJKPlayerOpen = false;
    }

    @Override
    public void onClick(View v) {
        if (v == mPlayPause) {
            if (mStreamPlayer != null) {
                if (mStreamPlayer.isStreamReceiving()) {
                    mVideoView.pause();
                    mStreamPlayer.pauseStream();
                } else if (mStreamPlayer.isStreamPausing()) {
                    mStreamPlayer.playStream();
                    mVideoView.start();
                }
            }
        } else if (v == mCancelPlayback) {
            dismiss();
        } else if (v == mFastForward) {
            if (isUseMap) a.getApplication().showToastLong(R.string.stop_trajectory_tracking);
            else fastForward();
        } else if (v == stopFastForwardIbtn) {
            fastForwardLevel = -1;
            //mStreamPlayer.toggleBuffering(true);
            fastForward();
        } else if (v == mMovRecordBtn) {
            if (!AppUtils.isFastDoubleClick(2000)) {
                mHandler.post(handlerRecord);
            } else {
                showToastShort(R.string.wait_a_moment);
            }

        } else if (v == mShrinkButton) {
            mapView.setVisibility(View.GONE);
            mShrinkButton.setVisibility(View.GONE);

            mExpandButton.setVisibility(View.VISIBLE);
            if (mapUtil != null) {
                mapUtil.onPause();
                isUseMap = false;
            }
        } else if (v == mExpandButton) {
            if (fastForwardLevel > 0) {
                a.getApplication().showToastLong(R.string.resume_normal_playback_speed);
                return;
            }
            mapView.setVisibility(View.VISIBLE);
            mShrinkButton.setVisibility(View.VISIBLE);

            mExpandButton.setVisibility(View.GONE);
            if (mapUtil != null) {
                mapUtil.onResume();
                isUseMap = true;
            }
        }
    }

    //停止录制
    private void stopRecordMov() {
        Dbug.w(tag, "stopRecordMov  ___0.0___ ");
        setCancelable(true);
        if(mVideoManager != null){
            mVideoManager.tryToStopDownload();
            mVideoManager = null;
        }
    }

    private void fastForward() {
        int level = (fastForwardLevel + 1) % 7;
        if (level == 0)
            mStreamPlayer.toggleBuffering(true);
        else
            mStreamPlayer.toggleBuffering(false);
        ClientManager.getClient().tryToFastForward(level, new SendResponse() {
            @Override
            public void onResponse(Integer code) {
                if (code != SEND_SUCCESS) {
                    Dbug.e(tag, "Send failed");
                }
            }
        });
    }

    private Runnable handlerRecord = new Runnable() {
        @Override
        public void run() {
            boolean isDownloading = VideoManager.isDownloading();
            Dbug.i(tag, "handlerRecord isDownloading ->" + isDownloading);
            if (isDownloading) {
                stopRecordMov();
            } else {
                startRecordMov();
            }
        }
    };

    //录制mov视频
    private void startRecordMov() {
        if(mStreamPlayer != null) {
            setCancelable(false);
            mVideoManager = new VideoManager();
            mVideoManager.setRecordTimerType(MovWrapper.TIME_MASTER_SEQUENCE);
            mVideoManager.setOnDownloadListener(this);
            mVideoManager.setPlaybackStream(mStreamPlayer);
            String outPath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), mApplication.getCameraDir(), DIR_DOWNLOAD)
                    + File.separator + AppUtils.getRecordVideoName();
            Dbug.w(tag, "startRecordMov outPath = " + outPath);
            //配置封装器参数
            MovWrapper mWrapper = mVideoManager.getMovWrapper();
            if (mWrapper != null) {
                DeviceSettingInfo deviceSettingInfo = mApplication.getDeviceSettingInfo();
                if (DeviceClient.CAMERA_REAR_VIEW == deviceSettingInfo.getCameraType()) {
                    mWrapper.setFrameRate(deviceSettingInfo.getRearRate());
                } else {
                    mWrapper.setFrameRate(deviceSettingInfo.getFrontRate());
                }
                MediaInfo mediaInfo = mStreamPlayer.getCurrentMediaInfo();
                if (mediaInfo != null) {
                     //Dbug.e(tag, "mediaInfo is "+mediaInfo);
                    if (!mWrapper.setAudioTrack(mediaInfo.getSampleRate(), AUDIO_CHANNEL, AUDIO_FORMAT))
                        Dbug.e(tag, "Set audio track failed");
                }

            }
            mVideoManager.startDownload(outPath);
        }else{
            Dbug.e(tag, "mStreamPlayer can not be empty.");
            mApplication.showToastShort(R.string.record_fail);
        }
    }

    private final OnProgressListener onProgressListener = new OnProgressListener() {
        @Override
        public void onStart() {
            MediaInfo mediaInfo = mStreamPlayer.getCurrentMediaInfo();
            //Dbug.w(tag, "onStart: mediaInfo=" + mediaInfo);
            mPlaybackProg.setMax(mediaInfo.getDuration());
            mPlaybackProg.setProgress(0);
            if (mapUtil != null) {
                mHandler.removeMessages(MSG_CLEAR_TRACK_POINT);
                mHandler.sendEmptyMessageDelayed(MSG_CLEAR_TRACK_POINT, 1000);
            }
        }

        @Override
        public void onProgress(int progress) {
            if (mPlaybackProg != null) {
                if (mPlaybackProg.getMax() > 0) {
                    //Dbug.e(tag, "===onProgress:" + progress + ", max="+mPlaybackProg.getMax());
                    mPlaybackProg.setProgress(progress);
                } else {
                    mPlaybackProg.setProgress(0);
                }
            }
        }

        @Override
        public void onFinish() {
            Dbug.w(tag, "===onFinish");
            if (mapUtil != null) mapUtil.addEndPointOverlay(trackPoints);
        }
    };
    private final OnPlaybackListener mPlayerListener = new OnPlaybackListener() {
        @Override
        public void onUpdate(MediaInfo mediaInfo) {

        }

        @Override
        public void onVideo(int t, int channel, byte[] data, long sequence, long timestamp) {

        }

        @Override
        public void onAudio(int t, int channel, byte[] data, long sequence, long timestamp) {

        }

        @Override
        public void onStateChanged(int state) {
            switch (state) {
                case PlaybackStream.Status.PREPARE:
                    Dbug.i(tag, "prepare-------");
                    //showPlayerView();
                    if (mMovRecordBtn.getVisibility() != View.VISIBLE) {
                        mMovRecordBtn.setVisibility(View.VISIBLE);
                    }
                    String RTS_URL = "tcp://127.0.0.1:" + mTCPPort;
                    initPlayer(RTS_URL);
                    break;
                case PlaybackStream.Status.END:
                    Dbug.i(tag, "file end-------");
                    break;
                case PlaybackStream.Status.PLAYING:
                    Dbug.i(tag, "playing-------");
                    mPlayPause.setImageResource(R.mipmap.ic_playback_pause);
                    mHandler.sendEmptyMessage(TAG_REFRESH_VIDEO_TIME);
                    break;
                case PlaybackStream.Status.PAUSE:
                    Dbug.i(tag, "pause-------");
                    mPlayPause.setImageResource(R.mipmap.ic_playback_play);
                    mHandler.removeMessages(TAG_REFRESH_VIDEO_TIME);
                    break;
                case PlaybackStream.Status.STOP:
                    Dbug.i(tag, "finish-------");
                    if (mMovRecordBtn.getVisibility() != View.GONE) {
                        mMovRecordBtn.setVisibility(View.GONE);
                    }
                    stopRecordMov();
                    mHandler.removeCallbacksAndMessages(null);
                    mPlayPause.setVisibility(View.GONE);
                    showToastShort(R.string.play_over);
                    dismiss();
                    break;
            }
        }

        @Override
        public void onError(int code, String message) {

        }
    };


    @Override
    public void onStartLoad() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dbug.w(tag, "onStartLoad  ___(-)___ ");
                handleStartRecode();
            }
        });
    }

    @Override
    public void onProgress(int progress) {
        Dbug.w(tag, "onProgress >>> progress : " + progress);
    }

    @Override
    public void onCompletion() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dbug.w(tag, "onCompletion  ___\\(O-O)/___ ");
                handleStopRecode();
                showToastShort(R.string.record_success);
            }
        });
    }

    @Override
    public void onError(int code, String msg) {
        Dbug.w(tag, "onError >>> code : " + code);
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVideoManager != null) {
                        String outputPath = mVideoManager.getOutPath();
                        if (!TextUtils.isEmpty(outputPath)) {
                            File file = new File(outputPath);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                    handleStopRecode();
                    showToastShort(R.string.record_fail);
                }
            });
        }
    }

    private void handleStartRecode() {
        mCancelPlayback.setVisibility(View.INVISIBLE);
        mMovRecordBtn.setImageResource(R.mipmap.ic_cuting_mov);
        mFastForward.setVisibility(View.INVISIBLE);
        mPlayPause.setVisibility(View.INVISIBLE);
        stopFastForwardIbtn.setVisibility(View.INVISIBLE);
    }

    private void handleStopRecode() {
        mCancelPlayback.setVisibility(View.VISIBLE);
        mMovRecordBtn.setImageResource(R.mipmap.ic_cut_mov);
        mFastForward.setVisibility(View.VISIBLE);
        mPlayPause.setVisibility(View.VISIBLE);
        stopFastForwardIbtn.setVisibility(View.VISIBLE);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    private OnDismissListener onDismissListener;

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null)
            onDismissListener.onDismiss();
    }

    private void hideWidget() {
        TranslateAnimation a = new TranslateAnimation(RELATIVE_TO_SELF, 0, RELATIVE_TO_SELF, 1, RELATIVE_TO_SELF, 0, RELATIVE_TO_SELF, 0);
        a.setDuration(500);
        a.setInterpolator(new LinearInterpolator());
        widgetParent.startAnimation(a);
        widgetParent.setVisibility(View.INVISIBLE);
        mPlaybackProg.setVisibility(View.GONE);
        if (mapUtil != null) {
            if (mapView.getVisibility() == View.GONE) mExpandButton.setVisibility(View.GONE);
            if (mapView.getVisibility() == View.VISIBLE) mShrinkButton.setVisibility(View.GONE);
        }
    }

    private void showWidget() {
        mPlaybackProg.setVisibility(View.VISIBLE);
        widgetParent.setVisibility(View.VISIBLE);
        TranslateAnimation a = new TranslateAnimation(RELATIVE_TO_SELF, 1, RELATIVE_TO_SELF, 0, RELATIVE_TO_SELF, 0, RELATIVE_TO_SELF, 0);
        a.setDuration(500);
        a.setInterpolator(new LinearInterpolator());
        widgetParent.startAnimation(a);
        if (mapUtil != null) {
            if(mapView.getVisibility() == View.GONE) mExpandButton.setVisibility(View.VISIBLE);
            if (mapView.getVisibility() == View.VISIBLE) mShrinkButton.setVisibility(View.VISIBLE);
        }
    }

    private IMediaController iMediaController = new IMediaController() {
        @Override
        public void hide() {
            hideWidget();
        }

        @Override
        public boolean isShowing() {
            return (widgetParent != null && widgetParent.getVisibility() == View.VISIBLE);
        }

        @Override
        public void setAnchorView(View view) {

        }

        @Override
        public void setEnabled(boolean enabled) {
            //widgetParent.setVisibility(enabled?View.VISIBLE:View.INVISIBLE);
        }

        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl player) {

        }

        @Override
        public void show(int timeout) {
            showWidget();
        }

        @Override
        public void show() {
            showWidget();
        }

        @Override
        public void showOnce(View view) {
            Dbug.i(tag, "show once");
        }
    };

    @Override
    public void onGps(GpsInfo gpsInfo) {
        com.baidu.trace.model.LatLng latLng = new com.baidu.trace.model.LatLng(gpsInfo.getLatitude(), gpsInfo.getLongitude());
        LatLng ll = MapUtil.convertTrace2Map(latLng);
        //Dbug.w(tag, "latLng=" + latLng+", ll=" + ll);
        trackPoints.add(ll);
        if (mapUtil != null) mapUtil.drawHistoryTrack(trackPoints, SortType.asc);
    }
}
