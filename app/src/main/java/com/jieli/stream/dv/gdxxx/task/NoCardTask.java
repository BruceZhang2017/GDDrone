package com.jieli.stream.dv.gdxxx.task;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.player.MovWrapper;
import com.jieli.lib.dv.control.player.OnRealTimeListener;
import com.jieli.lib.dv.control.player.OnRecordListener;
import com.jieli.lib.dv.control.player.RealtimeStream;
import com.jieli.lib.dv.control.player.Stream;
import com.jieli.media.codec.FrameCodec;
import com.jieli.media.codec.bean.MediaMeta;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.DeviceSettingInfo;
import com.jieli.stream.dv.gdxxx.bean.TaskReply;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.io.File;

/**
 * 无卡录像/拍照等处理
 * @author zqjasonZhong
 *         date : 2017/10/24
 */
public class NoCardTask extends HandlerThread{
    private String tag = getClass().getSimpleName();
    private Handler mWorkHandler;
    private Handler mUIHandler;
    private MovWrapper mMovWrapper;
    private RealtimeStream mRealtimeStream;

    private String savePath;
    private boolean isPhoto;
    private boolean isVideo;
    private boolean isRecData;

    private static final long DEFAULT_VIDEO_SIZE = 60 * 1024 * 1024; //720P 1分钟大概60M
    private static final long MIN_STORAGE_LIMIT = 200 * 1024 * 1024; //最小内存限制 100M

    public NoCardTask(String name) {
        super(name, Process.THREAD_PRIORITY_MORE_FAVORABLE);
    }

    @Override
    protected void onLooperPrepared() {
        mWorkHandler = new Handler(getLooper(), mHandlerCallback);
        super.onLooperPrepared();
    }

    private static final int MSG_HANDLER_VIDEO = 0xcdc0;
    private static final int MSG_HANDLER_PHOTO = 0xcdc1;
    private static final int MSG_SAVE_PHOTO = 0xcdc2;

