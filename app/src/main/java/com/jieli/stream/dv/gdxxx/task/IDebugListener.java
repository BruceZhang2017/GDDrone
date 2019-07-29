package com.jieli.stream.dv.gdxxx.task;

public interface IDebugListener {

    void onStartDebug(String remoteIp, int sendDataLen, int sendDataInterval);

    void onDebugResult(int dropCount, int dropSum);

    void onError(int code, String message);
}
