package com.jieli.stream.dv.gdxxx.bean;

import android.text.TextUtils;

/**
 * 任务回复
 * @author zqjasonZhong
 *         date : 2017/10/24
 */
public class TaskReply {
    private int result;
    private int code;
    private String msg;
    private byte[] data;

    public int getResult() {
        return result;
    }

    public TaskReply setResult(int result) {
        this.result = result;
        return this;
    }

    public int getCode() {
        return code;
    }

    public TaskReply setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public TaskReply setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public TaskReply setData(byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "{" + "\"result\": " +result + ",\n" +
                "\"code\": " + code + ",\n" +
                "\"msg\":\"" + (TextUtils.isEmpty(msg)? "" : msg) + "\",\n" +
                "\"data\":\"" + (data == null ? "" : new String(data)) + "\"}";
    }
}
