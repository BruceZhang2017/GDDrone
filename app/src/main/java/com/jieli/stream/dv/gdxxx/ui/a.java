package com.jieli.stream.dv.gdxxx.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.stream.dv.gdxxx.bean.DeviceDesc;
import com.jieli.stream.dv.gdxxx.bean.DeviceSettingInfo;
import com.jieli.stream.dv.gdxxx.ui.activity.FlashActivity;
import com.jieli.stream.dv.gdxxx.ui.activity.MainActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.service.CommunicationService;
import com.jieli.stream.dv.gdxxx.ui.service.ScreenShotService;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

import java.io.File;
import java.util.Stack;

import static com.jieli.stream.dv.gdxxx.util.IConstant.DIR_FRONT;
import static com.jieli.stream.dv.gdxxx.util.IConstant.DIR_REAR;
import static com.jieli.stream.dv.gdxxx.util.IConstant.KEY_APP_LANGUAGE_CODE;
import static com.jieli.stream.dv.gdxxx.util.IConstant.KEY_ROOT_PATH_NAME;
import static com.jieli.stream.dv.gdxxx.util.IConstant.SERVICE_CMD_CLOSE_SCREEN_TASK;

/**
 * Application类
 * date : 2017-02-27
 */
public class a extends Application {
    private static a sMyApplication = null;
    private Toast mToastShort, mToastLong;
    private DeviceDesc deviceDesc;

    private int appVersion;
    private int searchMode;
    private String appName;
    private String appVersionName;
    private String UUID;
    private boolean sdcardExist;
    private boolean isUpgrading;
    private boolean isAbnormalExitThread;
    private Stack<BaseActivity> activityStack;

    public String GENERIC_CMD_D1="";
    public String GENERIC_CMD_D2="";
    public String GENERIC_CMD_D3="";
    public String GENERIC_CMD_D4="";
    public String GENERIC_CMD_D5="";
    public String GENERIC_CMD_D6="";
    public String GENERIC_CMD_D7="";
    public String GENERIC_CMD_D8="";
    public String GENERIC_CMD_D9="";
    public String GENERIC_CMD_D10="";
    public String GENERIC_CMD_D11="";
    public String GENERIC_CMD_D12="";


    public final int Camera_brt_Default=0;//亮度
    public final int Camera_exp_Default=0;//曝光
    public final int Camera_ctr_Default=255;//对比度
    public final int Camera_wbl_Default=5;//白平衡

    public final boolean Camera_Watermark_Default=false;


    //主页Sensor参数设置面板
    public int Camera_brt=Camera_brt_Default;//亮度
    public int Camera_exp=Camera_exp_Default;//曝光
    public int Camera_ctr=Camera_ctr_Default;//对比度
    public int Camera_wbl=Camera_wbl_Default;//白平衡

    public boolean Camera_Watermark=Camera_Watermark_Default;//水印

    public boolean isCameraInit=false;

    public String WiFiPassword=null;

    public int UAV_height=0;//无人机高度

    private DeviceSettingInfo deviceSettingInfo;
    public static boolean isFactoryMode = true;
    private boolean isWifiDirectGO = false;

    public boolean lastPictureIsSelOne=false;
    public boolean lastVideoIsSelOne =false;


    public boolean lastPictureIsGetFirst=false;
    public RelativeLayout lastPicture_First_rlMain =null;
    public RelativeLayout lastPicture_Check_rlMain =null;

    public boolean lastVideoIsGetFirst=false;
    public RelativeLayout lastVideo_First_rlMain =null;
    public RelativeLayout lastVideo_Check_rlMain =null;

    public String lastPictureSel=null;
    public String lastVideoSel =null;

    public void setCheckPicture_FirstFile(){
//         if(lastPicture_Check_rlMain!=null){
//             lastPicture_Check_rlMain.setBackgroundResource(0);
//         }
//         if(lastPicture_First_rlMain!=null){
//             lastPicture_First_rlMain.setBackgroundResource(R.color.bb_darkBackgroundColor);
//         }
    }
    public void setCheckVideo_FirstFile(){
//        if(lastVideo_Check_rlMain!=null){
//            lastVideo_Check_rlMain.setBackgroundResource(0);
//        }
//        if(lastVideo_First_rlMain!=null){
//            lastVideo_First_rlMain.setBackgroundResource(R.color.bb_darkBackgroundColor);
//        }
    }


//    public static boolean isOpenLeakCanary = false;
//    private RefWatcher mRefWatcher;
//
//    public RefWatcher getRefWatcher() {
//        return mRefWatcher;
//    }

