package com.jieli.stream.dv.gdxxx.util;


import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * okHttp辅助类
 * @author zqjasonZhong
 *         date : 2017/7/1
 */
public class HttpManager {

    /**
     * 下载文件
     * @param url        文件下载路径
     * @param callback   请求回调
     */
    public static void downloadFile(String url, Callback callback){
        if (TextUtils.isEmpty(url)) {
            Dbug.e("downloadFile", "url is null");
            return;
        }
        Request request =  new Request.Builder()
                .url(url).build();
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger());
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Call mCall = new OkHttpClient().newBuilder()
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(logInterceptor)
                .build().newCall(request);

        mCall.enqueue(callback);
    }

    /**
     * 上传文件
     * @param url        上传路径
     * @param filePath   文件路径
     * @param callback   请求回调
     */
    public static void uploadFile(String url, String filePath, Callback callback){
        if(!TextUtils.isEmpty(url) && !TextUtils.isEmpty(filePath)){
            File file = new File(filePath);
            if(file.exists() && file.isFile()){
                String requestUrl  = AppUtils.formatUrl(ClientManager.getClient().getConnectedIP(), IConstant.DEFAULT_HTTP_PORT, url);
                if(!TextUtils.isEmpty(requestUrl)){
                    HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger());
                    logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    RequestBody body = RequestBody.create(MediaType.parse(getMimeType(requestUrl)), file);
                    Request request = new Request.Builder().url(requestUrl).post(body).build();
                    Call call = new OkHttpClient().newBuilder()
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .addInterceptor(logInterceptor)
                            .build().newCall(request);
                    call.enqueue(callback);
                }
            }
        }
    }

    /**
     * 获得MimeType
     * @param url  请求路径
     */
    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
