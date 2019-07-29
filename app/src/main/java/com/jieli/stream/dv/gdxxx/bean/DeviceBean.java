package com.jieli.stream.dv.gdxxx.bean;

import android.text.TextUtils;

/**
 * 设备属性类
 * date : 2017/2/28
 */
public class DeviceBean {
    private String wifiSSID;
    private String wifiIP;
    private String wifiType;
    private int mode;
    private String versionName;
    private double version;
//    private boolean isNeedUpdate =  false;


    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getWifiIP() {
        return wifiIP;
    }

    public void setWifiIP(String wifiIP) {
        this.wifiIP = wifiIP;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public String getWifiType() {
        return wifiType;
    }

    public void setWifiType(String wifiType) {
        this.wifiType = wifiType;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String toString(){
        String content = "";
        if(!TextUtils.isEmpty(wifiSSID)){
            content += "\"wifiSSID\" : \""+wifiSSID+"\",\n";
        }
        if(!TextUtils.isEmpty(wifiIP)){
            content += "\"wifiIP\" : \""+wifiIP+"\",\n";
        }
        if(!TextUtils.isEmpty(wifiType)){
            content += "\"wifiType\" : \""+wifiType+"\",\n";
        }
        content += "\"mode\":"+mode;
        if(!TextUtils.isEmpty(versionName)){
            content += "\"versionName\" : \""+versionName+"\",\n";
        }
        content += "\"version\":"+version;
        return content;
    }
}
