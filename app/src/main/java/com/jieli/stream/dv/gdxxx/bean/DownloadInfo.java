package com.jieli.stream.dv.gdxxx.bean;

import java.io.Serializable;

/**
 * Description:
 * Author:created by bob on 17-9-29.
 */
public class DownloadInfo implements Serializable {
    private String path;
    private int duration;
    private int offset;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
