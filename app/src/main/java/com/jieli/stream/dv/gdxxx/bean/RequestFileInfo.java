package com.jieli.stream.dv.gdxxx.bean;

/**
 * Created by 陈森华 on 2017/9/4.
 * 功能：用一句话描述
 */

public class RequestFileInfo {
    private FileInfo fileInfo;
    private boolean isContent;

    public RequestFileInfo(FileInfo fileInfo, boolean isContent) {
        this.fileInfo = fileInfo;
        this.isContent = isContent;
    }

    public RequestFileInfo() {
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public boolean isContent() {
        return isContent;
    }

    public void setContent(boolean content) {
        isContent = content;
    }
}