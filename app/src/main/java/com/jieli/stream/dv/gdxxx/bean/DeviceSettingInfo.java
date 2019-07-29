package com.jieli.stream.dv.gdxxx.bean;

import com.jieli.lib.dv.control.DeviceClient;

import static com.jieli.stream.dv.gdxxx.util.IConstant.AUDIO_SAMPLE_RATE_DEFAULT;
import static com.jieli.stream.dv.gdxxx.util.IConstant.VIDEO_FRAME_RATE_DEFAULT;

/**
 * Created by 陈森华 on 2017/7/24.
 * 功能：用一句话描述
 */

public class DeviceSettingInfo {
   //语言位置，对应array的language数组
    private int lanIndex;

    //图片质量位置，photo_quality数组
    private int photoQualityIndex;

    //录像时是否录音
    private  boolean videoMic;

    //设备TF卡总存储空间
    private int totalStorage;

    //设备TF卡剩余存储空间
    private int leftStorage;

    //停车监控
    private boolean videoParCar;

    //录像时间水印
    private boolean videoDate;

    //重力感应灵敏度 状态(0:关闭,1:low, 2:med, 3:hig)
    private int gravitySensor;

    //按键声音
    private boolean keyVoice;

    // 电池电量:level:（0：3.7V 1：3.85V 2：4.0V 3：4.05V, 4: 充电中）
    private int batStatus;

    //光源频率: 参数fre:(50 或 60)单位:Hz
    private int lightFrequece;

   // 自动关机:状态aff(0 或 3 或 5 或 10) 单位min
    private int autoShutdown;

    //设置屏幕保护: 参数pro:(0 30 60 120)单位sec
    private int screenOn;

    //TV模式: 参数tvm:(0:pal,1:ntsc )
    private int tvMode;

    //双路录像:状态two: (0：off， 1：on)
    private boolean doubleVideo;

    //循环录像: 参数： cyc: 状态(0 3 5 10)单位：min
    private int videoLoop;

   //夜视增强:参数：wdr: 状态(0：关闭， 1：打开)
    private boolean videoWdr;

    //曝光补偿:参数：exp: (3 ~ -3)间隔:1
    private int videoExp;

    //移动侦测:参数:mot: 状态(0：关闭， 1：打开)
    private boolean moveCheck;

    //间隔录影:参数：gap(0 100 200 500) 单位：ms
    private int videoInv;

    //拍照分辨率:参数：res（0:vga, 1:1.3M, 2:2M ,3:3M ,4:5M, 5:8M, 6:10M, 7:12M)
    private int photoReso;

    //定时拍照时间:参数: phm (0：关 1：定时2秒 2：定时5秒 3：定时10秒)
    private int selfTime;

    //设备连拍张数:参数：cyt: (0：关 1：开)
    private boolean burstShot;

    //图像锐度：参数： acu:(0：lo 1：md 2: hi )
    private int photoSharpness;

    //设置图像ISO:参数:iso:(0 100 200 400 )
    private int photoIso;

    //曝光补偿：:曝光参数：exp(+3 ~ -3 )
    private int photoExp;

    //防手抖:参数：soksok(0: off 1: on )
    private boolean antiTremor;

    //设置拍照时间水印：参数：dat(0: off 1: on )
    private boolean photoDate;

    //录像状态
    private int recordState;

    //是否开启投屏
    private boolean isOpenProjection;

    //是否开启实时语音
    private boolean isRTVoice;

    //摄像头的类型 CAMERA_FRONT_VIEW or CAMERA_REAR_VIEW
    private int cameraType = DeviceClient.CAMERA_FRONT_VIEW;

    //后拉设备是否存在
    private boolean isExistRearView;

    //前视录像分辨率等级
    private int frontRecordLevel;

    //前视清晰度等级
    private int frontLevel;

    //前视数据格式 (默认是H264)
    private int frontFormat = DeviceClient.RTS_H264;

    //前视录像帧数(默认是30)
    private int frontRate = VIDEO_FRAME_RATE_DEFAULT;

    //后视录像分辨率等级
    private int rearRecordLevel;

    //后视清晰度等级
    private int rearLevel;

    //后视数据格式 (默认是H264)
    private int rearFormat = DeviceClient.RTS_H264;

    //后视录像帧数 (默认是30)
    private int rearRate = VIDEO_FRAME_RATE_DEFAULT;

    //后视音频采样率
    private int rearSampleRate = AUDIO_SAMPLE_RATE_DEFAULT;
    //前视音频采样率
    private int frontSampleRate = AUDIO_SAMPLE_RATE_DEFAULT;
    //时间水印开关
    private boolean isOpenBootSound;

    public boolean isPhotoDate() {
        return photoDate;
    }

    public void setPhotoDate(boolean photoDate) {
        this.photoDate = photoDate;
    }

    public boolean isAntiTremor() {
        return antiTremor;
    }

