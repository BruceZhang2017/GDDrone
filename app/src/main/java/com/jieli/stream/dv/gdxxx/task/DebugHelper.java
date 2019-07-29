package com.jieli.stream.dv.gdxxx.task;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;

import com.jieli.lib.dv.control.utils.Dlog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;

public class DebugHelper {
    private final static String tag = DebugHelper.class.getSimpleName();

    private DatagramSocket mSocket;
    private ReceiveThread mReceiveThread;
    private SendThread mSendThread;
    private HashSet<IDebugListener> mIDebugListeners;
    private Handler mHandler;

    private int mPort = DEBUG_PORT;
    private String mBroadCastIP;
    private static int mSeq;

    private static final int DEBUG_PORT = 3889;//AC56 : 3889
//    private static final int DEBUG_PORT = 3333;//AC52 : 3333
    private final static String DEBUG_PACKET_FLAG = "MSSDP_NOTIFY ";
    private final static String DEBUG_START_FLAG = "UX_SEND_LEN";
    private final static String DEBUG_RESULT_FLAG = "UX_REPORT";
    private final static String DEBUG_SEND_FLAG = "UX_DATA";

    /*error code*/
    public static final int ERROR_UDP_UNINIT = 1;
    public static final int ERROR_NETWORK_EXCEPTION = 2;
    public static final int ERROR_DATA_EXCEPTION = 3;


