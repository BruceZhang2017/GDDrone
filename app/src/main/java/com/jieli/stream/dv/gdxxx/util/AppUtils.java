package com.jieli.stream.dv.gdxxx.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.player.Stream;
import com.jieli.lib.dv.control.utils.TopicParam;
import com.jieli.stream.dv.gdxxx.BuildConfig;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.AppInfo;
import com.jieli.stream.dv.gdxxx.bean.DeviceDesc;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.bean.ItemBean;
import com.jieli.stream.dv.gdxxx.bean.SDFileInfo;
import com.jieli.stream.dv.gdxxx.bean.ServerInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.zh_jieli.juson.netcheck.OuterChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 工具类
 * date : 2017/2/27
 */
public class AppUtils implements IConstant {
    private static final String TAG = "AppUtils";

    /**
     * 获取图像资源引用id
     *
     * @param context   上下文
     * @param dirName   文件夹名称
     * @param imageName 图片名称
     */
    public static int getResourceId(Context context, String dirName, String imageName) {
        if (context != null && !TextUtils.isEmpty(dirName) && !TextUtils.isEmpty(imageName)) {
            return context.getResources().getIdentifier(imageName, dirName, context.getPackageName());
        }
        return 0;
    }


    /**
     * 获取屏幕分辨信息
     *
     * @param context 上下文
     * @return 数组 : 0 -- 密度，1 -- 宽度， 2 -- 高度
     */
    public static int[] getWindowParams(Context context) {
        // 获取屏幕分辨率
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int[] params = new int[3];
        params[0] = (int) dm.density;                            //density
        params[1] = (int) (dm.widthPixels * params[0] + 0.5f);    //screen width
        params[2] = (int) (dm.heightPixels * params[0] + 0.5f);   //screen height
        return params;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     */
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        Display display = windowManager.getDefaultDisplay();
        return display.getWidth();
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        Display display = windowManager.getDefaultDisplay();
        return display.getHeight();
    }

