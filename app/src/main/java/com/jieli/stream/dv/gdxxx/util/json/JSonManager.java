package com.jieli.stream.dv.gdxxx.util.json;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.jieli.lib.dv.control.utils.Dlog;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.util.json.listener.OnCompletedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Description:
 * Author:created by bob on 17-5-22.
 */
public class JSonManager {
    private static String tag = "JSonManager";
    private final static String TXT_FILE_LIST = "file_list";
    private final static String TXT_TYPE = "y";
    private final static String TXT_PATH = "f";
    private final static String TXT_CREATE_TIME = "t";
    private final static String TXT_DURATION = "d";
    private final static String TXT_SIZE = "s";
    private final static String TXT_WIDTH = "w";
    private final static String TXT_HEIGHT = "h";
    private final static String TXT_RATE = "p";
    private final static String TXT_CAMERA_TYPE = "c";
    private Handler mHandler;
    private List<FileInfo> mList = new ArrayList<>();
    private static JSonManager instance = null;
    private static String mJSonData;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());

    private JSonManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static JSonManager getInstance() {
        if (instance == null) {
            instance = new JSonManager();
        }

        return instance;
    }

    public void parseJSonData(final String jsonData, final OnCompletedListener<Boolean> listener) {
        if (!TextUtils.isEmpty(jsonData)) {
            mJSonData = jsonData;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    parseJSon(listener);
                }
            });
            thread.start();
        } else {
            dispatchParseJSonState(listener, false);
        }
    }

    private synchronized void parseJSon(final OnCompletedListener<Boolean> listener) {
        if (TextUtils.isEmpty(mJSonData)) {
            throw new NullPointerException("JSon data is null");
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(mJSonData);
        } catch (JSONException e) {
            dispatchParseJSonState(listener, false);
            e.printStackTrace();
        }

        if (jsonObject == null) {
            throw new IllegalArgumentException("The data object maybe not JSON:"+mJSonData);
        }

        Iterator iterator = jsonObject.keys();
        JSONArray jsonArray = null;
        while (iterator.hasNext()) {
            if (iterator.next().equals(TXT_FILE_LIST)) {
                try {
                    jsonArray = jsonObject.getJSONArray(TXT_FILE_LIST);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        if (jsonArray == null) {
            dispatchParseJSonState(listener, false);
            return;
        }

        JSONObject infoObject = null;
        String path;
        String type;

        List<FileInfo> fileInfos = new ArrayList<>();
       /* if (mList.size() > 0) {
            mList.clear();
        }*/
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                infoObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (infoObject == null) {
                continue;
            }
            path = infoObject.optString(TXT_PATH);
            FileInfo fileInfo;
            if (!TextUtils.isEmpty(path)) {
                type = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
                //Dbug.w(tag, "===type=" + type);
                fileInfo = new FileInfo();
                switch (type) {
                    case "jpeg":
                    case "jpg":
                        fileInfo.setVideo(false);
                        break;
                    case "mov":
                    case "avi":
                        fileInfo.setVideo(true);
                        break;
                    default:
                        Dlog.e(tag, "error:" + type);
                        dispatchParseJSonState(listener, false);
                        return;
                }
                fileInfo.setType(infoObject.optInt(TXT_TYPE));
                fileInfo.setDuration(infoObject.optInt(TXT_DURATION));
                fileInfo.setHeight(infoObject.optInt(TXT_HEIGHT));
                fileInfo.setWidth(infoObject.optInt(TXT_WIDTH));
                fileInfo.setRate(jsonObject.optInt(TXT_RATE));
                fileInfo.setName(path.substring(path.lastIndexOf("/") + 1));
                fileInfo.setPath(infoObject.optString(TXT_PATH));
                fileInfo.setCreateTime(infoObject.optString(TXT_CREATE_TIME));
                fileInfo.setSize(infoObject.optLong(TXT_SIZE));
                if(infoObject.has(TXT_CAMERA_TYPE)){
                    fileInfo.setCameraType(infoObject.optString(TXT_CAMERA_TYPE));
                }

                Date date = null;
                try {
                    date = dateFormat.parse(infoObject.optString(TXT_CREATE_TIME));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ////////Set start time
                Calendar startTime = Calendar.getInstance();
                if (date != null) {
                    startTime.setTime(date);
                    fileInfo.setStartTime(startTime);
                } else {
                    Dlog.e(tag, "Parse start time string fail!");
                }

                ////////Set end time
                Calendar endTime = Calendar.getInstance();
                if (date != null) {
                    endTime.setTime(date);
                    endTime.set(Calendar.SECOND, endTime.get(Calendar.SECOND) + fileInfo.getDuration());
                    fileInfo.setEndTime(endTime);
                } else {
                    Dlog.e(tag, "Parse end time string fail!");
                }
                fileInfos.add(fileInfo);
            } else {
                Dlog.e(tag, "Invalid path received from device");
                dispatchParseJSonState(listener, false);
                return;
            }
        }
        setFileInfos(fileInfos);
        dispatchParseJSonState(listener, true);
    }

    private void setFileInfos(List<FileInfo> infos) {
        synchronized (mList) {
            mList = infos;
        }
    }

    private void dispatchParseJSonState(final OnCompletedListener<Boolean> listener, final Boolean state) {
        if (listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!state) {
                        mList.clear();
                    }
                    listener.onCompleted(state);
                }
            });
        }
    }

    public List<FileInfo> getInfoList() {
        synchronized (mList) {
            return mList;
        }
    }

    public List<FileInfo> getVideoInfoList() {
        synchronized (mList) {
            if (TextUtils.isEmpty(mJSonData)) {
                throw new NullPointerException("JSon data is null");
            }
            List<FileInfo> list = new ArrayList<>();
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).isVideo()) {
                    list.add(mList.get(i));
                }
            }
            return list;
        }
    }

    public List<FileInfo> getPictureInfoList() {
        synchronized (mList) {
            if (TextUtils.isEmpty(mJSonData)) {
                throw new NullPointerException("JSon data is null");
            }
            List<FileInfo> list = new ArrayList<>();
            for (int i = 0; i < mList.size(); i++) {
                if (!mList.get(i).isVideo()) {
                    list.add(mList.get(i));
                }
            }
            return list;
        }
    }

    /**
     * 解析json文本成为设备的文件信息
     *
     * @param jsonText json文本
     */
    public static FileInfo parseFileInfo(String jsonText) {
        if (!TextUtils.isEmpty(jsonText)) {
            try {
                JSONObject jsonObject = new JSONObject(jsonText);
                String path = jsonObject.optString(TXT_PATH);
                if (!TextUtils.isEmpty(path)) {
                    FileInfo fileInfo;
                    String type = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
                    //Dbug.w(tag, "===type=" + type);
                    fileInfo = new FileInfo();
                    switch (type) {
                        case "jpeg":
                        case "JPEG":
                        case "jpg":
                        case "JPG":
                            fileInfo.setVideo(false);
                            break;
                        case "mov":
                        case "MOV":
                        case "avi":
                            fileInfo.setVideo(true);
                            break;
                        default:
                            Dlog.e(tag, "error:" + type);
                            return null;
                    }
                    fileInfo.setType(jsonObject.optInt(TXT_TYPE));
                    fileInfo.setDuration(jsonObject.optInt(TXT_DURATION));
                    fileInfo.setHeight(jsonObject.optInt(TXT_HEIGHT));
                    fileInfo.setWidth(jsonObject.optInt(TXT_WIDTH));
                    fileInfo.setRate(jsonObject.optInt(TXT_RATE));
                    fileInfo.setName(path.substring(path.lastIndexOf("/") + 1));
                    fileInfo.setPath(jsonObject.optString(TXT_PATH));
                    fileInfo.setCreateTime(jsonObject.optString(TXT_CREATE_TIME));
                    fileInfo.setSize(jsonObject.optLong(TXT_SIZE));
                    if(jsonObject.has(TXT_CAMERA_TYPE)){
                        fileInfo.setCameraType(jsonObject.optString(TXT_CAMERA_TYPE));
                    }

                    if (fileInfo.isVideo()) {
                        Date date = null;
                        try {
                            date = dateFormat.parse(jsonObject.optString(TXT_CREATE_TIME));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        ////////Set start time
                        Calendar startTime = Calendar.getInstance();
                        if (date != null) {
                            startTime.setTime(date);
                            fileInfo.setStartTime(startTime);
                        } else {
                            Dlog.e(tag, "Parse start time string fail!");
                        }

                        ////////Set end time
                        Calendar endTime = Calendar.getInstance();
                        if (date != null) {
                            endTime.setTime(date);
                            endTime.set(Calendar.SECOND, endTime.get(Calendar.SECOND) + fileInfo.getDuration());
                            fileInfo.setEndTime(endTime);
                        } else {
                            Dlog.e(tag, "Parse end time string fail!");
                        }
                    }
                    return fileInfo;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 把文件信息转化为json文本
     *
     * @param list 设备的文件信息
     */
    public static String convertJson(List<FileInfo> list) {
        String json = "";
        if (list != null) {
            if (list.size() > 0) {
                json = "{\"" + TXT_FILE_LIST + "\": [";
                for (int i = 0; i < list.size(); i++) {
                    FileInfo info = list.get(i);
                    if (info != null) {
                        json += "{\n" +
                                "\"" + TXT_TYPE + "\" : " + info.getType() + ",\n" +
                                "\"" + TXT_PATH + "\" : \"" + info.getPath() + "\",\n" +
                                "\"" + TXT_CREATE_TIME + "\" : \"" + info.getCreateTime() + "\",\n" +
                                "\"" + TXT_DURATION + "\" : \"" + info.getDuration() + "\",\n" +
                                "\"" + TXT_HEIGHT + "\" : " + info.getHeight() + ",\n" +
                                "\"" + TXT_WIDTH + "\" : " + info.getWidth() + ",\n" +
                                "\"" + TXT_RATE + "\" : " + info.getRate() + ",\n" +
                                "\"" + TXT_SIZE + "\" : \"" + info.getSize() + "\"\n" +
                                "}";
                        if (i != (list.size() - 1)) {
                            json += ",\n";
                        }
                    }
                }
                json += "\n]}";
            } else {
                json = "{\"" + TXT_FILE_LIST + "\": []}";
            }
        }
        mJSonData = json;
        return mJSonData;
    }

    public String getVideosDescription() {
        return mJSonData;
    }

    public void release() {
        instance = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        clearData();
    }

    public void clearData() {
        mJSonData = null;
        if (mList != null) {
            mList.clear();
        }
    }
}
