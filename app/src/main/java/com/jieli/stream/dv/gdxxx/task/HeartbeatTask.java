package com.jieli.stream.dv.gdxxx.task;

import android.os.Handler;
import android.os.SystemClock;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.utils.Dlog;
import com.jieli.stream.dv.gdxxx.ui.service.CommunicationService;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;

import java.lang.ref.WeakReference;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

public class HeartbeatTask extends Thread {
    String tag = getClass().getSimpleName();
    private int mTimeoutCount = 0;
    private int mTimeout = DEFAULT_HEARTBEAT_TIMEOUT;
    private long mPeriod = DEFAULT_HEARTBEAT_PERIOD;
    /**
     * 默认每隔5秒发送一次心跳数据
     */
    public static final int DEFAULT_HEARTBEAT_PERIOD = 5 * 1000;//5S
    /**
     * 默认6次不回复，作超时处理
     */
    public static final int DEFAULT_HEARTBEAT_TIMEOUT = 6;//6 times
    private boolean isHeartbeatTaskRunning = false;
    private WeakReference<Handler> mHandlerWeakReference;

    public boolean isHeartbeatTaskRunning() {
        return isHeartbeatTaskRunning;
    }
    public void stopRunning() {
        isHeartbeatTaskRunning = false;
        mTimeoutCount = 0;
    }

    public void setPeriodAndTimeout(long period, int timeout) {
        if (period <= 0)
            period = DEFAULT_HEARTBEAT_PERIOD;
        if (timeout <= 0)
            timeout = DEFAULT_HEARTBEAT_TIMEOUT;
        mPeriod = period;
        mTimeout = timeout;
    }

    public long getPeriod() {
        return mPeriod;
    }

    public long getTimeout() {
        return mTimeout;
    }

    public HeartbeatTask(Handler handler) {
        mHandlerWeakReference = new WeakReference<>(handler);
    }

    public void resetTimeoutCount() {
        mTimeoutCount = 0;
    }

    @Override
    public void run() {
        super.run();

        isHeartbeatTaskRunning = true;
        mTimeoutCount = 0;
        Dlog.w(tag, "HeartbeatTask: start" );
        while (isHeartbeatTaskRunning) {
           ClientManager.getClient().tryToKeepAlive(new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code != SEND_SUCCESS) {
                        Dbug.e(tag, "Send failed!!!");
                    }
                }
            });
            //Dlog.i(tag, "HeartbeatTask: 4="+sPeriod);
            SystemClock.sleep(mPeriod);
//            Dlog.i(tag, "HeartbeatTask: mTimeoutCount=" + mTimeoutCount);
            mTimeoutCount++;
            if (mTimeoutCount > mTimeout) { // limit time
                isHeartbeatTaskRunning = false;
                if (mHandlerWeakReference.get() != null) {
                    mHandlerWeakReference.get().obtainMessage(CommunicationService.MSG_HEARTBEAT_CONNECTION_TIMEOUT)
                            .sendToTarget();
                }
                Dlog.e(tag, "HeartbeatTask: over time" );
                break;
            }
        }
        Dlog.i(tag, "HeartbeatTask ending..." + mTimeoutCount);
    }
}
