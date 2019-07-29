package com.jieli.stream.dv.gdxxx.data;

import android.text.TextUtils;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.model.MediaInfo;
import com.jieli.lib.dv.control.player.AviWrapper;
import com.jieli.lib.dv.control.player.MovWrapper;
import com.jieli.lib.dv.control.player.OnAviWrapperListener;
import com.jieli.lib.dv.control.player.OnRecordListener;
import com.jieli.lib.dv.control.utils.ClientContext;
import com.jieli.stream.dv.gdxxx.bean.DeviceSettingInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.io.File;

/**
 * Description:Save real-time media data and wrap as MOV file.
 * Author:created by bob on 17-11-21.
 */
public class VideoRecord {
    private final String tag = getClass().getSimpleName();
    private static final long DEFAULT_VIDEO_SIZE = 60 * 1024 * 1024; //720P 1分钟大概60M
    private static final long MIN_STORAGE_LIMIT = 200 * 1024 * 1024; //最小内存限制 100M
    private MediaInfo mMediaInfo = null;


    private MovWrapper mMovWrapper = null;
    private AviWrapper mAviWrapper;
    private OnRecordStateListener onStreamRecordListener = null;
    private String mCurrentFilePath;

    public VideoRecord() {
        mMediaInfo = null;
    }

    public VideoRecord(MediaInfo mediaInfo) {
        mMediaInfo = mediaInfo;
    }

    private void dispatchErrorMessage(final String msg) {
        ClientContext.post(new Runnable() {
            @Override
            public void run() {
                if (onStreamRecordListener != null)
                    onStreamRecordListener.onError(msg);
            }
        });
    }

    public void prepare(OnRecordStateListener listener) {
        onStreamRecordListener = listener;
        boolean ret;
        if (mMediaInfo != null) {
            String path = mMediaInfo.getPath();
            if (TextUtils.isEmpty(path)) {
                Dbug.e(tag, "Filename is null");
                return;
            }
            if(path.endsWith("AVI") || path.endsWith("avi")){
                ret = startAviWrapper();
            }else{
                ret = startMovWrapper();
            }
        } else {
            if(a.getApplication().getDeviceDesc().getVideoType() == DeviceClient.RTS_JPEG){
                ret = startAviWrapper();
            }else{
                ret = startMovWrapper();
            }
        }
        if (ret){
            ClientContext.post(new Runnable() {
                @Override
                public void run() {
                    if (onStreamRecordListener != null)
                        onStreamRecordListener.onPrepared();
                }
            });
        }
    }

