package com.jieli.stream.dv.gdxxx.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LruCache;

import com.jieli.stream.dv.gdxxx.R;

import static com.jieli.stream.dv.gdxxx.util.IConstant.DEFAULT_PATH;

/**
 * @author zqjasonZhong
 *         date : 2017/7/22
 */
public class ImageLoader {
    private static String TAG = "ImageLoader";
    private static ImageLoader instance;
    //缩略图缓存，key : 图片地址  value : 缩略图
    private LruCache<String, Bitmap> mImageCache;

    public ImageLoader(){
        // 获取当前进程的可用内存（单位KB）
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 缓存大小为当前进程可用内存的1/8
        int cacheSize = maxMemory / 8;
        //设置缓存空间的大小
        mImageCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return sizeOfBitmap(value);
            }
        };
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

    public static ImageLoader getInstance(){
        if(instance == null){
            synchronized (ImageLoader.class){
                if(instance == null){
                    instance = new ImageLoader();
                }
            }
        }
        return instance;
    }

    /**
     * 加载本地图片
     * @param context  上下文
     * @param path      图片路径
     */
    public Bitmap loadImage(@NonNull Context context, String path){
        Bitmap bitmap;
        if(TextUtils.isEmpty(path)){
            bitmap = mImageCache.get(DEFAULT_PATH);
            if(bitmap == null){
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_default_picture);
                mImageCache.put(DEFAULT_PATH, bitmap);
            }
        }else{
            bitmap = mImageCache.get(path);
            if(bitmap == null){
                bitmap = BitmapFactory.decodeFile(path);
                if(bitmap == null){
                    bitmap = loadImage(context, null);
                }
            }
        }
        return bitmap;
    }

    public void clearCache(){
        if(mImageCache != null){
            mImageCache.evictAll();
        }
        System.gc();
    }

    public void release(){
        instance = null;
        clearCache();
    }
}