    public static String mAppFilePath="";
    public static String mVideoPath=":/WeiCam/Movies/";
    public static String mPicturePath=":/WiFiCam/Pictures/";
    public static String mVideoPath_Rear=":/WeiCam/Movies/";
    public static String mPicturePath_Rear=":/WiFiCam/Pictures/";
    public static String ThumbPath =":/WiFiCam/Thumb/";

    @Override
    public void onCreate() {
        super.onCreate();
//        if (isOpenLeakCanary) {
//            mRefWatcher = LeakCanary.install(this);
//            mRefWatcher.watch(this);
//            if (LeakCanary.isInAnalyzerProcess(this)) {
//                // This process is dedicated to LeakCanary for heap analysis.
//                // You should not init your app in this process.
//                return;
//            }
//        }
        sMyApplication = this;
        SDKInitializer.initialize(sMyApplication);
        appName = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_ROOT_PATH_NAME, null);
        PackageManager pm = this.getPackageManager();
        if (TextUtils.isEmpty(appName)) {
            appName = getApplicationInfo().loadLabel(pm).toString();
        }
        try {
            appVersion = pm.getPackageInfo(getPackageName(), 0).versionCode;
            appVersionName = pm.getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        mAppFilePath = getSystemPhotoPath("/GH240HD/");
        addPath(mAppFilePath);

        mPicturePath = getSystemPhotoPath("/GH240HD_Pictures/");
        //addPath(mPicturePath);

        mVideoPath = getSystemPhotoPath("/GH240HD_Movies/");
        //addPath(mVideoPath);

        mPicturePath_Rear = getSystemPhotoPath("/GH240HD_Pictures_Rear/");
        //addPath(mPicturePath_Rear);

        mVideoPath_Rear = getSystemPhotoPath("/GH240HD_Movies_Rear/");
        //addPath(mVideoPath_Rear);

        deviceDesc = new DeviceDesc();
        deviceSettingInfo = new DeviceSettingInfo();
        changeLanguage();

        Camera_brt=PreferencesHelper.getSharedPreferences(getApplicationContext()).getInt("Camera_brt", Camera_brt_Default);
        Camera_exp=PreferencesHelper.getSharedPreferences(getApplicationContext()).getInt("Camera_exp", Camera_exp_Default);
        Camera_ctr=PreferencesHelper.getSharedPreferences(getApplicationContext()).getInt("Camera_ctr", Camera_ctr_Default);
        Camera_wbl=PreferencesHelper.getSharedPreferences(getApplicationContext()).getInt("Camera_wbl", Camera_wbl_Default);

        Camera_Watermark=PreferencesHelper.getSharedPreferences(getApplicationContext()).getBoolean("Camera_Watermark", Camera_Watermark_Default);


        //MobSDK.init(this);
    }

