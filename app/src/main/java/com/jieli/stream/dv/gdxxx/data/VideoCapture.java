package com.jieli.stream.dv.gdxxx.data;

import android.os.Handler;
import android.text.TextUtils;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.media.codec.FrameCodec;
import com.jieli.media.codec.bean.MediaMeta;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.io.File;

/**
 * Description: Capture keyframe of playing stream and code as JPEG.
 * Author:created by bob on 17-11-24.
 */
public class VideoCapture {
    private final String tag = getClass().getSimpleName();
    private FrameCodec mFrameCodec = null;
    private OnVideoCaptureListener mOnCaptureListener = null;
    private int mRetryTime=0;
    private static volatile boolean isReady = true;
    private MyHandler mHandler;

    private static class MyHandler extends Handler {
    }

    public VideoCapture () {
        mHandler = new MyHandler();
    }

    public void setOnCaptureListener(OnVideoCaptureListener listener) {
        mOnCaptureListener = listener;
    }
    /**
     * 判断帧类型并保存成图片
     * @param data    帧数据
     */
    public void capture(byte[] data) {
        if(data == null) return;
        //Dbug.e(tag, "Capture retry:" + (++mRetryTime));
        if(a.getApplication().getDeviceDesc().getVideoType() == DeviceClient.RTS_JPEG) {
            //Dbug.e(tag, "Capture success. retry:" + mRetryTime);
            if (!isReady) return;
            isReady = false;
            if (mOnCaptureListener != null) {
                isReady = true;
                mOnCaptureListener.onCompleted();
            }
            String outPath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(),
                    a.getApplication().getCameraDir(), IConstant.DIR_DOWNLOAD) + File.separator + AppUtils.getLocalPhotoName();
            if (!TextUtils.isEmpty(outPath)) {
                AppUtils.bytesToFile(data, outPath);
            }
        }else {
            if (AppUtils.checkFrameType(data) == IConstant.FRAME_TYPE_I) {//只能I帧拍？
                mRetryTime = 0;
                if (mOnCaptureListener != null) {
                    mOnCaptureListener.onCompleted();
                }
                if (mFrameCodec == null) {
                    mFrameCodec = new FrameCodec();
                    mFrameCodec.setOnFrameCodecListener(mOnFrameCodecListener);
                }
                int currentLevel = AppUtils.getStreamResolutionLevel();
                int[] rtsResolution = AppUtils.getRtsResolution(currentLevel);
                //Dbug.e(tag, "Capture success. retry:" + mRetryTime + ", " + rtsResolution[0] + ", " + rtsResolution[1]);
                mFrameCodec.convertToJPG(data, rtsResolution[0], rtsResolution[1], null);
            } else {
                mRetryTime++;
                //Dbug.w(tag, "Capture failed. retry:" + mRetryTime);
                if (mRetryTime > 90 && mOnCaptureListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mOnCaptureListener.onFailed();
                        }
                    });
                    mRetryTime = 0;
                }
            }
        }

    }

    /**
     * 释放资源
     */
    public void destroy() {
        if (mFrameCodec != null) {
            mFrameCodec.destroy();
            mFrameCodec.setOnFrameCodecListener(null);
            mFrameCodec = null;
        }
        mOnCaptureListener = null;
    }

    /**
     * 转码成JPG的回调处理
     */
    private final FrameCodec.OnFrameCodecListener mOnFrameCodecListener = new FrameCodec.OnFrameCodecListener() {
        @Override
        public void onCompleted(byte[] data, MediaMeta mediaMeta) {
            boolean result = false;
            String outPath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(),
                    a.getApplication().getCameraDir(), IConstant.DIR_DOWNLOAD) + File.separator + AppUtils.getLocalPhotoName();
            if(data != null && !TextUtils.isEmpty(outPath)){
                result = AppUtils.bytesToFile(data, outPath);
            }
            Dbug.w(tag, "result " + result + ", outPath " + outPath);
        }

        @Override
        public void onError(String s) {
            Dbug.e(tag, "Codec error:" + s);
            if (mOnCaptureListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOnCaptureListener.onFailed();
                    }
                });
            }
        }
    };
}