    /**
     * dp covert to px
     *
     * @param context 上下文
     * @param dp      dp
     */
    public static int dp2px(Context context, int dp) {
        if (context == null) return 0;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * 判断APP是否在后台
     *
     * @param context 上下文
     * @return true : 后台 false : 前景
     */
    public static boolean isAppInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if(am == null) return false;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            if (runningProcesses == null) return false;
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

    /**
     * 检测当前网络是否可用
     *
     * @return true : 可用  false : 不可用
     */
    public static boolean checkNetworkIsAvailable() {
        List<HashMap> list = new ArrayList<>();
        HashMap map = new HashMap();
        map.put("name", "baidu.com");
        map.put("host", "111.13.101.208");
        map.put("port", 80);
        list.add(map);
        map = new HashMap();
        map.put("name", "baidu.com");
        map.put("host", "123.125.114.144");
        map.put("port", 80);
        list.add(map);
        map = new HashMap();
        map.put("name", "baidu.com");
        map.put("host", "180.149.132.47");
        map.put("port", 80);
        list.add(map);
        map = new HashMap();
        map.put("name", "baidu.com");
        map.put("host", "220.181.57.217");
        map.put("port", 80);
        list.add(map);
        map = new HashMap();
        map.put("name", "qq.com");
        map.put("host", "125.39.240.113");
        map.put("port", 80);
        list.add(map);
        map = new HashMap();
        map.put("name", "qq.com");
        map.put("host", "61.135.157.156");
        map.put("port", 80);
        list.add(map);

        return OuterChecker.check(list, 1000);
    }

    /**
     * 格式化url
     *
     * @param ip   ip
     * @param port 端口号
     * @param path 路径
     * @return 完整的url
     */
    public static String formatUrl(String ip, int port, String path) {
        String url = null;
        if (!TextUtils.isEmpty(ip) && port > 0) {
            url = "http://" + ip + ":" + port + "/";
            if (!TextUtils.isEmpty(path)) {
                url += path;
            }
        }
        return url;
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     */
    public static boolean checkFileExist(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            return file.exists();
        }
        return false;
    }

    /**
     * 查询本地文件列表
     *
     * @param path 文件路径
     */
    public static List<FileInfo> queryLocalFileList(String path) {
        List<FileInfo> fileList = null;
        try {
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                if (file.exists()) {
                    fileList = new ArrayList<>();
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        if (files != null && file.length() > 0) {
                            Map<String, FileInfo> fileMap = new HashMap<>();
                            List<String> fileDateList = new ArrayList<>();
                            for (File file1 : files) {
                                if (file1 != null && file1.isFile()) {
                                    String createTime = TimeFormate.formatYMD_HMS(file1.lastModified());
                                    FileInfo fileInfo = new FileInfo();
                                    if(file1.getName().contains(SOS_PREFIX)){
                                        fileInfo.setType(2);
                                    }else{
                                        fileInfo.setType(1);
                                    }
                                    fileInfo.setName(file1.getName());
                                    fileInfo.setPath(file1.getAbsolutePath());
                                    fileInfo.setSize(file1.length());
                                    fileInfo.setCreateTime(createTime);
                                    fileInfo.setVideo((judgeFileType(file1.getName()) == FILE_TYPE_VIDEO));
                                    fileInfo.setSource(COME_FORM_LOCAL);
                                    if(!TextUtils.isEmpty(fileInfo.getPath()) && fileInfo.getPath().contains(DIR_REAR)){
                                        fileInfo.setCameraType(CAMERA_TYPE_REAR);
                                    }else{
                                        fileInfo.setCameraType(CAMERA_TYPE_FRONT);
                                    }
                                    //fileMap.put(createTime, fileInfo);
                                    //fileDateList.add(createTime);
                                    fileMap.put(file1.getName(), fileInfo);
                                    fileDateList.add(file1.getName());
                                }
                            }
                            if (fileDateList.size() > 0) {
                                descSort(fileDateList);
                                for (String date : fileDateList) {
                                    if (!TextUtils.isEmpty(date)) {
                                        FileInfo info = fileMap.remove(date);
                                        if (null != info) {
                                            fileList.add(info);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        String createTime = TimeFormate.formatYMD_HMS(file.lastModified());
                        if (!TextUtils.isEmpty(createTime)) {
                            FileInfo fileInfo = new FileInfo();
                            if(file.getName().contains(SOS_PREFIX)){
                                fileInfo.setType(2);
                            }else{
                                fileInfo.setType(1);
                            }
                            fileInfo.setName(file.getName());
                            fileInfo.setPath(file.getAbsolutePath());
                            fileInfo.setSize(file.length());
                            fileInfo.setCreateTime(createTime);
                            fileInfo.setVideo((judgeFileType(file.getName()) == FILE_TYPE_VIDEO));
                            fileInfo.setSource(COME_FORM_LOCAL);
                            if(!TextUtils.isEmpty(fileInfo.getPath()) && fileInfo.getPath().contains(DIR_REAR)){
                                fileInfo.setCameraType(CAMERA_TYPE_REAR);
                            }else{
                                fileInfo.setCameraType(CAMERA_TYPE_FRONT);
                            }
                            fileList.add(fileInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    /**
     * desc sort
     */
    public static void descSort(List<String> drs) {
        if (drs != null && drs.size() > 0) {
            Collections.sort(drs, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    if (s.compareTo(t1) > 0) {
                        return -1;
                    } else if (s.compareTo(t1) < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        }
    }

    /**
     * 倒序排列文件列表
     *
     * @param src 文件列表
     */
    public static void descSortWay(List<FileInfo> src) {
        if (src != null && src.size() > 0) {
            Collections.sort(src, new Comparator<FileInfo>() {
                @Override
                public int compare(FileInfo f1, FileInfo f2) {
                    String date1 = f1.getCreateTime();
                    String date2 = f2.getCreateTime();
                    assert (!TextUtils.isEmpty(date1));
                    assert (!TextUtils.isEmpty(date2));
                    if (date1.compareTo(date2) > 0) {
                        return -1;
                    } else if (date1.compareTo(date2) < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        }
    }

    /**
     * 查询所有本地文件的信息
     *
     * @param srcPath       本地存储根目录
     * @param targetDirName 目标文件夹
     */
    public static List<FileInfo> queryAllLocalFileList(String srcPath, String targetDirName) {
        if (!TextUtils.isEmpty(srcPath) && !TextUtils.isEmpty(targetDirName)) {
            File srcFile = new File(srcPath);
            if (srcFile.exists()) {
                List<FileInfo> fileInfoList = new ArrayList<>();
                if (srcFile.isDirectory()) {
                    if (targetDirName.equals(srcFile.getName())) {
                        fileInfoList = queryLocalFileList(srcFile.getAbsolutePath());
                    } else {
                        File[] files = srcFile.listFiles();
                        if (null != files) {
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    List<FileInfo> tempList = queryAllLocalFileList(file.getAbsolutePath(), targetDirName);
                                    if (tempList != null && tempList.size() > 0) {
                                        fileInfoList.addAll(tempList);
                                    }
                                }
                            }
                        }
                    }
                }

                if (fileInfoList.size() > 0) {
                    descSortWay(fileInfoList);
                }
                return fileInfoList;
            }
        }
        return null;
    }

    /**
     * 合并列表
     *
     * @param srcList 原始列表
     * @param addList 添加列表
     */
    public static List<ItemBean> mergeList(List<ItemBean> srcList, List<ItemBean> addList) {
        if (srcList == null) {
            srcList = new ArrayList<>();
        }
        if (addList != null && addList.size() > 0) {
            if (srcList.size() == 0) {
                srcList.addAll(addList);
            } else {
                for (ItemBean bean : addList) {
                    String addDate = bean.getData();
                    ItemBean temp = srcList.get(0);
                    if (addDate.compareTo(temp.getData()) > 0) {
                        srcList.add(0, bean);
                    } else {
                        boolean isCopying = false;
                        for (ItemBean item : srcList) {
                            String srcDate = item.getData();
                            if (addDate.equals(srcDate)) {
                                isCopying = true;
                                item.getInfoList().addAll(bean.getInfoList());
                                break;
                            }
                        }
                        if (!isCopying) {
                            srcList.add(bean);
                        }
                    }
                }
            }
        }
        return srcList;
    }

    /**
     * 转化成ItemBean列表
     *
     * @param srcList 源始列表
     */
    public static List<ItemBean> convertDataList(List<FileInfo> srcList) {
        if (srcList != null && srcList.size() > 0) {
            /*desc sort*/
            descSortWay(srcList);
            /*get create date */
            TreeSet<String> dateSet = new TreeSet<>(new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    if (s.compareTo(t1) > 0) {
                        return -1;
                    } else if (s.compareTo(t1) < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            for (FileInfo info : srcList) {
                String date = subDateFlag(info.getCreateTime());
                if (!TextUtils.isEmpty(date)) {
                    dateSet.add(date);
                }
            }
            List<ItemBean> itemBeanList = new ArrayList<>();
            /*get itemBean list*/
            if (dateSet.size() > 0) {
                for (String date : dateSet) {
                    List<FileInfo> infoList = new ArrayList<>();
                    boolean copyStart = false;
                    for (FileInfo fileInfo : srcList) {
                        if (fileInfo != null) {
                            String createTime = fileInfo.getCreateTime();
                            if (!TextUtils.isEmpty(createTime) && createTime.startsWith(date)) {
                                copyStart = true;
                                infoList.add(fileInfo);
                            } else {
                                if (copyStart) {
                                    break;
                                }
                            }
                        }
                    }
                    if (infoList.size() > 0) {
                        ItemBean itemBean = new ItemBean();
                        itemBean.setData(date);
                        itemBean.setInfoList(infoList);
                        itemBeanList.add(itemBean);
                    }
                }
            }
            return itemBeanList;
        }
        return null;
    }

    /**
     * 截取日期标签（format yyyyMMdd）
     * create date format : yyyyMMddHHmmss
     */
    private static String subDateFlag(String createTime) {
        if (!TextUtils.isEmpty(createTime) && createTime.length() >= 8) {
            return createTime.substring(0, 8);
        }
        return null;
    }

    /**
     * 格式化日期
     *
     * @param date （format : yyyyMMdd）
     * @return (format : yyyy-MM-dd)
     */
    public static String formatDate(String date) {
        if (!TextUtils.isEmpty(date) && date.length() >= 8) {
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        }
        return null;
    }

    /**
     * 拼接目录路径
     *
     * @param rootName     根路径名称
     * @param oneDirName   一级目录名称
     * @param twoDirName   二级目录名称
     * @param threeDirName 三级目录名称
     * @return 拼合的路径
     */
    public static String splicingFilePath(String rootName, String oneDirName, String twoDirName, String threeDirName) {
        File file;
        String path;
        if (!TextUtils.isEmpty(rootName)) {
            path = rootName;//ROOT_PATH;
//            if (rootName.contains(File.separator)) {
//                String[] dirNames = rootName.split(File.separator);
//                for (String name : dirNames) {
//                    if (!TextUtils.isEmpty(name)) {
//                        path += File.separator + name;
//                        file = new File(path);
//                        if (!file.exists()) {
//                            if (file.mkdir()) {
//                                Dbug.w(TAG, "create root dir success! path : " + path);
//                            }
//                        }
//                    }
//                }
//            } else {
//                path += File.separator + rootName;
//                file = new File(path);
//                if (!file.exists()) {
//                    if (file.mkdir()) {
//                        Dbug.w(TAG, "create root dir success! path : " + path);
//                    }
//                }
//            }


            if (TextUtils.isEmpty(oneDirName)) {
                return path;
            }
            path = path + oneDirName;
            file = new File(path);
            if (!file.exists()) {
                if (file.mkdir()) {
                    Dbug.w(TAG, "create one dir success!");
                }
            }
            if (TextUtils.isEmpty(twoDirName)) {
                return path;
            }
            path = path + File.separator + twoDirName;
            file = new File(path);
            if (!file.exists()) {
                if (file.mkdir()) {
                    Dbug.w(TAG, "create two dir success!");
                }
            }
            if (TextUtils.isEmpty(threeDirName)) {
                return path;
            }
            path = path + File.separator + threeDirName;
            file = new File(path);
            if (!file.exists()) {
                if (file.mkdir()) {
                    Dbug.w(TAG, "create three sub dir success!");
                }
            }
            return path;
        } else {
            return ROOT_PATH;
        }
    }
    public static String splicingFilePath(String oneName, String twoDirName, String threeDirName) {
        File file;
        String path;
        if (!TextUtils.isEmpty(oneName)) {
            path = oneName;

            if (TextUtils.isEmpty(twoDirName)) {
                return path;
            }
            path = path + twoDirName;
            file = new File(path);
            if (!file.exists()) {
                if (file.mkdir()) {
                    Dbug.w(TAG, "create two dir success!");
                }
            }
            if (TextUtils.isEmpty(threeDirName)) {
                return path;
            }
            path = path + File.separator + threeDirName;
            file = new File(path);
            if (!file.exists()) {
                if (file.mkdir()) {
                    Dbug.w(TAG, "create three sub dir success!");
                }
            }
            return path;
        } else {
            return ROOT_PATH;
        }
    }

    /**
     * 判断文件类型
     *
     * @param filename 文件名
     */
    public static int judgeFileType(String filename) {
        if (!TextUtils.isEmpty(filename)) {
            if ((filename.endsWith(".png") || filename.endsWith(".PNG")
                    || filename.endsWith(".JPEG") || filename.endsWith(".jpeg")
                    || filename.endsWith(".jpg") || filename.endsWith(".JPG"))) {
                return FILE_TYPE_PIC;
            } else if ((filename.endsWith(".mov") || filename.endsWith(".MOV")
                    || filename.endsWith(".mp4") || filename.endsWith(".MP4")
                    || filename.endsWith(".avi") || filename.endsWith(".AVI"))) {
                return FILE_TYPE_VIDEO;

            }
        }
        return FILE_TYPE_UNKNOWN;
    }

    /**
     * 获取下载文件名称
     *
     * @param fileInfo 文件信息
     * @return 文件保存名称
     */
    public static String getDownloadFilename(FileInfo fileInfo) {
        String result = null;
        if (fileInfo != null) {
            String filename = fileInfo.getName();
            if (!TextUtils.isEmpty(filename)) {
                if (fileInfo.getSource() == COME_FORM_LOCAL) {
                    result = filename;
                } else {
                    String createTime = fileInfo.getCreateTime();
                    if (filename.contains(".")) {
                        String[] strings = filename.split("\\.");
                        if (strings.length > 1) {
                            String fileSuffix = strings[strings.length - 1];
                            String filePrefix = "";
                            for (int i = 0; i < strings.length - 1; i++) {
                                filePrefix += strings[i];
                            }
                            result = filePrefix + "_" + createTime + "." + fileSuffix;
                        }
                    } else {
                        result = filename + "_" + createTime;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取视频缩略图的名称
     *
     * @param fileInfo 文件信息
     */
    public static String getVideoThumbName(FileInfo fileInfo) {
        String thumbName = null;
        if (fileInfo != null) {
            String filename = fileInfo.getName();
            if (!TextUtils.isEmpty(filename)) {
                String createTime = fileInfo.getCreateTime();
                if (filename.contains(".")) {
                    String[] strings = filename.split("\\.");
                    if (strings.length > 0) {
                        String fileSuffix = "jpg";
                        String filePrefix = "";
                        for (int i = 0; i < strings.length - 1; i++) {
                            filePrefix += strings[i];
                        }
                        if(fileInfo.isVideo()){
                            int duration = fileInfo.getDuration();
                            thumbName = filePrefix + "_" + createTime + "_" + duration + "." +fileSuffix;
                        }else{
                            thumbName = filePrefix + "_" + createTime + "." + fileSuffix;
                        }
                    }
                } else {
                    thumbName = filename + "_" + createTime + ".jpg";
                }
            }
        }
        return thumbName;
    }

    /**
     * 选择指定类型的文件列表
     *
     * @param drsList  原始文件列表
     * @param fileType 文件类型
     */
    public static List<FileInfo> selectTypeList(List<FileInfo> drsList, int fileType) {
        if (null == drsList || drsList.size() == 0) {
            return drsList;
        }
        List<FileInfo> resultList = new ArrayList<>();
        for (FileInfo info : drsList) {
            if (info != null) {
                if (fileType == FILE_TYPE_UNKNOWN) {
                    resultList.add(info);
                } else {
                    String filename = info.getName();
                    if (AppUtils.judgeFileType(filename) == fileType) {
                        resultList.add(info);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * 把Bitmap保存为文件
     *
     * @param bitmap     Bitmap数据
     * @param outputPath 输出路径
     * @param quality    压缩比例(0-100)
     */
    public static boolean bitmapToFile(Bitmap bitmap, String outputPath, int quality) {
        if (bitmap == null || TextUtils.isEmpty(outputPath)) {
            return false;
        }
        FileOutputStream outStream = null;
        boolean result = false;
        try {
            outStream = new FileOutputStream(outputPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outStream);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 数据保存成文件
     * @param data        数据
     * @param outputPath  输出文件路径
     * @return            是否成功
     */
    public static boolean bytesToFile(byte[] data, String outputPath){
        if(data != null && !TextUtils.isEmpty(outputPath)){
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(outputPath);
                Dbug.e(TAG, "data =" +data);
                outputStream.write(data);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * 调用第三方软件浏览文件
     *
     * @param activity activity
     * @param fileName 文件名
     * @param savePath 文件路径
     */
    public static void browseFileWithOther(BaseActivity activity, String fileName, String savePath) {
        try {
            if (activity != null && savePath != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                int fileType = AppUtils.judgeFileType(fileName);
                switch (fileType) {
                    case FILE_TYPE_PIC:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", new File(savePath));
                            intent.setDataAndType(contentUri, "image/*");
                        }else{
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.parse("file://" + savePath), "image/*");
                        }
                        break;
                    case FILE_TYPE_VIDEO:
                        String stend = "";
                        if (fileName.endsWith(".mov") || fileName.endsWith(".MOV")) {
                            stend = "mov";
                        } else if (fileName.endsWith(".avi") || fileName.endsWith(".AVI")) {
                            stend = "avi";
                        } else if (fileName.endsWith(".mp4") || fileName.endsWith(".MP4")) {
                            stend = "mp4";
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", new File(savePath));
                            intent.setDataAndType(contentUri, "video/" + stend);
                        }else{
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.parse("file://" + savePath), "video/" + stend);
                        }
                        break;
                    default:
                        Dbug.e(TAG, "无法打开文件，路径：" + savePath);
                        break;
                }
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Dbug.e(TAG, " error  " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get files and read data from the raw folder in resources
     *
     * @param mContext context
     * @param rawId    raw resource ID
     */
    public static String getFromRaw(Context mContext, int rawId) {
        if (mContext == null) {
            return null;
        }
        String result = null;
        InputStream in = null;
        try {
            in = mContext.getResources().openRawResource(rawId);
            //Number of bytes in the file
            int size = 0;
            if (in != null) {
                size = in.available();
            }
            if (size > 0) {
                byte[] buffer = new byte[size];
                //Read the data in the file to the byte array.
                int length = in.read(buffer);
                if (length >= size) {
                    result = new String(buffer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 删除文件或文件夹
     *
     * @param file 文件或文件夹
     */
    public static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            if (file.delete()) {
                Dbug.i(TAG, "delete file success!");
            }
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                if (file.delete()) {
                    Dbug.i(TAG, "delete empty file success!");
                }
                return;
            }
            for (File childFile : childFiles) {
                deleteFile(childFile);
            }
            if (file.delete()) {
                Dbug.i(TAG, "delete empty file success!");
            }
        }
    }

    /**
     * read text content from path
     */
    public static String readTxtFile(String filePath) {
        String textStr = "";
        if (filePath == null || filePath.isEmpty()) {
            return textStr;
        }
        InputStreamReader read = null;
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                read = new InputStreamReader(
                        new FileInputStream(file), encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    textStr = textStr + lineTxt + '\n';
                }
                read.close();
            } else {
                Dbug.e(TAG, "Cannot find the specified file");
            }
        } catch (Exception e) {
            Dbug.e(TAG, " err : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    Dbug.e(TAG, " IOException : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return textStr;
    }

    /**
     * 解析Application 描述文档
     *
     * @param content 文档数据
     */
    public static AppInfo parseApplicationMsg(String content) {
        if (TextUtils.isEmpty(content)) return null;
        JSONObject jsonObject;
        AppInfo info = new AppInfo();
        info.setAppName(a.getApplication().getAppName());
        info.setAppPlatform(TopicParam.OS_ANDROID);
        info.setAppVersion(a.getApplication().getAppVersion());
        List<String> devTypeList = new ArrayList<>();
        try {
            jsonObject = new JSONObject(content);
            if (jsonObject.has(DEV_TYPE)) {
                String devTypeContent = jsonObject.getString(DEV_TYPE);
                if (!TextUtils.isEmpty(devTypeContent)) {
                    JSONArray jsonArray = new JSONArray(devTypeContent);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String devType = jsonArray.getString(i);
                        if (!TextUtils.isEmpty(devType)) {
                            devTypeList.add(devType);
                        }
                    }
                    if (devTypeList.size() > 0) {
                        info.setDev_type(devTypeList);
                    }
                }
            }
            if (jsonObject.has(DEV_LIST)) {
                String devListContent = jsonObject.getString(DEV_LIST);
                JSONObject devListJson = new JSONObject(devListContent);
                if (devTypeList.size() > 0) {
                    Map<String, List<String>> devListMap = new HashMap<>();
                    for (String type : devTypeList) {
                        if (TextUtils.isEmpty(type)) continue;
                        if (devListJson.has(type)) {
                            String deviceVersionContent = devListJson.getString(type);
                            if (!TextUtils.isEmpty(deviceVersionContent)) {
                                JSONArray jsonArray = new JSONArray(deviceVersionContent);
                                List<String> versionList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String version = jsonArray.getString(i);
                                    if (!TextUtils.isEmpty(version)) {
                                        versionList.add(version);

                                    }
                                }
                                if (versionList.size() > 0) {
                                    Collections.sort(versionList, new Comparator<String>() {
                                        @Override
                                        public int compare(String s, String t1) {
                                            if (s.compareTo(t1) > 0) {
                                                return -1;
                                            } else if (s.compareTo(t1) < 0) {
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        }
                                    });
                                    devListMap.put(type, versionList);
                                }
                            }
                        }
                    }
                    if (devListMap.size() > 0) {
                        info.setDev_list(devListMap);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return info;
    }

    /**
     * 解析设备描述信息
     *
     * @param deviceDescTxt 设备描述信息
     */
    public static DeviceDesc parseDeviceDescTxt(String deviceDescTxt) {
        //Dbug.e(TAG, "deviceDescTxt=\n" + deviceDescTxt);
        DeviceDesc deviceDesc = null;
        if (!TextUtils.isEmpty(deviceDescTxt)) {
            deviceDesc = new DeviceDesc();
            try {
                JSONObject jsonObject = new JSONObject(deviceDescTxt);
                if (jsonObject.has(DEV_UUID)) {
                    String uuid = jsonObject.getString(DEV_UUID);
                    if (!TextUtils.isEmpty(uuid)) {
                        deviceDesc.setUuid(uuid);
                    }
                }
                if (jsonObject.has(DEV_PRODUCT)) {
                    String product = jsonObject.getString(DEV_PRODUCT);
                    if (!TextUtils.isEmpty(product)) {
                        deviceDesc.setProduct_type(product);
                    }
                }
                if (jsonObject.has(DEV_MATCH_APP)) {
                    String appName = jsonObject.getString(DEV_MATCH_APP);
                    if (!TextUtils.isEmpty(appName)) {
                        deviceDesc.setMatch_app_type(appName);
                    }
                }
                if (jsonObject.has(DEV_FIRMWARE_VERSION)) {
                    String version = jsonObject.getString(DEV_FIRMWARE_VERSION);
                    if (!TextUtils.isEmpty(version)) {
                        deviceDesc.setFirmware_version(version);
                    }
                }
                if(jsonObject.has(DEV_DEVICE_TYPE)){
                    String type = jsonObject.getString(DEV_DEVICE_TYPE);
                    if(!TextUtils.isEmpty(type)){
                        deviceDesc.setDevice_type(type);
                    }
                }
                if(jsonObject.has(DEV_SUPPORT_BUMPING)){
                    String bumping = jsonObject.getString(DEV_SUPPORT_BUMPING);
                    if(SUPPORT_BUMPING.equals(bumping)){
                        deviceDesc.setSupport_bumping(true);
                    }else{
                        deviceDesc.setSupport_bumping(false);
                    }
                }
                if (jsonObject.has(DEV_RTS_TYPE)) {
                    String type = jsonObject.getString(DEV_RTS_TYPE);
                    if (RTS_TYPE_JPEG.equals(type)) {
                        deviceDesc.setVideoType(DeviceClient.RTS_JPEG);
                    } else {
                        deviceDesc.setVideoType(DeviceClient.RTS_H264);
                    }
                }
                if (jsonObject.has(DEV_RTS_NET_TYPE)) {
                    String type = jsonObject.getString(DEV_RTS_NET_TYPE);
                    if (RTS_NET_TYPE_TCP.equals(type)) {
                        deviceDesc.setNetMode(Stream.Protocol.TCP_MODE);
                    } else if (RTS_NET_TYPE_UDP.equals(type)){
                        deviceDesc.setNetMode(Stream.Protocol.UDP_MODE);
                    }
                }

                if(jsonObject.has(DEV_SUPPORT_PROJECTION)){
                    String projection = jsonObject.getString(DEV_SUPPORT_PROJECTION);
                    if(SUPPORT_PROJECTION.equals(projection)){
                        deviceDesc.setSupport_projection(true);
                    }else{
                        deviceDesc.setSupport_projection(false);
                    }
                }
                if(jsonObject.has(DEV_FRONT_SUPPORT)){
                    String frontSupport = jsonObject.getString(DEV_FRONT_SUPPORT);
                    if(!TextUtils.isEmpty(frontSupport)){
                        JSONArray jsonArray = new JSONArray(frontSupport);
                        String[] support = new String[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String level = jsonArray.getString(i);
                            if (!TextUtils.isEmpty(level)) {
                                support[i] = level;
                            }
                        }
                        deviceDesc.setFront_support(support);
                    }
                }
                if(jsonObject.has(DEV_REAR_SUPPORT)){
                    String rearSupport = jsonObject.getString(DEV_REAR_SUPPORT);
                    if(!TextUtils.isEmpty(rearSupport)){
                        JSONArray jsonArray = new JSONArray(rearSupport);
                        String[] support = new String[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String level = jsonArray.getString(i);
                            if (!TextUtils.isEmpty(level)) {
                                support[i] = level;
                            }
                        }
                        deviceDesc.setRear_support(support);
                    }
                }
                if (jsonObject.has(DEV_APP_LIST)) {
                    String appList = jsonObject.getString(DEV_APP_LIST);
                    if (!TextUtils.isEmpty(appList)) {
                        JSONObject jsonObject1 = new JSONObject(appList);
                        if (jsonObject1.has(DEV_MATCH_ANDROID_VER)) {
                            String appMatchVer = jsonObject1.getString(DEV_MATCH_ANDROID_VER);
                            if (!TextUtils.isEmpty(appMatchVer)) {
                                JSONArray jsonArray = new JSONArray(appMatchVer);
                                List<String> versionList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String version = jsonArray.getString(i);
                                    if (!TextUtils.isEmpty(version)) {
                                        versionList.add(version);
                                    }
                                }
                                if (versionList.size() > 0) {
                                    Collections.sort(versionList, new Comparator<String>() {
                                        @Override
                                        public int compare(String s, String t1) {
                                            if (s.compareTo(t1) > 0) {
                                                return -1;
                                            } else if (s.compareTo(t1) < 0) {
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        }
                                    });
                                    DeviceDesc.AppListBean appListBean = new DeviceDesc.AppListBean();
                                    appListBean.setMatch_android_ver(versionList);
                                    deviceDesc.setApp_list(appListBean);
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return deviceDesc;
    }

    /**
     * 解析服务器索引信息
     *
     * @param serverAndroidTxt  安卓索引信息
     * @param serverFirmwareTxt 设备索引信息
     */
    private static ServerInfo parseServerTxtInfo(String serverAndroidTxt, String serverFirmwareTxt) {
        ServerInfo serverInfo = null;
        if (!TextUtils.isEmpty(serverAndroidTxt)) {
            Map<String, List<Integer>> serverAndroidMap = new HashMap<>();
            try {
                JSONObject jsonObject = new JSONObject(serverAndroidTxt);
                Iterator<String> iterator = jsonObject.keys();
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (!TextUtils.isEmpty(key)) {
                            String versionData = jsonObject.getString(key);
                            if (!TextUtils.isEmpty(versionData)) {
                                List<Integer> versions = new ArrayList<>();
                                JSONArray jsonArray = new JSONArray(versionData);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String version = jsonArray.getString(i);
                                    if (!TextUtils.isEmpty(version)) {
                                        int versionCode = 0;
                                        try {
                                            versionCode = Integer.valueOf(version);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        versions.add(versionCode);
                                    }
                                }
                                if (versions.size() > 0) {
                                    Collections.sort(versions, new Comparator<Integer>() {
                                        @Override
                                        public int compare(Integer integer, Integer t1) {
                                            if (integer > t1) {
                                                return -1;
                                            } else if (integer < t1) {
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        }
                                    });
                                    serverAndroidMap.put(key, versions);
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (serverAndroidMap.size() > 0) {
                serverInfo = new ServerInfo();
                serverInfo.setAndroidVersionMap(serverAndroidMap);
            }
        }

        if (!TextUtils.isEmpty(serverFirmwareTxt)) {
            Map<String, List<String>> serverFirmwareMap = new HashMap<>();
            try {
                JSONObject jsonObject = new JSONObject(serverFirmwareTxt);
                Iterator<String> iterator = jsonObject.keys();
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (!TextUtils.isEmpty(key)) {
                            String versionData = jsonObject.getString(key);
                            if (!TextUtils.isEmpty(versionData)) {
                                List<String> versions = new ArrayList<>();
                                JSONArray jsonArray = new JSONArray(versionData);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String version = jsonArray.getString(i);
                                    if (!TextUtils.isEmpty(version)) {
                                        versions.add(version);
                                    }
                                }
                                if (versions.size() > 0) {
                                    Collections.sort(versions, new Comparator<String>() {
                                        @Override
                                        public int compare(String s, String t1) {
                                            if (s.compareTo(t1) > 0) {
                                                return -1;
                                            } else if (s.compareTo(t1) < 0) {
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        }
                                    });
                                    serverFirmwareMap.put(key, versions);
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (serverFirmwareMap.size() > 0) {
                if (serverInfo == null) {
                    serverInfo = new ServerInfo();
                }
                serverInfo.setFirmwareVersionMap(serverFirmwareMap);
            }
        }

        return serverInfo;
    }

    /**
     * 检查与更新
     *
     * @param mContext   上下文
     * @param updateType 检查升级类型
     */
    public static String checkUpdateFilePath(Context mContext, int updateType) {
        if (mContext == null) {
            Dbug.e(TAG, "mContext is null");
            return null;
        }
         /* Get version info */
        a mApplication = a.getApplication();
        if (mApplication == null) {
            Dbug.e(TAG, "mApplication is null");
            return null;
        }
        String appDesc = getFromRaw(mApplication, R.raw.app_desc);
        AppInfo appInfo = parseApplicationMsg(appDesc);
        if (appInfo == null) {
            Dbug.e(TAG, "appInfo is null");
            return null;
        }
        String currentSDK;
        int currentAPK = appInfo.getAppVersion();
        if (currentAPK == 0) {
            Dbug.e(TAG, "currentAPK=" +currentAPK);
            return null;
        }
        ServerInfo serverInfo;
        String uploadPath = splicingFilePath(mApplication.getAppFilePath(), UPGRADE, null, null);
        String versionPath = splicingFilePath(mApplication.getAppFilePath(), VERSION, null, null);

        String serverAndroidPath = versionPath + File.separator + ANDROID_DIR + "_" + VERSION_JSON;
        String serverFirmwarePath = versionPath + File.separator + FIRMWARE_DIR + "_" + VERSION_JSON;
        if (updateType == UPGRADE_APK_TYPE) {
            //delete server Text
//            deleteFile(new File(versionPath));

            //download server txt
            FTPClientUtil ftpClientUtil = FTPClientUtil.getInstance();
            List<String> platforms = new ArrayList<>();
            platforms.add(ANDROID_DIR);
            platforms.add(FIRMWARE_DIR);
            ftpClientUtil.downloadTxt(platforms, false);
        }

        String updatePath = null;
        int serverLatestAPK = 0;
        String serverLatestSDKStr = "";
        boolean isSameVersion = false;
        switch (updateType) {
            case UPGRADE_APK_TYPE:
                //delete APK
                File file = new File(uploadPath);
                if (file.exists()) {
                    File[] files = file.listFiles();
                    for (File f : files) {
                        if (f.getName().endsWith(".apk") || f.getName().endsWith(".APK")) {
                            if (!f.delete()) {
                                Dbug.e(TAG, "Delete failure:" + f.getName());
                            }
                        }
                    }
                }
                Dbug.w(TAG, "-checkAndUpdate- serverAndroidPath : " + serverAndroidPath);
                String newServerText = readTxtFile(serverAndroidPath);
                if (TextUtils.isEmpty(newServerText)) {
                    return null;
                }
                serverInfo = parseServerTxtInfo(newServerText, null);
                if (serverInfo != null) {
                    Map<String, List<Integer>> androidMap = serverInfo.getAndroidVersionMap();
                    if (androidMap != null) {
                        List<Integer> versionList = androidMap.get(appInfo.getAppName());
                        if (versionList != null && versionList.size() > 0) {
                            serverLatestAPK = versionList.get(0);
                        }
                    }
                    if (serverLatestAPK > 0) {
                        if (serverLatestAPK > currentAPK) {
                            updatePath = ANDROID_DIR + File.separator + appInfo.getAppName() + File.separator + serverLatestAPK;
                            Dbug.w(TAG, " APK updatePath : " + updatePath);
                        } else if (serverLatestAPK == currentAPK) {
                            isSameVersion = true;
                            Dbug.w(TAG, "-checkAndUpdate- currentAPK = " + currentAPK);
                        } else {
                            Dbug.w(TAG, "-checkAndUpdate- serverLatestAPK = " + serverLatestAPK + " ,currentAPK = " + currentAPK);
                        }
                    }
                }
                break;
            case UPGRADE_SDK_TYPE:
                String output = versionPath + File.separator + DEVICE_DESCRIPTION;
                String descTxt = readTxtFile(output);
                if (TextUtils.isEmpty(descTxt)) {
                    Dbug.e(TAG, " SDK update >> descTxt is empty!");
                    return null;
                }
                DeviceDesc deviceDesc = parseDeviceDescTxt(descTxt);
                if (deviceDesc == null || TextUtils.isEmpty(deviceDesc.getFirmware_version())) {
                    Dbug.e(TAG, " SDK update >> deviceInfo or Firmware_version is empty !");
                    return null;
                }
                String product = deviceDesc.getProduct_type();
                if (TextUtils.isEmpty(product)) {
                    Dbug.e(TAG, " SDK update >> product is empty!");
                    return null;
                }
                currentSDK = deviceDesc.getFirmware_version();
                if (TextUtils.isEmpty(currentSDK)) {
                    Dbug.e(TAG, " SDK update >> currentSDK is empty!");
                    return null;
                }
                Dbug.e(TAG, " SDK update >> currentSDK : " + currentSDK);
                a.getApplication().setDeviceDesc(deviceDesc);
                String firmwareServerText = readTxtFile(serverFirmwarePath);
                if (TextUtils.isEmpty(firmwareServerText)) {
                    Dbug.e(TAG, " SDK update >> firmwareServerText is empty!");
                    return null;
                }
                serverInfo = parseServerTxtInfo(null, firmwareServerText);
                if (serverInfo != null) {
                    Map<String, List<String>> firmwareMap = serverInfo.getFirmwareVersionMap();
                    if (firmwareMap != null) {
                        List<String> versionList = firmwareMap.get(product);
                        if (versionList != null && versionList.size() > 0) {
                            serverLatestSDKStr = versionList.get(0);
                        }
                    }
                }
                if (!TextUtils.isEmpty(serverLatestSDKStr)) {
                    Dbug.w(TAG, " serverLatestSDKStr : " + serverLatestSDKStr);
                    if (serverLatestSDKStr.compareTo(currentSDK) > 0) {
                        updatePath = FIRMWARE_DIR + File.separator + product + File.separator + serverLatestSDKStr;
                        Dbug.e(TAG, " SDK updatePath : " + updatePath);
                    } else if (serverLatestSDKStr.equals(currentSDK)) {
                        isSameVersion = true;
                    }
                }
                break;
        }

        if (TextUtils.isEmpty(updatePath)) {
            if (isSameVersion) {
                return mApplication.getString(R.string.latest_version);
            } else {
                Dbug.e(TAG, " update failure");
                return null;
            }
        }

        return updatePath;
    }

    /**
     * 设置屏幕调整模式为手动调整
     *
     * @param activity 对应Activity
     */
    public static void setScreenManualMode(Activity activity) {
        setScreenMode(activity, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 设置屏幕调整模式
     *
     * @param activity 上下文
     * @param mode     模式
     */
    public static void setScreenMode(Activity activity, int mode) {
        if (activity != null) {
            ContentResolver contentResolver = activity.getContentResolver();
            int currentMode = getScreenMode(activity);
            if (currentMode != -1) {
                if (currentMode != mode) {
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
                }
            }
        }
    }

    /**
     * 获得当前屏幕调整模式
     *
     * @param activity 当前Activity
     */
    public static int getScreenMode(Activity activity) {
        int mode = -1;
        if (activity != null) {
            ContentResolver contentResolver = activity.getContentResolver();
            try {
                mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mode;
    }

    /**
     * 获得当前屏幕亮度
     *
     * @param activity 当前Activity
     */
    public static int getScreenBrightness(Activity activity) {
        int defVal = 125;
        if (activity != null) {
            ContentResolver contentResolver = activity.getContentResolver();
            defVal = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, defVal);
        }
        return defVal;
    }

    /**
     * 设置屏幕亮度
     *
     * @param activity   当前Activity
     * @param brightness 亮度值
     */
    public static void setBrightness(Activity activity, int brightness) {
        if (activity != null) {
//            setScreenManualMode(activity);
            //判断是否授权系统设置
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(activity)) {
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:" + activity.getPackageName()));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    activity.startActivityForResult(intent, 10001);
                    return;
                } else {
                    //有了权限，你要做什么呢？具体的动作
                }
            }

            Window window = activity.getWindow();
            if (window != null) {
                WindowManager.LayoutParams lp = window.getAttributes();
                if (lp != null) {
                    lp.screenBrightness = (brightness / 255.0F); // 预览亮度, 一个浮点数0-1
                    window.setAttributes(lp);
                    Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);// brightness设置屏幕亮度，值为30-255
                }
            }
        }
    }

    /**
     * 改变应用语言
     *
     * @param context 上下文
     * @param lanIndex  语言设置
     */
    public static void changeAppLanguage(Context context, String lanIndex) {
//        Resources resources = context.getApplicationContext().getResources();
//        DisplayMetrics dm = resources.getDisplayMetrics();
//        Configuration config = resources.getConfiguration();
//        Locale locale = LangUtil.getLanguage(lanIndex);
//        if (locale != null) {
//            // 应用用户选择语言
//            config.setLocale(locale);
//            Locale.setDefault(locale);
//            resources.updateConfiguration(config, dm);
//        } else {
//            Dbug.e(TAG, "No language match:"+lanIndex);
//        }
    }


    /**
     * 获取手机外部可用存储空间
     *
     * @return 获取手机外部可用存储空间大小：单位byte
     */
    public static long getAvailableExternalMemorySize() {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSize;
        long blockCount;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = statFs.getBlockSizeLong();
            blockCount = statFs.getAvailableBlocksLong();
        } else {
            blockSize = statFs.getBlockSize();
            blockCount = statFs.getAvailableBlocks();
        }
        return blockSize * blockCount;
    }

    /**
     * 获取手机外存储空间
     *
     * @return 手机外存储空间容量：单位byte
     */
    public static long getExternalMemorySize() {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSize;
        long blockCount;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = statFs.getBlockSizeLong();
            blockCount = statFs.getBlockCountLong();
        } else {
            blockSize = statFs.getBlockSize();
            blockCount = statFs.getBlockCount();
        }
        return blockSize * blockCount;
    }

    /**
     * 获取手机外部存储空间
     *
     * @param context 上下文
     * @return 以M, G为单位的容量
     */
    public static String getExternalMemorySize(Context context) {
        return Formatter.formatFileSize(context, getExternalMemorySize());
    }

    /**
     * 获取手机外部可用存储空间
     *
     * @param context 上下文
     * @return 以M, G为单位的容量
     */
    public static String getAvailableExternalMemorySize(Context context) {

        return Formatter.formatFileSize(context, getAvailableExternalMemorySize());
    }


    /**
     * 按新的宽高缩放图片
     *
     * @param bm        原始图像
     * @param newWidth  新的宽度
     * @param newHeight 新的调试
     * @return 调整后的图像
     */
    public static Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBmp = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        if (!bm.isRecycled()) {
            bm.recycle();
        }
        return newBmp;
    }


    /**
     * 获取一个文件夹下的所有文件
     **/
    public static ArrayList<SDFileInfo> getFiles(String path) {
        File f = new File(path);
        File[] files = f.listFiles();
        if (files == null) {
            return null;
        }

        ArrayList<SDFileInfo> fileList = new ArrayList<>();
        // 获取文件列表
        for (File file : files) {
            SDFileInfo fileInfo = new SDFileInfo();
            fileInfo.Name = file.getName();
            fileInfo.IsDirectory = file.isDirectory();
            fileInfo.Path = file.getPath();
            fileInfo.Size = file.length();
            fileList.add(fileInfo);
        }

        // 排序
        Collections.sort(fileList, new FileComparator());

        return fileList;
    }

    static class HiddenFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden();
        }
    }
    public static ArrayList<SDFileInfo> getFirmwareFile(String path) {
        File f = new File(path);
        File[] files = f.listFiles(new HiddenFilter());
        if (files == null) {
            return null;
        }

        ArrayList<SDFileInfo> fileList = new ArrayList<>();
        // 获取文件列表
        for (File file : files) {
            String fileName = file.getName();
            if (file.isDirectory() || fileName.endsWith(".bfu") || fileName.endsWith(".BFU")
                    || fileName.endsWith(".BIN") || fileName.endsWith(".bin")) {
                SDFileInfo fileInfo = new SDFileInfo();
                fileInfo.Name = file.getName();
                fileInfo.IsDirectory = file.isDirectory();
                fileInfo.Path = file.getPath();
                fileInfo.Size = file.length();
                fileList.add(fileInfo);
            }
        }

        // 排序
        Collections.sort(fileList, new FileComparator());

        return fileList;
    }

    /**
     * 检查文件是否空文件夹
     *
     * @param path 文件路径
     */
    public static boolean checkIsEmptyFolder(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null || files.length == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 防止多次点击事件
     **/
    private static long lastClickTime;

    public static boolean isFastDoubleClick(int delayTime) {
        long time = System.currentTimeMillis();
        if (lastClickTime == 0) {
            lastClickTime = time;
            return false;
        } else {
            if (time - lastClickTime >= delayTime) {
                lastClickTime = time;
                return false;
            } else {
                lastClickTime = time;
                return true;
            }
        }
    }

    /**
     * 获得碰撞视频名
     */
    public static String getCrashVideoName() {
        long time = Calendar.getInstance().getTimeInMillis();
        return SOS_PREFIX + "_" + time + ".mov";
    }

    /**
     * 获得下载视频名
     */
    public static String getRecordVideoName() {
        long time = Calendar.getInstance().getTimeInMillis();
        return REC_PREFIX + "_" + time + ".mov";
    }

    public static String getRecordAviName(){
        long time = Calendar.getInstance().getTimeInMillis();
        return REC_PREFIX + "_" + time + ".avi";
    }

    /**
     * 获得本地保存图片名
     */
    public static String getLocalPhotoName(){
        long time = Calendar.getInstance().getTimeInMillis();
        return JPG_PREFIX + "_" + time + ".jpg";
    }

    /**
     * 查询缩略图文件夹路径
     *
     * @param path 应用根目录
     */
    public static List<String> queryThumbDirPath(String path) {
        List<String> pathList = null;
        if (!TextUtils.isEmpty(path)) {
            File srcFile = new File(path);
            if (srcFile.exists()) {
                pathList = new ArrayList<>();
                if (srcFile.isDirectory()) {
                    if (DIR_THUMB.equals(srcFile.getName())) {
                        pathList.add(srcFile.getAbsolutePath());
                    } else {
                        File[] files = srcFile.listFiles();
                        if (files != null && files.length > 0) {
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    List<String> temp = queryThumbDirPath(file.getAbsolutePath());
                                    if (temp != null && temp.size() > 0) {
                                        pathList.addAll(temp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pathList;
    }

    /**
     * 查询缩略图信息列表
     * @param path   指定路径
     */
    public static List<FileInfo> queryThumbInfoList(String path){
        List<FileInfo> thumbList = null;
        if(path != null){
            List<String> pathList = queryThumbDirPath(path);
            if(pathList != null && pathList.size() > 0){
                thumbList = new ArrayList<>();
                for (String sub : pathList){
                    List<FileInfo> subList = queryLocalFileList(sub);
                    if(subList != null){
                        thumbList.addAll(subList);
                    }
                }
                if(thumbList.size() > 0){
                    descSortWay(thumbList);
                }
            }
        }
        return thumbList;
    }

    /**
     * 查询缩略图路径
     * @param filename  文件名
     * @param path      查询路径
     */
    public static String queryThumbPath(String filename, String path){
        String thumbName = "";
        if(!TextUtils.isEmpty(filename) && !TextUtils.isEmpty(path)){
            String flag;
            int index = filename.lastIndexOf(".");
            if (index != -1) {
                flag = filename.substring(0, index);
            }else{
                flag = filename;
            }
            List<FileInfo> thumbList = queryThumbInfoList(path);
            if(thumbList != null){
                for (FileInfo info : thumbList){
                    String savePath = info.getPath();
                    if(!TextUtils.isEmpty(savePath) && savePath.contains(flag)){
                        thumbName = savePath;
                        break;
                    }
                }
            }
        }
        return thumbName;
    }

    /**
     * 从缩略图路径中获得时长
     * @param thumbPath  缩略图路径
     */
    public static int parseThumbPathForDuration(String thumbPath){
        int duration = 0;
        if(!TextUtils.isEmpty(thumbPath)){
            int index = thumbPath.lastIndexOf(".");
            if(index != -1){
                thumbPath = thumbPath.substring(0, index);
            }
            if(thumbPath.contains("_")){
                String[] args = thumbPath.split("_");
                if(args.length > 1){
                    try{
                        duration = Integer.valueOf(args[args.length - 1]);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return duration;
    }

    /**
     * 获取文件或文件夹的大小
     *
     * @param file 文件或文件夹
     */
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        if (file == null || !file.exists()) {
            return size;
        }
        try {
            File[] fileList = file.listFiles();
            if (fileList != null && fileList.length > 0) {
                for (File subFile : fileList) {
                    // 如果下面还有文件
                    if (subFile.isDirectory()) {
                        size = size + getFolderSize(subFile);
                    } else {
                        size = size + subFile.length();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size 容量大小
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    /**
     * 获得对应清晰度的宽高
     * @param level  清晰度
     */
    public static int[] getRtsResolution(int level){
        int[] resolution = new int[2];
        int width;
        int height;
        if(RTS_LEVEL_FHD == level){
            width = 1920;
            height = 1080;
        }else if(RTS_LEVEL_SD == level){
            width = 640;
            height = 480;
        }else{
            width = 1280;
            height = 720;
        }
        resolution[0] = width;
        resolution[1] = height;
        return resolution;
    }

    /**
     * 区分清晰度等级
     * @param rtsWidth    实时流宽度
     * @param rtsHeight   实时流高度
     */
    public static int adjustRtsResolution(int rtsWidth, int rtsHeight){
        int resolution;
        if(rtsWidth == 1920 && rtsHeight == 1080){
            resolution = RTS_LEVEL_FHD;
        }else if(rtsWidth == 640 && rtsHeight == 480){
            resolution = RTS_LEVEL_SD;
        }else{
            resolution = RTS_LEVEL_HD;
        }
        return resolution;
    }

    /**
     * 区分前后视类型
     * @param fileInfo    文件信息
     */
    public static String checkCameraDir(FileInfo fileInfo){
        String cameraDir = DIR_FRONT;
        if(fileInfo != null){
            String camera = fileInfo.getCameraType();
            if(CAMERA_TYPE_REAR.equals(camera)){
                cameraDir = DIR_REAR;
            }
        }
        return cameraDir;
    }

    /**
     * 判断帧类型
     * @param data  帧数据
     * @return {@link IConstant#FRAME_TYPE_UNKNOWN} or
     * {@link IConstant#FRAME_TYPE_I} or
     * {@link IConstant#FRAME_TYPE_P} or
     * {@link IConstant#FRAME_TYPE_B}
     */
    public static int checkFrameType(byte[] data){
        int type = FRAME_TYPE_UNKNOWN;
        if(data != null && data.length > 5){
            byte[] head = new byte[5];
            System.arraycopy(data, 0, head, 0, 5);
            if(head[0] == 0x00 && head[1] == 0x00){ //前丙个byte必须为0x00
                if(head[2] == 0x01){                //第三个byte为0x01的情况
                    if(head[3] == 0x67){            //sps 简单判断为i帧
                        type = FRAME_TYPE_I;
                    }else if(head[3] == 0x41){      //p帧标识（仅DV16）
                        type = FRAME_TYPE_P;
                    }
                }else if(head[2] == 0x00 && head[3] == 0x01){  //第三个byte为0x00的情况
                    if(head[4] == 0x67){             //sps 简单判断为i帧
                        type = FRAME_TYPE_I;
                    }else if(head[4] == 0x41){       //p帧标识（仅DV16）
                        type = FRAME_TYPE_P;
                    }
                }
            }
        }
        return type;
    }

    /**
     * 获取当前直播的分辨率等级
     * @return 分辨率等级
     */
    public static int getStreamResolutionLevel() {
        a mainApplication = a.getApplication();
        int cameraType = mainApplication.getDeviceSettingInfo().getCameraType();
        int currentLevel;
        String[] levels;
        if (cameraType == DeviceClient.CAMERA_FRONT_VIEW) {
            currentLevel = PreferencesHelper.getSharedPreferences(mainApplication).getInt(KEY_FRONT_RES_LEVEL, RTS_LEVEL_HD);
            levels = mainApplication.getDeviceDesc().getFront_support();
        } else  {
            currentLevel = PreferencesHelper.getSharedPreferences(mainApplication).getInt(KEY_REAR_RES_LEVEL, RTS_LEVEL_HD);
            levels = mainApplication.getDeviceDesc().getRear_support();
        }
        if (levels != null && levels.length > 0) {
            boolean isExit = false;
            for (int i = 0; i < levels.length; i++) {
                int l = Integer.parseInt(levels[i]);
                if (currentLevel == l) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit) {
                currentLevel = Integer.parseInt(levels[0]);
            }
        }
        return currentLevel;
    }

    /**
     * 保存直播分辨率等级
     * @param level 分辨率等级
     */
    public static void saveStreamResolutionLevel(int level) {
        a mainApplication = a.getApplication();
        int cameraType = mainApplication.getDeviceSettingInfo().getCameraType();
        if(cameraType == DeviceClient.CAMERA_REAR_VIEW){
            PreferencesHelper.putIntValue(mainApplication, KEY_REAR_RES_LEVEL, level);
        }else{
            PreferencesHelper.putIntValue(mainApplication, KEY_FRONT_RES_LEVEL, level);
        }
    }

    public static String getMediaDirectory(String cameraType){
        String cameraDir = DIR_FRONT;
        if(!TextUtils.isEmpty(cameraType) && CAMERA_TYPE_REAR.equals(cameraType)) {
            cameraDir = DIR_REAR;
        }
        return cameraDir;
    }

    public static int getCameraType(String path){
        int currentCamera = DeviceClient.CAMERA_FRONT_VIEW;
        if(!TextUtils.isEmpty(path) && path.contains(DEV_WORKSPACE_REAR)){
            currentCamera = DeviceClient.CAMERA_REAR_VIEW;
        }
        return currentCamera;
    }

    public static String getAutoRearCameraKey(String deviceID){
        String key = "";
        if(!TextUtils.isEmpty(deviceID)){
            key = deviceID + "_rear_camera";
        }
        return key;
    }
}
