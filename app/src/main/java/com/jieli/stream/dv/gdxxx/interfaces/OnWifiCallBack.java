package com.jieli.stream.dv.gdxxx.interfaces;

import android.net.wifi.WifiInfo;

/**
 * Wifi的回调方法
 * @author zqjasonZhong
 *  date : 2017/3/8
 */
public interface OnWifiCallBack {

    /**
     * 网络连接成功
     * @param info  wifi信息
     */
    void onConnected(WifiInfo info);

    /**
     * 网络出现错误
     * @param errCode   错误码
     */
    void onError(int errCode);
}
