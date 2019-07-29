package com.jieli.stream.dv.gdxxx.ui.base;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

/**
 * Activity基类
 * date : 2017/02/27
 */
public abstract class BaseActivity extends FragmentActivity implements IConstant, IActions {
    private final String TAG = getClass().getSimpleName();
    private Toast mToast;
    public WifiHelper mWifiHelper;
    public a mApplication;
    private BaseWifiBroadcastReceiver mReceiver;

    private NotifyDialog mNotifyDialog;
    //private boolean isNeedReconnection = false;

    /**
     * Wifi广播处理
     */
    private class BaseWifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null) return;
            SharedPreferences sharedPreferences;
            String action = intent.getAction();
            if(TextUtils.isEmpty(action)) return;
            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    if (mWifiHelper == null) {
                        mWifiHelper = WifiHelper.getInstance(getApplicationContext());
                    }
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    if (networkInfo == null) {
                        Dbug.e(TAG, "networkInfo is null");
                        mWifiHelper.notifyWifiError(ERROR_NETWORK_INFO_EMPTY);
                        return;
                    }
//                    Dbug.w(TAG, "networkInfo : " + networkInfo.toString());
                    boolean isWifiConnected = (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED);
                    if (isWifiConnected) {
                        if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                            Dbug.e(TAG, "networkType is not TYPE_WIFI");
                            mWifiHelper.notifyWifiError(ERROR_NETWORK_TYPE_NOT_WIFI);
                            return;
                        }
                        if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getSSID())) {
                            Dbug.e(TAG, "wifiInfo is  empty or ssid is null");
                            mWifiHelper.notifyWifiError(ERROR_WIFI_INFO_EMPTY);
                            return;
                        }
