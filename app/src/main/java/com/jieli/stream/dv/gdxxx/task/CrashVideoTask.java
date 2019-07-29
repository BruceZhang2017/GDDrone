package com.jieli.stream.dv.gdxxx.task;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.player.MovWrapper;
import com.jieli.lib.dv.control.player.OnRecordListener;
import com.jieli.lib.dv.control.projection.tools.FormatHex;
import com.jieli.stream.dv.gdxxx.bean.DeviceSettingInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.ScanFilesHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 接收碰撞视频数据线程
 *
 * @author zqjasonZhong
 *         date : 2017/8/1
 */
public class CrashVideoTask extends Thread {
    private String tag = "CrashVideoTask";
    private Socket mSocket;
    private VideoServer.CrashVideoListener listener;
    private MovWrapper movWrapper;
    private String savePath;

    public CrashVideoTask(Socket mSocket, VideoServer.CrashVideoListener listener) {
        this.mSocket = mSocket;
        this.listener = listener;
        movWrapper = new MovWrapper();
    }

    public boolean initMovWrapper(String savePath, int width, int height, int fps) {
        File file = new File(savePath);
        if (file.exists()) {
            if (file.delete()) {
                Dbug.e(tag, "delete file success, saveFilePath = " + savePath);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.savePath = savePath;
        movWrapper.setFrameRate(fps);
        movWrapper.setResolution(width, height);
        movWrapper.setOnRecordListener(onRecordListener);
        boolean ret = movWrapper.create(savePath);
        Dbug.e(tag, "create file result : " + ret + " , savePath = " + savePath);
        return ret;
    }

    private OnRecordListener onRecordListener = new OnRecordListener() {
        @Override
        public void onError(int code, String msg) {
            super.onError(code, msg);
            Dbug.e(tag, "wrapper mov error! code = "+code +", msg = " + msg);
            publishError(code, msg);
        }

        @Override
        public void onStateChanged(int state, String msg) {
            super.onStateChanged(state, msg);
            if (state == MovWrapper.REC_STATE_START) {
                Dbug.i(tag, "wrapper mov start! path = " + msg);
                if(listener != null){
                    listener.onStateChange(IConstant.STATE_START, null);
                }
            } else if (state == MovWrapper.REC_STATE_END) {
                Dbug.e(tag, "wrapper mov finish! path = " + msg);
                if(listener != null){
                    listener.onStateChange(IConstant.STATE_END, savePath);
                }
                ScanFilesHelper scanFilesHelper = new ScanFilesHelper(a.getApplication());
                scanFilesHelper.scanFiles(savePath);
            }
        }
    };

    @Override
    public void run() {
        super.run();
        if(mSocket == null) return;
        try {
            int port = mSocket.getPort();
            String saveDir = IConstant.DIR_FRONT;
            int cameraType = DeviceClient.CAMERA_FRONT_VIEW;
            if(port == IConstant.REAR_EMERGENCY_VIDEO_PORT){
                saveDir = IConstant.DIR_REAR;
                cameraType = DeviceClient.CAMERA_REAR_VIEW;
            }
            int[] videoParams = AppUtils.getRtsResolution(getCameraLevel(cameraType));
            String savePath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(), saveDir, IConstant.DIR_RECORD)
                    + File.separator + AppUtils.getCrashVideoName();
            Dbug.w(tag, "saveDir >> " +saveDir);
            boolean isMovOk = initMovWrapper(savePath, videoParams[0], videoParams[1], getVideoRate(cameraType));
            if(!isMovOk){
                publishError(IConstant.ERROR_INIT_MOV, "init mov wrapper failed.");
            }
            while (mSocket.isConnected() && isMovOk) {
                InputStream inputStream;
                try {
                    inputStream = mSocket.getInputStream();
                    int ret = parsePacket(inputStream);
                    if (ret <= 0) {
                        if(ret < 0){
                            publishError(IConstant.ERROR_DEVICE_OFFLINE, "device offline.");
                        }
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    publishError(IConstant.ERROR_NETWORK, e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            publishError(IConstant.ERROR_NETWORK, e.getMessage());
        } finally {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (movWrapper != null) {
                if(!movWrapper.close()){
                    Dbug.w(tag, "movWrapper close failed.");
                    publishError(IConstant.ERROR_CLOSE_MOV, "close mov wrapper failed.");
                }else{
                    Dbug.w(tag, "movWrapper close ....savePath = " +savePath);
                }
                movWrapper.setOnRecordListener(null);
            }
        }
    }

    /*
    * 动态接收数据
    * 数据格式:
    * |1byte 类型 | 1byte 保留 | 2byte  payload长度 | 4byte 序号 | 4byte 帧大小 | 4byte 偏移 | 4byte 时间戳 | payload  |
    * 有效位：类型[0] + 序号[4-7] + 帧大小[8-11] + 时间戳[16-19] + payload[n]
    */
    private int parsePacket(InputStream inputStream) {
        int result = 0;
        if (inputStream != null) {
            try {
                byte[] headBuf = new byte[20];
                byte[] typeBuf = new byte[1];
                byte[] seqBuf = new byte[4];
                byte[] frameSizeBuf = new byte[4];
                byte[] dateFlagBuf = new byte[4];
                byte[] tempBuf;
                int headSize = 0;
                int length;
                while (headSize != 20) {
                    int size = 20 - headSize;
                    if (size >= 4) {
                        tempBuf = new byte[4];
                    } else {
                        tempBuf = new byte[size];
                    }
                    length = inputStream.read(tempBuf);
                    if (length < 0) {
                        return 0;
                    }
                    if(a.getApplication().isAbnormalExitThread()){
                        return -1;
                    }
                    System.arraycopy(tempBuf, 0, headBuf, headSize, length);
                    headSize += length;
                }
//                Dbug.w(tag,"-parsePacket- headBuf = "+ FormatHex.encodeHexStr(headBuf));
                System.arraycopy(headBuf, 0, typeBuf, 0, 1);
                System.arraycopy(headBuf, 4, seqBuf, 0, 4);
                System.arraycopy(headBuf, 8, frameSizeBuf, 0, 4);
                System.arraycopy(headBuf, 16, dateFlagBuf, 0, 4);

                int type = FormatHex.byteArrayToInt(typeBuf);
                int seq = FormatHex.byteArrayToInt(seqBuf);
                int frameSize = FormatHex.byteArrayToInt(frameSizeBuf);
                int dateFlag = FormatHex.byteArrayToInt(dateFlagBuf);

//                Dbug.w(tag,"-parsePacket- type : "+ type+", playLoad : " +FormatHex.encodeHexStr(frameSizeBuf) + ", len = "+ frameSize);
                if (frameSize > 0 && frameSize < 5 * 1024 * 1024) {
                    byte[] payload = new byte[frameSize];
                    byte[] temp;
                    int totalSize = 0;
                    int payloadLen;
                    while (totalSize != frameSize) {
                        int len = frameSize - totalSize;
                        if (len >= 1024) {
                            temp = new byte[1024];
                            payloadLen = inputStream.read(temp);
                        } else {
                            temp = new byte[len];
                            payloadLen = inputStream.read(temp);
//                            Dbug.w(tag,"-parsePacket- len : " + len+", payloadLen = " +payloadLen);
                        }
                        if (payloadLen < 0) {
                            return 0;
                        }
                        if(a.getApplication().isAbnormalExitThread()){
                            return -1;
                        }
                        System.arraycopy(temp, 0, payload, totalSize, payloadLen);
                        totalSize += payloadLen;
                    }

                    /*if (type == StreamType.TYPE_AUDIO) {
//                        Dbug.e(tag,"-parsePacket- audio frameSize : " + frameSize + "\nplayLoad : " +FormatHex.encodeHexStr(payload));
                        int len = payload.length;
                        //小端转大端
                        for (int i = 0; i < len / 2; i++) {
                            byte tmp;
                            tmp = payload[i * 2];
                            payload[i * 2] = payload[i * 2 + 1];
                            payload[i * 2 + 1] = tmp;
                        }
                    }*/
                    movWrapper.write(type, payload);
                    result = 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return result;
    }

    private int getCameraLevel(int cameraType){
        int level = IConstant.RTS_LEVEL_HD;
        DeviceSettingInfo settingInfo = a.getApplication().getDeviceSettingInfo();
        if(settingInfo != null){
            if(cameraType == DeviceClient.CAMERA_REAR_VIEW){
                level = settingInfo.getRearLevel();
            }else{
                level = settingInfo.getFrontLevel();
            }
        }
        return level;
    }

    private int getVideoRate(int cameraType){
        int rate = 30;
        DeviceSettingInfo settingInfo = a.getApplication().getDeviceSettingInfo();
        if(settingInfo != null){
            if(cameraType == DeviceClient.CAMERA_REAR_VIEW){
                rate = settingInfo.getRearRate();
            }else{
                rate = settingInfo.getFrontRate();
            }
        }
        return rate;
    }

    private void publishError(int code, String msg){
        Dbug.e(tag, "code >> "+ code +", msg : " +msg);
        if(listener != null){
            listener.onError(code, msg);
        }
        File file = new File(savePath);
        if(file.exists()){
            file.delete();
        }
    }

}
