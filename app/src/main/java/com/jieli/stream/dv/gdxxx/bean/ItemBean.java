package com.jieli.stream.dv.gdxxx.bean;

import java.util.List;

/**
 * @author zqjasonZhong
 *         date : 2017/6/20
 */
public class ItemBean {
    private String data;
    private List<FileInfo> infoList;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<FileInfo> getInfoList() {
        return infoList;
    }

    public void setInfoList(List<FileInfo> infoList) {
        this.infoList = infoList;
    }
}
