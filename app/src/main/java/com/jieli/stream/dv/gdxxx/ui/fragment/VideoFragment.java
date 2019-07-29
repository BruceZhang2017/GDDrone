package com.jieli.stream.dv.gdxxx.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.intercom.IntercomManager;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.model.PictureInfo;
import com.jieli.lib.dv.control.utils.Dlog;
import com.jieli.stream.dv.gdxxx.task.DebugHelper;
import com.jieli.stream.dv.gdxxx.task.IDebugListener;
import com.jieli.lib.dv.control.player.OnFrameListener;
import com.jieli.lib.dv.control.player.OnRealTimeListener;
import com.jieli.lib.dv.control.player.RealtimeStream;
import com.jieli.lib.dv.control.player.Stream;
import com.jieli.lib.dv.control.player.VideoThumbnail;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.lib.dv.control.utils.TopicParam;
import com.jieli.media.codec.FrameCodec;
import com.jieli.media.codec.bean.MediaMeta;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.audio.AudioRecordManager;
import com.jieli.stream.dv.gdxxx.bean.DeviceDesc;
import com.jieli.stream.dv.gdxxx.bean.DeviceSettingInfo;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.data.OnRecordStateListener;
import com.jieli.stream.dv.gdxxx.data.OnVideoCaptureListener;
import com.jieli.stream.dv.gdxxx.data.VideoCapture;
import com.jieli.stream.dv.gdxxx.data.VideoRecord;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.activity.GenericActivity;
import com.jieli.stream.dv.gdxxx.ui.activity.MainActivityOld;
import com.jieli.stream.dv.gdxxx.ui.activity.MainActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.ui.service.ScreenShotService;
import com.jieli.stream.dv.gdxxx.ui.widget.NoScrollGridView;
import com.jieli.stream.dv.gdxxx.ui.widget.PopupMenu;
import com.jieli.stream.dv.gdxxx.ui.widget.Rotate3dAnimation;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IRenderView;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IjkVideoView;
import com.jieli.stream.dv.gdxxx.ui.widget.media.InfoHudViewHolder;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.HttpManager;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.ThumbLoader;
import com.jieli.stream.dv.gdxxx.util.ThumbnailManager;
import com.jieli.stream.dv.gdxxx.util.TimeFormate;
import com.jieli.stream.dv.gdxxx.util.json.JSonManager;
import com.jieli.stream.dv.gdxxx.util.json.listener.OnCompletedListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ResponseBody;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.app.Activity.RESULT_OK;
import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;


