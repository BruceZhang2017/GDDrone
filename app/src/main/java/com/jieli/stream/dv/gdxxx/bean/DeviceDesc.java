package com.jieli.stream.dv.gdxxx.bean;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.util.List;

/**
 * 设备描述信息
 * @author zqjasonZhong
 *         date : 2017/7/4
 */
public class DeviceDesc {

    /**
     * uuid : xxxx
     * product_type : AC54xx_wifi_car_camera
     * match_app_type : GD240 HD
     * firmware_version : 1.0.1
     * "double_video": "1"
     * "video_bumping": "1"
     * app_list : {"match_android_ver":["1","2"],"match_ios_ver":["1.0","2.0"]}
     */

    private String uuid;
    private String product_type;
    private String match_app_type;
    private String firmware_version;
    private AppListBean app_list;
    private String device_type = IConstant.DEV_REC_DUAL;   //1:是双录 0：是单路
    private boolean support_bumping;                      //1：开启  0：关闭
    private boolean support_projection;                   //1: 开启  0：关闭
    private String[] front_support;
    private String[] rear_support;
    private int videoType = DeviceClient.RTS_H264;//H264 or JPEG
    private int netMode = -1;//TCP or UDP

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public String getMatch_app_type() {
        return match_app_type;
    }

    public void setMatch_app_type(String match_app_type) {
        this.match_app_type = match_app_type;
    }

    public String getFirmware_version() {
        return firmware_version;
    }

    public void setFirmware_version(String firmware_version) {
        this.firmware_version = firmware_version;
    }

    public AppListBean getApp_list() {
        return app_list;
    }

    public void setApp_list(AppListBean app_list) {
        this.app_list = app_list;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public boolean isSupport_bumping() {
        return support_bumping;
    }

    public void setSupport_bumping(boolean support_bumping) {
        this.support_bumping = support_bumping;
    }

    public String[] getFront_support() {
        return front_support;
    }

    public void setFront_support(String[] front_support) {
        this.front_support = front_support;
    }

    public String[] getRear_support() {
        return rear_support;
    }

    public void setRear_support(String[] rear_support) {
        this.rear_support = rear_support;
    }

    public void setSupport_projection(boolean support_projection) {
        this.support_projection = support_projection;
    }

    public boolean isSupport_projection() {
        return support_projection;
    }

    public int getVideoType() {
        return videoType;
    }

    public void setVideoType(int videoType) {
        this.videoType = videoType;
    }

    public int getNetMode() {
        return netMode;
    }

    public void setNetMode(int netMode) {
        this.netMode = netMode;
    }

    public static class AppListBean {
        private List<String> match_android_ver;
        private List<String> match_ios_ver;

        public List<String> getMatch_android_ver() {
            return match_android_ver;
        }

        public void setMatch_android_ver(List<String> match_android_ver) {
            this.match_android_ver = match_android_ver;
        }

        public List<String> getMatch_ios_ver() {
            return match_ios_ver;
        }

        public void setMatch_ios_ver(List<String> match_ios_ver) {
            this.match_ios_ver = match_ios_ver;
        }
    }

}