    public DebugHelper(){
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void startDebug(){
        createUDPClient();
        startRecvThread();
    }

    public void closeDebug(){
        stopSendThread();
        stopRecvThread();
        closeUDPClient();
        if(mIDebugListeners != null){
            mIDebugListeners.clear();
            mIDebugListeners = null;
        }
    }

    public boolean registerDebugListener(IDebugListener listener){
        if(mIDebugListeners == null){
            mIDebugListeners = new HashSet<>();
        }
        return listener != null && mIDebugListeners.add(listener);
    }

    public boolean unregisterDebugListener(IDebugListener listener){
        return listener != null && mIDebugListeners != null && mIDebugListeners.remove(listener);
    }

    private void createUDPClient(){
        if(mSocket == null){
            try{
                mSocket = new DatagramSocket(mPort);
                mSocket.setBroadcast(true);
                mSocket.setReuseAddress(true);
            }catch (IOException e){
                e.printStackTrace();
                mSocket = null;
            }
        }
    }

    private void closeUDPClient(){
        if(mSocket != null){
            if(!mSocket.isClosed()){
                mSocket.close();
            }
            mSocket = null;
        }
    }

    private void startRecvThread(){
        if(mReceiveThread == null || !mReceiveThread.isThreadAlive){
            mReceiveThread = new ReceiveThread(mSocket);
            mReceiveThread.start();
        }
    }

    private void stopRecvThread(){
        if(mReceiveThread != null){
            if(mReceiveThread.isThreadAlive){
                mReceiveThread.stopThread();
            }
            mReceiveThread = null;
        }
    }

    private void notifyDebugStart(final String ip, final int len, final int interval){
        mBroadCastIP = ip;
        startSendThread(mBroadCastIP, len, interval);
        if(mHandler != null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                   if(mIDebugListeners != null){
                       HashSet<IDebugListener> temp = (HashSet<IDebugListener>)mIDebugListeners.clone();
                       for (IDebugListener listener : temp){
                           listener.onStartDebug(ip, len, interval);
                       }
                   }
                }
            });
        }
    }

    private void notifyDebugResult(final int dropCount, final int dropSum){
        if(mHandler != null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mIDebugListeners != null){
                        HashSet<IDebugListener> temp = (HashSet<IDebugListener>)mIDebugListeners.clone();
                        for (IDebugListener listener : temp){
                            listener.onDebugResult(dropCount, dropSum);
                        }
                    }
                }
            });
        }
    }

    private void notifyErrorEvent(final int code, final String msg){
        if(mHandler != null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mIDebugListeners != null){
                        HashSet<IDebugListener> temp = (HashSet<IDebugListener>)mIDebugListeners.clone();
                        for (IDebugListener listener : temp){
                            listener.onError(code, msg);
                        }
                    }
                }
            });
        }
    }

    private class ReceiveThread extends Thread{
        private volatile boolean isThreadAlive = false;
        private DatagramSocket mSocket;

        ReceiveThread(DatagramSocket socket){
            mSocket = socket;
        }

        @Override
        public synchronized void start() {
            isThreadAlive = true;
            super.start();
        }

        private synchronized void stopThread(){
            isThreadAlive = false;
        }

        @Override
        public void run() {
            Dlog.i(tag, "ReceiveThread running...");
            while (isThreadAlive){
                if(mSocket == null){
                    notifyErrorEvent(ERROR_UDP_UNINIT, code2Msg(ERROR_UDP_UNINIT));
                    break;
                }
                try{
                    byte[] mBuffer = new byte[1024 * 5]; // 5kb
                    DatagramPacket mRcvDatagramPacket = new DatagramPacket(mBuffer, mBuffer.length);
                    mSocket.receive(mRcvDatagramPacket);
                    if(mRcvDatagramPacket.getAddress() != null) {
                        String remoteIP = mRcvDatagramPacket.getAddress().getHostAddress();
                        if (mRcvDatagramPacket.getData() != null && mRcvDatagramPacket.getData().length > 0) {
                            String mReceiveContent = new String(mRcvDatagramPacket.getData()).trim();
//                            Dlog.i(tag, "ReceiveThread >>> mReceiveContent : " + mReceiveContent);
                            if (mReceiveContent.startsWith(DEBUG_PACKET_FLAG)) { //debug数据
                                mReceiveContent = mReceiveContent.replace(DEBUG_PACKET_FLAG, "");
                                int index = mReceiveContent.indexOf(DEBUG_START_FLAG);
                                if (index != -1) {
                                    if (mReceiveContent.contains(",")) {
                                        int sendDataLen = 0;
                                        int sendDataInterval = 0;
                                        String[] args = mReceiveContent.split(",", 2);
                                        if (args[0].contains(":")) {
                                            String[] params = args[0].split(":", 2);
                                            sendDataLen = string2Int(params[1]);
                                        }
                                        if (args[1].contains(":")) {
                                            String[] params = args[1].split(":", 2);
                                            sendDataInterval = string2Int(params[1]);
                                        }
//                                        Dlog.i(tag, "ReceiveThread >>> sendDataLen : " +sendDataLen+", sendDataInterval : " + sendDataInterval);
                                        if (sendDataLen > 0) {
                                            notifyDebugStart(remoteIP, sendDataLen, sendDataInterval);
                                        }else{
                                            notifyErrorEvent(ERROR_DATA_EXCEPTION, code2Msg(ERROR_DATA_EXCEPTION));
                                        }
                                    }
                                } else {
                                    index = mReceiveContent.indexOf(DEBUG_RESULT_FLAG);
                                    if (index != -1) {
                                        if (mReceiveContent.contains(":")) {
                                            String[] sons = mReceiveContent.split(":", 2);
                                            String sonContent = sons[1];
//                                            Dlog.i(tag, "ReceiveThread >>> sonContent : " +sonContent);
                                            if (sonContent.contains(",")) {
                                                int dropCount = 0;
                                                int dropSum = 0;
                                                String[] args = sonContent.split(",", 2);
                                                if (args[0].contains(":")) {
                                                    String[] params = args[0].split(":", 2);
                                                    dropCount = string2Int(params[1]);
                                                }
                                                if (args[1].contains(":")) {
                                                    String[] params = args[1].split(":", 2);
                                                    dropSum = string2Int(params[1]);
                                                }
//                                                Dlog.i(tag, "ReceiveThread >>> dropCount : " +dropCount+", dropSum : " + dropSum);
                                                notifyDebugResult(dropCount, dropSum);
                                            }
                                        }
                                    } else {
                                        Dlog.e(tag, "unknown data : " + mReceiveContent);
                                        notifyErrorEvent(ERROR_DATA_EXCEPTION, mReceiveContent);
                                    }
                                }
                            }
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    notifyErrorEvent(ERROR_NETWORK_EXCEPTION, e.getMessage());
                }
            }
            isThreadAlive = false;
        }

        private int string2Int(String value){
            int ret = 0;
            if(value != null && value.length() > 0){
                try{
                    ret = Integer.valueOf(value);
                }catch (Exception e){
                    e.printStackTrace();
                    ret = 0;
                }
            }
            return ret;
        }
    }

    private void startSendThread(String ip, int dataLen, int interval){
//        stopSendThread();
        if(mSendThread == null || !mSendThread.isThreadAlive){
            mSendThread = new SendThread(mSocket, ip, dataLen, interval);
            mSendThread.start();
        }
    }

    private void stopSendThread(){
        if(mSendThread != null){
            if(mSendThread.isThreadAlive){
                mSendThread.stopThread();
            }
            mSendThread = null;
        }
    }

    private class SendThread extends Thread{
        private volatile boolean isThreadAlive = false;
        private DatagramSocket mSocket;
        private InetAddress mInetAddress;
        private int dataLen;
        private int interval;

        SendThread(DatagramSocket socket, String ip, int dataLen, int interval){
            mSocket = socket;
            this.dataLen = dataLen;
            this.interval = interval;
            try {
                mInetAddress = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                mInetAddress = null;
            }
        }

        @Override
        public synchronized void start() {
            isThreadAlive = true;
            super.start();
        }

        public synchronized void stopThread(){
            isThreadAlive = false;
        }

        @Override
        public void run() {
            Dlog.i(tag, "SendThread running...");
            while (isThreadAlive){
                DatagramPacket datagramPacket = createPacket();
                if(datagramPacket != null && mSocket != null){
                    try {
                        mSocket.send(datagramPacket);
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        notifyErrorEvent(ERROR_NETWORK_EXCEPTION, e.getMessage());
                    }
                }
            }
        }

        private DatagramPacket createPacket(){
            DatagramPacket packet = null;
            if(dataLen > 0 && mInetAddress != null){
                mSeq++;
                String head = DEBUG_PACKET_FLAG +DEBUG_SEND_FLAG+":"+mSeq+" ";
                byte[] headBuf = head.getBytes();
                byte[] payload = new byte[dataLen];
                for (int i = 0; i < dataLen; i++){
                    payload[i] = (byte)(new Random(100).nextInt());
                }
                byte[] data = new byte[headBuf.length + dataLen];
                System.arraycopy(headBuf, 0, data, 0, headBuf.length);
                System.arraycopy(payload, 0, data, headBuf.length, dataLen);
//                Dlog.i(tag, "send data : " + new String(data));
                packet = new DatagramPacket(data, data.length, mInetAddress, mPort);
            }
            return packet;
        }
    }

    private String code2Msg(int code){
        String msg = "";
        switch (code){
            case ERROR_UDP_UNINIT:
                msg = "udp socket init failed.";
                break;
            case ERROR_NETWORK_EXCEPTION:
                msg = "network error.";
                break;
            case ERROR_DATA_EXCEPTION:
                msg = "receive data is error.";
                break;
        }
        return msg;
    }

    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = 0;

    private static long getTotalRxBytes(Context mContext) {
        return TrafficStats.getUidRxBytes(mContext.getApplicationInfo().uid)==TrafficStats.UNSUPPORTED ? 0 : Math.abs(TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes());
    }

    /**
     * 获取网络速度(仅Wifi)
     *
     * @param context  上下文
     */
    public static String getNetSpeed(Context context) {
        long nowTotalRxBytes = getTotalRxBytes(context);
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return Formatter.formatFileSize(context, speed) +"/s";
    }

}
