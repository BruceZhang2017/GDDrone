package com.jieli.stream.dv.gdxxx.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LruCache;

import com.jieli.lib.dv.control.utils.Dlog;
import com.jieli.media.codec.FrameCodec;
import com.jieli.media.codec.bean.MediaMeta;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.interfaces.OnAviThumbListener;
import com.jieli.stream.dv.gdxxx.ui.a;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ResponseBody;

import static com.jieli.stream.dv.gdxxx.util.IConstant.DEFAULT_PATH;
import static com.jieli.stream.dv.gdxxx.util.IConstant.DIR_DOWNLOAD;
import static com.jieli.stream.dv.gdxxx.util.IConstant.DIR_RECORD;
import static com.jieli.stream.dv.gdxxx.util.IConstant.DIR_THUMB;

/**
 * 缩略图加载器
 *
 * @author zqjasonZhong
 *         date : 2017/5/24
 */
public class ThumbLoader {
    private static String TAG = "ThumbLoader";
    private static ThumbLoader instance;
    //缩略图缓存，key : 图片地址  value : 缩略图
    private LruCache<String, Bitmap> mThumbnailCache;
    private Map<String, Integer> mDurationMap;

    private ThumbLoader() {
        // 获取当前进程的可用内存（单位KB）
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 缓存大小为当前进程可用内存的1/8
        int cacheSize = maxMemory / 8;
        //设置缓存空间的大小
        mThumbnailCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return sizeOfBitmap(value);
            }
        };

        mDurationMap = new HashMap<>();
    }

    public static ThumbLoader getInstance() {
        if (instance == null) {
            synchronized (ThumbLoader.class) {
                if (instance == null) {
                    instance = new ThumbLoader();
                }
            }
        }
        return instance;
    }

    /**
     * 获取bitmap内存，单位KB
     */
    private int sizeOfBitmap(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount() / 1024;
        } else {
            return bitmap.getByteCount() / 1024;
        }
    }

    /**
     * 加载网络缩略图
     *
     * @param context  上下文
     * @param url      图片网络地址
     * @param width    缩略图的宽
     * @param height   缩略图的高
     * @param listener 下载监听器
     */
    public void loadWebThumbnail(@NonNull final Context context, final String url, final int width,
                                 final int height, final OnLoadThumbListener listener) {
        Bitmap bitmap;
        final Handler handler = new Handler(context.getMainLooper());
        if (TextUtils.isEmpty(url)) {
            bitmap = mThumbnailCache.get(DEFAULT_PATH);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_default_picture);
                mThumbnailCache.put(DEFAULT_PATH, bitmap);
            }
            handler.post(new OnCompleteRunnable(bitmap, listener));
        } else {
            bitmap = mThumbnailCache.get(url);
            if (bitmap == null) {
                HttpManager.downloadFile(url, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Dlog.e(TAG, "-loadThumbnail- error = " + e.getMessage() + ", url = " + url);
                        loadWebThumbnail(context, null, width, height, listener);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                        if (response.code() == 200) {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                byte[] bytes = responseBody.bytes();
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if (bmp != null) {
                                    bmp = ThumbnailUtils.extractThumbnail(bmp, width, height,
                                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                                    mThumbnailCache.put(url, bmp);
//                                    Dlog.w(TAG, "-loadThumbnail- OnCompleteRunnable ok , url : " +url);
                                    handler.post(new OnCompleteRunnable(bmp, listener));
                                } else {
                                    loadWebThumbnail(context, null, width, height, listener);
                                }
                            } else {
                                loadWebThumbnail(context, null, width, height, listener);
                            }
                        } else {
                            loadWebThumbnail(context, null, width, height, listener);
                        }
                        response.close();
                    }
                });
            } else {
//                Dlog.w(TAG, "-loadWebThumbnail- bitmap = "+bitmap+" ,url = " +url);
                handler.post(new OnCompleteRunnable(bitmap, listener));
            }
        }
    }

    /**
     * 加载网络缩略图
     *
     * @param context  上下文
     * @param url      图片网络地址
     * @param savePath 图片缓存地址
     * @param width    缩略图的宽
     * @param height   缩略图的高
     * @param listener 下载监听器
     */
    public void loadWebThumbnail(@NonNull final Context context, final String url, final String savePath, final int width,
                                 final int height, final OnLoadThumbListener listener) {
        Bitmap bitmap;
        final Handler handler = new Handler(context.getMainLooper());
        if (TextUtils.isEmpty(url)) {
            bitmap = mThumbnailCache.get(DEFAULT_PATH);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_default_picture);
                mThumbnailCache.put(DEFAULT_PATH, bitmap);
            }
            handler.post(new OnCompleteRunnable(bitmap, listener));
        } else {
            bitmap = mThumbnailCache.get(url);
            Dlog.w(TAG, "-loadWebThumbnail- bitmap = "+bitmap+" ,url = " +url);
            if (bitmap == null) {
                HttpManager.downloadFile(url, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Dlog.e(TAG, "-loadThumbnail- error = " + e.getMessage() + ", url = " + url);
                        loadWebThumbnail(context, null, null, width, height, listener);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                        if (response.code() == 200) {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                byte[] bytes = responseBody.bytes();
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if (bmp != null) {
                                    bmp = ThumbnailUtils.extractThumbnail(bmp, width, height,
                                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                                    mThumbnailCache.put(url, bmp);
                                    if (AppUtils.bitmapToFile(bmp, savePath, 100)) {
                                        Dbug.d(TAG, "save thumdNail ok");
                                    } else {
                                        Dbug.d(TAG, "save thumdNail fail");
                                    }
                                    handler.post(new OnCompleteRunnable(bmp, listener));
                                } else {
                                    loadWebThumbnail(context, null, null, width, height, listener);
                                }
                            } else {
                                loadWebThumbnail(context, null, null, width, height, listener);
                            }
                        } else {
                            loadWebThumbnail(context, null, null, width, height, listener);
                        }
                        response.close();
                    }
                });
            } else {
                handler.post(new OnCompleteRunnable(bitmap, listener));
            }
        }
    }


    private class OnCompleteRunnable implements Runnable {
        private WeakReference<Bitmap> weakReference;
        private WeakReference<OnLoadThumbListener> listenerReference;

        OnCompleteRunnable(Bitmap bitmap, OnLoadThumbListener listener) {
            weakReference = new WeakReference<>(bitmap);
            listenerReference = new WeakReference<>(listener);
        }

        @Override
        public void run() {
            Bitmap bmp = weakReference.get();
            OnLoadThumbListener listener = listenerReference.get();
            if (bmp != null && listener != null) {
                listener.onComplete(bmp);
            }
        }
    }

    /**
     * 加载本地缩略图
     *
     * @param context 上下文
     * @param url     图片本地路径
     * @param width   缩略图的宽
     * @param height  缩略图的高
     * @return 缩略图
     */
    public Bitmap loadLocalThumbnail(@NonNull Context context, String url, final int width, final int height) {
        Bitmap bitmap;
        if (TextUtils.isEmpty(url)) {
            bitmap = mThumbnailCache.get(DEFAULT_PATH);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_default_picture);
                mThumbnailCache.put(DEFAULT_PATH, bitmap);
            }
        } else {
            bitmap = mThumbnailCache.get(url);
            if (bitmap == null) {
//                Dlog.i(TAG, "getImageThumbnail url = " + url);
                bitmap = getImageThumbnail(url, width, height);
                if (bitmap == null) {
                    bitmap = loadLocalThumbnail(context, null, width, height);
                } else {
                    mThumbnailCache.put(url, bitmap);
                }
            }
        }
        return bitmap;
    }

    /**
     * 加载本地视频缩略图
     *
     * @param context  上下文
     * @param path     文件路径
     * @param width    缩略图的宽
     * @param height   缩略图的高
     * @param listener 结果回调
     */
    public void loadLocalVideoThumb(@NonNull final Context context, final String path,
                                    final int width, final int height,
                                    final OnLoadVideoThumbListener listener) {
        Bitmap bitmap;
        final Handler handler = new Handler(context.getMainLooper());
        if (TextUtils.isEmpty(path)) {
            bitmap = mThumbnailCache.get(DEFAULT_PATH);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_default_picture);
                mThumbnailCache.put(DEFAULT_PATH, bitmap);
            }
            handler.post(new OnLoadVideoThumbRunnable(bitmap, 0, listener));
        } else {
            bitmap = mThumbnailCache.get(path);
//            Dbug.w(TAG, "-loadLocalVideoThumb- path=" + path);
            if (bitmap == null) {
                String filename = "";
                if(path.contains("/")){
                    String[] strs = path.split("/");
                    filename = strs[strs.length - 1];
                }
                if(!TextUtils.isEmpty(filename)){
                    String thumbPath = AppUtils.queryThumbPath(filename,
                            AppUtils.splicingFilePath(a.getApplication().getAppFilePath(), null, null, null));
                    if(!TextUtils.isEmpty(thumbPath)){
//                        Dbug.i(TAG, "-loadLocalVideoThumb- thumbPath=" + thumbPath);
                        bitmap = getImageThumbnail(thumbPath, width, height);
                    }
                    if(bitmap != null){
                        int duration = AppUtils.parseThumbPathForDuration(thumbPath);
                        mThumbnailCache.put(path, bitmap);
                        mDurationMap.put(path, duration);
                        handler.post(new OnLoadVideoThumbRunnable(bitmap, duration, listener));
                    }else{
                        if(checkIsAvi(path)){
                            getThumbForAvi(context, path, width, height, handler, listener);
                        }else{
                            getThumbFromMov(context, path, width, height, handler, listener);
                        }
                    }
                }else{
                    loadLocalVideoThumb(context, null, width, height, listener);
                }
            } else {
                Integer duration = mDurationMap.get(path);
                if (duration == null) {
                    duration = 0;
                }
                handler.post(new OnLoadVideoThumbRunnable(bitmap, duration, listener));
            }
        }
    }

    private String replaceFilePath(String path, int duration){
        String thumbPath = "";
        if(!TextUtils.isEmpty(path)){
            String dirPath;
            String fileName = "";
            int index = path.lastIndexOf("/");
            if(index != -1){
                dirPath = path.substring(0, index);
                fileName = path.substring(index);
            }else{
                dirPath = path;
            }
            dirPath = dirPath.replace(DIR_DOWNLOAD, DIR_THUMB);
            dirPath = dirPath.replace(DIR_RECORD, DIR_THUMB);
            File file = new File(dirPath);
            if(!file.exists()){
                file.mkdir();
            }
            if(!TextUtils.isEmpty(fileName)){
                index = fileName.lastIndexOf(".");
                if(index != -1){
                    fileName = fileName.substring(0, index);
                }
                thumbPath = dirPath + File.separator + fileName + "_" + duration +".jpg";
            }else {
                thumbPath = dirPath;
            }
        }
        return thumbPath;
    }

    private class OnLoadVideoThumbRunnable implements Runnable {
        private WeakReference<OnLoadVideoThumbListener> weakReference;
        private WeakReference<Bitmap> bitmapWeakReference;
        private int duration;

        OnLoadVideoThumbRunnable(Bitmap bitmap, int duration, OnLoadVideoThumbListener listener) {
            this.duration = duration;
            bitmapWeakReference = new WeakReference<>(bitmap);
            weakReference = new WeakReference<>(listener);
        }

        @Override
        public void run() {
            Bitmap bitmap = bitmapWeakReference.get();
            OnLoadVideoThumbListener listener = weakReference.get();
            if (bitmap != null && listener != null) {
                listener.onComplete(bitmap, duration);
            }
        }
    }

    /**
     * 下载网络图片
     *
     * @param webUrl   图片网络地址
     * @param saveUrl  保存路径
     * @param listener 下载监听器
     */
    public void downloadWebImage(@NonNull Context context, String webUrl, final String saveUrl, final OnDownloadListener listener) {
        final Handler handler = new Handler(context.getMainLooper());
        if (!TextUtils.isEmpty(webUrl) && !TextUtils.isEmpty(saveUrl)) {
            File file = new File(saveUrl);
            if (file.exists()) {
                if (file.isFile()) {
                    if (file.delete()) {
                        Dbug.w(TAG, "delete file ok!");
                    }
                }
            }
            HttpManager.downloadFile(webUrl, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Dlog.e(TAG, "-downloadWebImage- onErrorResponse = " + e.getMessage());
                    handler.post(new OnResultRunnable(false, saveUrl, listener));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (response.code() == 200) {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            byte[] bytes = responseBody.bytes();
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            if (bmp != null) {
                                if (AppUtils.bitmapToFile(bmp, saveUrl, 100)) {
                                    handler.post(new OnResultRunnable(true, saveUrl, listener));
                                }
                            }
                        }
                    }
                    response.close();
                }
            });
        }
    }

    private class OnResultRunnable implements Runnable {
        private boolean result;
        private String obj;
        private WeakReference<OnDownloadListener> weakReference;

        OnResultRunnable(boolean result, String obj, OnDownloadListener listener) {
            this.result = result;
            this.obj = obj;
            weakReference = new WeakReference<>(listener);
        }

        @Override
        public void run() {
            OnDownloadListener listener = weakReference.get();
            if (listener != null) {
                listener.onResult(result, obj);
            }
        }
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     * 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     * 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     * 用这个工具生成的图像不会被拉伸。
     *
     * @param imagePath 图像的路径
     * @param width     指定输出图像的宽度
     * @param height    指定输出图像的高度
     * @return 生成的缩略图
     */
    private Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public void addBitmap(String key, Bitmap bmp) {
        if (!TextUtils.isEmpty(key) && bmp != null) {
            if (mThumbnailCache != null) {
                mThumbnailCache.put(key, bmp);
            }
        }
    }

    public Bitmap getBitmap(String key) {
        Bitmap bitmap = null;
        if (mThumbnailCache != null) {
            bitmap = mThumbnailCache.get(key);
        }
        return bitmap;
    }

    public void removeBitmap(String key){
        Bitmap bmp;
        if(mThumbnailCache != null){
            bmp = mThumbnailCache.remove(key);
            if(bmp != null && !bmp.isRecycled()){
                bmp.recycle();
            }
        }
    }

    public interface OnLoadThumbListener {
        void onComplete(Bitmap bitmap);
    }

    public interface OnLoadVideoThumbListener {
        void onComplete(Bitmap bitmap, int duration);
    }

    public interface OnDownloadListener {
        void onResult(boolean result, String url);
    }


    public void clearCache() {
        if (mThumbnailCache != null) {
            mThumbnailCache.evictAll();
        }
        if (mDurationMap != null) {
            mDurationMap.clear();
        }
        System.gc();
    }

    public void release() {
        instance = null;
        clearCache();
    }

    private boolean checkIsAvi(String fileName){
        boolean ret = false;
        if(!TextUtils.isEmpty(fileName) && (fileName.endsWith(".AVI") || fileName.endsWith(".avi"))){
            ret = true;
        }
        return ret;
    }

    private void getThumbForAvi(final Context context, final String path, final int width, final int height,
                                final Handler handler, final OnLoadVideoThumbListener listener){
        AviThumbUtil.getRecordVideoThumb(path, new OnAviThumbListener() {
            @Override
            public void onCompleted(byte[] bytes, MediaMeta mediaMeta) {
                if(bytes != null && mediaMeta != null){
                    Dbug.w(TAG, "getThumbForAvi -onCompleted- bytes size=" + bytes.length +"\n mediaMeta : "+ mediaMeta.toString());
                    ByteArrayInputStream inputStream = null;
                    Bitmap bitmap = null;
                    try {
                        inputStream = new ByteArrayInputStream(bytes);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (bitmap != null) {
                        if (bitmap.getWidth() > width || bitmap.getHeight() > height) {
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                                    width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                            mThumbnailCache.put(path, bitmap);
                            int duration = mediaMeta.getDuration();
                            Dbug.w(TAG, "-getThumbForAvi- duration = " + duration);
                            mDurationMap.put(path, duration);
                            handler.post(new OnLoadVideoThumbRunnable(bitmap, duration, listener));
                            if(duration > 0){
                                String saveThumbPath  = replaceFilePath(path, duration);
                                Dbug.i(TAG, "-getThumbForAvi- saveThumbPath=" + saveThumbPath);
                                AppUtils.bitmapToFile(bitmap, saveThumbPath, 100);
                            }
                        }
                    } else {
                        loadLocalVideoThumb(context, null, width, height, listener);
                    }
                }
            }

            @Override
            public void onError(String msg) {
                loadLocalVideoThumb(context, null, width, height, listener);
            }
        });
    }

    private void getThumbFromMov(final Context context, final String path, final int width, final int height,
                                 final Handler handler, final OnLoadVideoThumbListener listener){
        FrameCodec mFrameCodec = new FrameCodec();
        mFrameCodec.setOnFrameCodecListener(new FrameCodec.OnFrameCodecListener() {
            @Override
            public void onCompleted(byte[] bytes, MediaMeta mediaMeta) {
                if(bytes != null && mediaMeta != null){
                    Dbug.w(TAG, "getThumbFromMov -onCompleted- bytes size=" + bytes.length +"\n mediaMeta : "+ mediaMeta.toString());
                    ByteArrayInputStream inputStream = null;
                    Bitmap bitmap = null;
                    try {
                        inputStream = new ByteArrayInputStream(bytes);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (bitmap != null) {
                        if (bitmap.getWidth() > width || bitmap.getHeight() > height) {
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                                    width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                            mThumbnailCache.put(path, bitmap);
                            int duration = mediaMeta.getDuration() / 1000; //covert to second
                            Dbug.w(TAG, "-getThumbFromMov- duration = " + duration);
                            mDurationMap.put(path, duration);
                            handler.post(new OnLoadVideoThumbRunnable(bitmap, duration, listener));
                            if(duration > 0){
                                String saveThumbPath  = replaceFilePath(path, duration);
                                Dbug.i(TAG, "-getThumbFromMov- saveThumbPath=" + saveThumbPath);
                                AppUtils.bitmapToFile(bitmap, saveThumbPath, 100);
                            }
                        }
                    } else {
                        loadLocalVideoThumb(context, null, width, height, listener);
                    }
                }
            }

            @Override
            public void onError(String s) {
                loadLocalVideoThumb(context, null, width, height, listener);
            }
        });
        try {
            mFrameCodec.convertToJPG(path);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mFrameCodec.destroy();
        }
    }
}
