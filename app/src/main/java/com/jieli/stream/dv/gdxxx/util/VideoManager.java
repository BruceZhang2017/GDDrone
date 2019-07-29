package com.jieli.stream.dv.gdxxx.util;

import android.text.TextUtils;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.model.MediaInfo;
import com.jieli.lib.dv.control.player.MovWrapper;
import com.jieli.lib.dv.control.player.OnPlaybackListener;
import com.jieli.lib.dv.control.player.OnRecordListener;
import com.jieli.lib.dv.control.player.PlaybackStream;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.stream.dv.gdxxx.bean.DownloadInfo;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.interfaces.OnDownloadListener;
import com.jieli.stream.dv.gdxxx.util.json.JSonManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 视频下载管理器
 * @author zqjasonZhong
 *         date : 2017/8/2
 */
public class VideoManager {
    private static String tag  = VideoManager.class.getSimpleName();

    private MovWrapper movWrapper;
    private PlaybackStream playbackStream;
    private Timer mTimerThread;

    private long startTime;
    private int duration;
    private volatile int mTimer = 0;
    private volatile long mLastTime = 0;
    private static boolean isDownloading;
    private String outPath;

    private OnDownloadListener listener;

    public static final int ERROR_PARAMS = 0x00e3;
    public static final int ERROR_CLOSE_RECORD = 0x00e4;

    public VideoManager(){
        movWrapper = new MovWrapper();
        movWrapper.setOnRecordListener(mOnRecordListener);
    }

    /**
     * 获得封装器对象（用于设置参数）
     */
    public synchronized MovWrapper getMovWrapper(){
        return movWrapper;
    }

    /**
     * 设置回放播放器
     * （必须在下载视频前设置）
     * @param playbackStream  回放播放器
     */
    public void setPlaybackStream(PlaybackStream playbackStream) {
        this.playbackStream = playbackStream;
        playbackStream.registerPlayerListener(onPlaybackListener);
    }

    /**
     * 判断是否下载状态
     */
    public static boolean isDownloading() {
        return isDownloading;
    }

    /**
     * 获得输出文件路径
     */
    public String getOutPath() {
        return outPath;
    }

    /**
     * 设置下载监听器
     * @param listener  下载监听器
     */
    public void setOnDownloadListener(OnDownloadListener listener){
        this.listener = listener;
    }

    /**
     * 设置录制时间累计的方式
     * @param timerType 类型有{@link MovWrapper#TIME_MASTER_SYSTEM},
     * {@link MovWrapper#TIME_MASTER_SEQUENCE}
     * @return 是否成功
     */
    public boolean setRecordTimerType(int timerType) {
        return movWrapper != null && movWrapper.setTimeMaster(timerType);
    }

    /**
     * 开始下载录像
     * @param outPath  保存路径
     */
    public void startDownload(String outPath){
        if(!TextUtils.isEmpty(outPath)){
            this.outPath = outPath;
            if(!movWrapper.create(outPath)){
                if(listener != null){
                    listener.onError(ERROR_PARAMS, "create output path failed.");
                }
                isDownloading = false;
            }
        }
    }

    /**
     * 停止下载
     */
    public void tryToStopDownload(){
        if(isDownloading){
            if(!movWrapper.close()) {
                if(listener != null){
                    listener.onError(ERROR_CLOSE_RECORD, "close recording failed, please try later.");
                }
            }
        }
    }

