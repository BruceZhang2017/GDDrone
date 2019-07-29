package com.jieli.stream.dv.gdxxx.ui.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.jieli.lib.dv.control.projection.UDPSocketManager;
import com.jieli.lib.dv.control.projection.beans.StreamType;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenShotService extends Service {
    private String TAG = "ScreenShotService";
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;
    private static Intent mResultData = null;

    private UDPSocketManager udpSocketManager;
    private PowerManager.WakeLock wakeLock;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mScreenOrientation;

    private static final int MSG_START_SCREEN_TASK = 0x003;
    private static final int MSG_STOP_SCREEN_TASK = 0x004;
    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            if(msg != null){
                switch (msg.what){
                    case MSG_START_SCREEN_TASK:
                        startTask();
                        break;
                    case MSG_STOP_SCREEN_TASK:
                        stopTask();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    public ScreenShotService() {
    }

    public static Intent getResultData() {
        return mResultData;
    }

    public static void setResultData(Intent mResultData) {
        ScreenShotService.mResultData = mResultData;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "ScreenShotService");
        wakeLock.setReferenceCounted(false);
        initParams();
        udpSocketManager = UDPSocketManager.getInstance(ClientManager.getClient().getConnectedIP());
        udpSocketManager.setOnSocketErrorListener(new UDPSocketManager.OnSocketErrorListener() {
            @Override
            public void onError(int code) {

                Intent intent = new Intent(IActions.ACTION_PROJECTION_STATUS);
                intent.putExtra(IConstant.KEY_PROJECTION_STATUS, false);
                sendBroadcast(intent);
            }
        });
//        if (a.isOpenLeakCanary) ((a)getApplication()).getRefWatcher().watch(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            int cmd = intent.getIntExtra(IConstant.SERVICE_CMD, -1);
            switch (cmd) {
                case IConstant.SERVICE_CMD_OPEN_SCREEN_TASK:
                    if(mHandler != null){
                        mHandler.removeMessages(MSG_START_SCREEN_TASK);
                        mHandler.sendEmptyMessageDelayed(MSG_START_SCREEN_TASK, 300L);
                    }
                    break;
                case IConstant.SERVICE_CMD_CLOSE_SCREEN_TASK:
                    if(mHandler != null){
                        mHandler.removeMessages(MSG_STOP_SCREEN_TASK);
                        mHandler.sendEmptyMessageDelayed(MSG_STOP_SCREEN_TASK, 300L);
                    }
                    break;
                case IConstant.SERVICE_CMD_SCREEN_CHANGE:
                    int newOrientation = intent.getIntExtra(IConstant.SCREEN_ORIENTATION, -1);
                    Dbug.w(TAG, "newOrientation : "+ newOrientation +" , mScreenOrientation : " +mScreenOrientation);
                    if(newOrientation != -1 && newOrientation != mScreenOrientation && mResultData != null
                            && (udpSocketManager != null && udpSocketManager.isSendThreadRunning())){
                        mScreenOrientation = newOrientation;
                        resumeScreenShot();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        release();
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    /**
     * 设置屏幕参数
     */
    private void initParams(){
        WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    /**
     * 开始截屏投射
     */
    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
        if(mImageReader != null){
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = imageReader.acquireLatestImage();
                    if (image != null) {
//                        Dbug.w(TAG, "-onImageAvailable- ");
                        Bitmap bitmap = getScreenShot(image);
                        if(bitmap != null) {
                            bitmap = AppUtils.scaleImage(bitmap, 640, 480);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            if(bitmap != null){
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream); //压缩至60%
                                byte[] byteArray = stream.toByteArray();
                                if(byteArray != null){
//                                    Dbug.w(TAG, "screen shot bitmap size : " + byteArray.length);
                                    if (udpSocketManager != null) {
                                        udpSocketManager.writeData(StreamType.TYPE_JPEG, byteArray);
                                    }
                                }
                                if(!bitmap.isRecycled()){
                                    bitmap.recycle();
                                }
                            }
                        }
                    }else{
                        startScreenShot();
                    }
                }
            }, mHandler);
        }
    }

    /**
     * 启动MediaProjection
     */
    public void setUpMediaProjection() {
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        } else {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    /**
     * 开始屏幕投射
     */
    private void virtualDisplay() {
        if(mMediaProjection != null) {
            if(mImageReader == null){
                mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 2);
            }
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
            mScreenOrientation = getResources().getConfiguration().orientation;
        }
    }

    /**
     * 开始投截屏
     */
    private void startScreenShot(){
        Dbug.w(TAG, "-startScreenShot-");
        if (wakeLock != null) {
            wakeLock.acquire();
        }
        startVirtual();
    }

    /**
     * 停止截屏
     */
    private void stopScreenShot(){
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if(mImageReader != null){
            mImageReader.close();
            mImageReader = null;
        }
        stopVirtual();
        tearDownMediaProjection();
    }

    /**
     * 关闭MediaProjection
     */
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    /**
     * 停止屏幕投射
     */
    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    /**
     * 重新调整截图尺寸
     */
    private void resumeScreenShot(){
        stopScreenShot();
        SystemClock.sleep(100);
        initParams();
        startScreenShot();
    }

    /**
     * 获得投屏图像
     * @param image   Image对象
     */
    private Bitmap getScreenShot(Image image){
        if(image != null){
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            return bitmap;
        }
        return null;
    }

    /**
     * 开始投屏任务
     */
    private void startTask(){
        if(mResultData != null){
            if(!AppUtils.isAppInBackground(getApplicationContext())){ //应用不在后台，则调用home键功能
                Intent intent= new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            if(udpSocketManager != null){
                udpSocketManager.initSendThread();
            }
            startScreenShot();
        }
    }

    /**
     * 停止投屏任务
     */
    private void stopTask(){
        if(udpSocketManager != null){
            udpSocketManager.stopSendDataThread();
        }
        stopScreenShot();
    }

    /**
     * 释放资源
     */
    private void release(){
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        stopTask();
        if(udpSocketManager != null){
            udpSocketManager.release();
            udpSocketManager = null;
        }
    }

}