//                        Dbug.i(TAG, "wifiInfo : " + wifiInfo.toString());
                        String preSsid = PreferencesHelper.getSharedPreferences(mApplication).getString(CURRENT_WIFI_SSID, "");
                        //防止已连接的情况下，不经手动点击就进入主界面
                        if (!TextUtils.isEmpty(preSsid)) mWifiHelper.notifyWifiConnect(wifiInfo);
                        else Dbug.i(TAG, "It has been try to connect device");
                    }
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    if (mWifiHelper == null) {
                        mWifiHelper = WifiHelper.getInstance(getApplicationContext());
                    }
                    SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    int supplicantError = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    Dbug.i(TAG, "supplicantError=" + supplicantError + ", state=" + state);
                    if (SupplicantState.DISCONNECTED.equals(state) && supplicantError == WifiManager.ERROR_AUTHENTICATING) {
                        sharedPreferences = PreferencesHelper.getSharedPreferences(getApplicationContext());
                        String cSSID = sharedPreferences.getString(CURRENT_WIFI_SSID, null);
                        if (!TextUtils.isEmpty(cSSID)) {
                            mWifiHelper.removeSavedNetWork(cSSID);
                            PreferencesHelper.remove(getApplicationContext(), cSSID);
                            PreferencesHelper.remove(getApplicationContext(), CURRENT_WIFI_SSID);
                        }
                        mWifiHelper.notifyWifiError(ERROR_WIFI_PWD_NOT_MATCH);
                    }
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    if (mWifiHelper == null) {
                        mWifiHelper = WifiHelper.getInstance(getApplicationContext());
                    }
                    if (!mWifiHelper.isWifiOpen()) {
                        int wifiState;
                        if (WifiHelper.isNetWorkConnectedType(getApplicationContext(), ConnectivityManager.TYPE_MOBILE)) {
                            wifiState = ERROR_NETWORK_TYPE_NOT_WIFI;
                        } else {
                            wifiState = ERROR_NETWORK_NOT_OPEN;
                        }
                        mWifiHelper.notifyWifiError(wifiState);
                    }
                    break;
                case ACTION_EMERGENCY_VIDEO_STATE:
                    int videoState = intent.getIntExtra(ACTION_KEY_VIDEO_STATE, -1);
                    int errorCode = intent.getIntExtra(ACTION_KEY_ERROR_CODE, -1);
                    String msg = intent.getStringExtra(ACTION_KEY_EMERGENCY_MSG);
                    BaseActivity mActivity = mApplication.getTopActivity();
                    if(mActivity == null){
                        mActivity = BaseActivity.this;
                    }
                    if(errorCode == -1){
                        switch (videoState){
                            case STATE_START:
                                showNotifyDialog(mActivity);
                                break;
                            case STATE_END:
                                dismissNotifyDialog();
                                if(!TextUtils.isEmpty(msg)){
                                    mApplication.showToastShort(getString(R.string.end_crash_video_task, msg));
                                }
                                break;
                        }
                    }else{
                        if(errorCode == ERROR_DEVICE_OFFLINE){
                            mApplication.setAbnormalExitThread(false);
                        }
                        dismissNotifyDialog();
                        if(!TextUtils.isEmpty(msg)){
                            mApplication.showToastShort(getString(R.string.error_crash_video_task, msg));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = a.getApplication();
//        if (a.isOpenLeakCanary) mApplication.getRefWatcher().watch(this);
        mWifiHelper = WifiHelper.getInstance(mApplication);
        mApplication.pushActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadCastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mReceiver != null) {
            a.getApplication().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissNotifyDialog();
        mApplication.popActivity(this);
    }

    private void registerBroadCastReceiver() {
        if (mReceiver == null) {
            mReceiver = new BaseWifiBroadcastReceiver();
        }
        IntentFilter baseFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION); //Wifi 网络状态改变广播
        baseFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);                    //Wifi 认证状态广播
        baseFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);                        //网络改变广播
        baseFilter.addAction(ACTION_EMERGENCY_VIDEO_STATE);
        a.getApplication().registerReceiver(mReceiver, baseFilter);
    }

    /*
     * ====================================== command api ========================================
     */
    /**
     * 显示内容
     *
     * @param msg      内容
     * @param duration 显示间隔
     */
    @SuppressLint("ShowToast")
    public void showToast(String msg, int duration) {
        if (!TextUtils.isEmpty(msg) && !isFinishing() && duration >= 0) {
            if (mToast == null) {
                mToast = Toast.makeText(this.getApplicationContext(), msg, duration);
            } else {
                mToast.setText(msg);
                mToast.setDuration(duration);
            }
            mToast.setGravity(Gravity.CENTER, 0, 0);
            mToast.show();
        }
    }

    public void showToastShort(String info) {
        showToast(info, Toast.LENGTH_SHORT);
    }

    protected void showToastShort(int info) {
        showToastShort(getResources().getString(info));
    }

    public void showToastLong(String msg) {
        showToast(msg, Toast.LENGTH_LONG);
    }

    public void showToastLong(int msg) {
        showToastLong(getResources().getString(msg));
    }

    /**
     * 切换fragment
     *
     * @param containerId layout id
     * @param fragment    fragment
     * @param fragmentTag fragment tag
     */
    public void changeFragment(int containerId, Fragment fragment, String fragmentTag) {
        if (fragment != null && !isFinishing() && !isDestroyed()) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (fragmentTransaction != null) {
                    if (!TextUtils.isEmpty(fragmentTag)) {
                        fragmentTransaction.replace(containerId, fragment, fragmentTag);
                    } else {
                        fragmentTransaction.replace(containerId, fragment);
                    }
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commitAllowingStateLoss();
                }
            }
        }
    }

    public void changeFragment(int containerId, Fragment fragment) {
        changeFragment(containerId, fragment, null);
    }

    public void showNotifyDialog(BaseActivity activity){
        if(activity != null && !activity.isFinishing()){
            if(mNotifyDialog == null){
                mNotifyDialog = NotifyDialog.newInstance(R.string.dialog_warning, true, R.string.start_crash_video_task);
                mNotifyDialog.setCancelable(false);
            }
            if(!mNotifyDialog.isShowing()){
                mNotifyDialog.show(activity.getSupportFragmentManager(), "notify_dialog");
            }
        }
    }

    public void dismissNotifyDialog(){
        if(mNotifyDialog != null){
            if(mNotifyDialog.isShowing() && !isFinishing()){
                mNotifyDialog.dismiss();
            }
            mNotifyDialog = null;
        }
    }






    //自动生成按下按钮背影效果代码(如果收不到MotionEvent.ACTION_UP事件可以将onTouch返回值变成true即可)
    public View.OnTouchListener BtnAutoBackgroundEvent=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //方式一:设置整个按钮透明度
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.1f);
                //aView.setImageAlpha();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1);
            }
            return false;
        }
    };
    public View.OnFocusChangeListener BtnFocusAutoBackgroundEvent=new View.OnFocusChangeListener(){
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            //方式一:设置整个按钮透明度
            if (hasFocus) {
                v.setAlpha(0.1f);
            } else {
                v.setAlpha(1);
            }
        }
    };
    //View自动加上按下按钮效果
    public View findViewById_AutoBack(int iResID, final View.OnClickListener btnEvent){
        View aView=this.findViewById(iResID);
        aView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnEvent!=null) {
                    btnEvent.onClick(v);
                }
                v.setAlpha(1f);
            }
        });
        aView.setOnTouchListener(BtnAutoBackgroundEvent);
        aView.setOnFocusChangeListener(BtnFocusAutoBackgroundEvent);
        return aView;
    }
    public ImageButton findImageButton_AutoBack( int iResID, View.OnClickListener btnEvent){
        return (ImageButton)this.findViewById_AutoBack(iResID, btnEvent);
    }
}