    final static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * 获取要下载的文件
     * @param start 开始时间(单位为毫秒)
     * @param end 结束时间(单位为毫秒)
     * @return 找到下载文件则返回下载信息，否则返回null
     */
    public static DownloadInfo getDownloadFileInfo (long start, long end) {
        if (start <= 0 || end <= 0 || start >= end) {
            Dbug.e(tag, "Illegal argument exception. start=" +start + ", end=" + end);
            return null;
        }
        Dbug.i(tag, "start:"+mDateFormat.format(start) + ", end:"+mDateFormat.format(end));
        final FileInfo startVideoInfo = getSelectedMedia(start);
        if (startVideoInfo == null) {
            Dbug.e(tag, "Start VideoInfo not found.");
            return null;
        }

        long d = getDownloadVideoDuration(start, end) / 1000;//cast to second
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setDuration((int) d);
        downloadInfo.setOffset(startVideoInfo.getOffset());
        downloadInfo.setPath(startVideoInfo.getPath());
        Dbug.i(tag, "download duration=" + d);
        if (d < 0) {
            Dbug.e(tag, "Total time error:" + d);
            return null;
        }
        return downloadInfo;
    }
    /**
     * 截取视频
     * @param startMSec    开始时间(单位为毫秒)
     * @param endMSec      结束时间(单位为毫秒)
     * @param path         输出路径
     * @return             是否开始下载
     */
    public boolean tryToDownload(long startMSec, long endMSec, String path){
        if(startMSec > 0 && !TextUtils.isEmpty(path)){
            this.outPath = path;
            FileInfo fileInfo = getSelectedMedia(startMSec);
            if(fileInfo != null){
                startTime = startMSec;
                if(endMSec > 0 && endMSec > startMSec){
                    duration = (int)(getDownloadVideoDuration(startMSec, endMSec) / 1000);
                    Dbug.w(tag, "-tryToDownload- duration : " + duration);
                    if(duration > movWrapper.getVideoDuration() * 60){
                        //保证录像时长不超过规定时长
                        movWrapper.setVideoDuration((duration / 60) + 2);
                    }
                }
                int offset = fileInfo.getOffset();
                Dbug.w(tag, "-tryToDownload- offset : " + offset);
                if(!playbackStream.isStreamReceiving()){
                    ClientManager.getClient().tryToStartPlayback(fileInfo.getPath(), offset, new SendResponse() {
                        @Override
                        public void onResponse(Integer code) {
                            if(code != Constants.SEND_SUCCESS){
                                isDownloading = false;
                            }else{
                                if(!movWrapper.create(outPath)){
                                    if(listener != null){
                                        listener.onError(ERROR_PARAMS, "create output path failed.");
                                    }
                                    isDownloading = false;
                                    mTimer = 0;
                                    mLastTime = 0;
                                }
                            }
                        }
                    });
                }else{
                    if(!movWrapper.create(outPath)){
                        if(listener != null){
                            listener.onError(ERROR_PARAMS, "create output path failed.");
                        }
                        isDownloading = false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 获取选择媒体信息
     * @param milliseconds   选择时间（毫秒）
     */
    private static synchronized FileInfo getSelectedMedia(long milliseconds) {
        if (milliseconds <= 0) {
            Dbug.e(tag, "milliseconds <= 0: " + milliseconds);
            return null;
        }
        List<FileInfo> videoInfoList = JSonManager.getInstance().getVideoInfoList();
        if (videoInfoList != null) {
            AppUtils.descSortWay(videoInfoList);
            for (FileInfo videoInfo : videoInfoList) {
                if (milliseconds >= videoInfo.getStartTime().getTimeInMillis()
                        && milliseconds <= videoInfo.getEndTime().getTimeInMillis()) {
                    long offset = (milliseconds - videoInfo.getStartTime().getTimeInMillis());
                    videoInfo.setOffset((int)offset);
                    return videoInfo;
                }
            }
            Dbug.w(tag, "Can not find out milliseconds=" + milliseconds);
        }
        return null;
    }

    /**
     * 获取间隔时长
     * @param startMilliseconds    开始时间
     * @param endMilliseconds      结束时间
     */
    private static long getDownloadVideoDuration (long startMilliseconds, long endMilliseconds) {
        if (endMilliseconds <= startMilliseconds) {
            throw new IllegalArgumentException("Start time >= end time.");
        }

        List<FileInfo> videoInfoList = JSonManager.getInstance().getVideoInfoList();
        long duration = 0;
        if (videoInfoList != null) {
            for (FileInfo videoInfo : videoInfoList) {
                /*如果startMilliseconds 比文件结束点大，则继续*/
                if (startMilliseconds > videoInfo.getEndTime().getTimeInMillis()) {
                    continue;
                }
                /*如果 endMilliseconds 比文件开始时间小，则继续*/
                if (endMilliseconds < videoInfo.getStartTime().getTimeInMillis()) {
                    continue;
                }
                /*startMilliseconds 所在文件, 且endMilliseconds 不在此文件，记下剩下的时间*/
                if (startMilliseconds >= videoInfo.getStartTime().getTimeInMillis()
                        && startMilliseconds <= videoInfo.getEndTime().getTimeInMillis()
                        && endMilliseconds > videoInfo.getEndTime().getTimeInMillis()) {
                    duration += (videoInfo.getEndTime().getTimeInMillis() - startMilliseconds);
                    continue;
                }
                /*startMilliseconds 和 endMilliseconds在同一个文件*/
                if (startMilliseconds >= videoInfo.getStartTime().getTimeInMillis() && endMilliseconds <= videoInfo.getEndTime().getTimeInMillis()) {
                    duration = endMilliseconds - startMilliseconds;
                    return duration;
                }
                /*文件的开始时间大于 startMilliseconds, 且endMilliseconds 大于等于此文件的结束时间*/
                if (videoInfo.getStartTime().getTimeInMillis() > startMilliseconds
                        && videoInfo.getEndTime().getTimeInMillis() <= endMilliseconds) {
                    duration += videoInfo.getDuration() * 1000;
                    continue;
                }
                /*此文件的开始时间大于 startMilliseconds，且此文件的结束时间大于等于 endMilliseconds*/
                if (videoInfo.getStartTime().getTimeInMillis() > startMilliseconds
                        && videoInfo.getEndTime().getTimeInMillis() >= endMilliseconds) {
                    duration += endMilliseconds - videoInfo.getStartTime().getTimeInMillis();
                }
            }
        } else {
            Dbug.w(tag, "Can not find out thumbnail at " + startMilliseconds);
        }
        return duration;
    }

    private final OnPlaybackListener onPlaybackListener = new OnPlaybackListener() {
        /*
         * 回放数据回调
         * */
        @Override
        public void onUpdate(MediaInfo mediaInfo) {

        }

        @Override
        public void onVideo(int type, int channel, byte[] data, long sequence, long timestamp) {
            if (isDownloading){
//                Dbug.w(tag, "record --> -onVideo- : " + data.length);
                movWrapper.write(type, data);
            }
        }

        @Override
        public void onAudio(int type, int channel, byte[] data, long sequence, long timestamp) {
            if (isDownloading){
//                Dbug.w(tag, "record --> -onAudio- : " + data.length);
                movWrapper.write(type, data);
            }
        }

        @Override
        public void onStateChanged(int state) {

        }

        @Override
        public void onError(int code, String message) {

        }
    };

    /*
    * 录像回调
    * */
    private OnRecordListener mOnRecordListener = new OnRecordListener() {
        @Override
        public void onError(int code, String msg) {
            super.onError(code, msg);
            tryToStopDownload();
            if (listener != null) {
                listener.onError(code, msg);
            }
        }

        @Override
        public void onStateChanged(int state, String msg) {
            super.onStateChanged(state, msg);
            Dbug.e(tag, "-onStateChanged- state : " +state);
            if (state == MovWrapper.REC_STATE_START) {
                isDownloading = true;
                mTimer = 0;
                mLastTime = 0;
                mTimerThread = new Timer();
                mTimerThread.schedule(new MyTimeTask(), 0, 1000);
                if (listener != null) {
                    listener.onStartLoad();
                }
            } else if (state == MovWrapper.REC_STATE_END) {
                if (listener != null) {
                    listener.onCompletion();
                }
                if(mTimerThread != null){
                    mTimerThread.cancel();
                    mTimerThread = null;
                }
                isDownloading = false;
                mTimer = 0;
                mLastTime = 0;
                duration = 0;
            }
        }
    };

    public void release(){
        tryToStopDownload();
        mOnRecordListener = null;
        listener = null;
        movWrapper = null;
        if(playbackStream != null){
            playbackStream.unregisterPlayerListener(onPlaybackListener);
            playbackStream = null;
        }
    }

    private class MyTimeTask extends TimerTask{

        @Override
        public void run() {
            if(playbackStream != null && isDownloading && duration > 0){
                long currentTime = playbackStream.getCurrentPosition();
                if(currentTime >= startTime){
                    if(mLastTime != currentTime){
                        mLastTime = currentTime;
                        mTimer++;
                        int progress = (mTimer * 100) / duration;
                        if(progress < 100){
                            if(listener != null){
                                listener.onProgress(progress);
                            }
                        }else{
                            tryToStopDownload();
                            isDownloading = false;
                            ClientManager.getClient().tryToChangePlaybackState(2, new SendResponse() {
                                @Override
                                public void onResponse(Integer code) {

                                }
                            });
                            if(mTimerThread != null){
                                mTimerThread.cancel();
                                mTimerThread = null;
                            }
                        }
                    }
                }
            }
        }
    }
}
