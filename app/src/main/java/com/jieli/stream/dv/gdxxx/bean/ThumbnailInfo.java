package com.jieli.stream.dv.gdxxx.bean;

import android.graphics.Bitmap;

/**
 * Description:
 * Author:created by bob on 17-6-15.
 */
public class ThumbnailInfo extends FileInfo {
    private Bitmap bitmap;
    private String saveUrl;


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setSaveUrl(String saveUrl) {
        this.saveUrl = saveUrl;
    }

    public String getSaveUrl() {
        return saveUrl;
    }
}
