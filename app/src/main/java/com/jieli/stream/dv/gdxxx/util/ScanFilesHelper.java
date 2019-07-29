package com.jieli.stream.dv.gdxxx.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

/**
 * 通知系统扫描媒体库
 * @author zqjasonZhong
 *         date : 2017/3/30
 */
public class ScanFilesHelper implements MediaScannerConnection.MediaScannerConnectionClient{
    private Context mContext;

    private String mFilePath;
    private MediaScannerConnection mMediaScannerConnection;

//    public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";

    public ScanFilesHelper(Context context){
        this.mContext = context;
        if(mMediaScannerConnection == null){
            mMediaScannerConnection = new MediaScannerConnection(context, this);
        }
    }

    private void scanFile(String absolutePath){
        if(!TextUtils.isEmpty(absolutePath) &&
                (mMediaScannerConnection != null && mMediaScannerConnection.isConnected())){
            Dbug.e("ScanFilesHelper", "scan file absolutePath = " +absolutePath);
            File file = new File(absolutePath);
            if(file.exists()){
                if (file.isFile()) {
                    mMediaScannerConnection.scanFile(file.getAbsolutePath(), null);
                    return;
                }
                File[] files = file.listFiles();
                if (files == null) {
                    return;
                }
                for (File temp : file.listFiles()) {
                    if(temp != null){
                        scanFile(temp.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void updateToDeleteFile(String deletePath){
        MediaScannerConnection.scanFile(mContext, new String[]{deletePath},null,null);
    }

    public void scanFiles(String filePath){
        this.mFilePath = filePath;
        if(mMediaScannerConnection != null && !mMediaScannerConnection.isConnected()){
            mMediaScannerConnection.connect();
        }else{
            mMediaScannerConnection = new MediaScannerConnection(mContext, this);
            mMediaScannerConnection.connect();
        }
    }

    @Override
    public void onMediaScannerConnected() {
        if(!TextUtils.isEmpty(mFilePath)){
            scanFile(mFilePath);
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        if(mMediaScannerConnection != null){
            mMediaScannerConnection.disconnect();
        }
    }

    public void release(){
        if(mMediaScannerConnection != null){
            if(mMediaScannerConnection.isConnected()){
                mMediaScannerConnection.disconnect();
            }
            mMediaScannerConnection = null;
        }
        if(!TextUtils.isEmpty(mFilePath)){
            mFilePath = null;
        }
        if(mContext != null){
            mContext = null;
        }
    }
}
