package com.jieli.stream.dv.gdxxx.data;

/**
 * Description:
 * Author:created by bob on 17-11-22.
 */
public interface OnRecordStateListener {
    void onPrepared();
    void onStop();
    void onError(String message);
}
