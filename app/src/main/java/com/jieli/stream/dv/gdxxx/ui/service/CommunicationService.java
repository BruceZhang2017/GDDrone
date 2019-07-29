package com.jieli.stream.dv.gdxxx.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.listener.OnConnectStateListener;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.lib.dv.control.utils.TopicParam;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.DeviceDesc;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.task.ClearThumbTask;
import com.jieli.stream.dv.gdxxx.task.HeartbeatTask;
import com.jieli.stream.dv.gdxxx.task.SDPServer;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.HttpManager;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.TimeFormate;
import com.jieli.stream.dv.gdxxx.util.json.JSonManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

public class CommunicationService extends Service implements IConstant, IActions {
    private static final String tag = CommunicationService.class.getSimpleName();
    private final Handler mHandler = new MyHandler();
    private HeartbeatTask mHeartbeatTask = null;
    private LoadDeviceDesTxt loadDeviceDesTxt;
    private a mApplication;
    private SDPServer mServer;
    private ClearThumbTask clearThumbTask;

    private boolean isReceiveBumpingData;
    private int mFrameRate = VIDEO_FRAME_RATE_DEFAULT;
    private int mSampleRate = AUDIO_SAMPLE_RATE_DEFAULT;

    /**
     * Handler messages
     */
    public static final int MSG_HEARTBEAT_CONNECTION_TIMEOUT = 0X10;
    private static final int MSG_CONNECT_CTP = 0x11;
    private static final int MSG_DISCONNECT_CTP = 0x12;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Dbug.i(tag, "=====CommunicationService==onCreate=====");
        ClientManager.getClient().registerNotifyListener(onNotifyResponse);
        ClientManager.getClient().registerConnectStateListener(connectStateListener);
        mApplication = a.getApplication();
//        if (a.isOpenLeakCanary) mApplication.getRefWatcher().watch(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        int cmd = intent.getIntExtra(SERVICE_CMD, -1);
        Dbug.i(tag, "onStartCommand==========cmd=" + cmd);
        switch (cmd) {
            case SERVICE_CMD_CONNECT_CTP:
                String ip = intent.getStringExtra(KEY_CONNECT_IP);
                mHandler.removeMessages(MSG_CONNECT_CTP);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CONNECT_CTP, ip), 300);
                break;
            case SERVICE_CMD_DISCONNECT_CTP:
                ClientManager.getClient().disconnect();
                break;

        }
        return START_STICKY;
    }

    private final OnNotifyListener onNotifyResponse = new OnNotifyListener() {

        @Override
        public void onNotify(NotifyInfo data) {
            if(data == null) return;
            if (!Topic.KEEP_ALIVE.equals(data.getTopic()))Dbug.w(tag, "Device Notify=" + data);
            String topic = data.getTopic();
            if (TextUtils.isEmpty(topic)) return;
            Intent intent;
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, "Topic " + data.getTopic() + ", error msg: "+Code.getCodeDescription(data.getErrorType()));
                if(Topic.APP_ACCESS.equals(topic)) {
                    intent = new Intent(ACTION_DEV_ACCESS);
                    intent.putExtra(KEY_ALLOW_ACCESS, false);
                    sendBroadcast(intent);
                }else if(Topic.APP_SET_PROJECTION.equals(topic)
                        || Topic.DEV_REQ_PROJECTION.equals(data.getTopic())){
                    mApplication.getDeviceSettingInfo().setOpenProjection(false);
                    intent = new Intent(ACTION_PROJECTION_STATUS);
                    intent.putExtra(KEY_PROJECTION_STATUS, false);
                    sendBroadcast(intent);
                    mApplication.sendScreenCmdToService(SERVICE_CMD_CLOSE_SCREEN_TASK);
                }else if(Topic.CLOSE_PULL_RT_STREAM.equals(topic)){
                    mApplication.getDeviceSettingInfo().setExistRearView(false);
                    if (Code.ERROR_REAR_CAMERA_OFFLINE == data.getErrorType()) {
                        if(mApplication.getDeviceSettingInfo().getCameraType() == DeviceClient.CAMERA_REAR_VIEW){
                            PreferencesHelper.putBooleanValue(getApplicationContext(), AppUtils.getAutoRearCameraKey(mApplication.getUUID()), true);
                        }
                        mApplication.getDeviceSettingInfo().setCameraType(DeviceClient.CAMERA_FRONT_VIEW);
                    }
                }
                return;
            }
            switch (topic) {
                case Topic.KEEP_ALIVE_INTERVAL:
                    int interval = Integer.parseInt(data.getParams().get(TopicKey.TIMEOUT));
                    //start heartbeat task.
                    if (mHeartbeatTask == null) {
                        mHeartbeatTask = new HeartbeatTask(mHandler);
                        mHeartbeatTask.start();
                    }
                    Dbug.i(tag, "Timeout interval:" + interval);
                    mHeartbeatTask.setPeriodAndTimeout((interval/HeartbeatTask.DEFAULT_HEARTBEAT_TIMEOUT)
                            , HeartbeatTask.DEFAULT_HEARTBEAT_TIMEOUT);
                    break;
                case Topic.TF_STATUS:
                    //此一定运行
                    Dbug.i(tag, "TF Card state:" + data.getParams().get(TopicKey.ONLINE));
                    intent = new Intent(ACTION_TF_CARD_STATE);
                    if (TopicParam.TF_ONLINE.equals(data.getParams().get(TopicKey.ONLINE))) {
                        a.getApplication().setSdcardExist(true);
                        intent.putExtra(KEY_TF_STATE, true);
                    } else {
                        a.getApplication().setSdcardExist(false);
                        intent.putExtra(KEY_TF_STATE, false);
                    }

                    sendBroadcast(intent);
                    break;
                case Topic.APP_ACCESS:
                    String date = TimeFormate.formatYMD_HMS(Calendar.getInstance().getTime());
                    ClientManager.getClient().tryToSyncDevDate(date, new SendResponse() {
                        @Override
                        public void onResponse(Integer code) {
                            if (code != SEND_SUCCESS) {
                                Dbug.e(tag, "Send failed");
                            }
                        }
                    });
                    intent = new Intent(ACTION_DEV_ACCESS);
                    intent.putExtra(KEY_ALLOW_ACCESS, true);
                    sendBroadcast(intent);
                    if(clearThumbTask == null){
                        clearThumbTask = new ClearThumbTask();
                        clearThumbTask.setOnClearThumbTaskListener(new ClearThumbTask.OnClearThumbTaskListener() {
                            @Override
                            public void onFinish() {
                                clearThumbTask = null;
                            }
                        });
                        clearThumbTask.start();
                    }
                    if (!a.getApplication().isUpgrading()) {
                        if (loadDeviceDesTxt == null) {
                            loadDeviceDesTxt = new LoadDeviceDesTxt(CommunicationService.this);
                            loadDeviceDesTxt.start();
                        }
                    }
                    break;
                case Topic.DEVICE_UUID:
                    String devUUID = data.getParams().get(TopicKey.UUID);
                    if (!TextUtils.isEmpty(devUUID)) {
                        Dbug.i(tag, "device uuid :" + devUUID);
                        a.getApplication().setUUID(devUUID);
                    }
                    break;
                case Topic.KEEP_ALIVE:
                    if (mHeartbeatTask != null) {
                        mHeartbeatTask.resetTimeoutCount();
                    }
                    break;
                case Topic.VIDEO_CTRL:
                    String state = data.getParams().get(TopicKey.STATUS);
                    if (!TextUtils.isEmpty(state)) {
                        boolean isRecord = TopicParam.OPEN.equals(state);
                        if (isRecord) {
                            mApplication.getDeviceSettingInfo().setRecordState(STATUS_RECORDING);
                        } else {
                            mApplication.getDeviceSettingInfo().setRecordState(STATUS_NOT_RECORD);
                        }
                    }
                    break;
                case Topic.VIDEO_FINISH:
                    String state1 = data.getParams().get(TopicKey.STATUS);
                    if (!TextUtils.isEmpty(state1)) {
                        boolean isRecord = TopicParam.OPEN.equals(state1);
                        if (isRecord) {
                            mApplication.getDeviceSettingInfo().setRecordState(STATUS_RECORDING);
                        } else {
                            mApplication.getDeviceSettingInfo().setRecordState(STATUS_NOT_RECORD);
                        }
                    }
                    String desc = data.getParams().get(TopicKey.DESC);
                    if (!TextUtils.isEmpty(desc)) {
                        desc = desc.replaceAll("\\\\", "");
                        Dbug.w(tag, "-VIDEO_FINISH- desc = " + desc);
                        FileInfo fileInfo = JSonManager.parseFileInfo(desc);
                        if (fileInfo != null) {
                            List<FileInfo> totalList = JSonManager.getInstance().getInfoList();
                            if (totalList == null) {
                                totalList = new ArrayList<>();
                            }
                            int cameraType = DeviceClient.CAMERA_FRONT_VIEW;
                            if(CAMERA_TYPE_REAR.equals(fileInfo.getCameraType())){
                                cameraType = DeviceClient.CAMERA_REAR_VIEW;
                            }
                            if(cameraType == mApplication.getDeviceSettingInfo().getCameraType()) {
                                totalList.add(fileInfo);
                                JSonManager.convertJson(totalList);
                            }
                        }
                    } else Dbug.e(tag, "CMD:VIDEO_FINISH:desc is null");
                    break;
                case Topic.TF_CARD_CAPACITY:
                    if (null != data.getParams()) {
                        String left = data.getParams().get(TopicKey.LEFT);
                        String total = data.getParams().get(TopicKey.TOTAL);
                        int leftValue = 0;
                        int totalValue = 0;
                        try {
                            leftValue = Integer.valueOf(left);
                            totalValue = Integer.valueOf(total);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mApplication.getDeviceSettingInfo().setLeftStorage(leftValue);
                        mApplication.getDeviceSettingInfo().setTotalStorage(totalValue);
                       /* Dbug.e(tag, "tf cap lefe="+left+"total=" +total);
                        PreferencesHelper.putIntValue(getApplicationContext(),KEY_TF_STORAGE_LEFT,Integer.valueOf(left));
                        PreferencesHelper.putIntValue(getApplicationContext(),KEY_TF_STORAGE_TOTAL,Integer.valueOf(total));*/
                    }
                    /*Intent storageIntent = new Intent(ACTION_DEVICE_CAPACITY);
                    storageIntent.putExtra(KEY_DEVICE_STORAGE_AVAILABLE, data.getParams().get(TopicKey.LEFT));
                    storageIntent.putExtra(KEY_DEVICE_STORAGE_TOTAL, data.getParams().get(TopicKey.TOTAL));
                    sendBroadcast(storageIntent);*/
                    break;
                case Topic.FORMAT_TF_CARD:
                    sendBroadcast(new Intent(ACTION_FORMAT_TF_CARD));
                    break;
                case Topic.VIDEO_MIC:
                    mApplication.getDeviceSettingInfo().setVideoMic("1".equals(data.getParams().get(TopicKey.MIC)));
                    break;
                case Topic.PHOTO_QUALITY:
                    String qua = data.getParams().get(TopicKey.QUA);
                    if (!TextUtils.isEmpty(qua) && TextUtils.isDigitsOnly(qua))
                        mApplication.getDeviceSettingInfo().setPhotoQualityIndex(Integer.valueOf(qua));
                    break;
                case Topic.LANGUAGE:
                    String lan = data.getParams().get(TopicKey.LAG);
                    if (!TextUtils.isEmpty(lan) && TextUtils.isDigitsOnly(lan)) {
                        String preIndex = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_APP_LANGUAGE_CODE, "-1");
                        if (!lan.equals(preIndex)) {
                            //Dbug.e(tag, "Language =" + lan + ", preIndex="+preIndex);
                            AppUtils.changeAppLanguage(getApplicationContext(), lan);
                            sendBroadcast(new Intent(IActions.ACTION_LANGUAAGE_CHANGE));
                            PreferencesHelper.putStringValue(getApplicationContext(), KEY_APP_LANGUAGE_CODE, lan);
                        } else Dbug.w(tag, "lan=" + lan + ", preIndex="+preIndex);
                    } else Dbug.w(tag, "lan=" + lan);
                    break;
                case Topic.GRA_SEN:
                    String gra = data.getParams().get(TopicKey.GRA);
                    if (!TextUtils.isEmpty(gra) && TextUtils.isDigitsOnly(gra))
                        mApplication.getDeviceSettingInfo().setGravitySensor(Integer.valueOf(gra));
                    break;
                case Topic.VIDEO_PAR_CAR:
                    mApplication.getDeviceSettingInfo().setVideoParCar("1".equals(data.getParams().get(TopicKey.PAR)));
                    break;
                case Topic.VIDEO_DATE:
                    mApplication.getDeviceSettingInfo().setVideoDate("1".equals(data.getParams().get(TopicKey.DAT)));
                    break;
                case Topic.DEVICE_KEY_SOUND:
                    mApplication.getDeviceSettingInfo().setKeyVoice("1".equals(data.getParams().get(TopicKey.KVO)));
                    break;
                case Topic.BATTERY_STATUS:
                    String level = data.getParams().get(TopicKey.LEVEL);
                    if (!TextUtils.isEmpty(level) && TextUtils.isDigitsOnly(level))
                        mApplication.getDeviceSettingInfo().setBatStatus(Integer.valueOf(level));
                    break;
                case Topic.LIGHT_FRE:
                    String frequency = data.getParams().get(TopicKey.FREQUENCY);
                    if (!TextUtils.isEmpty(frequency) && TextUtils.isDigitsOnly(frequency))
                        mApplication.getDeviceSettingInfo().setLightFrequece(Integer.valueOf(frequency));
                    break;
                case Topic.AUTO_SHUTDOWN:
                    String aff = data.getParams().get(TopicKey.AFF);
                    if (!TextUtils.isEmpty(aff) && TextUtils.isDigitsOnly(aff))
                        mApplication.getDeviceSettingInfo().setAutoShutdown(Integer.valueOf(aff));
                    break;
                case Topic.SCREEN_PRO:
                    String pro = data.getParams().get(TopicKey.PRO);
                    if (!TextUtils.isEmpty(pro) && TextUtils.isDigitsOnly(pro))
                        mApplication.getDeviceSettingInfo().setScreenOn(Integer.valueOf(pro));
                    break;
                case Topic.TV_MODE:
                    String tvm = data.getParams().get(TopicKey.TV_MODE);
                    if (!TextUtils.isEmpty(tvm) && TextUtils.isDigitsOnly(tvm))
                        mApplication.getDeviceSettingInfo().setTvMode(Integer.valueOf(tvm));
                    break;
                case Topic.DOUBLE_VIDEO:
                    mApplication.getDeviceSettingInfo().setDoubleVideo("1".equals(data.getParams().get(TopicKey.TWO)));
                    break;
                case Topic.VIDEO_LOOP:
                    String cyc = data.getParams().get(TopicKey.CYC);
                    if (!TextUtils.isEmpty(cyc) && TextUtils.isDigitsOnly(cyc))
                        mApplication.getDeviceSettingInfo().setVideoLoop(Integer.valueOf(cyc));
                    break;
                case Topic.VIDEO_WDR:
                    mApplication.getDeviceSettingInfo().setVideoWdr("1".equals(data.getParams().get(TopicKey.WDR)));
                    break;
                case Topic.VIDEO_EXP:
                    String exp = data.getParams().get(TopicKey.EXP);
                    if (!TextUtils.isEmpty(exp) && TextUtils.isDigitsOnly(exp))
                        mApplication.getDeviceSettingInfo().setVideoExp(Integer.valueOf(exp));
                    break;
                case Topic.MOVE_CHECK:
                    mApplication.getDeviceSettingInfo().setMoveCheck("1".equals(data.getParams().get(TopicKey.MOT)));
                    break;
                case Topic.VIDEO_INV:
                    String gap = data.getParams().get(TopicKey.GAP);
                    if (!TextUtils.isEmpty(gap) && TextUtils.isDigitsOnly(gap))
                        mApplication.getDeviceSettingInfo().setVideoInv(Integer.valueOf(gap));
                    break;
                case Topic.PHOTO_RESO:
                    String resolution = data.getParams().get(TopicKey.RESOLUTION);
                    if (!TextUtils.isEmpty(resolution) && TextUtils.isDigitsOnly(resolution))
                        mApplication.getDeviceSettingInfo().setPhotoReso(Integer.valueOf(resolution));
                    break;
                case Topic.SELF_TIMER:
                    String phm = data.getParams().get(TopicKey.PHM);
                    if (!TextUtils.isEmpty(phm) && TextUtils.isDigitsOnly(phm))
                        mApplication.getDeviceSettingInfo().setSelfTime(Integer.valueOf(phm));
                    break;
                case Topic.BURST_SHOT:
                    mApplication.getDeviceSettingInfo().setBurstShot("1".equals(data.getParams().get(TopicKey.CYT)));
                    break;
                case Topic.PHOTO_SHARPNESS:
                    String acu = data.getParams().get(TopicKey.ACU);
                    if (!TextUtils.isEmpty(acu) && TextUtils.isDigitsOnly(acu))
                        mApplication.getDeviceSettingInfo().setPhotoSharpness(Integer.valueOf(acu));
                    break;
                case Topic.PHOTO_ISO:
                    String iso = data.getParams().get(TopicKey.ISO);
                    if (!TextUtils.isEmpty(iso) && TextUtils.isDigitsOnly(iso))
                        mApplication.getDeviceSettingInfo().setPhotoIso(Integer.valueOf(iso));
                    break;
                case Topic.ANTI_TREMOR:
                    mApplication.getDeviceSettingInfo().setAntiTremor(TopicParam.OPEN.equals(data.getParams().get(TopicKey.SOK)));
                    break;
                case Topic.PHOTO_DATE:
                    mApplication.getDeviceSettingInfo().setPhotoDate(TopicParam.OPEN.equals(data.getParams().get(TopicKey.DAT)));
                    break;
                case Topic.BOARD_VOICE:
                    mApplication.getDeviceSettingInfo().setOpenBootSound(TopicParam.OPEN.equals(data.getParams().get(TopicKey.BVO)));
                    break;
                case Topic.APP_SET_PROJECTION:
                    if (null != data.getParams()) {
                        boolean isOpen = TopicParam.OPEN.equals(data.getParams().get(TopicKey.STATUS));
                        int cmd;
                        if (isOpen) {
                            cmd = SERVICE_CMD_OPEN_SCREEN_TASK;
                        } else {
                            cmd = SERVICE_CMD_CLOSE_SCREEN_TASK;
                        }
                        mApplication.getDeviceSettingInfo().setOpenProjection(isOpen);
                        intent = new Intent(ACTION_PROJECTION_STATUS);
                        intent.putExtra(KEY_PROJECTION_STATUS, isOpen);
                        sendBroadcast(intent);
                        a.getApplication().sendScreenCmdToService(cmd);
                    }
                    break;
                case Topic.DEV_REQ_PROJECTION:
                    if (null != data.getParams()) {
                        boolean isOpen = TopicParam.OPEN.equals(data.getParams().get(TopicKey.STATUS));
                        int cmd;
                        if (isOpen) {
                            cmd = SERVICE_CMD_OPEN_SCREEN_TASK;
                            ClientManager.getClient().tryToScreenShotTask(true, 640, 480, 30, new SendResponse() {
                                @Override
                                public void onResponse(Integer code) {

                                }
                            });
                        } else {
                            cmd = SERVICE_CMD_CLOSE_SCREEN_TASK;
                        }
                        mApplication.getDeviceSettingInfo().setOpenProjection(isOpen);
                        intent = new Intent(ACTION_PROJECTION_STATUS);
                        intent.putExtra(KEY_PROJECTION_STATUS, isOpen);
                        sendBroadcast(intent);
                        mApplication.sendScreenCmdToService(cmd);
                    }
                    break;
                case Topic.CHECK_GRAVITY_SENSOR:
                    if (null != data.getParams()) {
                        boolean isOpen = TopicParam.OPEN.equals(data.getParams().get(TopicKey.STATUS));
                        if (isOpen) {
                            //send cmd
                            ClientManager.getClient().tryToCollisionVideo(new SendResponse() {
                                @Override
                                public void onResponse(Integer code) {
                                }
                            });
                        }
                    }
                    break;
                case Topic.COLLISION_DETECTION_VIDEO:
                    if (null != data.getParams()) {
                        int width, height, fps, duration;
                        String widthStr = data.getParams().get(TopicKey.WIDTH);
                        String heightStr = data.getParams().get(TopicKey.HEIGHT);
                        String fpsStr = data.getParams().get(TopicKey.FRAME_RATE);
                        String durationStr = data.getParams().get(TopicKey.DURATION);
                        if(!TextUtils.isEmpty(widthStr) &&  !TextUtils.isEmpty(heightStr)
                                && !TextUtils.isEmpty(fpsStr) && !TextUtils.isEmpty(durationStr)){
                            try {
                                width = Integer.valueOf(widthStr);
                                height = Integer.valueOf(heightStr);
                                fps = Integer.valueOf(fpsStr);
                                duration = Integer.valueOf(durationStr);
                                if (width > 0 && height > 0 && fps > 0 && duration > 0) {
                                    Dbug.i(tag, "-detection video- width : " + width + ", height = " + height
                                            + ", fps : "+ fps + ", duration : " + duration);
//                                if(videoServer != null && videoServer.isThreadRunning()){
//                                    videoServer.initMovWrapper(null, width, height, fps, duration);
//                                }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case Topic.RT_TALK_CTL:
                    if (null != data.getParams()) {
                        boolean isRtVoiceOpen = TopicParam.OPEN.equals(data.getParams().get(TopicKey.STATUS));
                        mApplication.getDeviceSettingInfo().setRTVoice(isRtVoiceOpen);
                    }
                    break;
                case Topic.OPEN_FRONT_RTS:
                    mApplication.getDeviceSettingInfo().setCameraType(DeviceClient.CAMERA_FRONT_VIEW);
                    break;
                case Topic.OPEN_REAR_RTS:
                    mApplication.getDeviceSettingInfo().setCameraType(DeviceClient.CAMERA_REAR_VIEW);
                    break;
                case Topic.PULL_VIDEO_STATUS:
                    if (null != data.getParams()) {
                        Dbug.w(tag, " >>>> data : "+data.toString());
                        boolean isExistRearCamera = TopicParam.TF_ONLINE.equals(data.getParams().get(TopicKey.STATUS));
                        Dbug.w(tag, "isExistRearCamera : " +isExistRearCamera);
                        mApplication.getDeviceSettingInfo().setExistRearView(isExistRearCamera);
                        if(isExistRearCamera){
                            String rearWidth = data.getParams().get(TopicKey.WIDTH);
                            String rearHeight = data.getParams().get(TopicKey.HEIGHT);
                            String rearFormat = data.getParams().get(TopicKey.FORMAT);
                            String rearRate = data.getParams().get(TopicKey.FRAME_RATE);
                            if(!TextUtils.isEmpty(rearWidth) && TextUtils.isDigitsOnly(rearWidth) &&
                                    !TextUtils.isEmpty(rearHeight) && TextUtils.isDigitsOnly(rearHeight)) {
                                int width = Integer.valueOf(rearWidth);
                                int height = Integer.valueOf(rearHeight);
                                int rearLevel = AppUtils.adjustRtsResolution(width, height);
                                mApplication.getDeviceSettingInfo().setRearLevel(rearLevel);
                            }
                            if(!TextUtils.isEmpty(rearFormat) && TextUtils.isDigitsOnly(rearFormat)){
                                int format = Integer.valueOf(rearFormat);
                                if(format == DeviceClient.RTS_JPEG){
                                    mApplication.getDeviceSettingInfo().setRearFormat(DeviceClient.RTS_JPEG);
                                }else{
                                    mApplication.getDeviceSettingInfo().setRearFormat(DeviceClient.RTS_H264);
                                }
                            }
                            if(!TextUtils.isEmpty(rearRate) && TextUtils.isDigitsOnly(rearRate)){
                                mApplication.getDeviceSettingInfo().setRearRate(Integer.valueOf(rearRate));
                            }
                        }else if(isReceiveBumpingData){
                            mApplication.setAbnormalExitThread(true);
                        }
                    }
                    break;
                case Topic.VIDEO_PARAM:
                    if (null != data.getParams()) {
                        Dbug.w(tag, "data : "+data.toString());
                        String rtsWidth = data.getParams().get(TopicKey.WIDTH);
                        String rtsHeight = data.getParams().get(TopicKey.HEIGHT);
                        String rtsFormat = data.getParams().get(TopicKey.FORMAT);
                        String frontRate = data.getParams().get(TopicKey.FRAME_RATE);
                        String sampleRate = data.getParams().get(TopicKey.SAMPLE);
                        if(!TextUtils.isEmpty(rtsWidth) && TextUtils.isDigitsOnly(rtsWidth) &&
                                !TextUtils.isEmpty(rtsHeight) && TextUtils.isDigitsOnly(rtsHeight)){
                            int width = Integer.valueOf(rtsWidth);
                            int height = Integer.valueOf(rtsHeight);
                            int frontLevel = AppUtils.adjustRtsResolution(width, height);
                            mApplication.getDeviceSettingInfo().setFrontRecordLevel(frontLevel);
                            mApplication.getDeviceSettingInfo().setFrontLevel(frontLevel);
                        }
                        if(!TextUtils.isEmpty(rtsFormat) && TextUtils.isDigitsOnly(rtsFormat)){
                            int format = Integer.valueOf(rtsFormat);
                            if(format == DeviceClient.RTS_JPEG){
                                mApplication.getDeviceSettingInfo().setFrontFormat(DeviceClient.RTS_JPEG);
                            }else{
                                mApplication.getDeviceSettingInfo().setFrontFormat(DeviceClient.RTS_H264);
                            }
                        }
                        if(!TextUtils.isEmpty(frontRate) && TextUtils.isDigitsOnly(frontRate)){
                            mFrameRate = Integer.valueOf(frontRate);
                            mApplication.getDeviceSettingInfo().setFrontRate(mFrameRate);
                        }
                        if(!TextUtils.isEmpty(sampleRate) && TextUtils.isDigitsOnly(sampleRate)){
                            mSampleRate = Integer.valueOf(sampleRate);
                            mApplication.getDeviceSettingInfo().setFrontSampleRate(mSampleRate);
                        }
                        initSdpServer(mFrameRate, mSampleRate);
                    }
                    break;
                case Topic.PULL_VIDEO_PARAM:{
                    //Dbug.w(tag, "PULL_VIDEO_PARAM:"+data.toString());
                    if (null != data.getParams()) {
                        String rearWidth = data.getParams().get(TopicKey.WIDTH);
                        String rearHeight = data.getParams().get(TopicKey.HEIGHT);
                        String rearFormat = data.getParams().get(TopicKey.FORMAT);
                        String rearRate = data.getParams().get(TopicKey.FRAME_RATE);
                        String sampleRate = data.getParams().get(TopicKey.SAMPLE);
                        if (!TextUtils.isEmpty(rearWidth) && TextUtils.isDigitsOnly(rearWidth) &&
                                !TextUtils.isEmpty(rearHeight) && TextUtils.isDigitsOnly(rearHeight)) {
                            int width = Integer.valueOf(rearWidth);
                            int height = Integer.valueOf(rearHeight);
                            int rearLevel = AppUtils.adjustRtsResolution(width, height);
                            mApplication.getDeviceSettingInfo().setRearRecordLevel(rearLevel);
                            mApplication.getDeviceSettingInfo().setRearLevel(rearLevel);
                        }
                        if (!TextUtils.isEmpty(rearFormat) && TextUtils.isDigitsOnly(rearFormat)) {
                            int format = Integer.valueOf(rearFormat);
                            if (format == DeviceClient.RTS_JPEG) {
                                mApplication.getDeviceSettingInfo().setRearFormat(DeviceClient.RTS_JPEG);
                            } else {
                                mApplication.getDeviceSettingInfo().setRearFormat(DeviceClient.RTS_H264);
                            }
                        }
                        if(!TextUtils.isEmpty(rearRate) && TextUtils.isDigitsOnly(rearRate)){
                            mFrameRate = Integer.valueOf(rearRate);
                            mApplication.getDeviceSettingInfo().setRearRate(mFrameRate);
                        }
                        if(!TextUtils.isEmpty(sampleRate) && TextUtils.isDigitsOnly(sampleRate)){
                            mSampleRate = Integer.valueOf(sampleRate);
                            mApplication.getDeviceSettingInfo().setRearSampleRate(mSampleRate);
                        }
                        initSdpServer(mFrameRate, mSampleRate);
                    }
                    break;
                }
//                case "GENERIC_CMD":{//设置回应命令的数据
//                    if (data.getParams() != null) {
//                        String sRespone = data.getParams().get("status");
//                        int i=0;
//                    }
//                    break;
//                }
//                case "PHOTO_BRIGHTNESS":{
//                    if (data.getParams() != null) {
//                        String sValue = data.getParams().get("brt");
//                        if(!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)){
//                            mApplication.Camera_brt=Integer.valueOf(sValue);
//                        }
//                    }
//                    break;
//                }
//                case Topic.PHOTO_EXP:
//                    if (data.getParams() != null) {
//                        String sValue = data.getParams().get(TopicKey.EXP);
//                        if (!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)) {
//                            mApplication.getDeviceSettingInfo().setPhotoExp(Integer.valueOf(sValue));
//                            mApplication.Camera_exp = Integer.valueOf(sValue);
//                        }
//                    }
//                    break;
//                case "PHOTO_CONTRAST":{
//                    if (data.getParams() != null) {
//                        String sValue = data.getParams().get("ctr");
//                        if(!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)){
//                            mApplication.Camera_ctr=Integer.valueOf(sValue);
//                        }
//                    }
//                    break;
//                }
//                case Topic.WHITE_BALANCE:{
//                    if (data.getParams() != null) {
//                        String sValue = data.getParams().get("wbl");
//                        if(!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)){
//                            mApplication.Camera_wbl=Integer.valueOf(sValue);
//                        }
//                    }
//                    break;
//                }
//                case "UAV_HEIGHT":{//无人机高度
//                    if (data.getParams() != null) {
//                        String sValue = data.getParams().get("height");
//                        if(!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)){
//                            mApplication.UAV_height=Integer.valueOf(sValue);
//
//                            Intent intentSendHight = new Intent("UAV_HEIGHT_recv");
//                            intentSendHight.putExtra("height", mApplication.UAV_height);
//                            sendBroadcast(intentSendHight);
//                        }
//                    }
//                    break;
//                }
                default:
                    break;
            }
        }
    };

    private OnConnectStateListener connectStateListener = new OnConnectStateListener() {
        @Override
        public void onStateChanged(Integer state) {
            switch (state) {
                case Constants.DEVICE_STATE_CONNECTED:
//                    UdpClient.getInstance().createClient(ClientManager.getClient().getConnectedIP(), UdpClient.UDP_CLIENT_PORT);
//                    if (videoServer == null) {
//                        videoServer = new VideoServer(VIDEO_SERVER_PORT, 2);
//                        videoServer.setCrashVideoListener(mCrashVideoListener);
//                        videoServer.start();
//                    }
                    break;
                case Constants.DEVICE_STATE_CONNECTION_TIMEOUT:
                case Constants.DEVICE_STATE_EXCEPTION:
                case Constants.DEVICE_STATE_DISCONNECTED:
                case Constants.DEVICE_STATE_UNREADY:
                    Dbug.e(tag, "stop mHeartbeatTask");
                    if (mHeartbeatTask != null) {
                        if (mHeartbeatTask.isHeartbeatTaskRunning()) {
                            mHeartbeatTask.stopRunning();
                        }
                        mHeartbeatTask = null;
                    }
//                    if (videoServer != null) {
//                        videoServer.stopServer();
//                        videoServer = null;
//                    }
//                    UdpClient.getInstance().closeClient(false);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        //Dbug.w(tag, "onDestroy=============");
        release();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Dbug.w(tag, "onTaskRemoved=============");
        release();
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    private void release() {
        Dbug.e(tag, "======= (( release )) =====");
        ClientManager.getClient().unregisterNotifyListener(onNotifyResponse);
        ClientManager.getClient().unregisterConnectStateListener(connectStateListener);
//        ClientManager.getClient().release();
        mHandler.removeCallbacksAndMessages(null);
        if(clearThumbTask != null){
            clearThumbTask.stopClear();
            clearThumbTask = null;
        }
        if (loadDeviceDesTxt != null) {
            loadDeviceDesTxt.interrupt();
            loadDeviceDesTxt = null;
        }
        if (mHeartbeatTask != null) {
            mHeartbeatTask.stopRunning();
            mHeartbeatTask = null;
        }
        if (mServer != null) {
            mServer.stopRunning();
            mServer = null;
        }
    }

    private int getRtsFormat() {
        int format = DeviceClient.RTS_H264;
        DeviceDesc settingInfo = a.getApplication().getDeviceDesc();
        if (settingInfo != null) {
            format = settingInfo.getVideoType();
        }
        return format;
    }

    private void initSdpServer(int frameRate, int sampleRate) {
        if (mServer == null) {
            //Dbug.e(tag, "=========initSdpServer=========");
            mServer = new SDPServer(SDP_PORT, getRtsFormat());
            mServer.setFrameRate(frameRate);
            mServer.setSampleRate(sampleRate);
            mServer.setRtpVideoPort(RTP_VIDEO_PORT1);
            mServer.setRtpAudioPort(RTP_AUDIO_PORT1);
            mServer.start();
        }
    }

    private static class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Dbug.w(tag, "-handleMessage- what : " + msg.what);
            switch (msg.what) {
                case MSG_HEARTBEAT_CONNECTION_TIMEOUT://超时断开连接后的处理
                    ClientManager.getClient().disconnect();
                    //a.getApplication().popActivityOnlyMain();//结束最顶页面
                    String s = a.getApplication().getString(R.string.connection_timeout);
                    a.getApplication().showToastLong(s);
                    break;
                case MSG_CONNECT_CTP:
                    String ip = (String) msg.obj;
                    if(TextUtils.isEmpty(ip)){
                        ip = DEFAULT_DEV_IP;
                    }
                    if (!ClientManager.getClient().isConnected()) {
                        ClientManager.getClient().connect(ip, CTP_TCP_PORT);
                    }
                    break;
                case MSG_DISCONNECT_CTP:
                    ClientManager.getClient().disconnect();
                    break;
            }
        }
    }

    private static class LoadDeviceDesTxt extends Thread {
        private WeakReference<CommunicationService> mServiceRef;

        LoadDeviceDesTxt(CommunicationService service) {
            mServiceRef = new WeakReference<>(service);
        }
        @Override
        public void run() {
            super.run();
            String connectIP = ClientManager.getClient().getConnectedIP();
            if (!TextUtils.isEmpty(connectIP)) {
                String url = AppUtils.formatUrl(connectIP, IConstant.DEFAULT_HTTP_PORT, "mnt/spiflash/res/dev_desc.txt");
                Dbug.i(tag, "download url = " + url);
                HttpManager.downloadFile(url, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Dbug.w(tag, "download failed, reason = " + e.getMessage());
                        if (!call.isExecuted()) {
                            call.enqueue(this);
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Dbug.i(tag, "download code = " + response.code());
                        if (response.code() == 200) {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                byte[] bytes = responseBody.bytes();
                                if (bytes != null) {
                                    //Dbug.i(tag, "download ok, desc ： " + new String(bytes));
                                    String output = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(), IConstant.VERSION, null, null)
                                            + File.separator + DEVICE_DESCRIPTION;
                                    OutputStream outputStream = null;
                                    try {
                                        outputStream = new FileOutputStream(new File(output));
                                        outputStream.write(bytes);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        if (outputStream != null) {
                                            outputStream.close();
                                        }
                                    }
                                    if (ClientManager.getClient().isConnected()) {
                                        if (mServiceRef == null) {
                                            Dbug.e(tag, "context is null");
                                            return;
                                        }
                                        String upgradePath = AppUtils.checkUpdateFilePath(mServiceRef.get(), UPGRADE_SDK_TYPE);
                                        if (!TextUtils.isEmpty(upgradePath) && !upgradePath.equals(mServiceRef.get().getString(R.string.latest_version))) {
                                            Dbug.w(tag, "sdk upgradePath = " + upgradePath);
                                            ArrayList<String> upgradePathList = new ArrayList<>();
                                            upgradePathList.add(upgradePath);
                                            Intent intent = new Intent(ACTION_UPGRADE_FILE);
                                            Bundle bundle = new Bundle();
                                            bundle.putInt(UPDATE_TYPE, UPGRADE_SDK_TYPE);
                                            bundle.putStringArrayList(UPDATE_PATH, upgradePathList);
                                            intent.putExtra(KEY_DATA, bundle);
                                            if (mServiceRef != null && mServiceRef.get() != null)
                                                mServiceRef.get().sendBroadcast(intent);
                                        }
                                    }
                                }
                            }
                        }
                        response.close();
                    }
                });
            }
        }
    }
}