    public static synchronized a getApplication() {
        return sMyApplication;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @SuppressLint("ShowToast")
    public void showToastShort(String info) {
        if (mToastShort != null) {
            mToastShort.setText(info);
        } else {
            mToastShort = Toast.makeText(this, info, Toast.LENGTH_SHORT);
            mToastShort.setGravity(Gravity.CENTER, 0, 10);
        }
        mToastShort.show();
    }

    public void showToastShort(int info) {
        showToastShort(getResources().getString(info));
    }

    @SuppressLint("ShowToast")
    public void showToastLong(String msg) {
        if (mToastLong != null) {
            mToastLong.setText(msg);
        } else {
            mToastLong = Toast.makeText(this, msg, Toast.LENGTH_LONG);
            mToastLong.setGravity(Gravity.CENTER, 0, 0);
        }
        mToastLong.show();
    }

    public void showToastLong(int msg) {
        showToastLong(getResources().getString(msg));
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public boolean isSdcardExist() {
        return sdcardExist;
    }

    public void setSdcardExist(boolean sdcardExist) {
        this.sdcardExist = sdcardExist;
    }

    public DeviceSettingInfo getDeviceSettingInfo() {
        return deviceSettingInfo;
    }

    public String getAppName() {
        return appName;
    }

    public int getAppVersion() {
        return appVersion;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setDeviceDesc(DeviceDesc deviceDesc) {
        if(deviceDesc != null){
            this.deviceDesc = deviceDesc;
        }
    }

    public DeviceDesc getDeviceDesc() {
        return deviceDesc;
    }

    public boolean isUpgrading() {
        return isUpgrading;
    }

    public void setUpgrading(boolean upgrading) {
        isUpgrading = upgrading;
    }

    public void sendCommandToService(int cmd) {
       sendCommandToService(cmd, null);
    }

    public void sendCommandToService(int cmd, String ip){
        Intent intent = new Intent(getApplicationContext(), CommunicationService.class);
        intent.putExtra(IConstant.SERVICE_CMD, cmd);
        if(!TextUtils.isEmpty(ip)){
            intent.putExtra(IConstant.KEY_CONNECT_IP, ip);
        }
        getApplicationContext().startService(intent);
    }

    public void sendScreenCmdToService(int cmd) {
        Intent intent = new Intent(this, ScreenShotService.class);
        intent.putExtra(IConstant.SERVICE_CMD, cmd);
        getApplicationContext().startService(intent);
    }

    public void pushActivity(BaseActivity baseActivity) {
        if (activityStack == null)
            activityStack = new Stack<>();
        activityStack.add(baseActivity);
        //  Dbug.e("activityStack", "add activity = " + baseActivity.toString());
    }

    public void popActivity(BaseActivity baseActivity) {
        activityStack.remove(baseActivity);
        // Dbug.e("activityStack", "remove activity = " + baseActivity.toString());
    }

    public BaseActivity getTopActivity() {
        return activityStack.lastElement();
    }

    public void popAllActivity() {
        for (BaseActivity activity : activityStack) {
            if (activity != null) {
                activity.finish();
            }
        }
    }
    public static Handler MainHandler=new Handler();

    public void popActivityOnlyMain() {
        for (BaseActivity activity : activityStack) {
            if (activity != null && !(activity instanceof MainActivity) && !(activity instanceof FlashActivity)) {
                activity.finish();
            }
        }
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCameraDir(){
        String dir;
        if(deviceSettingInfo.getCameraType() == DeviceClient.CAMERA_REAR_VIEW){
            dir = DIR_REAR;
        }else {
            dir = DIR_FRONT;
        }
        return dir;
    }

    public boolean isAbnormalExitThread() {
        return isAbnormalExitThread;
    }

    public void setAbnormalExitThread(boolean abnormalExitThread) {
        isAbnormalExitThread = abnormalExitThread;
    }

    public int getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(int searchMode) {
        this.searchMode = searchMode;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Dbug.w(getClass().getSimpleName(), "onConfigurationChanged orientation : " + newConfig.orientation);
        Intent intent = new Intent(this, ScreenShotService.class);
        intent.putExtra(IConstant.SERVICE_CMD, IConstant.SERVICE_CMD_SCREEN_CHANGE);
        intent.putExtra(IConstant.SCREEN_ORIENTATION, newConfig.orientation);
        getApplicationContext().startService(intent);
        //系统配置改变时需要重新设置语言
        changeLanguage();
    }

    private void changeLanguage() {
        String index = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_APP_LANGUAGE_CODE, "-1");
        if (!"-1".equals(index)) {
            AppUtils.changeAppLanguage(getApplicationContext(), index);
        }
    }

    public void switchWifi() {
        ClientManager.getClient().close();
        sendScreenCmdToService(SERVICE_CMD_CLOSE_SCREEN_TASK);
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                WifiHelper.getInstance(sMyApplication).removeCurrentNetwork(sMyApplication);
            }
        }, 1000);
    }

    public boolean isWifiDirectGO() {
        return isWifiDirectGO;
    }





    public boolean isCamera=false;