    public void setAntiTremor(boolean antiTremor) {
        this.antiTremor = antiTremor;
    }

    public void setPhotoExp(int photoExp) {
        this.photoExp = photoExp;
    }

    public int getPhotoExp() {
        return photoExp;
    }

    public void setPhotoIso(int photoIso) {
        this.photoIso = photoIso;
    }

    public int getPhotoIso() {
        return photoIso;
    }

    public void setPhotoSharpness(int photoSharpness) {
        this.photoSharpness = photoSharpness;
    }

    public int getPhotoSharpness() {
        return photoSharpness;
    }

    public boolean isBurstShot() {
        return burstShot;
    }

    public void setBurstShot(boolean burstShot) {
        this.burstShot = burstShot;
    }

    public void setSelfTime(int selfTime) {
        this.selfTime = selfTime;
    }

    public int getSelfTime() {
        return selfTime;
    }

    public void setPhotoReso(int photoReso) {
        this.photoReso = photoReso;
    }

    public int getPhotoReso() {
        return photoReso;
    }

    public void setVideoInv(int videoInv) {
        this.videoInv = videoInv;
    }

    public int getVideoInv() {
        return videoInv;
    }

    public boolean isMoveCheck() {
        return moveCheck;
    }

    public void setMoveCheck(boolean moveCheck) {
        this.moveCheck = moveCheck;
    }

    public void setVideoExp(int videoExp) {
        this.videoExp = videoExp;
    }

    public int getVideoExp() {
        return videoExp;
    }

    public boolean isVideoWdr() {
        return videoWdr;
    }

    public void setVideoWdr(boolean videoWdr) {
        this.videoWdr = videoWdr;
    }

    public void setVideoLoop(int videoLoop) {
        this.videoLoop = videoLoop;
    }

    public int getVideoLoop() {
        return videoLoop;
    }

    public boolean isDoubleVideo() {
        return doubleVideo;
    }

    public void setDoubleVideo(boolean doubleVideo) {
        this.doubleVideo = doubleVideo;
    }

    public void setTvMode(int tvMode) {
        this.tvMode = tvMode;
    }

    public int getTvMode() {
        return tvMode;
    }

    public void setScreenOn(int screenOn) {
        this.screenOn = screenOn;
    }

    public int getScreenOn() {
        return screenOn;
    }

    public void setAutoShutdown(int autoShutdown) {
        this.autoShutdown = autoShutdown;
    }

    public int getAutoShutdown() {
        return autoShutdown;
    }

    public void setLightFrequece(int lightFrequece) {
        this.lightFrequece = lightFrequece;
    }

    public int getLightFrequece() {
        return lightFrequece;
    }

    public void setBatStatus(int batStatus) {
        this.batStatus = batStatus;
    }

    public int getBatStatus() {
        return batStatus;
    }

    public boolean isKeyVoice() {
        return keyVoice;
    }

    public void setKeyVoice(boolean keyVoice) {
        this.keyVoice = keyVoice;
    }

    public boolean isVideoParCar() {
        return videoParCar;
    }

    public void setVideoParCar(boolean videoParCar) {
        this.videoParCar = videoParCar;
    }

    public boolean isVideoDate() {
        return videoDate;
    }

    public void setVideoDate(boolean videoDate) {
        this.videoDate = videoDate;
    }

    public int getGravitySensor() {
        return gravitySensor;
    }

    public void setGravitySensor(int gravitySensor) {
        this.gravitySensor = gravitySensor;
    }

    public boolean isVideoMic() {
        return videoMic;
    }

    public void setVideoMic(boolean videoMic) {
        this.videoMic = videoMic;
    }

    public int getLanIndex() {
        return lanIndex;
    }

    public void setLanIndex(int lanIndex) {
        this.lanIndex = lanIndex;
    }

    public int getPhotoQualityIndex() {
        return photoQualityIndex;
    }

    public void setPhotoQualityIndex(int photoQualityIndex) {
        this.photoQualityIndex = photoQualityIndex;
    }

    public int getTotalStorage() {
        return totalStorage;
    }

    public void setTotalStorage(int totalStorage) {
        this.totalStorage = totalStorage;
    }

    public int getLeftStorage() {
        return leftStorage;
    }

    public void setLeftStorage(int leftStorage) {
        this.leftStorage = leftStorage;
    }

    public int getRecordState() {
        return recordState;
    }

    public void setRecordState(int recordState) {
        this.recordState = recordState;
    }

    public boolean isOpenProjection() {
        return isOpenProjection;
    }

    public void setOpenProjection(boolean openProjection) {
        isOpenProjection = openProjection;
    }

    public boolean isRTVoice() {
        return isRTVoice;
    }

    public void setRTVoice(boolean RTVoice) {
        isRTVoice = RTVoice;
    }

    public boolean isExistRearView() {
        return isExistRearView;
    }

    public void setExistRearView(boolean existRearView) {
        isExistRearView = existRearView;
    }

