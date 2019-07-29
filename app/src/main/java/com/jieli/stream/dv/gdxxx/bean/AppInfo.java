package com.jieli.stream.dv.gdxxx.bean;

import java.util.List;
import java.util.Map;

/**
 * @author zqjasonZhong
 *         date : 2017/7/3
 */
public class AppInfo {

    private String appName;
    private String appPlatform;
    private int appVersion;
    private List<String> dev_type;
    private Map<String, List<String>> dev_list;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String app_name) {
        this.appName = app_name;
    }

    public String getAppPlatform() {
        return appPlatform;
    }

    public void setAppPlatform(String app_platform) {
        this.appPlatform = app_platform;
    }

    public int getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(int app_version) {
        this.appVersion = app_version;
    }

    public List<String> getDev_type(){
        return dev_type;
    }

    public void setDev_type(List<String> dev_type){
        this.dev_type = dev_type;
    }

    public Map<String, List<String>> getDev_list() {
        return dev_list;
    }

    public void setDev_list(Map<String, List<String>> dev_list) {
        this.dev_list = dev_list;
    }

    public String toString(){
        String devTypeStr = "";
        String devListStr = "";
        if(dev_type != null){
            for (String str : dev_type){
                devTypeStr += str+",\t";
                List<String> valueList = dev_list.get(str);
                for (String value : valueList){
                    devListStr += value+",\t";
                }
            }
        }
        return "{\n"+
                "appName : " +appName+"\n"+
                "appPlatform : " +appPlatform+"\n"+
                "appVersion : " +appVersion+"\n"+
                "dev_type : " +devTypeStr+"\n"+
                "dev_list : " +devListStr+"\n"+
                "}";
    }
}
