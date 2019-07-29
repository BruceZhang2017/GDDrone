package com.jieli.stream.dv.gdxxx.task;

import com.jieli.stream.dv.gdxxx.util.Dbug;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 接收碰撞数据服务器
 * @author zqjasonZhong
 *         date : 2017/7/26
 */
public class VideoServer extends Thread {
    private String tag = "VideoServer";
    private ServerSocket mServerSocket;
    private ExecutorService executorService;
    private CrashVideoListener listener;

    private boolean isThreadRunning;

    public VideoServer(int port, int limit){
        try {
            mServerSocket = new ServerSocket();
            mServerSocket.setReuseAddress(true);
            mServerSocket.bind(new InetSocketAddress(port), limit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService = Executors.newFixedThreadPool(limit);
    }

    public void setCrashVideoListener(CrashVideoListener listener){
        this.listener = listener;
    }

    public boolean isThreadRunning() {
        return isThreadRunning;
    }

    public void stopServer(){
        isThreadRunning = false;
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mServerSocket = null;
    }

    @Override
    public void run() {
        super.run();
        isThreadRunning = true;
        while (isThreadRunning){
            if(mServerSocket != null){
                Socket mSocket;
                try {
                    mSocket = mServerSocket.accept();
                    if(mSocket != null){
                        Dbug.i(tag, "socket alive = " + mSocket.isConnected()+", address = " + mSocket.getInetAddress().toString());
                        //启动接收碰撞数据线程
                        if(executorService != null){
                            executorService.submit(new CrashVideoTask(mSocket, listener));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isThreadRunning = false;
                    break;
                }
            }else{
                isThreadRunning = false;
                break;
            }
        }
        if(executorService != null){
            executorService.shutdownNow();
        }
    }

    public interface CrashVideoListener{
        void onStateChange(int state, String msg);

        void onError(int code, String msg);
    }
}