    public static final int MSG_VIDEO_RESULT = 0xcdcd;
    public static final int MSG_PHOTO_RESULT = 0xcdcf;
    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message != null){
                switch (message.what){
                    case MSG_HANDLER_VIDEO:{
                        if(!isVideo){
                            startVideo();
                        }else{
                            stopVideo();
                        }
                        break;
                    }
                    case MSG_HANDLER_PHOTO:{
                        TaskReply reply = new TaskReply();
                        if(isRecData) {
                            long storageSize = AppUtils.getAvailableExternalMemorySize();
                            if(storageSize > MIN_STORAGE_LIMIT) { //大于限制才允许截图
                                isPhoto = true;
                                reply.setResult(IConstant.STATE_START)
                                        .setCode(IConstant.RESULT_SUCCESS);
                                dispensedUIMsg(MSG_PHOTO_RESULT, reply);
                                if(mUIHandler != null){
                                    mUIHandler.postDelayed(timeoutTask, 3000L);
                                }
                            }else{
                                isPhoto = false;
                                reply.setResult(IConstant.STATE_START)
                                        .setCode(IConstant.RESULT_FALSE)
                                        .setMsg(a.getApplication().getString(R.string.storage_error));
                                dispensedUIMsg(MSG_PHOTO_RESULT, reply);
                            }
                        }else {
                            isPhoto = false;
                            reply.setResult(IConstant.STATE_START)
                                    .setCode(IConstant.RESULT_FALSE)
                                    .setMsg(a.getApplication().getString(R.string.open_rts_tip));
                            dispensedUIMsg(MSG_PHOTO_RESULT, reply);
                        }
                        break;
                    }
                    case MSG_SAVE_PHOTO:{
                        byte[] data = (byte[])message.obj;
                        adjustAndSavePhoto(data);
                        break;
                    }
                }
            }
            return false;
        }
    };

    /**
     * 设置UI回调
     * @param handler  ui回调
     */
    public void setUIHandler(Handler handler){
        mUIHandler = handler;
    }

    /**
     * 设置实时流
     * @param stream 实时流
     */
    public void setRealtimeStream(RealtimeStream stream){
        mRealtimeStream = stream;
        mRealtimeStream.registerPlayerListener(mOnRealTimeListener);
    }

    /**
     * 设置是否接收数据
     * @param isRecData 是否接收数据
     */
    public void setRecData(boolean isRecData){
        this.isRecData = isRecData;
    }

    /**
     * 录像
     */
    public void tryToVideo(){
        if(mWorkHandler != null){
            mWorkHandler.sendEmptyMessage(MSG_HANDLER_VIDEO);
        }
    }

    /**
     * 拍照
     */
    public void tryToTaskPhoto(){
        if(mWorkHandler != null){
            mWorkHandler.removeMessages(MSG_HANDLER_PHOTO);
            mWorkHandler.sendEmptyMessageDelayed(MSG_HANDLER_PHOTO, 300L);
        }
    }

    /**
     * 是否在拍照
     */
    public boolean isPhoto() {
        return isPhoto;
    }

    /**
     * 是否在录像
     */
    public boolean isVideo() {
        return isVideo;
    }

    /**
     * 开始录像任务
     */
    private void startVideo(){
        if(isRecData && !isVideo) {  //检测是否存在接收数据
            int duration;
            long storageSize = AppUtils.getAvailableExternalMemorySize();
            if(storageSize > MIN_STORAGE_LIMIT){
                if((storageSize / DEFAULT_VIDEO_SIZE) > 35){
                    duration = 30;
                }else {
                    duration = (int)(storageSize / DEFAULT_VIDEO_SIZE) - 5;
                }
                mMovWrapper = new MovWrapper();
                mMovWrapper.setTimeMaster(MovWrapper.TIME_MASTER_SYSTEM);
                mMovWrapper.setVideoDuration(duration);
                mMovWrapper.setOnRecordListener(mOnRecordListener);
                DeviceSettingInfo deviceSettingInfo = a.getApplication().getDeviceSettingInfo();
                if (DeviceClient.CAMERA_REAR_VIEW == deviceSettingInfo.getCameraType()) {
                    mMovWrapper.setFrameRate(deviceSettingInfo.getRearRate());
                    mMovWrapper.setAudioTrack(deviceSettingInfo.getRearSampleRate(), IConstant.AUDIO_CHANNEL,  IConstant.AUDIO_FORMAT);
                } else {
                    mMovWrapper.setFrameRate(deviceSettingInfo.getFrontRate());
                    mMovWrapper.setAudioTrack(deviceSettingInfo.getFrontSampleRate(),  IConstant.AUDIO_CHANNEL,  IConstant.AUDIO_FORMAT);
                }
                savePath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(),
                        a.getApplication().getCameraDir(),  IConstant.DIR_DOWNLOAD) + File.separator + AppUtils.getRecordVideoName();
                if(!mMovWrapper.create(savePath)){
                    Dbug.e(tag, "create output path failed.");
                    savePath = null;
                    TaskReply reply = new TaskReply();
                    reply.setResult(IConstant.RESULT_FALSE)
                            .setCode(IConstant.ERROR_INIT_MOV)
                            .setMsg(a.getApplication().getString(R.string.init_mov_error));
                    dispensedUIMsg(MSG_VIDEO_RESULT, reply);
                }
            }else{
                TaskReply reply = new TaskReply();
                reply.setResult(IConstant.RESULT_FALSE)
                        .setCode(IConstant.ERROR_STORAGE)
                        .setMsg(a.getApplication().getString(R.string.storage_error));
                dispensedUIMsg(MSG_VIDEO_RESULT, reply);
            }
        }else{
            isVideo = false;
            isRecData = false;
            TaskReply reply = new TaskReply();
            reply.setResult(IConstant.RESULT_FALSE)
                    .setCode(IConstant.ERROR_DATA_IS_NULL)
                    .setMsg(a.getApplication().getString(R.string.open_rts_tip));
            dispensedUIMsg(MSG_VIDEO_RESULT, reply);
        }
    }

    /**
     * 停止录像任务
     */
    private void stopVideo(){
        if(isVideo){
            isVideo = false;
            if(mMovWrapper != null && !mMovWrapper.close()){
                Dbug.w(tag, "close mov wrapper failed.");
            }
        }
    }

    /**
     * 判断帧类型并保存成图片
     * @param data    帧数据
     */
    private void adjustAndSavePhoto(byte[] data){
        if(data != null && AppUtils.checkFrameType(data) == IConstant.FRAME_TYPE_I){
            FrameCodec mFrameCodec = new FrameCodec();
            mFrameCodec.setOnFrameCodecListener(mOnFrameCodecListener);
            int cameraType = a.getApplication().getDeviceSettingInfo().getCameraType();
            int[] rtsResolution = AppUtils.getRtsResolution(getCameraLevel(cameraType));
            mFrameCodec.convertToJPG(data, rtsResolution[0], rtsResolution[1], null);
            mFrameCodec.destroy();
        }
    }

    /**
     * 获得摄像头的清晰度
     * @param cameraType   摄像头类型
     */
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

    /**
     * 保存图片
     * @param data  帧数据
     */
    private synchronized boolean saveFile(byte[] data){
        boolean result = false;
        String outPath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(),
                a.getApplication().getCameraDir(), IConstant.DIR_DOWNLOAD) + File.separator + AppUtils.getLocalPhotoName();
        if(data != null && !TextUtils.isEmpty(outPath)){
            result = AppUtils.bytesToFile(data, outPath);
            TaskReply reply = new TaskReply();
            if(result){
                reply.setResult(IConstant.STATE_END)
                        .setCode(IConstant.RESULT_SUCCESS)
                        .setMsg(outPath);
            }else{
                reply.setResult(IConstant.STATE_END)
                        .setCode(IConstant.RESULT_FALSE)
                        .setMsg(a.getApplication().getString(R.string.save_file_failed));
            }
            if(mUIHandler != null){
                mUIHandler.removeCallbacks(timeoutTask);
            }
            dispensedUIMsg(MSG_PHOTO_RESULT, reply);
        }
        isPhoto = false;
        return result;
    }

    private Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            isPhoto = false;
            TaskReply reply = new TaskReply();
            reply.setResult(IConstant.STATE_END)
                    .setCode(IConstant.RESULT_FALSE)
                    .setMsg(a.getApplication().getString(R.string.take_photo_timeout));
            if(mUIHandler != null){
                mUIHandler.removeCallbacks(timeoutTask);
            }
            dispensedUIMsg(MSG_PHOTO_RESULT, reply);
        }
    };

    /**
     * 通知UI消息
     * @param what    消息类型
     * @param reply   回复
     */
    private void dispensedUIMsg(int what, TaskReply reply){
        if(mUIHandler != null){
            mUIHandler.sendMessage(mUIHandler.obtainMessage(what, reply));
        }
    }

    /**
     * 转码成JPG的回调处理
     */
    private final FrameCodec.OnFrameCodecListener mOnFrameCodecListener = new FrameCodec.OnFrameCodecListener() {
        @Override
        public void onCompleted(byte[] bytes, MediaMeta mediaMeta) {
            saveFile(bytes);
        }

        @Override
        public void onError(String s) {
            isPhoto = false;
            TaskReply reply = new TaskReply();
            reply.setResult(IConstant.STATE_END)
                    .setCode(IConstant.RESULT_FALSE)
                    .setMsg(s);
            if(mUIHandler != null){
                mUIHandler.removeCallbacks(timeoutTask);
            }
            dispensedUIMsg(MSG_PHOTO_RESULT, reply);
        }
    };

    /**
     * 录像回调处理
     */
    private OnRecordListener mOnRecordListener = new OnRecordListener() {

        @Override
        public void onStateChanged(int state, String msg) {
            super.onStateChanged(state, msg);
            if(isRecData) {
                TaskReply reply = new TaskReply();
                isVideo = (state == MovWrapper.REC_STATE_START);
                reply.setResult(state);
                reply.setMsg(msg);
                dispensedUIMsg(MSG_VIDEO_RESULT, reply);
            }else{
                stopVideo();
            }
        }

        @Override
        public void onError(int code, String msg) {
            super.onError(code, msg);
            if(isVideo){
                isVideo = false;
                if(!TextUtils.isEmpty(savePath)){
                    File file = new File(savePath);
                    if(file.exists() && !file.delete()){
                        Dbug.w(tag, "error file delete failed.");
                    }
                    savePath = null;
                }
                TaskReply reply = new TaskReply();
                reply.setResult(IConstant.RESULT_FALSE)
                        .setCode(code)
                        .setMsg(msg);
                dispensedUIMsg(MSG_VIDEO_RESULT, reply);
            }
        }
    };

    /**
     * 实时流回调处理
     */
    private OnRealTimeListener mOnRealTimeListener = new OnRealTimeListener() {
        @Override
        public void onVideo(int type, int channel, byte[] data, long sequence, long timestamp) {
            if(isVideo){
                if(mMovWrapper != null){
                    mMovWrapper.write(type, data);
                }
            }
            if(isPhoto){
                if(mWorkHandler != null){
                    mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MSG_SAVE_PHOTO, data));
                }
            }
        }

        @Override
        public void onAudio(int type, int channel, byte[] data, long sequence, long timestamp) {
            if(isVideo){
                if(mMovWrapper != null){
                    mMovWrapper.write(type, data);
                }
            }
        }

        @Override
        public void onStateChanged(int state) {
            isRecData = (state == Stream.Status.PLAYING);
            if(!isRecData){
                TaskReply reply = new TaskReply();
                reply.setResult(IConstant.RESULT_FALSE)
                        .setCode(IConstant.ERROR_DATA_IS_NULL)
                        .setMsg(a.getApplication().getString(R.string.open_rts_tip));
                if(isVideo){
                    stopVideo();
                    dispensedUIMsg(MSG_VIDEO_RESULT, reply);
                }
                if(isPhoto){
                    isPhoto = false;
                    dispensedUIMsg(MSG_PHOTO_RESULT, reply);
                }
            }
        }

        @Override
        public void onError(int code, String message) {
            isRecData = false;
            TaskReply reply = new TaskReply();
            if(isVideo){
                stopVideo();
                reply.setResult(IConstant.RESULT_FALSE)
                        .setCode(code)
                        .setMsg(message);
                dispensedUIMsg(MSG_VIDEO_RESULT, reply);
            }
            if(isPhoto){
                isPhoto = false;
                reply.setResult(IConstant.STATE_END)
                        .setCode(code)
                        .setMsg(message);
                if(mUIHandler != null){
                    mUIHandler.removeCallbacks(timeoutTask);
                }
                dispensedUIMsg(MSG_PHOTO_RESULT, reply);
            }
            if(mWorkHandler != null){
                mWorkHandler.removeCallbacksAndMessages(null);
            }
        }
    };

    public void release(){
        stopVideo();
        isPhoto = false;
        if(mRealtimeStream != null){
            mRealtimeStream.unregisterPlayerListener(mOnRealTimeListener);
            mRealtimeStream = null;
        }
    }
}