    public int getCameraType() {
        return cameraType;
    }

    public void setCameraType(int cameraType) {
        this.cameraType = cameraType;
    }

    public int getFrontLevel() {
        return frontLevel;
    }

    public void setFrontLevel(int frontLevel) {
        this.frontLevel = frontLevel;
    }

    public int getFrontFormat() {
        return frontFormat;
    }

    public void setFrontFormat(int frontFormat) {
        this.frontFormat = frontFormat;
    }

    public int getFrontRate() {
        return frontRate;
    }

    public void setFrontRate(int frontRate) {
        this.frontRate = frontRate;
    }

    public int getRearLevel() {
        return rearLevel;
    }

    public void setRearLevel(int rearLevel) {
        this.rearLevel = rearLevel;
    }

    public int getRearFormat() {
        return rearFormat;
    }

    public void setRearFormat(int rearFormat) {
        this.rearFormat = rearFormat;
    }

    public int getRearRate() {
        return rearRate;
    }

    public void setRearRate(int rearRate) {
        this.rearRate = rearRate;
    }

    public int getFrontRecordLevel() {
        return frontRecordLevel;
    }

    public void setFrontRecordLevel(int frontRecordLevel) {
        this.frontRecordLevel = frontRecordLevel;
    }

    public int getRearRecordLevel() {
        return rearRecordLevel;
    }

    public void setRearRecordLevel(int rearRecordLevel) {
        this.rearRecordLevel = rearRecordLevel;
    }

    @Override
    public String toString() {
        return "{" + "\"lanIndex\":" + lanIndex +",\n"+
                "\"photoQualityIndex\": " + photoQualityIndex +",\n"+
                "\"videoMic\":" + videoMic + ",\n"+
                "\"totalStorage\":" + totalStorage + ",\n"+
                "\"leftStorage\":" + leftStorage + ",\n"+
                "\"videoParCar\":" + videoParCar + ",\n"+
                "\"videoDate\":" + videoDate + ",\n"+
                "\"gravitySensor\":" + gravitySensor + ",\n"+
                "\"keyVoice\":" + keyVoice + ",\n"+
                "\"batStatus\":" + batStatus + ",\n"+
                "\"lightFrequece\":" + lightFrequece + ",\n"+
                "\"autoShutdown\":" + autoShutdown + ",\n"+
                "\"screenOn\":" + screenOn + ",\n"+
                "\"tvMode\":" + tvMode + ",\n"+
                "\"doubleVideo\":" + doubleVideo + ",\n"+
                "\"videoLoop\":" + videoLoop + ",\n"+
                "\"videoWdr\":" + videoWdr + ",\n"+
                "\"videoExp\":" + videoExp + ",\n"+
                "\"moveCheck\":" + moveCheck + ",\n"+
                "\"videoInv\":" + videoInv + ",\n"+
                "\"photoReso\":" + photoReso + ",\n"+
                "\"selfTime\":" + selfTime + ",\n"+
                "\"burstShot\":" + burstShot + ",\n"+
                "\"photoSharpness\":" + photoSharpness + ",\n"+
                "\"photoIso\":" + photoIso + ",\n"+
                "\"photoExp\":" + photoExp + ",\n"+
                "\"antiTremor\":" + antiTremor + ",\n"+
                "\"photoDate\":" + photoDate + ",\n"+
                "\"recordState\":" + recordState + ",\n"+
                "\"isOpenProjection\":" + isOpenProjection + ",\n"+
                "\"isRTVoice\":" + isRTVoice + ",\n"+
                "\"isExistRearView\":" + isExistRearView + ",\n"+
                "\"cameraType\":" + cameraType + ",\n"+
                "\"frontRecordLevel\":" + frontRecordLevel +
                "\"frontLevel\":" + frontLevel + ",\n"+
                "\"frontFormat\":" + frontFormat + ",\n"+
                "\"frontRate\":" + frontRate + ",\n"+
                "\"rearRecordLevel\":" + rearRecordLevel +
                "\"rearLevel\":" + rearLevel + ",\n"+
                "\"rearFormat\":" + rearFormat + ",\n"+
                "\"rearRate\":" + rearRate +
                "\"rearSampleRate\":" + rearSampleRate + ",\n"+
                "\"frontSampleRate\":" + frontSampleRate +
                '}';
    }

    public int getRearSampleRate() {
        return rearSampleRate;
    }

    public void setRearSampleRate(int rearSampleRate) {
        this.rearSampleRate = rearSampleRate;
    }

    public int getFrontSampleRate() {
        return frontSampleRate;
    }

    public void setFrontSampleRate(int frontSampleRate) {
        this.frontSampleRate = frontSampleRate;
    }

    public boolean isOpenBootSound() {
        return isOpenBootSound;
    }

    public void setOpenBootSound(boolean openBootSound) {
        isOpenBootSound = openBootSound;
    }
}
