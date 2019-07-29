package com.jieli.stream.dv.gdxxx.task;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.utils.Dlog;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.bean.MediaTaskInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.ScanFilesHelper;
import com.jieli.stream.dv.gdxxx.util.ThumbLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

/**
 * @author zqjasonZhong
 *         date : 2017/6/27
 */
public class MediaTask extends HandlerThread implements IConstant, Handler.Callback {
    private static final String TAG = "MediaTask";
    private Handler mWorkHandler;
    private Handler mUIHandler;
    private Context mContext;
    private Call mCall;

    private boolean isCancelTask;

    private static final int MSG_START_TASK = 0x0050;
    private static final int MSG_STOP_TASK = 0x0051;
    private static final int MSG_ADD_TASK = 0x0052;
    private ScanFilesHelper scanFilesHelper;

    public MediaTask(Context context, String name) {
        super(name, Process.THREAD_PRIORITY_URGENT_AUDIO);
        this.mContext = context;
    }

    @Override
    protected void onLooperPrepared() {
        mWorkHandler = new Handler(getLooper(), this);
        super.onLooperPrepared();
    }

    public void setUIHandler(Handler handler) {
        mUIHandler = handler;
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Bundle bundle;
            switch (message.what) {
                case MSG_START_TASK:
                    bundle = message.getData();
                    if (bundle != null) {
                        MediaTaskInfo taskInfo = (MediaTaskInfo) bundle.getSerializable(MEDIA_TASK);
                        if (taskInfo != null) {
                            handlerTask(taskInfo);
                        }
                    }
//                    if (mWorkHandler != null) {
//                        Dbug.e(TAG, "MediaTask: 22=");
//                        mWorkHandler.removeMessages(MSG_START_TASK);
//                    }
                    break;
                case MSG_STOP_TASK:
                    if (mCall != null && mCall.isExecuted()) {
                        mCall.cancel();
                    }
                    isCancelTask = true;
                    if (mWorkHandler != null) {
                        mWorkHandler.removeMessages(MSG_START_TASK);
                    }
                    break;
                case MSG_ADD_TASK:
                    break;
            }
        }
        return false;
    }

    private void handlerTask(MediaTaskInfo taskInfo) {
        if (taskInfo != null) {
            FileInfo fileInfo = taskInfo.getInfo();
            int op = taskInfo.getOp();
            if (fileInfo != null) {
                switch (op) {
                    case OP_DOWNLOAD_FILES:
                        tryToDownload(fileInfo);
                        break;
                    case OP_DELETE_FILES:
                        tryToDelete(fileInfo);
                        break;
                }
            }
        }
    }

    private void tryToDownload(FileInfo fileInfo) {
        if (fileInfo == null) return;
        int source = fileInfo.getSource();
        if (source != COME_FORM_LOCAL) {
            final String filename = fileInfo.getName();
            String ip = ClientManager.getClient().getConnectedIP();
            String webUrl = AppUtils.formatUrl(ip, DEFAULT_HTTP_PORT, fileInfo.getPath());
            final String saveFilename = AppUtils.getDownloadFilename(fileInfo);
            final String saveUrl = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(),
                    AppUtils.checkCameraDir(fileInfo), DIR_DOWNLOAD) + File.separator + saveFilename;
            if (fileInfo.isVideo()) {
                File file = new File(saveUrl);
                if (file.exists()) {
                    if (file.length() >= fileInfo.getSize()) {
                        if (mUIHandler != null) {
                            Message message = mUIHandler.obtainMessage();
                            message.what = RESULT_DOWNLOAD_FILE;
                            message.arg1 = RESULT_FILE_EXIST;
                            message.obj = saveUrl;
                            mUIHandler.sendMessage(message);
                        }
                    } else {
                        if (file.delete()) {
                            tryToDownload(fileInfo);
                        } else {
                            if (mUIHandler != null) {
                                Message message = mUIHandler.obtainMessage();
                                message.what = RESULT_DOWNLOAD_FILE;
                                message.arg1 = RESULT_FALSE;
                                message.obj = filename;
                                mUIHandler.sendMessage(message);
                            }
                        }
                    }
                } else {
                    Request request = new Request.Builder()
                            .url(webUrl).build();
                    mCall = new OkHttpClient().newBuilder()
                            .writeTimeout(50, TimeUnit.SECONDS)
                            .build().newCall(request);
                    final long total = fileInfo.getSize();
                    mCall.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Dbug.e(TAG, "onFailure ~~~~~11111111111111111");
                            File file = new File(saveUrl);
                            if (file.exists()) {
                                if (file.delete()) {
                                    Dbug.w(TAG, "download file fail, delete file success!");
                                }
                            }
                            if (mUIHandler != null) {
                                Message message = mUIHandler.obtainMessage();
                                message.what = RESULT_DOWNLOAD_FILE;
                                message.arg1 = RESULT_FALSE;
                                message.obj = filename;
                                mUIHandler.sendMessage(message);
                            }
                            mCall = null;
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            Dbug.e(TAG, "onResponse ~~~~~22222222222222");
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                InputStream is = null;
                                FileOutputStream fos = null;
                                byte[] buf = new byte[4 * 1024];
                                int len;
                                long current = 0;
                                try {
                                    int progress;
                                    int lastProgress = -1;
                                    is = responseBody.byteStream();
                                    fos = new FileOutputStream(new File(saveUrl));
                                    while ((len = is.read(buf)) != -1) {
                                        current += len;
                                        fos.write(buf, 0, len);
                                        progress = (int) ((current * 100) / total);
                                        if (progress != lastProgress) {
                                            lastProgress = progress;
                                            if (mUIHandler != null) {
//                                                Dbug.e(TAG, "onResponse progress = "+ progress);
                                                Message message = mUIHandler.obtainMessage();
                                                message.what = DOWNLOAD_FILE_PROGRESS;
                                                message.arg1 = progress;
                                                message.obj = filename;
                                                mUIHandler.sendMessage(message);
                                            }
                                        }
                                    }
                                    fos.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    File file = new File(saveUrl);
                                    if (file.exists()) {
                                        if (file.delete()) {
                                            Dbug.w(TAG, "download file fail, delete file success!");
                                        }
                                    }
                                } finally {
                                    try {
                                        if (is != null) {
                                            is.close();
                                        }
                                        if (fos != null) {
                                            fos.close();
                                        }
                                    } catch (IOException e) {
                                        Dlog.e(TAG, e.toString());
                                    }
                                    if (current >= total) {
                                        if (isCancelTask) {
                                            isCancelTask = false;
                                            File file = new File(saveUrl);
                                            if (file.exists()) {
                                                if (file.delete()) {
                                                    Dbug.w(TAG, "download file fail, delete file success!");
                                                }
                                            }
                                        }
                                        if (mUIHandler != null) {
                                            Message message = mUIHandler.obtainMessage();
                                            message.what = RESULT_DOWNLOAD_FILE;
                                            message.arg1 = RESULT_SUCCESS;
                                            message.obj = saveUrl;
                                            mUIHandler.sendMessage(message);
                                            if (scanFilesHelper == null)
                                                scanFilesHelper = new ScanFilesHelper(mContext);
                                            scanFilesHelper.scanFiles(saveUrl);
                                        }
                                    } else {
                                        File file = new File(saveUrl);
                                        if (file.exists()) {
                                            if (file.delete()) {
                                                Dbug.w(TAG, "download file fail, delete file success!");
                                            }
                                        }
                                        if (mUIHandler != null) {
                                            Message message = mUIHandler.obtainMessage();
                                            message.what = RESULT_DOWNLOAD_FILE;
                                            message.arg1 = RESULT_CANCEL;
                                            message.obj = filename;
                                            mUIHandler.sendMessage(message);
                                        }
                                    }
                                    mCall = null;
                                }
                            }
                        }
                    });
                }
            } else {
                File file = new File(saveUrl);
                if (file.exists()) {
                    if (file.length() >= fileInfo.getSize()) {
                        if (mUIHandler != null) {
                            Message message = mUIHandler.obtainMessage();
                            message.what = RESULT_DOWNLOAD_FILE;
                            message.arg1 = RESULT_FILE_EXIST;
                            message.obj = saveUrl;
                            mUIHandler.sendMessage(message);
                        }
                    } else {
                        if (file.delete()) {
                            tryToDownload(fileInfo);
                        } else {
                            if (mUIHandler != null) {
                                Message message = mUIHandler.obtainMessage();
                                message.what = RESULT_DOWNLOAD_FILE;
                                message.arg1 = RESULT_FALSE;
                                message.obj = filename;
                                mUIHandler.sendMessage(message);
                            }
                        }
                    }
                } else {
                    ThumbLoader.getInstance().downloadWebImage(mContext, webUrl, saveUrl, new ThumbLoader.OnDownloadListener() {
                        @Override
                        public void onResult(boolean result, String url) {
                            if (result) {
                                if (isCancelTask) {
                                    isCancelTask = false;
                                    File file = new File(saveUrl);
                                    if (file.exists()) {
                                        if (file.delete()) {
                                            Dbug.w(TAG, "download image failed, delete file success!");
                                        }
                                    }
                                }
                                if (mUIHandler != null) {
                                    Message message = mUIHandler.obtainMessage();
                                    message.what = RESULT_DOWNLOAD_FILE;
                                    message.arg1 = RESULT_SUCCESS;
                                    message.obj = saveUrl;
                                    mUIHandler.sendMessage(message);
                                    if (scanFilesHelper == null)
                                        scanFilesHelper = new ScanFilesHelper(mContext);
                                    scanFilesHelper.scanFiles(saveUrl);
                                }
                            } else {
                                File file = new File(saveUrl);
                                if (file.exists()) {
                                    if (file.delete()) {
                                        Dbug.w(TAG, "download image failed, delete file success!");
                                    }
                                }
                                if (mUIHandler != null) {
                                    Message message = mUIHandler.obtainMessage();
                                    message.what = RESULT_DOWNLOAD_FILE;
                                    message.arg1 = RESULT_FALSE;
                                    message.obj = filename;
                                    mUIHandler.sendMessage(message);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private void tryToDelete(final FileInfo fileInfo) {
        if (fileInfo == null) return;
        int source = fileInfo.getSource();
        if (source == COME_FORM_LOCAL) {
            String filePath = fileInfo.getPath();
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    if (mUIHandler != null) {
                        Message message = mUIHandler.obtainMessage();
                        message.what = RESULT_DELETE_FILE;
                        message.arg1 = RESULT_SUCCESS;
                        message.obj = fileInfo.getName();
                        mUIHandler.sendMessage(message);
                        String saveUrl = fileInfo.getPath();
                        if (scanFilesHelper == null)
                            scanFilesHelper = new ScanFilesHelper(mContext);
                        scanFilesHelper.updateToDeleteFile(saveUrl);
                    }
                } else {
                    if (mUIHandler != null) {
                        Message message = mUIHandler.obtainMessage();
                        message.what = RESULT_DELETE_FILE;
                        message.arg1 = RESULT_FALSE;
                        message.obj = fileInfo.getName();
                        mUIHandler.sendMessage(message);
                    }
                }
            } else {
                if (mUIHandler != null) {
                    Message message = mUIHandler.obtainMessage();
                    message.what = RESULT_DELETE_FILE;
                    message.arg1 = RESULT_FALSE;
                    message.obj = fileInfo.getName();
                    mUIHandler.sendMessage(message);
                }
            }
        } else {
            List<String> params = new ArrayList<>();
            params.add(fileInfo.getPath());
            ClientManager.getClient().tryToDeleteFile(params, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code == SEND_SUCCESS) {
                        if (mUIHandler != null) {
                            Message message = mUIHandler.obtainMessage();
                            message.what = RESULT_DELETE_FILE;
                            message.arg1 = RESULT_SUCCESS;
                            message.obj = fileInfo.getName();
                            mUIHandler.sendMessage(message);
                            String saveUrl = fileInfo.getPath();
                            if (scanFilesHelper == null)
                                scanFilesHelper = new ScanFilesHelper(mContext);
                            scanFilesHelper.updateToDeleteFile(saveUrl);
                        }
                    } else {
                        if (mUIHandler != null) {
                            Message message = mUIHandler.obtainMessage();
                            message.what = RESULT_DELETE_FILE;
                            message.arg1 = RESULT_FALSE;
                            message.obj = fileInfo.getName();
                            mUIHandler.sendMessage(message);
                        }
                    }
                }
            });
        }
    }

    public void tryToStartTask(MediaTaskInfo taskInfo) {
        if (mWorkHandler != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(MEDIA_TASK, taskInfo);
            Message message = mWorkHandler.obtainMessage();
            message.what = MSG_START_TASK;
            message.setData(bundle);
            mWorkHandler.sendMessage(message);
        }
    }

    public void tryToStopTask() {
        if (mWorkHandler != null) {
            mWorkHandler.sendEmptyMessage(MSG_STOP_TASK);
        }
    }

    public void release(){
        mContext = null;
        if(mWorkHandler != null){
            mWorkHandler.removeCallbacksAndMessages(null);
        }
        if(mUIHandler != null){
            mUIHandler.removeCallbacksAndMessages(null);
        }
        System.gc();
    }

}
