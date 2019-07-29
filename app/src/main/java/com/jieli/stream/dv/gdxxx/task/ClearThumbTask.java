package com.jieli.stream.dv.gdxxx.task;

import android.text.TextUtils;

import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 清除旧的缓存
 * @author zqjasonZhong
 *         date : 2017/8/17
 */
public class ClearThumbTask extends Thread {
    private OnClearThumbTaskListener listener;
    private String appRootPath;
    private boolean isExitClear;

    public ClearThumbTask(){
        appRootPath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(), null, null, null);
    }

    public void stopClear(){
        isExitClear = true;
    }

    @Override
    public void run() {
        long thumbCacheSize = getCache();
        while(!isExitClear && thumbCacheSize >= IConstant.DEFAULT_CACHE_SIZE && !TextUtils.isEmpty(appRootPath)){
            clearOldThumbCache();
            thumbCacheSize = getCache();
        }
        if(listener != null){
            listener.onFinish();
        }
    }

    public interface OnClearThumbTaskListener{
        void onFinish();
    }

    public void setOnClearThumbTaskListener(OnClearThumbTaskListener listener){
        this.listener = listener;
    }

    /**
     * 获得应用的缓存大小
     */
    private long getCache() {
        long totalSize = 0;
        List<String> thumbPathList = AppUtils.queryThumbDirPath(appRootPath);
        if (thumbPathList != null && thumbPathList.size() > 0) {
            for (String thumbPath : thumbPathList) {
                File file = new File(thumbPath);
                if (file.exists()) {
                    try {
                        totalSize += AppUtils.getFolderSize(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return totalSize;
    }

    /**
     * 删除指定文件
     * @param path  文件路径
     * @return      结果
     */
    private boolean deleteFile(String path){
        if(!TextUtils.isEmpty(path)){
            File file = new File(path);
            if(file.exists() && file.isFile()){
                return file.delete();
            }
        }
        return false;
    }

    /**
     * 清除旧的缓存文件
     */
    private void clearOldThumbCache(){
        List<FileInfo> thumbList = AppUtils.queryThumbInfoList(appRootPath);
        if(thumbList != null){
            int listSize = thumbList.size();
            if(listSize > 0){
                int subSize = listSize / 3;
                if(subSize > 0){
                    List<FileInfo> deleteList = new ArrayList<>();
                    deleteList.addAll(thumbList.subList((listSize - subSize), listSize));
                    for (FileInfo info : deleteList){
                        if(info != null){
                            deleteFile(info.getPath());
                        }
                    }
                }
            }
        }
    }
}
