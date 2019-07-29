package com.jieli.stream.dv.gdxxx.util;

import android.text.TextUtils;

import com.jieli.lib.dv.control.utils.ClientContext;
import com.jieli.media.codec.bean.MediaMeta;
import com.jieli.stream.dv.gdxxx.interfaces.OnAviThumbListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author zqjasonZhong
 *         date : 2017/12/15
 */
public class AviThumbUtil {
    private static final String TAG = "AviThumbUtil";
    private static OnAviThumbListener mOnAviThumbListener;

    private static void dispenseOnCompletedEvent(final byte[] data, final MediaMeta meta){
        ClientContext.post(new Runnable() {
            @Override
            public void run() {
                if(mOnAviThumbListener != null){
                    mOnAviThumbListener.onCompleted(data, meta);
                }
            }
        });
    }

    private static void dispenseOnErrorEvent(final String msg){
        ClientContext.post(new Runnable() {
            @Override
            public void run() {
                if(mOnAviThumbListener != null){
                    mOnAviThumbListener.onError(msg);
                }
            }
        });
    }

    public static void getRecordVideoThumb(String inPath, OnAviThumbListener listener){
        mOnAviThumbListener = listener;
        if(TextUtils.isEmpty(inPath)){
            Dbug.e(TAG, "getRecordVideoThumb parameter is empty!");
            dispenseOnErrorEvent("params is not be empty.");
            return;
        }
        FileInputStream fileInputStream = null;
        byte[] data = new byte[1024]; //1KB
        byte[] headData = new byte[300 * 1024]; //300KB
        long picSize = 0;
        int currentSize = 0;
        int dataLength;
        byte[] secPerFrame = new byte[4];
        byte[] allFrameCount = new byte[4];
        byte[] width = new byte[4];
        byte[] height = new byte[4];
        byte[] thumbSize = new byte[4];
        long totalFrames;
        long microSecPerFrame;
        long videoWidth;
        long videoHeight;
        long duration = 0;
        int firstThumbPos = -1;
        File recordFile = new File(inPath);
        if(recordFile.exists()){
            MediaMeta mediaMeta = new MediaMeta();
            mediaMeta.setPath(inPath);
            try{
                fileInputStream = new FileInputStream(recordFile);
                while ((dataLength = fileInputStream.read(data)) != -1) {
                    if ((currentSize + dataLength) <= headData.length) {
                        System.arraycopy(data, 0, headData, currentSize, dataLength);
                    }
                    currentSize += dataLength;
                    if (currentSize < (headData.length - 1024)) {
                        if (currentSize >= (30 * 1024)) {
                            if (picSize == 0) {
                                System.arraycopy(headData, 32, secPerFrame, 0, 4);    // rate
                                System.arraycopy(headData, 48, allFrameCount, 0, 4);  // frame Count
                                System.arraycopy(headData, 64, width, 0, 4);          // width
                                System.arraycopy(headData, 68, height, 0, 4);         // height

                                totalFrames = getLong(allFrameCount, true);
                                microSecPerFrame = getLong(secPerFrame, true);
                                videoWidth = getLong(width, true);
                                videoHeight = getLong(height, true);
                                if (microSecPerFrame > 0) {
                                    duration = Math.round(totalFrames / (1000000.0f / microSecPerFrame));
                                    /*if ((1000000 / microSecPerFrame) > 0) {
                                        if ((totalFrames % (1000000 / microSecPerFrame)) == 0) {
                                            duration = totalFrames / (1000000 / microSecPerFrame);
                                        } else {
                                            duration = totalFrames / (1000000 / microSecPerFrame);// + 1;
                                        }
                                    }*/
                                }
                                mediaMeta.setWidth((int)videoWidth);
                                mediaMeta.setHeight((int)videoHeight);
                                mediaMeta.setDuration((int)duration);

                                for (int i = 3; i < headData.length; i++){
                                    if (headData[i-3] == 0x30 && headData[i-2] == 0x30 && headData[i-1] == 0x64 && headData[i] == 0x63){
                                        firstThumbPos = i + 1;
                                        break;
                                    }
                                }
                                if(-1 != firstThumbPos){
                                    System.arraycopy(headData, firstThumbPos, thumbSize, 0, 4);
                                    picSize = getLong(thumbSize, true);
                                }
                                Dbug.w(TAG, "getRecordVideoThumb firstThumbPos ==> " + firstThumbPos);
                                Dbug.w(TAG, "getRecordVideoThumb thumbSize ==> " + picSize);
                                Dbug.w(TAG, "getRecordVideoThumb allFrameCount ==> " + totalFrames);
                                Dbug.w(TAG, "getRecordVideoThumb secPerFrame ==> " + microSecPerFrame);
                                Dbug.w(TAG, "getRecordVideoThumb duration =====> " + duration);
                                Dbug.w(TAG, "getRecordVideoThumb width ==> "+ videoWidth);
                                Dbug.w(TAG, "getRecordVideoThumb height ==> " + videoHeight);
                                if (picSize == 0 || duration == 0) {
                                    dispenseOnErrorEvent("thumbnail is null.");
                                    break;
                                }
                            }
                        }
                        if (picSize > 0 && (currentSize >= (firstThumbPos + picSize + 1024))) {
                            byte[] thumbData = new byte[(int) picSize];
                            if (picSize + firstThumbPos + 4 <= headData.length) {
                                System.arraycopy(headData, firstThumbPos + 4, thumbData, 0, thumbData.length);
                                dispenseOnCompletedEvent(thumbData, mediaMeta);
                            }else {
                                dispenseOnErrorEvent("thumbnail data is not enough.");
                            }
                            break;
                        }
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
                dispenseOnErrorEvent(e.getMessage());
            }finally {
                try{
                    if(fileInputStream != null){
                        fileInputStream.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /* byte[] -> long */
    public static long getLong(byte[] buf, boolean asc) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 8) {
            throw new IllegalArgumentException("byte array size > 8 !");
        }
        long r = 0;
        if (asc)
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x00000000000000ff);
            }
        else
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0x00000000000000ff);
            }
        return r;
    }
}
