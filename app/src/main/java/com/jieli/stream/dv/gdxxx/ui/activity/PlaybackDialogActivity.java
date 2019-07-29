package com.jieli.stream.dv.gdxxx.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
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
import com.jieli.stream.dv.gdxxx.bean.DeviceDesc;
import com.jieli.stream.dv.gdxxx.data.OnRecordStateListener;
import com.jieli.stream.dv.gdxxx.data.VideoRecord;
import com.jieli.stream.dv.gdxxx.task.SDPServer;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IMediaController;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IRenderView;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IjkVideoView;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.TimeFormate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.view.animation.Animation.RELATIVE_TO_SELF;
import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

public class PlaybackDialogActivity extends BaseActivity implements View.OnClickListener, OnGpsListener {
    private String tag = getClass().getSimpleName();
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
    private VideoRecord mRecordVideo;
    private boolean isRecordPrepared = false;//For no-card of device mode only
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

    private int[] fastForwardRes = {R.mipmap.ic_fast_forward_1, R.mipmap.ic_fast_forward_2, R.mipmap.ic_fast_forward_4,
            R.mipmap.ic_fast_forward_8, R.mipmap.ic_fast_forward_16, R.mipmap.ic_fast_forward_32, R.mipmap.ic_fast_forward_64};
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_playback_dialog);
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mPlayPause = (ImageButton) findViewById(R.id.play_pause);
        mCancelPlayback = (ImageView) findViewById(R.id.cancel_playback);
        mFastForward = (ImageButton) findViewById(R.id.fast_forward);
        mMovRecordBtn = (ImageButton) findViewById(R.id.mov_record_btn);
        stopFastForwardIbtn = (ImageButton) findViewById(R.id.stop_fast_forward_btn);
        mTimeTextView = (TextView) findViewById(R.id.play_back_time_tv);
        widgetParent = (LinearLayout) findViewById(R.id.playback_widget_parent);
        mBufferingProg = (ProgressBar) findViewById(R.id.rts_buffering);
        mPlaybackProg = (ProgressBar) findViewById(R.id.playback_progress);
        mShrinkButton = (ImageView) findViewById(R.id.shrink_button);
        mExpandButton = (ImageView) findViewById(R.id.expand_button);
        mapView = (MapView) findViewById(R.id.track_query_mapView);

        mVideoView.setMediaController(iMediaController);
        mVideoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        mPlayPause.setOnClickListener(this);
        mCancelPlayback.setOnClickListener(this);
        stopFastForwardIbtn.setOnClickListener(this);
        mFastForward.setOnClickListener(this);
        mMovRecordBtn.setOnClickListener(this);
        mShrinkButton.setOnClickListener(this);
        mExpandButton.setOnClickListener(this);

        mApplication = (a) getApplication();
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

        mStreamPlayer = new PlaybackStream();
        mStreamPlayer.registerPlayerListener(mPlayerListener);
        mStreamPlayer.setOnProgressListener(onProgressListener);
        if (DEV_STA_MODE == mApplication.getSearchMode()) mStreamPlayer.setOnGpsListener(this);

        ClientManager.getClient().registerNotifyListener(mOnNotifyListener);
        Bundle args = getIntent().getExtras();
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
    protected void onStop() {
        super.onStop();
        Dbug.e(tag, "=======on stop=======");
        stopLocalRecording();

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
    protected void onDestroy() {
        super.onDestroy();
        Dbug.e(tag, "=======on destroy=======");
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
                    mStreamPlayer.configure(RTP_VIDEO_PORT1, RTP_AUDIO_PORT1);
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

    private int getRtsFormat() {
        int format = DeviceClient.RTS_H264;
        DeviceDesc settingInfo = a.getApplication().getDeviceDesc();
        if (settingInfo != null) {
            format = settingInfo.getVideoType();
        }
        return format;
    }

    private void initPlayer(String videoPath) {
        if (mVideoView != null && !TextUtils.isEmpty(videoPath)) {
            mServer = new SDPServer(mTCPPort, getRtsFormat());
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
            //dismiss();
            onBackPressed();
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

    private void startLocalRecording() {
        if (mRecordVideo == null) {
            mRecordVideo = new VideoRecord();
            mRecordVideo.prepare(new OnRecordStateListener() {
                @Override
                public void onPrepared() {
                    isRecordPrepared = true;
                    handleStartRecode();
                }

                @Override
                public void onStop() {
                    Dbug.i(tag, "Record onStop");
                    isRecordPrepared = false;
                    handleStopRecode();
                    showToastShort(R.string.record_success);
                }

                @Override
                public void onError(String message) {
                    Dbug.e(tag, "Record error:" + message);
                    if (mRecordVideo != null) {
                        String outputPath = mRecordVideo.getCurrentFilePath();
                        if (!TextUtils.isEmpty(outputPath)) {
                            File file = new File(outputPath);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                    handleStopRecode();
                    showToastShort(R.string.record_fail);

                    mRecordVideo = null;
                    isRecordPrepared = false;
                }
            });
        }
    }

    private void stopLocalRecording() {
        if (mRecordVideo != null) {
            isRecordPrepared = false;
            mRecordVideo.close();
            mRecordVideo = null;
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
            Dbug.i(tag, "handlerRecord isRecordPrepared ->" + isRecordPrepared);
            if (isRecordPrepared) {
                stopLocalRecording();
            } else {
                startLocalRecording();
            }
        }
    };

    private final OnProgressListener onProgressListener = new OnProgressListener() {
        @Override
        public void onStart() {
            MediaInfo mediaInfo = mStreamPlayer.getCurrentMediaInfo();
            Dbug.w(tag, "onStart: mediaInfo=" + mediaInfo);
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
            if (mRecordVideo != null && isRecordPrepared) {
                if (!mRecordVideo.write(t, data)) Dbug.e(tag, "Write video failed");
            }
        }

        @Override
        public void onAudio(int t, int channel, byte[] data, long sequence, long timestamp) {
            if (mRecordVideo != null && isRecordPrepared) {
                if (!mRecordVideo.write(t, data)) Dbug.e(tag, "Write audio failed");
            }
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
                    stopLocalRecording();
                    mHandler.removeCallbacksAndMessages(null);
                    mPlayPause.setVisibility(View.GONE);
                    showToastShort(R.string.play_over);
                    //dismiss();
                    onBackPressed();
                    break;
            }
        }

        @Override
        public void onError(int code, final String message) {
            if(code == 0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToastShort(R.string.connection_timeout);
                    }
                });
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToastShort(message);
                    }
                });
            }
        }
    };

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