public class VideoFragment extends BaseFragment implements View.OnClickListener, OnFrameListener,
        AdapterView.OnItemClickListener, AudioRecordManager.RecorderListener,
        IntercomManager.OnSocketErrorListener {
    private String tag = getClass().getSimpleName();

    private IjkVideoView mVideoView;
    private NoScrollGridView mGridView;
    private ImageButton mRTSPlayButton;
    private ImageButton mRecordButton;
    private ImageButton ibtnVoice;
    private ImageButton mProjectionButton;
    private ProgressBar mProgressBarLoading;
    private ImageView ivRecordFlag;
    private ImageView ivProjectionFlag;
    private RelativeLayout devMsgLayout;
    private ImageButton ibtnAdjustResolution;
    private MyGridViewAdapter mAdapter;
    private TableLayout mHudView;

    private NotifyDialog mErrorDialog;
    private WaitingDialog mWaitingDialog;
    private PopupMenu popupMenu;

    private PowerManager.WakeLock wakeLock;
    private RealtimeStream mRealtimeStream;
    private FrameCodec mFrameCodec = null;
    private VideoThumbnail mVideoThumbnail;
    private MyBroadcastReceiver mReceiver;
    private AudioRecordManager mAudioManager;
    private IntercomManager intercomManager;
    private VideoRecord mRecordVideo;
    private VideoCapture mVideoCapture;
    private NotifyDialog mLocalRecordingDialog;
    private DebugHelper mDebugHelper;

    private Set<SaveVideoThumb> collections;
    private List<FileInfo> totalList;
    private List<FileInfo> thumbList;
    private List<FileInfo> countList;
    private int recordStatus;
    private int viewWidth;
    private int viewHeight;
    private int mCameraType = DeviceClient.CAMERA_FRONT_VIEW;
    private int fps;
    private boolean isIJKPlayerOpen = false;
    private boolean isProjection;
    private boolean isRtspEnable;
    private boolean isRtVoiceOpen;
    private boolean isAdjustResolution;
    private boolean isSwitchCamera;
    private boolean isRecordPrepared = false;//For no-card of device mode only
    private boolean isCapturePrepared = false;//For no-card of device mode only
    private boolean isStartDebug;
    private boolean isSentOpenRtsCmd = false;
    private View mView;
    private static final int DELAY_TIME = 100;
    private static final int MSG_TAKE_VIDEO = 0x0a00;
    private static final int MSG_TAKE_PHOTO = 0x0a01;
    private static final int MSG_LOAD_DEV_THUMBS = 0x0a02;
    private static final int MSG_PROJECTION_CONTROL = 0x0a03;
    private static final int MSG_RT_VOICE_CONTROL = 0x0a04;
    private static final int MSG_CYC_SAVE_VIDEO = 0x0a05;
    private static final int MSG_FPS_COUNT = 0x0a06;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (getActivity() != null && message != null) {
                boolean open = false;
                switch (message.what) {
                    case MSG_TAKE_VIDEO:
                        if(isPlaying()) {
                            if(!AppUtils.isFastDoubleClick(2000)){
                                Dbug.e(tag, "---is playing " + isPlaying());
                                if (isRecordPrepared)
                                    stopLocalRecording();
                                else
                                    showLocalRecordDialog();
                            }else{
                                a.getApplication().showToastShort(R.string.dialod_wait);
                            }
                        } else {
                            a.getApplication().showToastShort(R.string.open_rts_tip);
                        }
                        break;
                    case MSG_TAKE_PHOTO:
                        //卡不在线
                        if(isPlaying()){
                            if (mVideoCapture == null) {
                                mVideoCapture = new VideoCapture();
                                mVideoCapture.setOnCaptureListener(new OnVideoCaptureListener() {
                                    @Override
                                    public void onCompleted() {
                                        isCapturePrepared = false;
                                    }

                                    @Override
                                    public void onFailed() {
                                        isCapturePrepared = false;
                                        a.getApplication().showToastShort(R.string.failure_photo);
                                    }
                                });
                            }
                            isCapturePrepared = true;
                            shootSound();
                        }else{
                            if(!isAdjustResolution && !isSwitchCamera){
                                a.getApplication().showToastShort(R.string.open_rts_tip);
                            }
                        }
                        break;
                    case MSG_LOAD_DEV_THUMBS:
                        int arg1 = message.arg1;
                        if (arg1 == 1) {
                            cancelSaveThread();
                        }
                        requestFileMsgText();
                        break;
                    case MSG_PROJECTION_CONTROL:
                        if (!isProjection) {
                            requestCapturePermission();
                        } else {
                            mApplication.sendScreenCmdToService(SERVICE_CMD_CLOSE_SCREEN_TASK);
                            ClientManager.getClient().tryToScreenShotTask(false, 0, 0, 0, new SendResponse() {
                                @Override
                                public void onResponse(Integer code) {

                                }
                            });
                            isProjection = false;
                            handlerProjectionUI();
                        }
                        break;
                    case MSG_RT_VOICE_CONTROL:
                        ClientManager.getClient().tryToRTIntercom(!isRtVoiceOpen, new SendResponse() {
                            @Override
                            public void onResponse(Integer code) {

                            }
                        });
                        break;
                    case MSG_CYC_SAVE_VIDEO:
                        if(recordStatus == STATUS_RECORDING){
                            ClientManager.getClient().tryToSaveCycVideo(new SendResponse() {
                                @Override
                                public void onResponse(Integer code) {

                                }
                            });
                        }else{
                            mApplication.showToastShort(R.string.no_video_tip);
                        }
                        break;
                    case MSG_FPS_COUNT:
                        updateDebugFps(fps);
                        fps = 0;
                        mHandler.removeMessages(MSG_FPS_COUNT);
                        mHandler.sendEmptyMessageDelayed(MSG_FPS_COUNT, 1000);
                        break;
                }
            }
            return false;
        }
    });

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (context != null && !TextUtils.isEmpty(action) && getActivity() != null) {
                switch (action) {
                    case ACTION_PROJECTION_STATUS:
                        boolean isOpen = intent.getBooleanExtra(KEY_PROJECTION_STATUS, false);
                        if (isOpen != isProjection) {
                            isProjection = isOpen;
                            handlerProjectionUI();
                        }
                        break;
                    case ACTION_FORMAT_TF_CARD:
                        clearDataAndUpdate();
                        break;
                    case ACTION_EMERGENCY_VIDEO_STATE:
                        int videoState = intent.getIntExtra(ACTION_KEY_VIDEO_STATE, -1);
                        int errorCode = intent.getIntExtra(ACTION_KEY_ERROR_CODE, -1);
                        if(errorCode == -1){
                            switch (videoState){
                                case STATE_START:
                                    closeRTS();
                                    break;
                                case STATE_END:
                                    if(mHandler != null){
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                openRTS();
                                            }
                                        }, 300);
                                    }
                                    break;
                            }
                        }else{
                            if(mHandler != null){
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        openRTS();
                                    }
                                }, 300);
                            }
                        }
                        break;
                    case ACTION_LANGUAAGE_CHANGE:
                        updateTextView();
                        break;
                }
            }
        }
    }

    private void updateTextView() {
        if (mView != null) {
            TextView deviceFiles = (TextView) mView.findViewById(R.id.device_file);
            deviceFiles.setText(R.string.device_media);
            TextView title = (TextView) mView.findViewById(R.id.video_top_tv);
            title.setText(R.string.live_preview);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mHandler != null) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_LOAD_DEV_THUMBS, 0, 0), 100);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_video, container, false);
        mVideoView = (IjkVideoView) mView.findViewById(R.id.video_view);
        mVideoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        mGridView = (NoScrollGridView) mView.findViewById(R.id.video_device_media);
        mRTSPlayButton = (ImageButton) mView.findViewById(R.id.btnPlay);
        mProjectionButton = (ImageButton) mView.findViewById(R.id.projection_control);
        ImageButton mFullScreenButton = (ImageButton) mView.findViewById(R.id.rts_fullscreen);
        mRecordButton = (ImageButton) mView.findViewById(R.id.record_control);
        ibtnVoice = (ImageButton) mView.findViewById(R.id.voice_control);
        ivRecordFlag = (ImageView) mView.findViewById(R.id.record_flag);
        ivProjectionFlag = (ImageView) mView.findViewById(R.id.projection_flag);
        ImageButton ibTakePhoto = (ImageButton) mView.findViewById(R.id.take_photo_control);
        ImageView ivReturn = (ImageView) mView.findViewById(R.id.video_top_return);
        mProgressBarLoading = (ProgressBar) mView.findViewById(R.id.rts_loading);
        ibtnAdjustResolution = (ImageButton) mView.findViewById(R.id.adjust_rts_resolution);
        mHudView = (TableLayout) mView.findViewById(R.id.mMoreHub);

        mGridView.setOnItemClickListener(this);
        mRTSPlayButton.setOnClickListener(this);
        mFullScreenButton.setOnClickListener(this);
        mProjectionButton.setOnClickListener(this);
        mRecordButton.setOnClickListener(this);
        ibTakePhoto.setOnClickListener(this);
        ivReturn.setOnClickListener(this);
        ibtnVoice.setOnClickListener(this);
        ibtnAdjustResolution.setOnClickListener(this);
        mVideoView.setOnPreparedListener(onPreparedListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnCompletionListener(onCompletionListener);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        if (pm != null) wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, tag);
        wakeLock.setReferenceCounted(false);

        collections = new HashSet<>();
        viewWidth = (AppUtils.getScreenWidth(getContext()) - (5 * AppUtils.dp2px(getContext(), 2))) / 4;
        viewHeight = viewWidth * 9 / 16;

        isRtspEnable = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(KEY_RTSP, false);
        initUI();
        mAdapter = new MyGridViewAdapter(getActivity().getApplicationContext());
        mGridView.setAdapter(mAdapter);

        ClientManager.getClient().tryToRequestRecordState(new SendResponse() {
            @Override
            public void onResponse(Integer code) {
                if (code != SEND_SUCCESS) {
                    Dbug.e(tag, "Send failed!!!");
                }
            }
        });
        updateDeviceFileList();
    }

    private void initUI(){
        handlerProjectionUI();

        DeviceDesc deviceDesc = a.getApplication().getDeviceDesc();
        if(deviceDesc != null){
            if(!deviceDesc.isSupport_projection()){
                mProjectionButton.setVisibility(View.GONE);
            }else{
                mProjectionButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        registerBroadcast();
    }

    private void registerBroadcast() {
        if (mReceiver == null) {
            mReceiver = new MyBroadcastReceiver();
        }
        IntentFilter filter = new IntentFilter(ACTION_PROJECTION_STATUS);
        filter.addAction(ACTION_FORMAT_TF_CARD);
        filter.addAction(ACTION_EMERGENCY_VIDEO_STATE);
        filter.addAction(ACTION_LANGUAAGE_CHANGE);
        a.getApplication().registerReceiver(mReceiver, filter);
    }

    private void openRTS() {
        if(isPlaying()){
            Dbug.e(tag, "It is playing, please stop it first.");
            return;
        }
        boolean isDebugOpen = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(IConstant.DEBUG_SETTINGS, false);
        if(isDebugOpen){
            if(mHudView != null){
                if(mHudView.getVisibility() != View.VISIBLE){
                    mHudView.setVisibility(View.VISIBLE);
                    mVideoView.setHudView(mHudView);
                }
            }
            startDebug();
        }else{
            if(mHudView != null){
                if(mHudView.getVisibility() != View.GONE){
                    fps = 0;
                    if(mHandler != null){
                        mHandler.removeMessages(MSG_FPS_COUNT);
                    }
                    mHudView.setVisibility(View.GONE);
                    mVideoView.setHudView(null);
                }
            }
        }
        int mRTSType = getRtsFormat();
        int cameraType = mApplication.getDeviceSettingInfo().getCameraType();
        if (isRtspEnable) {
            String strRtsp;
            if (cameraType == DeviceClient.CAMERA_REAR_VIEW){
                if(DeviceClient.RTS_H264 == mRTSType) {
                    strRtsp = String.format(RTSP_URL_REAR, ClientManager.getClient().getAddress());
                } else {
                    strRtsp = String.format(RTSP_REAR_JPEG_URL, ClientManager.getClient().getAddress());
                }
            } else {
                if(DeviceClient.RTS_H264 == mRTSType) {
                    strRtsp = String.format(RTSP_URL, ClientManager.getClient().getAddress());
                } else {
                    strRtsp = String.format(RTSP_FRONT_JPEG_URL, ClientManager.getClient().getAddress());
                }
            }
            initPlayer(strRtsp);
            return;
        }
        int level = AppUtils.getStreamResolutionLevel();
        int[] resolution = AppUtils.getRtsResolution(level);
        Dbug.i(tag, "open rts........... front=" +(cameraType == DeviceClient.CAMERA_FRONT_VIEW)+ ", h264 " +(mRTSType==DeviceClient.RTS_H264));
        ClientManager.getClient().tryToOpenRTStream(cameraType, mRTSType, resolution[0], resolution[1], getVideoRate(cameraType), new SendResponse() {
            @Override
            public void onResponse(Integer code) {
                if (code != SEND_SUCCESS) {
                    Dbug.e(tag, "Send failed!!!");
                } else {
                    isSentOpenRtsCmd = true;
                    int mode =  mApplication.getDeviceDesc().getNetMode();
                    Dbug.i(tag, "open rts mode " + mode);
                    if (mode == Stream.Protocol.TCP_MODE) {
                        createStream(mode, VIDEO_SERVER_PORT);//For TCP stream mode
                    }
                }
            }
        });
    }

    private void closeRTS() {
        Dbug.i(tag, "close rts................");
        deinitPlayer();

        boolean isDebugOpen = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(IConstant.DEBUG_SETTINGS, false);
        if(isDebugOpen) closeDebug();
        if (!isRtspEnable) {
            if(isPlaying()){
                int cameraType = mApplication.getDeviceSettingInfo().getCameraType();
                Dbug.i(tag, "cameraType = "+cameraType);
                ClientManager.getClient().tryToCloseRTStream(cameraType, new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        if (code != SEND_SUCCESS) {
                            Dbug.e(tag, "Send failed!!!");
                        }
                    }
                });
            }
        }
        stopLocalRecording();

        if (mVideoCapture != null) {
            mVideoCapture.destroy();
            mVideoCapture = null;
        }
        if (mRealtimeStream != null) {
            mRealtimeStream.close();
            mRealtimeStream = null;
        }
        if(mRTSPlayButton != null && mRTSPlayButton.getVisibility() != View.VISIBLE){
            mProgressBarLoading.setVisibility(View.GONE);
            mRTSPlayButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isPlaying() {
        if (isRtspEnable) return mVideoView!= null && mVideoView.isPlaying();

        return mRealtimeStream != null && mRealtimeStream.isReceiving();
    }

    private void initPlayer(String videoPath) {
        if (mVideoView != null && !TextUtils.isEmpty(videoPath)) {
            Dbug.i(tag, "Init Player. url="+videoPath);
            final Uri uri = Uri.parse(videoPath);
            // init player
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            isIJKPlayerOpen = true;
            mVideoView.setRealtime(true);
            mVideoView.setVideoURI(uri);
            mVideoView.start();

            mRTSPlayButton.setVisibility(View.GONE);
            mProgressBarLoading.setVisibility(View.VISIBLE);
        } else {
            Dbug.e(tag, "init player failed");
        }
    }

    private void deinitPlayer() {
        Dbug.w(tag, "deinit Player");
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        }

        if (isIJKPlayerOpen) IjkMediaPlayer.native_profileEnd();
        isIJKPlayerOpen = false;
    }

    private final IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mProgressBarLoading.setVisibility(View.GONE);
        }
    };

    private final IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mRTSPlayButton.setVisibility(View.VISIBLE);
        }
    };

    private final OnNotifyListener onNotifyResponse = new OnNotifyListener() {

        @Override
        public void onNotify(NotifyInfo data) {
            if(data == null) return;
            String topic = data.getTopic();
            if(TextUtils.isEmpty(topic) || topic.equals(Topic.KEEP_ALIVE)) return;
            //Dbug.e(tag, "Topic=" + data.getTopic());
            if(data.getErrorType() != Code.ERROR_NONE){
                if(recordStatus == STATUS_PREPARE){
                    recordStatus = STATUS_NOT_RECORD;
                }
                if(isAdjustResolution){
                    isAdjustResolution = false;
                    dismissWaitingDialog();
                }
            }
            switch (data.getErrorType()) {
                case Code.ERROR_NONE:
                    break;
                case Code.ERROR_RT_STREAM_OPEN_FAILED:
                    closeRTS();
                    return;
                default:
                    switch (topic) {
                        case Topic.MULTI_VIDEO_COVER:
                            Dbug.e(tag, "data : " + data.toString());
                            dismissWaitingDialog();
                            if (mVideoThumbnail != null) {
                                Dbug.i(tag, "mVideoThumbnail close - 000");
                                mVideoThumbnail.close();

                                if (Code.ERROR_REQUEST == data.getErrorType()) {
                                    if (mHandler != null) {
                                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_LOAD_DEV_THUMBS, 0, 0), DELAY_TIME);
                                    }
                                }
                            }
                            return;
                        case Topic.FRONT_MEDIA_FILE_LIST:
                        case Topic.REAR_MEDIA_FILE_LIST:
                            dismissWaitingDialog();
                            return;
                        default:
                            Dbug.e(tag, "Topic=" + data.getTopic() +", " + Code.getCodeDescription(data.getErrorType()));
                            return;
                    }
            }
            int port = RTS_UDP_PORT;
            switch (topic) {
                case Topic.OPEN_REAR_RTS:
                    port = RTS_UDP_REAR_PORT;
                    boolean isAutoRearCamera = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(AppUtils.getAutoRearCameraKey(mApplication.getUUID()), false);
                    if(isAutoRearCamera){
                        PreferencesHelper.putBooleanValue(mApplication, AppUtils.getAutoRearCameraKey(mApplication.getUUID()), false);
                    }
                case Topic.OPEN_FRONT_RTS:
                    Dbug.i(tag, "port : " + port +", Open result:"+data);
                    if (!isSentOpenRtsCmd) {
                        Dbug.w(tag, "Please don't do it again.");
                        return;
                    }
                    isSentOpenRtsCmd = false;
                    if(null != data.getParams()){
                        int width, height;
                        int format = -1;
                        boolean isFrontCamera = port == RTS_UDP_PORT;
                        width = height = 0;
                        String rtsWidth = data.getParams().get(TopicKey.WIDTH);
                        String rtsHeight = data.getParams().get(TopicKey.HEIGHT);
                        String rtsFormat = data.getParams().get(TopicKey.FORMAT);
                        if(!TextUtils.isEmpty(rtsWidth) && TextUtils.isDigitsOnly(rtsWidth)){
                            width = Integer.valueOf(rtsWidth);
                        }
                        if(!TextUtils.isEmpty(rtsHeight) && TextUtils.isDigitsOnly(rtsHeight)){
                            height = Integer.valueOf(rtsHeight);
                        }
                        if(!TextUtils.isEmpty(rtsFormat) && TextUtils.isDigitsOnly(rtsFormat)){
                            format = Integer.valueOf(rtsFormat);
                        }
                        int mode = mApplication.getDeviceDesc().getNetMode();
                        if (mode == Stream.Protocol.UDP_MODE || mRealtimeStream == null) {
                            createStream(Stream.Protocol.UDP_MODE, port);///For UDP stream mode
                        }
                        //Dbug.e(tag, "format " + format +", w " + width + ", h= " + height);
                        if (format == DeviceClient.RTS_JPEG) {
                            mRealtimeStream.setResolution(width, height);
                        }
                        if (isFrontCamera) {
                            mRealtimeStream.setFrameRate(mApplication.getDeviceSettingInfo().getFrontRate());
                            mRealtimeStream.setSampleRate(mApplication.getDeviceSettingInfo().getFrontSampleRate());
                        } else {
                            mRealtimeStream.setFrameRate(mApplication.getDeviceSettingInfo().getRearRate());
                            mRealtimeStream.setSampleRate((mApplication.getDeviceSettingInfo().getRearSampleRate()));
                        }
                        initPlayer(SDP_URL);
                        updateResolutionUI(port == RTS_UDP_REAR_PORT, width, height);

                        if(mApplication.getDeviceSettingInfo().isExistRearView()){
                            rotateDeviceMsgLayout();
                        }else{
                            mCameraType = mApplication.getDeviceSettingInfo().getCameraType();
                        }
                    }
                    break;
                case Topic.CLOSE_PULL_RT_STREAM:
                case Topic.CLOSE_RT_STREAM:
                    if (null != data.getParams()){
                        boolean closeRTS = TopicParam.SURE.equals(data.getParams().get(TopicKey.STATUS));
                        Dbug.w(tag, "close rt stream result : " + closeRTS + ", receiving=" + isPlaying() + ", playing=" + mVideoView.isPlaying());
                        //if (closeRTS) isRTPlaying = false;
                        if(closeRTS && isAdjustResolution){
                            isAdjustResolution = false;
                            dismissWaitingDialog();
                            openRTS();
                        }
                    }
                    break;
                case Topic.VIDEO_CTRL:  // 开/关录像回调信息
                    if (null == data.getParams()) {
                        Dbug.e(tag, "VIDEO_CTRL: param is null");
                        return;
                    }
                    String state = data.getParams().get(TopicKey.STATUS);
                    if (!TextUtils.isEmpty(state)) {
                        boolean isRecord = TopicParam.OPEN.equals(state);
                        if (isRecord) {
                            recordStatus = STATUS_RECORDING;
                            int currentRecordLevel = getCameraLevel(mApplication.getDeviceSettingInfo().getCameraType());
                            int currentStreamLevel = AppUtils.getStreamResolutionLevel();
                            //录像为1080P时, 强制RTP为 VGA/720P
                            if (currentRecordLevel == RTS_LEVEL_FHD && currentStreamLevel == RTS_LEVEL_FHD) {
                                switchStreamResolution(RTS_LEVEL_HD);
                                ibtnAdjustResolution.setImageResource(getLevelResId(RTS_LEVEL_HD));
                            }
                        } else {
                            recordStatus = STATUS_NOT_RECORD;
                        }
                        handlerVideoUI();
                        Dbug.w(tag, "state=" + state + ", dir=" + data.getParams().get(TopicKey.PATH));
                    } else {
                        Dbug.e(tag, "state is empty");
                    }
                    break;
                case Topic.VIDEO_FINISH: // video finish
                    if (null == data.getParams()) {
                        return;
                    }
                    String videoState = data.getParams().get(TopicKey.STATUS);
                    boolean isRecording = TopicParam.OPEN.equals(videoState);
                    if (isRecording) {
                        recordStatus = STATUS_RECORDING;
                    } else {
                        recordStatus = STATUS_NOT_RECORD;
                    }
                    String desc = data.getParams().get(TopicKey.DESC);
                    if (!TextUtils.isEmpty(desc)) {
                        desc = desc.replaceAll("\\\\", "");
                        Dbug.w(tag, "-VIDEO_FINISH- desc = " + desc);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateDeviceFileList();
                            }
                        }, 100);
                    } else Dbug.e(tag, "CMD:VIDEO_FINISH:desc is null");
                    break;
                case Topic.PHOTO_CTRL:
                    if (null == data.getParams()) {
                        return;
                    }
                    shootSound();
                    //flashScreen();
                    String photoDesc = data.getParams().get(TopicKey.DESC);
                    if (!TextUtils.isEmpty(photoDesc)) {
                        photoDesc = photoDesc.replaceAll("\\\\", "");
                        Dbug.w(tag, "-PHOTO_CTRL- photoDesc = " + photoDesc);
                        FileInfo fileInfo = JSonManager.parseFileInfo(photoDesc);
                        if (fileInfo != null) {
                            if (totalList == null) {
                                totalList = new ArrayList<>();
                            }
                            int cameraType = DeviceClient.CAMERA_FRONT_VIEW;
                            if(CAMERA_TYPE_REAR.equals(fileInfo.getCameraType())){
                                cameraType = DeviceClient.CAMERA_REAR_VIEW;
                            }
                            if(!totalList.contains(fileInfo) && (cameraType == mApplication.getDeviceSettingInfo().getCameraType())) {
                                totalList.add(fileInfo);
                                JSonManager.convertJson(totalList);
                                updateDeviceFileList();
                            }
                        }
                    }
                    break;
                case Topic.FRONT_MEDIA_FILE_LIST:
                case Topic.REAR_MEDIA_FILE_LIST:
                    if (null == data.getParams()) {
                        return;
                    }
                    String type = data.getParams().get(TopicKey.TYPE);
                    if (TopicParam.NONE.equals(type)) {
                        a.getApplication().showToastShort(R.string.device_no_media_files);
                        return;
                    }
                    String path = data.getParams().get(TopicKey.PATH);
                    if(!TextUtils.isEmpty(path)){
                        int cameraType = AppUtils.getCameraType(path);
                        if(cameraType == mApplication.getDeviceSettingInfo().getCameraType()){
                            String ip = ClientManager.getClient().getAddress();
                            String url = AppUtils.formatUrl(ip, DEFAULT_HTTP_PORT, path);
                            Dbug.w(tag, "url=" + url + ", ip="+ip+", path="+path);
                            HttpManager.downloadFile(url, new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Dbug.e(tag, "Error:" + e.getMessage());
                                    dismissWaitingDialog();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                                    Dbug.i(tag, "onResponse code = " + response.code());
                                    if (response.code() == 200) {
                                        ResponseBody responseBody = response.body();
                                        if (responseBody != null) {
                                            String content = new String(responseBody.bytes());
                                            Dbug.w(tag, "content length : " + content.length());
                                            if (!TextUtils.isEmpty(content)) {
//                                        Dbug.i(tag, "content=" + content);
                                                tryToParseData(content);
                                            }
                                        }
                                    }else{
                                        dismissWaitingDialog();
                                    }
                                    response.close();
                                }
                            });
                        } else Dbug.e(tag, "cameraType is " + cameraType);
                    }
                    break;
                case Topic.MULTI_VIDEO_COVER:
                    if (null == data.getParams()) {
                        return;
                    }
                    Dbug.w(tag, "-MULTI_VIDEO_COVER- result=" + data.getParams());
                    if (TopicParam.START.equals(data.getParams().get(TopicKey.STATUS))) {
                        if (mVideoThumbnail == null) {
                            mVideoThumbnail = ThumbnailManager.getInstance();
                        }
                        Dbug.i(tag, "-MULTI_VIDEO_COVER- create socket!");
                        mVideoThumbnail.create(THUMBNAIL_TCP_PORT, ClientManager.getClient().getAddress());
                        mVideoThumbnail.setOnFrameListener(VideoFragment.this);
                    }
                    break;
                case Topic.TF_STATUS:
                    if (null == data.getParams()) {
                        Dbug.e(tag, "No param");
                        return;
                    }
                    Dbug.e(tag, "TF state:" + data);
                    if (TopicParam.TF_ONLINE.equals(data.getParams().get(TopicKey.ONLINE))) {
                        Dbug.e(tag, "TF state online ");
                        stopLocalRecording();
                        if (mHandler != null) {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_LOAD_DEV_THUMBS, 1, 0));
                        }
                    } else {
                        if (recordStatus == STATUS_RECORDING)
                            hideVideoUI();
                        clearDataAndUpdate();
                    }
                    break;
                case Topic.FILES_DELETE:
                    if (null == data.getParams()) {
                        return;
                    }
                    String delPath = data.getParams().get(TopicKey.PATH);
                    if (!TextUtils.isEmpty(delPath)) {
                        FileInfo fileInfo = findFileInfo(delPath);
                        if (fileInfo != null) {
                            if (totalList != null) {
                                if (totalList.remove(fileInfo)) {
                                    updateDeviceFileList();
                                }
                            }
                        }
                    }
                    break;
                case Topic.RT_TALK_CTL:
                    if (null != data.getParams()) {
                        isRtVoiceOpen = TopicParam.OPEN.equals(data.getParams().get(TopicKey.STATUS));
                        if(isRtVoiceOpen){
                            if (mAudioManager == null) {
                                mAudioManager = AudioRecordManager.getInstance();
                                mAudioManager.setRecordListener(VideoFragment.this);
                            }
                            int ret = mAudioManager.startRecord();
                            switch (ret){
                                case AudioRecordManager.START_RECORD_OK:
                                    intercomManager = IntercomManager.getInstance(ClientManager.getClient().getAddress());
                                    intercomManager.initSendThread();
                                    intercomManager.setOnSocketErrorListener(VideoFragment.this);
                                    break;
                                case AudioRecordManager.ERR_AUDIO_IS_RECORDING:
                                    break;
                                case AudioRecordManager.ERR_SD_CARD_NOT_EXIST:
                                    break;
                            }
                        }else{
                            if(mAudioManager != null){
                                mAudioManager.stopRecord();
                            }
                            if(intercomManager != null){
                                intercomManager.stopSendDataThread();
                            }
                        }
                        handlerRTVoiceUI();
                    }
                    break;
                case Topic.PULL_VIDEO_STATUS:
                    if (null != data.getParams()) {
                        Dbug.w(tag, "PULL_VIDEO_STATUS >>>>>>>>>>>>>>>>>>>>>>>");
                        if(mHandler != null){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(!mApplication.getDeviceSettingInfo().isExistRearView()){
                                        Dbug.w(tag, "is playing " + isPlaying() + ", mCameraType=" + mCameraType);
                                        if(isPlaying() && mCameraType == DeviceClient.CAMERA_REAR_VIEW){
                                            closeRTS();
                                            if(mHandler != null){
                                                mHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        openRTS();
                                                    }
                                                }, 250);
                                            }
                                        }
                                    }
                                    syncDeviceState();
                                }
                            }, DELAY_TIME);
                        }
                    }
                    break;
                case Topic.COLLISION_DETECTION_VIDEO:
                    if (null != data.getParams()) {
                        String widthStr = data.getParams().get(TopicKey.WIDTH);
                        String heightStr = data.getParams().get(TopicKey.HEIGHT);
                        String fpsStr = data.getParams().get(TopicKey.FRAME_RATE);
                        String durationStr = data.getParams().get(TopicKey.DURATION);
                        if(!TextUtils.isEmpty(widthStr) &&  !TextUtils.isEmpty(heightStr)
                                && !TextUtils.isEmpty(fpsStr) && !TextUtils.isEmpty(durationStr)){
                            closeRTS();
                        }
                    }
                    break;
            }
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, final int framework_err, final int impl_err) {
            Dbug.e(tag, "Error: framework_err=" + framework_err + ",impl_err=" + impl_err);
            closeRTS();
            if (framework_err == -10000)//TODO 暂时不处理
                return true;
            mErrorDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.fail_to_play, R.string.dialog_ok,
                    new NotifyDialog.OnConfirmClickListener() {
                        @Override
                        public void onClick() {
                            mErrorDialog.dismiss();
                            getActivity().onBackPressed();
                        }
                    });
            mErrorDialog.show(getActivity().getSupportFragmentManager(), "ViewDialog");
            return true;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (JSonManager.getInstance().getInfoList().size() <= 0) {
            clearDataAndUpdate();
        }
        //Dbug.w(TAG, "--->>> onResume <<<--- \\(*o*)/ mode " + mApplication.getDeviceDesc().getNetMode());
        ClientManager.getClient().registerNotifyListener(onNotifyResponse);
        //Dbug.w(TAG, "--->>> registerNotifyListener <<< is playing " + mVideoView.isPlaying());
        if(mHandler != null){
            mHandler.postDelayed(updateUIFromDev, DELAY_TIME);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    openRTS();
                }
            }, 200);
        }
    }

    private void createStream(int mode, int port) {
        //Dbug.i(tag, "createStream==========mode=" + mode);
        if (mode == Stream.Protocol.TCP_MODE || mode == Stream.Protocol.UDP_MODE) {
            if (mRealtimeStream == null) {
                mRealtimeStream = new RealtimeStream(mode);
                mRealtimeStream.useDeviceTimestamp(true);
                mRealtimeStream.registerStreamListener(realtimePlayerListener);
            }
            //Dbug.i(tag, "Net mode="+ mode +", is receiving " + mRealtimeStream.isReceiving());
            if (!mRealtimeStream.isReceiving()) {
                if (mode == Stream.Protocol.TCP_MODE) {
                    mRealtimeStream.create(port, ClientManager.getClient().getAddress());
                } else {
                    mRealtimeStream.create(port);
                }
                mRealtimeStream.configure(RTP_VIDEO_PORT1, RTP_AUDIO_PORT1);
            } else Dbug.w(tag, "stream not receiving");
        } else {
            Dbug.e(tag, "Create stream failed!!!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Dbug.w(TAG, "--->>> onPause <<<--- is receiving " + isPlaying() + ", is playing=" + mVideoView.isPlaying());
        closeRTS();
        if (mRealtimeStream != null) {
            mRealtimeStream.unregisterStreamListener(realtimePlayerListener);
            mRealtimeStream.release();
            mRealtimeStream = null;
        }
        if (isRtVoiceOpen) {
            ibtnVoice.performClick();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mReceiver != null) {
            a.getApplication().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        cancelSaveThread();
        if(mAdapter != null){
            mAdapter.cancelAllTasks();
        }
        if (mFrameCodec != null) {
            mFrameCodec.destroy();
            mFrameCodec = null;
        }
        //Dbug.i(tag, "mVideoThumbnail close - 002");
        if (mVideoThumbnail != null && !mVideoThumbnail.close()) {
            Dbug.w(tag, "Close Video thumbnail failed");
        }
        ClientManager.getClient().unregisterNotifyListener(onNotifyResponse);
        mHandler.removeMessages(MSG_LOAD_DEV_THUMBS);
        dismissWaitingDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Dbug.w(TAG, "---------------------->>> onDestroy <<<--- \\('O')/");
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if(mAudioManager != null){
            mAudioManager.release();
            mAudioManager = null;
        }
        if(intercomManager != null){
            intercomManager.release();
            intercomManager = null;
        }
        if (isProjection) {
            mApplication.sendScreenCmdToService(SERVICE_CMD_CLOSE_SCREEN_TASK);
            ClientManager.getClient().tryToScreenShotTask(false, 0, 0, 0, new SendResponse() {
                @Override
                public void onResponse(Integer code) {

                }
            });
            isProjection = false;
        }

        closeRTS();

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if(mErrorDialog != null){
            if(mErrorDialog.isShowing()){
                mErrorDialog.dismiss();
            }
            mErrorDialog = null;
        }
        dismissWaitingDialog();
        if(popupMenu != null){
            popupMenu.dismiss();
            popupMenu = null;
        }
        System.gc();
    }

    @Override
    public void onClick(View v) {
        if (null != v && getActivity() != null) {
            switch (v.getId()) {
                case R.id.video_top_return:
                    BaseFragment fragment = (BaseFragment) getActivity().getSupportFragmentManager()
                            .findFragmentByTag(DeviceListFragment.class.getSimpleName());
                    if (fragment == null) {
                        fragment = new DeviceListFragment();
                    }
                    ((MainActivityOld)getActivity()).changeFragment(R.id.container, fragment, DeviceListFragment.class.getSimpleName());
                    break;
                case R.id.btnPlay:
                    openRTS();
                    break;
                case R.id.rts_fullscreen:
                    goToPlayBack(null);//全屏
                    break;
                case R.id.projection_control:
                    if (mHandler != null) {
                        mHandler.removeMessages(MSG_PROJECTION_CONTROL);
                        mHandler.sendEmptyMessageDelayed(MSG_PROJECTION_CONTROL, DELAY_TIME);
                    }
                    break;
                case R.id.record_control:
                    if (mHandler != null) {
                        mHandler.removeMessages(MSG_TAKE_VIDEO);
                        mHandler.sendEmptyMessageDelayed(MSG_TAKE_VIDEO, DELAY_TIME);
                    }
                    break;
                case R.id.take_photo_control:
                    if (mHandler != null) {
                        mHandler.removeMessages(MSG_TAKE_PHOTO);
                        mHandler.sendEmptyMessageDelayed(MSG_TAKE_PHOTO, DELAY_TIME);
                    }
                    break;
                case R.id.voice_control:
                    if (mHandler != null) {
                        mHandler.removeMessages(MSG_RT_VOICE_CONTROL);
                        mHandler.sendEmptyMessageDelayed(MSG_RT_VOICE_CONTROL, DELAY_TIME);
                    }
                    break;
                case R.id.adjust_rts_resolution:
                    if (isRtspEnable) {
                        a.getApplication().showToastLong(R.string.not_supported_in_rtsp);
                        break;
                    }
                    if (mVideoView.isPlaying() || isPlaying()) {
                        if (DEV_REC_DUAL.equals(mApplication.getDeviceDesc().getDevice_type()) && recordStatus == STATUS_RECORDING) {
                            a.getApplication().showToastShort(R.string.stop_recording_first);
                        } else {
                            showPopupMenu(v, getCameraLevel(mCameraType));
                        }
                    } else {
                        a.getApplication().showToastShort(R.string.open_rts_tip);
                    }
                    break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_BROWSE_FILE) {
            if (mApplication.isSdcardExist()) {
                if (mHandler != null) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_LOAD_DEV_THUMBS, 1, 0), DELAY_TIME);
                }
            }
        } else if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                ScreenShotService.setResultData(data);
                ClientManager.getClient().tryToScreenShotTask(true, 640, 480, 30, new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        if (code == SEND_SUCCESS) {
                            isProjection = true;
                            handlerProjectionUI();
                        }
                    }
                });
            }
        } else if(requestCode == CODE_PLAYBACK){
            mCameraType = mApplication.getDeviceSettingInfo().getCameraType();
            if (mApplication.isSdcardExist()) {
                if (mHandler != null) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_LOAD_DEV_THUMBS, 1, 0), DELAY_TIME);
                }
            }
        }
    }

    private Runnable dismissDialogRunnable = new Runnable() {
        @Override
        public void run() {
            dismissWaitingDialog();
            isSwitchCamera = false;
        }
    };

    private void requestFileMsgText() {
        Dbug.w(TAG, "-requestFileMsgText-isSdcardExist=" + mApplication.isSdcardExist());
        if (mApplication.isSdcardExist()) {
            mCameraType = mApplication.getDeviceSettingInfo().getCameraType();
            Dbug.w(TAG, "-requestFileMsgText- CameraType : " +mCameraType);
            ClientManager.getClient().tryToRequestMediaFiles(mCameraType, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code != SEND_SUCCESS) {
                        Dbug.e(tag, "Send failed");
                    }
                }
            });
        } else {
            clearDataAndUpdate();
        }
    }

    private void tryToParseData(String s) {
        if (TextUtils.isEmpty(s)) {
            Dbug.e(tag, "tryToParseData: desc is empty!!");
            return;
        }
        JSonManager.getInstance().parseJSonData(s, new OnCompletedListener<Boolean>() {
            @Override
            public void onCompleted(Boolean state) {
                if (state) {
                    totalList = JSonManager.getInstance().getInfoList();
                    updateDeviceFileList();
                } else {
                    Dbug.e(TAG, "-tryToParseData- parseJSonData failed!!!");
                    clearDataAndUpdate();
                    dismissWaitingDialog();
                }
            }
        });
    }


    private void updateDeviceFileList() {
        if (totalList != null && totalList.size() >= 0) {
            showWaitingDialog();
            if (totalList.size() == 0) {
                dismissWaitingDialog();
            }
            AppUtils.descSortWay(totalList);
            List<FileInfo> dataList = new ArrayList<>();
            int totalSize = totalList.size();
            if (totalSize > 7) {
                dataList.addAll(totalList.subList(0, 7));
            } else {
                dataList.addAll(totalList);
            }
            requestVideoThumbnail(dataList);
            ArrayList<FileInfo> temp = new ArrayList<>();
            if(countList == null){
                countList = new ArrayList<>();
            }else{
                countList.clear();
            }
            countList.addAll(dataList);
            temp.addAll(dataList);
            if (mAdapter == null) {
                mAdapter = new MyGridViewAdapter(getActivity().getApplicationContext());
            }
            mGridView.setAdapter(mAdapter);
            mAdapter.setDataList(temp);
            mAdapter.notifyDataSetChanged();
            dismissWaitingDialog();
        } else {
            Dbug.e(TAG, "updateDeviceFileList: total list is null");
        }
    }


    private Runnable updateUIFromDev = new Runnable() {
        @Override
        public void run() {
            syncDeviceState();
            if (isPlaying()) {
                mProgressBarLoading.setVisibility(View.GONE);
                mRTSPlayButton.setVisibility(View.GONE);
            } else {
                mRTSPlayButton.setVisibility(View.VISIBLE);
            }
            if (mRTSPlayButton.getVisibility() == View.VISIBLE) {
                mProgressBarLoading.setVisibility(View.GONE);
            }
        }
    };

    private void handlerProjectionUI() {
        if (ivProjectionFlag != null) {
            if (isProjection) {
                mProjectionButton.setImageResource(R.mipmap.ic_projection_selected);
                ivProjectionFlag.setVisibility(View.VISIBLE);
            } else {
                ivProjectionFlag.setVisibility(View.GONE);
                mProjectionButton.setImageResource(R.drawable.drawable_projection);
            }
        }
    }

    private void requestVideoThumbnail(List<FileInfo> fileInfoList) {
        //判断不在接收缩略图数据时才发命令请求
        if (mVideoThumbnail == null || !mVideoThumbnail.isReceiving()) {
            if (fileInfoList != null) {
                List<String> list = new ArrayList<>();
                if (thumbList == null) {
                    thumbList = new ArrayList<>();
                } else {
                    thumbList.clear();
                }
                for (FileInfo fileInfo : fileInfoList) {
                    if (fileInfo.isVideo()) {
                        String cameraDir = AppUtils.checkCameraDir(fileInfo);
                        String saveUrl = AppUtils.splicingFilePath(mApplication.getAppFilePath(), cameraDir, DIR_THUMB)
                                + File.separator + AppUtils.getVideoThumbName(fileInfo);
                        if (!AppUtils.checkFileExist(saveUrl)) {
                            if (!list.contains(fileInfo.getPath())) {
                                list.add(fileInfo.getPath());
                                thumbList.add(fileInfo);
                            }
                        }
                    }
                }
                if (list.size() > 0) {
                    Dbug.i(tag, "-requestVideoThumbnail- tryToRequestVideoCover size = " +list.size());
                    ClientManager.getClient().tryToRequestVideoCover(list, new SendResponse() {
                        @Override
                        public void onResponse(Integer code) {
                            if (code != SEND_SUCCESS) {
                                Dbug.e(tag, "Send failed");
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onFrame(byte[] data, PictureInfo mediaInfo) {
        Dbug.w(tag, "-onFrame- start! ");
        if (data != null && mediaInfo != null) {
            if (mediaInfo.getPath().endsWith(".AVI") || mediaInfo.getPath().endsWith(".avi")) {
                SaveVideoThumb saveVideoThumb = new SaveVideoThumb(data, mHandler);
                collections.add(saveVideoThumb);
                saveVideoThumb.start();
            } else {
                if (mFrameCodec == null) {
                    mFrameCodec = new FrameCodec();
                    mFrameCodec.setOnFrameCodecListener(mOnFrameCodecListener);
                }
                Dbug.w(tag, "-convertToJPG- mediaInfo =" + mediaInfo.toString());
                int width = mediaInfo.getWidth();
                int height = mediaInfo.getHeight();
                if(width >  0 && height > 0){
                    boolean ret = mFrameCodec.convertToJPG(data, width, height, mediaInfo.getPath());
                    Dbug.w(tag, "-convertToJPG- ret=" + ret);
                }
            }
        }
    }

    private final FrameCodec.OnFrameCodecListener mOnFrameCodecListener = new FrameCodec.OnFrameCodecListener() {
        @Override
        public void onCompleted(byte[] bytes, MediaMeta mediaMeta) {
            if(bytes != null){
                Dbug.w(tag, "-onCompleted- bytes size=" + bytes.length + ",mediaMeta=" +mediaMeta);
                SaveVideoThumb saveVideoThumb = new SaveVideoThumb(bytes, mHandler);
                collections.add(saveVideoThumb);
                saveVideoThumb.start();
            }
        }

        @Override
        public void onError(String s) {
            if (mVideoThumbnail != null) {
                Dbug.i(tag, "mVideoThumbnail close - 003");
                mVideoThumbnail.close();
                mVideoThumbnail = null;
            }
            if (thumbList != null && thumbList.size() >= 0) {
                thumbList.remove(0);
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mAdapter != null && getActivity() != null) {
            Dbug.w(tag, "onItemClick pos =" + i);
            if (mAdapter.checkIsBtn(i)) {
                if (getActivity() != null && isSdOnline()) {
                    toDeviceGallery();
                }
            } else {
                FileInfo info = (FileInfo) mAdapter.getItem(i);
                if (getActivity() != null && isSdOnline() && info != null) {
                    if(info.isVideo()){
                        Dbug.i(tag,"selsect fileinfo="+info.getName()+" "+info.getCreateTime());
                        goToPlayBack(info);
                    }else{
                        toDeviceGallery();
                    }
                }
            }
        }
    }

    private void toDeviceGallery(){
        String txtContent = JSonManager.getInstance().getVideosDescription();
        if (!TextUtils.isEmpty(txtContent)) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_VIDEO_LIST, txtContent);
            Intent it = new Intent(getActivity(), GenericActivity.class);
            it.putExtra(KEY_FRAGMENT_TAG, DEV_PHOTO_FRAGMENT);
            it.putExtra(KEY_DATA, bundle);
            startActivityForResult(it, CODE_BROWSE_FILE);
        } else {
            mApplication.showToastShort(R.string.loading);
        }
    }


    public void requestCapturePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            a.getApplication().showToastLong(R.string.projection_not_support);
        } else {
            if (getActivity() != null) {
                MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                        getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                if(mediaProjectionManager != null) {
                    startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                }
            }
        }
    }

    private boolean isSdOnline() {
        if (!a.getApplication().isSdcardExist())
            a.getApplication().showToastShort(getResources().getString(R.string.sdcard_offline));
        return a.getApplication().isSdcardExist();
    }

    private class MyGridViewAdapter extends BaseAdapter {
        private ArrayList<FileInfo> dataList = new ArrayList<>();
        private Map<String, LoadCover> taskCollection = new HashMap<>();
        private Context mContext;
        private String mIP;
        private boolean isCancelTask;

        MyGridViewAdapter(Context context) {
            mContext = context;
            mIP = ClientManager.getClient().getAddress();
        }

        void setDataList(ArrayList<FileInfo> dataList) {
            if(this.dataList != null){
                this.dataList.clear();
            }else{
                this.dataList = new ArrayList<>();
            }
            if(dataList != null){
                this.dataList.addAll(dataList);
            }
        }

        private void clearDataList() {
            if (dataList != null)
                dataList.clear();
        }

        private boolean checkIsBtn(int position) {
            boolean isOk = false;
            if (dataList == null) {
                if (position == 0) {
                    isOk = true;
                }
            } else {
                if (position >= dataList.size()) {
                    isOk = true;
                }
            }
            return isOk;
        }

        @Override
        public int getCount() {
            return dataList == null ? 1 : dataList.size() + 1;
        }

        @Override
        public Object getItem(int i) {
            FileInfo item = null;
            if (dataList != null && i >= 0 && i < dataList.size()) {
                item = dataList.get(i);
            }
            return item;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_image, viewGroup, false);
                viewHolder = new ViewHolder(view);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            if(!mGridView.isMeasure()) {
                if (dataList != null) {
                    if (i < dataList.size()) {
                        if (!TextUtils.isEmpty(mApplication.getUUID())) {
                            FileInfo fileInfo = (FileInfo) getItem(i);
                            if (fileInfo != null) {
                                String filename = fileInfo.getName();
                                String saveFilename = AppUtils.getDownloadFilename(fileInfo);
                                if (!TextUtils.isEmpty(filename)) {
                                    int fileType = AppUtils.judgeFileType(filename);
                                    switch (fileType) {
                                        case FILE_TYPE_PIC: {
                                            viewHolder.layoutVideo.setVisibility(View.GONE);
                                            String saveUrl = AppUtils.splicingFilePath(mApplication.getAppFilePath(),
                                                    AppUtils.checkCameraDir(fileInfo), DIR_DOWNLOAD) + File.separator + saveFilename;
                                            if (AppUtils.checkFileExist(saveUrl)) {
                                                Bitmap bitmap = ThumbLoader.getInstance().loadLocalThumbnail(mContext, saveUrl, viewWidth, viewHeight);
                                                viewHolder.imageView.setImageBitmap(bitmap);
                                                hideLoadDialog(fileInfo);
                                            } else {
                                                viewHolder.imageView.setImageResource(R.mipmap.ic_default_picture);
                                                viewHolder.imageView.setTag(saveUrl);
                                                getPictureThumb(fileInfo, i);
                                            }
                                            break;
                                        }
                                        case FILE_TYPE_VIDEO: {
                                            viewHolder.layoutVideo.setVisibility(View.VISIBLE);
                                            String savePath = AppUtils.splicingFilePath(mApplication.getAppFilePath(),
                                                    AppUtils.checkCameraDir(fileInfo), DIR_DOWNLOAD)
                                                    + File.separator + saveFilename;
                                            if (AppUtils.checkFileExist(savePath)) {
                                                viewHolder.imageView.setImageResource(R.mipmap.ic_default_picture);
                                                viewHolder.imageView.setTag(savePath);
                                                getLoadVideoThumb(fileInfo, i);
                                            } else {
                                                getVideoThumb(viewHolder.imageView, fileInfo);
                                            }
                                            viewHolder.tvDuration.setText(TimeFormate.getTimeFormatValue(fileInfo.getDuration()));
                                            break;
                                        }
                                        default:
                                            viewHolder.layoutVideo.setVisibility(View.GONE);
                                            viewHolder.imageView.setImageResource(R.mipmap.ic_default_picture);
                                            break;
                                    }
                                } else Dbug.e(tag, "filename=" + filename);
                            } else Dbug.e(tag, "FileInfo null");
                        }
                    } else {
                        viewHolder.layoutVideo.setVisibility(View.GONE);
                        viewHolder.imageView.setImageResource(R.mipmap.ic_gallery_gary);
                    }
                } else {
                    viewHolder.layoutVideo.setVisibility(View.GONE);
                    viewHolder.imageView.setImageResource(R.mipmap.ic_gallery_gary);
                }
            }
            return view;
        }

        private void getPictureThumb(FileInfo fileInfo, int position) {
            String savePath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(fileInfo), DIR_DOWNLOAD)
                    + File.separator + AppUtils.getDownloadFilename(fileInfo);
            //Dbug.i(tag, "-getPictureThumb- savePath : " +savePath);
            if(!taskCollection.containsKey(savePath)) {
                LoadCover loadPhotoCover = new LoadCover();
                taskCollection.put(savePath, loadPhotoCover);
                loadPhotoCover.execute(position);
            }
        }

        private void getVideoThumb(final ImageView ivCover, final FileInfo fileInfo) {
            //   Dbug.e(tag, "getVideoThumb");
            String cameraDir = AppUtils.checkCameraDir(fileInfo);
            String saveUrl = AppUtils.splicingFilePath(mApplication.getAppFilePath(), cameraDir, DIR_THUMB)
                    + File.separator + AppUtils.getVideoThumbName(fileInfo);
            File file = new File(saveUrl);
            if (file.exists()) {
                ThumbLoader.getInstance().loadLocalThumbnail(mContext, saveUrl, viewWidth, viewHeight);
                Bitmap bitmap = ThumbLoader.getInstance().getBitmap(saveUrl);
                if (bitmap != null) {
                    ivCover.setImageBitmap(bitmap);
                } else {
                    ivCover.setImageResource(R.mipmap.ic_default_picture);
                }
                hideLoadDialog(fileInfo);
            }
        }

        private void getLoadVideoThumb(FileInfo fileInfo, int position) {
            // Dbug.e(tag, "getLoadVideoThumb");
            String saveFilename = AppUtils.getDownloadFilename(fileInfo);
            String savePath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(fileInfo), DIR_DOWNLOAD)
                    + File.separator + saveFilename;
            if(!taskCollection.containsKey(savePath)) {
                LoadCover loadVideoCover = new LoadCover();
                taskCollection.put(savePath, loadVideoCover);
                loadVideoCover.execute(position);
            }
        }

        private class ViewHolder {
            private ImageView imageView;
            private RelativeLayout layoutVideo;
            private TextView tvDuration;

            ViewHolder(View view) {
                imageView = (ImageView) view.findViewById(R.id.item_image_iv);
                imageView.setLayoutParams(new RelativeLayout.LayoutParams(viewWidth, viewHeight));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                layoutVideo = (RelativeLayout) view.findViewById(R.id.item_image_video_layout);
                tvDuration = (TextView) view.findViewById(R.id.item_image_duration);

                view.setTag(this);
            }
        }

        /**
         * 取消所有正在下载或等待下载的任务。
         */
        private void cancelAllTasks() {
            isCancelTask = true;
            if (taskCollection != null) {
                Set<String> keySet = taskCollection.keySet();
                if(keySet.size() > 0) {
                    for (String url : keySet) {
                        LoadCover task = taskCollection.get(url);
                        if(task != null){
                            task.cancel(true);
                        }
                    }
                }
                taskCollection.clear();
            }
            isCancelTask = false;
        }

        private class LoadCover extends AsyncTask<Integer, Void,  Bitmap> {
            private int position;
            private String imageUrl;
            private Bitmap bmp;
            private FileInfo info;

            @Override
            protected Bitmap doInBackground(Integer... integers) {
                position = integers[0];
                info = (FileInfo) getItem(position);
                if(info != null){
                    int source = info.getSource();
                    if(source == COME_FORM_LOCAL){
                        imageUrl = info.getPath();
                    }else{
                        imageUrl = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(info), DIR_DOWNLOAD)
                                + File.separator + AppUtils.getDownloadFilename(info);
                    }
                    if(info.isVideo()) {
                        ThumbLoader.getInstance().loadLocalVideoThumb(mContext, imageUrl, viewWidth, viewHeight, new ThumbLoader.OnLoadVideoThumbListener() {
                            @Override
                            public void onComplete(Bitmap bitmap, int duration) {
                                bmp = bitmap;
                                info.setDuration(duration);
                            }
                        });
                    }else{
                        String saveThumbPath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(info),
                                DIR_THUMB) + File.separator + AppUtils.getVideoThumbName(info);
                        File file = new File(saveThumbPath);
                        Bitmap bitmap = null;
                        if (file.exists()) {
                            ThumbLoader.getInstance().loadLocalThumbnail(mContext, saveThumbPath, viewWidth, viewHeight);
                            bitmap = ThumbLoader.getInstance().getBitmap(saveThumbPath);
                        }
                        if(bitmap == null){
                            String url = AppUtils.formatUrl(mIP, DEFAULT_HTTP_PORT, info.getPath());
                            ThumbLoader.getInstance().loadWebThumbnail(mContext, url, saveThumbPath, viewWidth, viewHeight, new ThumbLoader.OnLoadThumbListener() {
                                @Override
                                public void onComplete(Bitmap bitmap) {
                                    if(bitmap != null){
                                        bmp = bitmap;
                                    }
                                }
                            });
                        }else{
                            bmp = bitmap;
                        }

                    }
                    int totalTime = 0;
                    while (bmp == null && !isCancelTask){
                        try {
                            Thread.sleep(5);
                            totalTime += 5;
                            if(totalTime > 2000){
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    bmp = null;
                }
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                ImageView ivCover = (ImageView) mGridView.findViewWithTag(imageUrl);
                if(ivCover != null){
                    if(bitmap != null){
                        ivCover.setImageBitmap(bitmap);
                    }else {
                        ivCover.setImageResource(R.mipmap.ic_default_picture);
                    }
                }
                hideLoadDialog(info);
                taskCollection.remove(imageUrl);
            }
        }
    }

    private void showWaitingDialog(){
        if(getActivity() != null && !getActivity().isDestroyed()) {
            if (mWaitingDialog == null) {
                mWaitingDialog = new WaitingDialog();
                mWaitingDialog.setCancelable(false);
                mWaitingDialog.setNotifyContent(getString(R.string.loading));
                mWaitingDialog.setOnWaitingDialog(new WaitingDialog.OnWaitingDialog() {
                    @Override
                    public void onCancelDialog() {
                        dismissWaitingDialog();
                        if(isAdjustResolution){
                            isAdjustResolution = false;
                        }
                    }
                });
            }
            if (!mWaitingDialog.isShowing()) {
                Dbug.w(tag, "mWaitingDialog show");
                mWaitingDialog.show(getFragmentManager(), "mLoadingDialog");
            }
        }
    }

    private void dismissWaitingDialog(){
        if(mWaitingDialog != null){
            if(mWaitingDialog.isShowing()){
                Dbug.w(tag, "mWaitingDialog dismiss");
                mWaitingDialog.dismiss();
            }
            mWaitingDialog = null;
        }
    }

    private class SaveVideoThumb extends Thread {
        private SoftReference<Handler> softReference;
        private byte[] data;

        SaveVideoThumb(byte[] data, Handler handler) {
            this.data = data;
            softReference = new SoftReference<>(handler);
        }

        @Override
        public void run() {
            super.run();
            Handler handler = softReference.get();
            Bitmap bitmap = null;
            if (data != null && data.length > 0) {
                try {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    FileInfo fileInfo = null;
                    if (thumbList != null && thumbList.size() > 0) {
                        fileInfo = thumbList.remove(0);
                    }
                    if (bitmap != null) {
                        if (fileInfo != null && !TextUtils.isEmpty(mApplication.getUUID())) {
                            String cameraDir = AppUtils.getMediaDirectory(fileInfo.getCameraType());
                            Dbug.w(tag, "-SaveVideoThumb- cameraDir : " + cameraDir);
                            String savePath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), cameraDir, DIR_THUMB)
                                    + File.separator + AppUtils.getVideoThumbName(fileInfo);
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap, viewWidth, viewHeight,
                                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                            ThumbLoader.getInstance().addBitmap(savePath, bitmap);
                            if (handler != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mAdapter != null) {
                                            Dbug.w(tag, "-update data changed-");
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                            Dbug.w(tag, "-SaveVideoThumb- savePath : " +savePath);
                            if (AppUtils.bitmapToFile(bitmap, savePath, 100)) {
                                Dbug.w(tag, "save bitmap ok!");
                            } else {
                                Dbug.w(tag, "save bitmap failed!");
                            }
                        }
                    }

                    if (thumbList != null && thumbList.size() == 0) {
                        Dbug.i(tag, "mVideoThumbnail close - 004");
                        if (handler != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mAdapter != null) {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                        if (mVideoThumbnail != null && !mVideoThumbnail.close()) {
                            Dbug.w(tag, "Close Video thumbnail failed");
                        }
                    }
                    collections.remove(this);
                }
            }
        }
    }

    private void cancelSaveThread() {
        if (collections != null) {
            for (SaveVideoThumb thread : collections) {
                thread.interrupt();
            }
            collections.clear();
        }
    }

    /**
     * 拍照声效
     */
    private void shootSound() {
        if (getActivity() == null) {
            return;
        }
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int volume = 0;
        if (audioManager != null) volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        Dbug.i(tag, "volume=:" + volume);

        if (volume != 0) {
            MediaPlayer mMediaPlayer = MediaPlayer.create(getActivity(), (R.raw.camera_click));
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                    }
                }
            });
            mMediaPlayer.start();
        }
    }

    private void handlerVideoUI() {
        if (recordStatus == STATUS_RECORDING) {
            showVideoUI();
        } else {
            hideVideoUI();
        }
    }

    private void showVideoUI(){
        if (wakeLock != null) {
            wakeLock.acquire(60*60*60);
        }
        ivRecordFlag.setVisibility(View.VISIBLE);
        mRecordButton.setImageResource(R.mipmap.ic_record_selected);
        Animation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(500);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        ivRecordFlag.startAnimation(alphaAnimation);
    }

    private void hideVideoUI(){
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
        }
        ivRecordFlag.clearAnimation();
        ivRecordFlag.setVisibility(View.GONE);
        mRecordButton.setImageResource(R.drawable.drawable_record_control);
    }

    private void handlerRTVoiceUI(){
        if(isRtVoiceOpen){
            ibtnVoice.setImageResource(R.mipmap.ic_microphone_selected);
        }else{
            ibtnVoice.setImageResource(R.drawable.drawable_voice_control);
        }
    }

    private void showPopupMenu(View view, int level){
        Map<Integer, Integer> resMap = new HashMap<>();
        String[] levels;
        if(mCameraType == DeviceClient.CAMERA_REAR_VIEW){
            levels = mApplication.getDeviceDesc().getRear_support();
        }else{
            levels = mApplication.getDeviceDesc().getFront_support();
        }
        if(levels != null) {
            int currentLevel = AppUtils.getStreamResolutionLevel();
            int currentRecordLevel = getCameraLevel(mApplication.getDeviceSettingInfo().getCameraType());
            Dbug.e(tag, "======currentLevel====== " + currentLevel);
            for (String str : levels){
                if(!TextUtils.isEmpty(str) && TextUtils.isDigitsOnly(str)){
                    int rtsLevel = Integer.valueOf(str);
                    if (recordStatus != STATUS_RECORDING) {
                        if (rtsLevel != currentLevel) {
                            resMap.put(rtsLevel, getLevelResId(rtsLevel));
                        }
                    } else {
                        if (currentRecordLevel == RTS_LEVEL_FHD && rtsLevel < RTS_LEVEL_FHD) {//Record:1080
                            if (rtsLevel != currentLevel) {
                                resMap.put(rtsLevel, getLevelResId(rtsLevel));
                            }
                        } else if (currentRecordLevel < RTS_LEVEL_FHD) {//Record: vga/720
                            if (rtsLevel != currentLevel) {
                                resMap.put(rtsLevel, getLevelResId(rtsLevel));
                            }
                        }
                    }
                }
            }
            if (popupMenu != null) {
                popupMenu.dismiss();
                popupMenu = null;
            }
            popupMenu = new PopupMenu(getContext(), resMap);
            popupMenu.setOnPopItemClickListener(mOnPopItemClickListener);
            popupMenu.showAsUp(view);
        }
    }

    private PopupMenu.OnPopItemClickListener mOnPopItemClickListener = new PopupMenu.OnPopItemClickListener() {
        @Override
        public void onItemClick(int level, Integer resId, int index) {
            switchStreamResolution(level);
        }
    };

    private void switchStreamResolution(int level) {
        isAdjustResolution = true;
        AppUtils.saveStreamResolutionLevel(level);
        closeRTS();
        showWaitingDialog();
    }

    private OnRealTimeListener realtimePlayerListener = new OnRealTimeListener() {
        @Override
        public void onVideo(int t, int channel, byte[] data, long sequence, long timestamp) {
//            Dbug.i(tag, "-onVideo- data length = " + data.length);
            if(isStartDebug && PreferencesHelper.getSharedPreferences(mApplication).getBoolean(IConstant.DEBUG_SETTINGS, false)){
                fps++;
            }
            if (mRecordVideo != null && isRecordPrepared) {
                if (!mRecordVideo.write(t, data)) Dbug.e(tag, "Write video failed");
            }
            if (mVideoCapture != null && isCapturePrepared) {
                mVideoCapture.capture(data);
            }
        }

        @Override
        public void onAudio(int t, int channel, byte[] data, long sequence, long timestamp) {
//            Dbug.i(tag, "-onAudio- data length = " + data.length);
            if (mRecordVideo != null && isRecordPrepared) {
                if (!mRecordVideo.write(t, data)) Dbug.e(tag, "Write audio failed");
            }
        }

        @Override
        public void onStateChanged(int state) {
            Dbug.e(tag, "onStateChanged:state=" + state);
            if(state == Stream.Status.PREPARE || state == Stream.Status.PLAYING){
                if(isSwitchCamera){
                    if(mHandler != null){
                        mHandler.removeCallbacks(dismissDialogRunnable);
                        mHandler.post(dismissDialogRunnable);
                    }
                    isSwitchCamera = false;
                }
            }else{
                if (state == Stream.Status.STOP) {
                    if (mProgressBarLoading.getVisibility() == View.VISIBLE) {
                        mProgressBarLoading.setVisibility(View.GONE);
                    }
                    if (mRTSPlayButton.getVisibility() != View.VISIBLE) {
                        mRTSPlayButton.setVisibility(View.VISIBLE);
                    }
                    stopLocalRecording();
                }
            }
        }

        @Override
        public void onError(int code, String message) {
            Dbug.e(tag, "code=" + code + ", message=" + message);
//            mApplication.showToastShort("player has a error, message : "+ message);
            closeRTS();
        }
    };

    @Override
    public void onRecord(byte[] data, int dB) {
        if(data != null){
            if(intercomManager != null){
                intercomManager.writeData(data);
            }
        }
    }

    @Override
    public void onError(int code) {
        if(intercomManager != null){
            intercomManager.stopSendDataThread();
        }
        if(mHandler != null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    isRtVoiceOpen = false;
                    handlerRTVoiceUI();
                }
            });
        }
    }


    private void hideLoadDialog(FileInfo fileInfo) {
//        Dbug.e(tag, fileInfo.getPath());
        countList.remove(fileInfo);
        if (countList.size() == 0)
            dismissWaitingDialog();
    }

    /**
     * 同步设备状态
     */
    private void syncDeviceState() {
        DeviceSettingInfo deviceSettingInfo = mApplication.getDeviceSettingInfo();
        if (deviceSettingInfo != null) {
            if (recordStatus != deviceSettingInfo.getRecordState()) {
                recordStatus = deviceSettingInfo.getRecordState();
                handlerVideoUI();
            }
            if (isProjection != deviceSettingInfo.isOpenProjection()) {
                isProjection = deviceSettingInfo.isOpenProjection();
                handlerProjectionUI();
            }
            if (isRtVoiceOpen != deviceSettingInfo.isRTVoice()) {
                isRtVoiceOpen = deviceSettingInfo.isRTVoice();
                handlerRTVoiceUI();
            }
            if(mCameraType != deviceSettingInfo.getCameraType()){
                mCameraType = deviceSettingInfo.getCameraType();
                if (mHandler != null) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_LOAD_DEV_THUMBS, 1, 0));
                }
            }
            int currentLevel = AppUtils.getStreamResolutionLevel();
            ibtnAdjustResolution.setImageResource(getLevelResId(currentLevel));
            if(!mApplication.isSdcardExist()){
                clearDataAndUpdate();
            }
        }
    }

    private void clearDataAndUpdate() {
        if (totalList != null) {
            totalList.clear();
        }
        if (mAdapter != null) {
            mAdapter.cancelAllTasks();
            mAdapter.clearDataList();
            mAdapter.notifyDataSetChanged();
        }
    }

    private FileInfo findFileInfo(String path) {
        FileInfo fileInfo = null;
        if (!TextUtils.isEmpty(path) && totalList != null) {
            for (FileInfo info : totalList) {
                if (path.equals(info.getPath())) {
                    fileInfo = info;
                    break;
                }
            }
        }
        return fileInfo;
    }

    private void rotateDeviceMsgLayout(){
        int cameraType = mApplication.getDeviceSettingInfo().getCameraType();
        boolean isSwitchCamera = (mCameraType != cameraType);
        if(!isSwitchCamera){
            return;
        }
        mCameraType = cameraType;
        float centerX;
        float centerY;
        Rotate3dAnimation rotation;

        int currentLevel = AppUtils.getStreamResolutionLevel();
        ibtnAdjustResolution.setImageResource(getLevelResId(currentLevel));
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_LOAD_DEV_THUMBS, 1, 0));
        }
    }



    private int getLevelResId(int level){
        int res;
        switch (level){
            case RTS_LEVEL_SD:
                res = R.drawable.drawable_resolution_sd;
                break;
            case RTS_LEVEL_FHD:
                res = R.drawable.drawable_resolution_fhd;
                break;
            default:
                res = R.drawable.drawable_resolution_hd;
                break;
        }
        return res;
    }

    private int getCameraLevel(int cameraType){
        int level = IConstant.RTS_LEVEL_HD;
        DeviceSettingInfo settingInfo = a.getApplication().getDeviceSettingInfo();
        if(settingInfo != null){
            if(cameraType == DeviceClient.CAMERA_REAR_VIEW){
                level = settingInfo.getRearLevel();
            }else{
                level = settingInfo.getFrontLevel();
            }
        }
        return level;
    }

    private int getRtsFormat(){
        int format = DeviceClient.RTS_H264;
        DeviceDesc settingInfo = a.getApplication().getDeviceDesc();
        if(settingInfo != null) {
            format = settingInfo.getVideoType();
        }
        return format;
    }

    private int getVideoRate(int cameraType){
        int rate = 30;
        DeviceSettingInfo settingInfo = a.getApplication().getDeviceSettingInfo();
        if(settingInfo != null){
            if(cameraType == DeviceClient.CAMERA_REAR_VIEW){
                rate = settingInfo.getRearRate();
            }else{
                rate = settingInfo.getFrontRate();
            }
        }
        return rate;
    }

    private void goToPlayBack(FileInfo info){
        ClientManager.getClient().unregisterNotifyListener(onNotifyResponse);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        if(info != null){
            intent.putExtra(KEY_FILE_INFO, info);
        }
        startActivityForResult(intent, CODE_PLAYBACK);
    }

    private void updateResolutionUI(boolean isRearCamera, int width, int height) {
        if (isAdjustResolution) {
            Dbug.w(tag, "adjust resolution step 006. isRear "+ isRearCamera + ", w " + width +", h " + height);
            int rtsLevel = AppUtils.adjustRtsResolution(width, height);
            if(rtsLevel != getCameraLevel(mApplication.getDeviceSettingInfo().getCameraType())){
                if (isRearCamera) {
                    mApplication.getDeviceSettingInfo().setRearLevel(rtsLevel);
                } else {
                    mApplication.getDeviceSettingInfo().setFrontLevel(rtsLevel);
                }
            }
            isAdjustResolution = false;
            dismissWaitingDialog();
        }
        int currentLevel = AppUtils.getStreamResolutionLevel();
        ibtnAdjustResolution.setImageResource(getLevelResId(currentLevel));
    }

    private void showLocalRecordDialog() {
        if(mLocalRecordingDialog == null) {
            mLocalRecordingDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.no_card_record_tip,
                    R.string.dialog_cancel, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            mLocalRecordingDialog.dismiss();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                @Override
                public void onClick() {
                    mLocalRecordingDialog.dismiss();
                    startLocalRecording();
                }
            });
            mLocalRecordingDialog.setCancelable(false);
        }
        if(!mLocalRecordingDialog.isShowing()){
            mLocalRecordingDialog.show(getActivity().getSupportFragmentManager(), "No_Card_Record");
        }
    }

    private void startLocalRecording() {
        if (mRecordVideo == null) {
            mRecordVideo = new VideoRecord();
            mRecordVideo.prepare(new OnRecordStateListener() {
                @Override
                public void onPrepared() {
                    isRecordPrepared = true;
                    showVideoUI();
                }

                @Override
                public void onStop() {
                    isRecordPrepared = false;
                    hideVideoUI();
                }

                @Override
                public void onError(String message) {
                    Dbug.e(tag, "Record error:" + message);
                    mRecordVideo = null;
                    isRecordPrepared = false;
                    hideVideoUI();
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

    private void startDebug(){
        fps = 0;
        isStartDebug = true;
        if(mHandler != null){
            mHandler.sendEmptyMessage(MSG_FPS_COUNT);
        }
        if(mDebugHelper == null){
            mDebugHelper = new DebugHelper();
            mDebugHelper.registerDebugListener(mIDebugListener);
        }
        mDebugHelper.startDebug();
    }

    private void closeDebug(){
        if(mDebugHelper != null){
            isStartDebug = false;
            fps = 0;
            if(mHandler != null){
                mHandler.removeMessages(MSG_FPS_COUNT);
            }
            mDebugHelper.unregisterDebugListener(mIDebugListener);
            mDebugHelper.closeDebug();
            mDebugHelper = null;
        }
    }

    private void updateDebug(int dropCount, int dropSum){
        if(mVideoView != null){
            InfoHudViewHolder mHudViewHolder = mVideoView.getHudView();
            if(mHudViewHolder != null){
                mHudViewHolder.setRowValue(R.string.drop_packet_count, dropCount+"");
                mHudViewHolder.setRowValue(R.string.drop_packet_sum, dropSum+"");
            }
        }
    }

    private void updateDebugFps(int fps){
        if(mVideoView != null){
            InfoHudViewHolder mHudViewHolder = mVideoView.getHudView();
            if(mHudViewHolder != null){
                mHudViewHolder.setRowValue(R.string.fps, fps+"");
            }
        }
    }

    private IDebugListener mIDebugListener = new IDebugListener() {
        @Override
        public void onStartDebug(String remoteIp, int sendDataLen, int sendDataInterval) {

        }

        @Override
        public void onDebugResult(int dropCount, int dropSum) {
            Dbug.w("zzc", "-onDebugResult- dropCount : " + dropCount + ", dropSum : " +dropSum);
            updateDebug(dropCount,dropSum);
        }

        @Override
        public void onError(int code, String message) {
            Dlog.w(tag, message);
        }
    };
}