    //WIFI网络是否可用 //必要时可配合t.startListen(new NetChangeBroadcast() 使用
    public boolean isConnectedWifi() {
        //if (context != null) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null && mWiFiNetworkInfo.isAvailable() && mWiFiNetworkInfo.getState()== NetworkInfo.State.CONNECTED) {
            //System.out.println(mWiFiNetworkInfo.getState()+"----"+mWiFiNetworkInfo.getDetailedState()+"-----");
//                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//                if(wifiManager.isWifiEnabled()){
//                    WifiInfo aWifiInfo=wifiManager.getConnectionInfo();
//                    if(aWifiInfo.getNetworkId() != -1){
//                        return true;// 是否网络连接
//                    }
//                }
            return true;
        }
        //}
        return false;
    }
    public String getWifiName(){
        String sWifiName = "";
        if(isConnectedWifi()) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            sWifiName = wifiInfo.getSSID();
            if (sWifiName!=null && sWifiName.length()>1 && sWifiName.substring(0, 1).equals("\"") && sWifiName.substring(sWifiName.length() - 1).equals("\"")) {
                sWifiName = sWifiName.substring(1, sWifiName.length() - 1);
            }
        }else{
            return null;
        }
        return sWifiName;
    }


    //解决微信分享失败问题，即先保存进相册再扫描进数据库---start
    //路径拼接
    public static String mixPath(String sPath1,String sPath2){
        boolean sExist1=sPath1.endsWith(File.separator);
        boolean sExist2=sPath2.startsWith(File.separator);
        if(sExist1 && sExist2) {
            return sPath1+sPath2.substring(1);
        }else if(sExist1 || sExist2) {
            return sPath1+sPath2;
        }else {
            return sPath1+File.separator+sPath2;
        }
    }

    public String getAppFilePath(){
        return mAppFilePath;
    }
    public String getPicturePath(){
        if(deviceSettingInfo.getCameraType() == DeviceClient.CAMERA_REAR_VIEW){
            return mPicturePath_Rear;
        }else {
            return mPicturePath;
        }
    }
    public String getVideoPath(){
        if(deviceSettingInfo.getCameraType() == DeviceClient.CAMERA_REAR_VIEW){
            return mVideoPath_Rear;
        }else {
            return mVideoPath;
        }
    }
    public String getPicturePath(String sShortPath){
        return mixPath(getPicturePath(),sShortPath);
    }
    public String getVideoPath(String sShortPath){
        return mixPath(getVideoPath(),sShortPath);
    }
    //相册路径
    public static String getSystemPhotoPath(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
    }
    //获取相册文件夹
    public static String getSystemPhotoPath(String sShortPath){
        return mixPath(getSystemPhotoPath(),sShortPath);
    }
    //
    public static boolean addPath(String sPath){
        if(sPath==null || sPath.length()<1){
            return false;
        }
        String sRealPath=sPath;
        File aFile = new File(sRealPath);
        //判断文件夹是否存在,如果不存在则创建文件夹
        if (aFile.exists()==false) {
            try {
                return aFile.mkdirs();//必须确定APP具有权限,如不能在用户未设置默认SD的外置SD访问;
                //return aFile.createNewFile();
            } catch (Exception e) {
                Log.e("addPath","添加文件路径出错");
                e.printStackTrace();
            }
            return aFile.exists();
        }
        return true;
    }
    //获取
    public static String getName(String sFile){
        int iPos=sFile.lastIndexOf("/");
        if(iPos<0){
            iPos=sFile.lastIndexOf("\\");
        }
        return iPos>-1?sFile.substring(iPos+1):sFile;
    }
    public static boolean isVideoFile(String sFile){
        String sFileExt=getExt(sFile);
        if(sFileExt.equals("mp4") || sFileExt.equals("mov") || sFileExt.equals("avi")){
            return true;
        }
        return false;
    }
    public static String getExt(String sFile){
        int iPos=sFile.lastIndexOf(".");
        return iPos>-1?sFile.substring(iPos+1).toLowerCase():sFile;
    }
    //直接在安卓图库的文件在相册显示
    public boolean insertPhotoPath(String sFileFull){
        if(sFileFull==null || sFileFull.length()<1){
            return false;
        }
        File aFile=new File(sFileFull);
        if(aFile==null || aFile.exists()==false){
            return false;
        }
        //其次把文件插入到系统图库
        try {
            String sFilePath=aFile.getAbsolutePath();
            String sFileName=getName(sFileFull);
            String sVideoDesc=sFileName;//null;
            if(isVideoFile(sFileFull)) {
                MediaScannerConnection.scanFile(getApplication(), new String[] {sFilePath}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                if(uri!=null) {
                                    getApplication().sendBroadcast(new Intent("android.hardware.action.NEW_VIDEO", uri));
                                    if(android.os.Build.VERSION.SDK_INT>=19) {
                                        getApplication().getContentResolver().takePersistableUriPermission(uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    }
                                }
                            }
                        }
                );
            }else{
                MediaStore.Images.Media.insertImage(getApplication().getContentResolver(),sFilePath , sFileName, null);
                //通知系统图库更新在相册显示出来
                scanSDFile(sFileFull);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //最后通知图库更新
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        return true;
    }
    public void scanSDFile(String sFullFile){
        MediaScannerConnection.scanFile(getApplication(), new String[]{sFullFile}, null, null);
    }

    //解决微信分享失败问题，即先保存进相册再扫描进数据库---end
}
