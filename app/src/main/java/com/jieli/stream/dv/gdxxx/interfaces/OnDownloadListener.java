package com.jieli.stream.dv.gdxxx.interfaces;

/**
 * 下载监听器
 * @author zqjasonZhong
 *         date : 2017/8/2
 */
public interface OnDownloadListener {

    /**
     * 录制（下载）开始
     */
    void onStartLoad();

    /**
     * 录制(下载)进度
     * @param progress 当前进度大小
     */
    void onProgress(int progress);

    /**
     * 录制(下载)完成
     */
    void onCompletion();

    /**
     * 录制错误回调
     * @param code   错误码
     * @param msg    错误信息
     */
    void onError(int code, String msg);
}
