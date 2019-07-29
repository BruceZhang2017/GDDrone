package com.jieli.stream.dv.gdxxx.util;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * OkHttp 自定义打印类
 * @author zqjasonZhong
 *         date : 2017/10/11
 */
public class HttpLogger implements HttpLoggingInterceptor.Logger{

    private static boolean isLog;

    public static void setLog(boolean log){
        isLog = log;
    }

    @Override
    public void log(String message) {
        if(isLog){
            Dbug.i("okHttp", message);
        }
    }
}
