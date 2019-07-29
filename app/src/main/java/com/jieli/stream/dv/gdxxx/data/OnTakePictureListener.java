package com.jieli.stream.dv.gdxxx.data;

/**
 * Description:
 * Author:created by admin on 19-1-1.
 */
public interface OnTakePictureListener {
    void onCompleted(String sFile);
    void onFailed(String sFile,String sResult);
}
