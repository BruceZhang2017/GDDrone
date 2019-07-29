package com.jieli.stream.dv.gdxxx.bean;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Description:
 * Author:created by bob on 17-5-22.
 */
public class FileInfo implements Serializable, Cloneable{
    private String name = "";  //File Name
    private String path = "";  //File Path
    private String createTime = "";//Creation time
    private long size;
    private boolean isVideo;
    private int duration;
    private int width;
    private int height;
    private int source;
    private int rate;
    private boolean isSelected = false;
    private int type;//0: invalid, 1:normal, 2:sos, 3:latency
    private int offset;
    private String cameraType = "0"; //0: front view video 1: rear view video
    private Calendar startTime;
    private Calendar endTime;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getCameraType() {
        return cameraType;
    }

    public void setCameraType(String cameraType) {
        this.cameraType = cameraType;
    }

    @Override
    public String toString() {
        return "[" +"name:" + name
                +", path:" + path
                +", createTime:"+createTime
                +", size:" + size
                +", isVideo:" + isVideo
                +", duration:" +duration
                +", width:"+width
                +", height:"+height
                +", rate:"+rate
                +", source:"+source
                +", isSelected:"+isSelected
                +", type:"+type
                +", offset:"+offset
                +", cameraType:"+cameraType
                +", startTime:"+(startTime != null ? startTime.toString(): null)
                +", endTime:"+(endTime != null?endTime.toString():null)
                + "]";
    }

    @Override
    public Object clone() {
        FileInfo o = null;
        try {
            o = (FileInfo) super.clone();
            o.setStartTime((Calendar) startTime.clone());
            o.setEndTime((Calendar) endTime.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }
}
