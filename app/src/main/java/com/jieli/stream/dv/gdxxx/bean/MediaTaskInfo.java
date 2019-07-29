package com.jieli.stream.dv.gdxxx.bean;

import java.io.Serializable;

/**
 * @author zqjasonZhong
 *         date : 2017/5/26
 */
public class MediaTaskInfo implements Serializable {

    private FileInfo info;
    private int op;

    public FileInfo getInfo() {
        return info;
    }

    public MediaTaskInfo setInfo(FileInfo info) {
        this.info = info;
        return this;
    }

    public int getOp() {
        return op;
    }

    public MediaTaskInfo setOp(int op) {
        this.op = op;
        return this;
    }


}
