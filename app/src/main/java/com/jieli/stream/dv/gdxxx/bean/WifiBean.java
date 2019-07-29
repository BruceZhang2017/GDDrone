package com.jieli.stream.dv.gdxxx.bean;

/**
 * Wifi 属性
 * date : 2017/3/6
 */
public class WifiBean {

    private String SSID;
    private String password;
    private int state;
    private boolean isSelect = false;
//    private WifiConfiguration configuration;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