    /**
     * 启动MOV封装器
     * @return 结果
     */
    private boolean startMovWrapper(){
        long storageSize = AppUtils.getAvailableExternalMemorySize();
        if(storageSize < MIN_STORAGE_LIMIT) {
            Dbug.e(tag, "Not enough storage space");
            dispatchErrorMessage("Not enough storage space");
            return false;
        }
        if(mAviWrapper != null){
            if(mAviWrapper.isRecording()){
                mAviWrapper.stopRecording();
            }
            mAviWrapper = null;
        }
        if (mMovWrapper != null) {
            mMovWrapper.close();
            mMovWrapper = null;
        }
        int duration;
        if((storageSize / DEFAULT_VIDEO_SIZE) > 35){
            duration = 30;//unit is minute 30
        } else {
            duration = (int)(storageSize / DEFAULT_VIDEO_SIZE) - 5;
        }

        mMovWrapper = new MovWrapper();
        mMovWrapper.setVideoDuration(duration);
        mMovWrapper.setOnRecordListener(onRecordListener);
        DeviceSettingInfo deviceSettingInfo = a.getApplication().getDeviceSettingInfo();
        if (DeviceClient.CAMERA_REAR_VIEW == deviceSettingInfo.getCameraType()) {
            if (mMediaInfo == null) {
                if (!mMovWrapper.setFrameRate(deviceSettingInfo.getRearRate()))
                    Dbug.e(tag, "Set frame rate failed");
                if (!mMovWrapper.setAudioTrack(deviceSettingInfo.getRearSampleRate(), IConstant.AUDIO_CHANNEL,  IConstant.AUDIO_FORMAT))
                    Dbug.e(tag, "Set audio track failed");
            } else {
                if (!mMovWrapper.setFrameRate(mMediaInfo.getFrameRate()))
                    Dbug.e(tag, "Set frame rate failed");
                if (!mMovWrapper.setAudioTrack(mMediaInfo.getSampleRate(), IConstant.AUDIO_CHANNEL,  IConstant.AUDIO_FORMAT))
                    Dbug.e(tag, "Set audio track failed");
            }

        } else {
            if (mMediaInfo == null) {
                if (!mMovWrapper.setFrameRate(deviceSettingInfo.getFrontRate()))
                    Dbug.e(tag, "Set frame rate failed");
                if (!mMovWrapper.setAudioTrack(deviceSettingInfo.getFrontSampleRate(),  IConstant.AUDIO_CHANNEL,  IConstant.AUDIO_FORMAT))
                    Dbug.e(tag, "Set audio track failed");
            } else {
                if (!mMovWrapper.setFrameRate(mMediaInfo.getFrameRate()))
                    Dbug.e(tag, "Set frame rate failed");
                if (!mMovWrapper.setAudioTrack(mMediaInfo.getSampleRate(),  IConstant.AUDIO_CHANNEL,  IConstant.AUDIO_FORMAT))
                    Dbug.e(tag, "Set audio track failed");
            }

        }
        String outputPath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(),
                a.getApplication().getCameraDir(),  IConstant.DIR_DOWNLOAD) + File.separator + AppUtils.getRecordVideoName();
        if (TextUtils.isEmpty(outputPath)) {
            Dbug.e(tag, "Output path is incorrect");
            dispatchErrorMessage("Output path is incorrect");
            return false;
        }
        Dbug.i(tag, "output path " + outputPath);
        mCurrentFilePath = outputPath;
        if (!mMovWrapper.create(outputPath)) {
            Dbug.e(tag, "Create MOV wrapper failed");
            dispatchErrorMessage("Create MOV wrapper failed");
            return false;
        }
        return true;
    }

    private boolean startAviWrapper(){
        long storageSize = AppUtils.getAvailableExternalMemorySize();
        if(storageSize < MIN_STORAGE_LIMIT) {
            Dbug.e(tag, "Not enough storage space");
            dispatchErrorMessage("Not enough storage space");
            return false;
        }
        if(mMovWrapper != null){
            mMovWrapper.close();
            mMovWrapper = null;
        }
        if(mAviWrapper != null){
            if(mAviWrapper.isRecording()){
                mAviWrapper.stopRecording();
            }
            mAviWrapper = null;
        }
        String outputPath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(),
                a.getApplication().getCameraDir(),  IConstant.DIR_DOWNLOAD);
        //Path a.getVideoPath();


        if (TextUtils.isEmpty(outputPath)) {
            Dbug.e(tag, "Output path is incorrect");
            dispatchErrorMessage("Output path is incorrect");
            return false;
        }
        Dbug.i(tag, "Output path is " + outputPath);
        mCurrentFilePath = outputPath;
        mAviWrapper = new AviWrapper();
        if (!mAviWrapper.create(outputPath)) {
            dispatchErrorMessage("Create failed");
            return false;
        }
        mAviWrapper.setVideoDuration(300);
        mAviWrapper.setOnRecordListener(onAviWrapperListener);
        if (mMediaInfo == null) {
            mAviWrapper.setAudioTrack(IConstant.AUDIO_SAMPLE_RATE_DEFAULT, IConstant.AUDIO_CHANNEL, IConstant.AUDIO_FORMAT);
        } else {
            mAviWrapper.setAudioTrack(mMediaInfo.getSampleRate(), IConstant.AUDIO_CHANNEL, IConstant.AUDIO_FORMAT);
        }
        DeviceSettingInfo deviceSettingInfo = a.getApplication().getDeviceSettingInfo();
        int frameRate;
        int videoWidth;
        int videoHeight;
        if (DeviceClient.CAMERA_REAR_VIEW == deviceSettingInfo.getCameraType()) {
            if (mMediaInfo == null) {
                frameRate = deviceSettingInfo.getRearRate();
            } else {
                frameRate = mMediaInfo.getFrameRate();
            }
        }else{
            if (mMediaInfo == null) {
                frameRate = deviceSettingInfo.getFrontRate();
            } else {
                frameRate = mMediaInfo.getFrameRate();
            }
        }
        int currentLevel = AppUtils.getStreamResolutionLevel();
        int[] rtsResolution = AppUtils.getRtsResolution(currentLevel);
        videoWidth = rtsResolution[0];
        videoHeight = rtsResolution[1];
        if(frameRate > 0 && videoWidth > 0 && videoHeight > 0){
            mAviWrapper.configureVideo(frameRate, videoWidth, videoHeight);
        }else{
            dispatchErrorMessage("params is incorrect");
            return false;
        }
        mAviWrapper.startRecording();
        return true;
    }

    public boolean write(int type, byte[] data) {
        //Dbug.e(tag, "================write t=" +type +", length "+data.length);
        boolean ret;
        if(mMovWrapper == null && mAviWrapper == null){
            ret = false;
        }else if(mAviWrapper != null){
            ret = mAviWrapper.write(type, data);
        }else {
            ret = mMovWrapper.write(type, data);
        }
        if(!ret){
            Dbug.w(tag, "write data failed.");
            close();
        }
        return ret;
    }

    public void close () {
        //Dbug.e(tag, "Close =========");
        if(mAviWrapper == null && mMovWrapper == null){
            Dbug.w(tag, "mAviWrapper or mMovWrapper not init.");
        }else if(mAviWrapper != null){
            if(mAviWrapper.isRecording()){
                mAviWrapper.stopRecording();
            }
            ClientContext.post(new Runnable() {
                @Override
                public void run() {
                    if (onStreamRecordListener != null)
                        onStreamRecordListener.onStop();
                    onStreamRecordListener = null;
                }
            });
            mAviWrapper.destroy();
            mAviWrapper = null;
        }else {
            if(mMovWrapper.close()){
                ClientContext.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onStreamRecordListener != null)
                            onStreamRecordListener.onStop();
                        onStreamRecordListener = null;
                    }
                });
            }else{
                Dbug.e(tag, "Mov close failed");
            }
            mMovWrapper = null;
        }
        mCurrentFilePath = null;
    }

    private final OnRecordListener onRecordListener = new OnRecordListener() {

        @Override
        public void onStateChanged(int state, String msg) {
            if(state == MovWrapper.REC_STATE_END){
                ClientContext.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onStreamRecordListener != null)
                            onStreamRecordListener.onStop();
                        onStreamRecordListener = null;
                    }
                });
            }
        }

        @Override
        public void onError(int code, String msg) {
            Dbug.e(tag, "Code " + code + ", msg:"+msg);
            dispatchErrorMessage(msg);
            close();
        }
    };

    private final OnAviWrapperListener onAviWrapperListener = new OnAviWrapperListener() {
        @Override
        public void onError(int code, String msg) {
            Dbug.e(tag, "Code " + code + ", msg:"+msg);
            dispatchErrorMessage(msg);
            close();
        }
        @Override
        public void onStateChanged(int state, String msg) {
            if(state == AviWrapper.REC_STATE_END){
                ClientContext.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onStreamRecordListener != null)
                            onStreamRecordListener.onStop();
                        onStreamRecordListener = null;
                    }
                });
            }
        }
    };

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }
}
