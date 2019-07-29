package com.jieli.stream.dv.gdxxx.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.listener.OnConnectStateListener;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.DeviceBean;
import com.jieli.stream.dv.gdxxx.interfaces.OnWifiCallBack;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.InputPasswordDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.ui.fragment.DeviceListFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.QRCodeFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.VideoFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.browse.BrowseFileFragment;
import com.jieli.stream.dv.gdxxx.ui.service.CommunicationService;
import com.jieli.stream.dv.gdxxx.ui.service.ScreenShotService;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.IWifiDirectListener;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.ThumbLoader;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;
import com.jieli.stream.dv.gdxxx.util.WifiP2pHelper;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

public class MainActivityOld extends BaseActivity implements OnWifiCallBack {
    String tag = getClass().getSimpleName();
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressedTimes;
    private Bundle mBundle;
    private BottomBar mBottomBar;

    private WaitingDialog waitingDialog;
    private NotifyDialog upgradeNotifyDialog;
    private WifiP2pHelper mWifiP2pHelper;

    private NotifyDialog openWifiDialog;
    private NotifyDialog reconnectionDialog;

    private boolean isReConnectDev = false;
    private int reConnectNum;

    private static final int MSG_RECONNECTION_DEVICE = 0;
    private static final int MSG_STOP_RECONNECTION_DEVICE = 1;
    private static final int MSG_SWITCH_TABS = 2;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_STOP_RECONNECTION_DEVICE:
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    reConnectNum = 0;
                    isReConnectDev = false;
                    removeDeviceWifiMsg();
                    mApplication.switchWifi();
                    dismissWaitingDialog();
                    showReconnectionDialog();
                    break;
                case MSG_RECONNECTION_DEVICE:
                    //需要重连，重连3次，不成功返回DeviceListFragment
                    Dbug.i(tag, "reconnecting reConnectNum=" + reConnectNum);
                    if (mWifiHelper.isWifiOpen()) {
                        reConnectNum++;
                        if (reConnectNum < 3) {
                            SharedPreferences sharedPreferences = PreferencesHelper.getSharedPreferences(getApplicationContext());
                            String saveSSID = sharedPreferences.getString(CURRENT_WIFI_SSID, null);
                            if(!TextUtils.isEmpty(saveSSID)){
                                isReConnectDev = true;
                                String savePwd = sharedPreferences.getString(saveSSID, null);
                                showWaitingDialog();
                                int reconnectType = sharedPreferences.getInt(RECONNECT_TYPE, 0);
                                if(reconnectType == 1){
                                    mHandler.sendEmptyMessage(MSG_STOP_RECONNECTION_DEVICE);
                                }else {
                                    mWifiHelper.connectWifi(mApplication, saveSSID, savePwd);
                                }
                            }else{
                                mHandler.sendEmptyMessage(MSG_STOP_RECONNECTION_DEVICE);
                            }
                        } else {
                            Dbug.i(tag, "stop reconnect ");
                            mHandler.sendEmptyMessage(MSG_STOP_RECONNECTION_DEVICE);
                        }
                    } else {
                        showOpenWifiDialog();
                    }
                    break;
                case MSG_SWITCH_TABS:
                    int tabId = msg.arg1;
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                    switch (tabId) {
                        case R.id.tab_device:
                            if (ClientManager.getClient().isConnected()) {
                                fragment = getSupportFragmentManager().findFragmentByTag(VideoFragment.class.getSimpleName());
                                if (fragment == null) {
                                    fragment = new VideoFragment();
                                }
                            } else if (!(fragment instanceof DeviceListFragment)) {
                                fragment = getSupportFragmentManager().findFragmentByTag(DeviceListFragment.class.getSimpleName());
                                if (fragment == null) {
                                    fragment = new DeviceListFragment();
                                }
                            }
                            changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
                            break;
                        case R.id.tab_gallery:
                            if (!(fragment instanceof BrowseFileFragment)) {
                                fragment = new BrowseFileFragment();
                            }
                            changeFragment(R.id.container, fragment, BrowseFileFragment.class.getSimpleName());
                            //mToolbar.setTitle(R.string.sleep);
                            break;
                        default:
                            Dbug.e(tag, "Not found tab id:" + tabId);
                            break;
                    }
                    break;
            }
        }
    };


    private BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {
                    switch (action) {
                        case ACTION_UPGRADE_FILE:
                            Bundle bundle = intent.getBundleExtra(KEY_DATA);
                            if (bundle != null) {
                                ArrayList<String> pathList = bundle.getStringArrayList(UPDATE_PATH);
                                if (pathList != null && pathList.size() > 0) {
                                    int updateType = bundle.getInt(UPDATE_TYPE);
                                    String descTxt = null;
                                    if (updateType == UPGRADE_APK_TYPE) {
                                        if (pathList.size() > 1) {
                                            descTxt = AppUtils.readTxtFile(pathList.get(1));
                                        }
                                    } else {
                                        descTxt = getString(R.string.firmware_upgrade_tip);
                                    }
                                    if (!TextUtils.isEmpty(descTxt)) {
                                        mBundle = bundle;
                                        showNotifyDialog(descTxt);
                                    }
                                }
                            }
                            break;
                        case ACTION_SDK_UPGRADE_SUCCESS:
                            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                            if (!(fragment instanceof DeviceListFragment)) {
                                fragment = getSupportFragmentManager().findFragmentByTag(DeviceListFragment.class.getSimpleName());
                                if (fragment == null) {
                                    fragment = new DeviceListFragment();
                                }
                            }
                            mBottomBar.selectTabAtPosition(mBottomBar.findPositionForTabWithId(R.id.tab_device));
                            changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
                            break;
                        case ACTION_DEV_ACCESS:
                            boolean isAllow = intent.getBooleanExtra(KEY_ALLOW_ACCESS, false);
                            Dbug.w(tag, "isAllow : " +isAllow);
                            if (isAllow) {
                                BaseFragment subFragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                                Dbug.w(tag, "ACTION_DEV_ACCESS : " +subFragment);
                                if (subFragment instanceof DeviceListFragment) {
                                    boolean isAutoRearCamera = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(AppUtils.getAutoRearCameraKey(mApplication.getUUID()), false);
                                    if(isAutoRearCamera){
                                        mApplication.getDeviceSettingInfo().setCameraType(DeviceClient.CAMERA_REAR_VIEW);
                                    }else{
                                        mApplication.getDeviceSettingInfo().setCameraType(DeviceClient.CAMERA_FRONT_VIEW);
                                    }
                                    subFragment = new VideoFragment();
                                    mBottomBar.selectTabAtPosition(mBottomBar.findPositionForTabWithId(R.id.tab_device));
                                    changeFragment(R.id.container, subFragment, subFragment.getClass().getSimpleName());
                                }
                            } else {
                                mApplication.showToastShort(R.string.dev_refused_access);
                                mApplication.switchWifi();
                            }
                            break;
                        case ACTION_ACCOUT_CHANGE:
                            changeAccountAction();
                            break;
                        case ACTION_LANGUAAGE_CHANGE:
                            initUI();
                            break;
                    }
                }
            }
        }
    };
    private OnTabSelectListener onTabSelectListener = new OnTabSelectListener() {
        @Override
        public void onTabSelected(@IdRes int tabId) {
            mHandler.removeMessages(MSG_SWITCH_TABS);
            Message m = Message.obtain();
            m.what = MSG_SWITCH_TABS;
            m.arg1 = tabId;
            mHandler.sendMessageDelayed(m, 300);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, CommunicationService.class));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //修改APP语言
        String lan = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_APP_LANGUAGE_CODE, "-1");
        if (!"-1".equals(lan))
            AppUtils.changeAppLanguage(getApplicationContext(), lan);


        Dbug.i(tag, "main activity running....");
        //Monitor phone music play state
        //startService(new Intent(this, MonitorMusicStateService.class));
        setContentView(R.layout.activity_mainold);

        //
        if(IConstant.isWifiP2pEnable){
            mWifiP2pHelper = WifiP2pHelper.getInstance(mApplication.getApplicationContext());
            if(mWifiP2pHelper != null){
                mWifiP2pHelper.registerBroadcastReceiver(mIWifiDirectListener);
            }
        }

        initUI();

        changeFragment(R.id.container, new DeviceListFragment(), DeviceListFragment.class.getSimpleName());
        IntentFilter filter = new IntentFilter(ACTION_UPGRADE_FILE);
        filter.addAction(ACTION_SDK_UPGRADE_SUCCESS);
        filter.addAction(ACTION_DEV_ACCESS);
        filter.addAction(ACTION_ACCOUT_CHANGE);
        filter.addAction(ACTION_LANGUAAGE_CHANGE);
        getApplicationContext().registerReceiver(mainReceiver, filter);
    }

    private void initUI() {
        mBottomBar = (BottomBar) findViewById(R.id.bottombar);
        mBottomBar.setItems(R.xml.bottombar_tabs_menu);
        mWifiHelper.registerOnWifiCallback(this);
        mBottomBar.setOnTabSelectListener(onTabSelectListener);
        //mBottomBar.selectTabAtPosition(mBottomBar.findPositionForTabWithId(R.id.tab_device));
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof QRCodeFragment) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        if (mBackPressedTimes + TIME_INTERVAL > System.currentTimeMillis()) {
            //super.onBackPressed();
            Dbug.e(tag, "onBackPressed");
            finish();
            return;
        } else {
            showToastShort(R.string.double_tap_to_exit);
        }

        mBackPressedTimes = System.currentTimeMillis();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if(mWifiP2pHelper != null){
            mWifiP2pHelper.release();
            mWifiP2pHelper = null;
        }
        mApplication.switchWifi();
        stopService(new Intent(this, CommunicationService.class));
        stopService(new Intent(this, ScreenShotService.class));
        releaseUI();
        removeDeviceWifiMsg();
//        WifiP2pHelper.getInstance(mApplication).release();
        getApplicationContext().unregisterReceiver(mainReceiver);
        mWifiHelper.unregisterOnWifiCallback(this);
        ClientManager.getClient().unregisterConnectStateListener(deviceConnectStateListener);
        ClientManager.release();
        ThumbLoader.getInstance().release();
        //Dbug.e(tag, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Dbug.e(tag, "onNewIntent");
        setIntent(intent);
        initUI();
    }

    private void releaseUI() {
        if (upgradeNotifyDialog != null) {
            if (upgradeNotifyDialog.isShowing()) {
                upgradeNotifyDialog.dismiss();
            }
            upgradeNotifyDialog = null;
        }
        if(reconnectionDialog != null){
            if(reconnectionDialog.isShowing()){
                reconnectionDialog.dismiss();
            }
            reconnectionDialog = null;
        }
        dismissWaitingDialog();
        System.gc();
    }

    private void showNotifyDialog(String content) {
        if (upgradeNotifyDialog == null) {
            upgradeNotifyDialog = NotifyDialog.newInstance(getString(R.string.upgrade_desc), content,
                    R.string.dialog_cancel, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            upgradeNotifyDialog.dismiss();
                            upgradeNotifyDialog = null;
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            if (mBundle != null) {
                                Intent it = new Intent(MainActivityOld.this, GenericActivity.class);
                                it.putExtra(KEY_FRAGMENT_TAG, UPGRADE_FRAGMENT);
                                it.putExtra(KEY_DATA, mBundle);
                                startActivity(it);
                            }
                            upgradeNotifyDialog.dismiss();
                            upgradeNotifyDialog = null;
                        }
                    });
            upgradeNotifyDialog.setContentTextLeft(true);
        }
        if (!TextUtils.isEmpty(content)) {
            upgradeNotifyDialog.setContent(content);
        }
        if (!upgradeNotifyDialog.isShowing()) {
            upgradeNotifyDialog.show(getSupportFragmentManager(), "notify_dialog");
        }
    }

    public void tryToConnectDevice(final DeviceBean deviceBean) {
        if(deviceBean == null) return;
        //若手机已连接，则进入App连接设备流程
        String ssid = WifiHelper.getInstance(getApplicationContext()).getCurrentConnectedSsid();
        if (!TextUtils.isEmpty(deviceBean.getWifiSSID()) && deviceBean.getWifiSSID().equals(ssid)) {
            WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wm != null) {
                WifiInfo winfo = wm.getConnectionInfo();
                if (winfo != null) {
                    onConnected(winfo);
                    return;
                }
            }
        }
        String selectedSSID = deviceBean.getWifiSSID();
        PreferencesHelper.putStringValue(getApplicationContext(), CURRENT_WIFI_SSID, selectedSSID);
        String wifiType = deviceBean.getWifiType();
        if (!TextUtils.isEmpty(wifiType) && !wifiType.contains("WPA")) {
            sendConnectWifiMsg(selectedSSID, null);
        } else {
            SharedPreferences sharedPreferences = PreferencesHelper.getSharedPreferences(a.getApplication());
            String savePwd = sharedPreferences.getString(selectedSSID, null);
            if (!TextUtils.isEmpty(savePwd)) {
                sendConnectWifiMsg(selectedSSID, savePwd);
            } else {
                Dbug.w(tag, "Please input PWD");
                final InputPasswordDialog inputPasswordDialog = InputPasswordDialog.newInstance(selectedSSID);
                inputPasswordDialog.show(getSupportFragmentManager(), InputPasswordDialog.class.getSimpleName());
                inputPasswordDialog.setOnInputCompletionListener(new InputPasswordDialog.OnInputCompletionListener() {
                    @Override
                    public void onCompletion(String ssid, String password) {
                        inputPasswordDialog.dismiss();
                        sendConnectWifiMsg(ssid, password);
                        PreferencesHelper.putStringValue(getApplicationContext(), ssid, password);
                    }
                });
            }
        }
    }

    public void connectDevice(String ip){
        Dbug.i(tag, "Second, connect device IP=" + ip +", isConnected=" + ClientManager.getClient().isConnected());
        if (!ClientManager.getClient().isConnected()) {
            mApplication.sendCommandToService(SERVICE_CMD_CONNECT_CTP, ip);
            ClientManager.getClient().registerConnectStateListener(deviceConnectStateListener);
        } else if (mApplication.getTopActivity() instanceof MainActivityOld) {
            BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            Dbug.e(tag, "Current fragment=" + fragment);
            if (fragment == null) {
                fragment = new VideoFragment();
                mBottomBar.selectTabAtPosition(mBottomBar.findPositionForTabWithId(R.id.tab_device));
                changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
            }
        } else {
            Dbug.e(tag, "connectDevice: unknown case");
        }
    }

    private final OnConnectStateListener deviceConnectStateListener = new OnConnectStateListener() {
        @Override
        public void onStateChanged(Integer code) {
            Dbug.i(tag, "--onStateChanged-- " + Constants.getConnectDescription(code));
            dismissWaitingDialog();
            switch (code) {
                case Constants.DEVICE_STATE_CONNECTED: {
                    Dbug.i(tag, "Third, connect device success...");
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    mHandler.removeMessages(MSG_STOP_RECONNECTION_DEVICE);
                    isReConnectDev = false;
                    ClientManager.getClient().tryToAccessDevice(String.valueOf(mApplication.getAppVersion()), new SendResponse() {
                        @Override
                        public void onResponse(Integer code) {
                            if (code != SEND_SUCCESS) {
                                Dbug.e(tag, "Send failed!!!");
                            }
                        }
                    });
                    break;
                }
                case Constants.DEVICE_STATE_CONNECTION_TIMEOUT:
                case Constants.DEVICE_STATE_EXCEPTION:
                    Dbug.w(tag, "error disconnected:WifiState=" + mWifiHelper.getWifiState());
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
                    break;
                case Constants.DEVICE_STATE_DISCONNECTED:
                case Constants.DEVICE_STATE_UNREADY:
                    Dbug.e(tag, "Disconnect with device!!! Code=" + code);
                    BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                    Dbug.e(tag, "normal disconnected fragment="+fragment);
                    if ((fragment instanceof BrowseFileFragment)) {
                        Dbug.w(tag, "Stay!!");
                        break;
                    }
                    fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(DeviceListFragment.class.getSimpleName());
                    if (fragment == null) {
                        fragment = new DeviceListFragment();
                    }
                    mBottomBar.selectTabAtPosition(mBottomBar.findPositionForTabWithId(R.id.tab_device));
                    changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
                    break;
            }
        }
    };

    private void showOpenWifiDialog() {
        if (openWifiDialog == null) {
            openWifiDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.open_wifi, R.string.dialog_exit, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                @Override
                public void onClick() {
                    openWifiDialog.dismiss();
                    mApplication.popAllActivity();
                }
            }, new NotifyDialog.OnPositiveClickListener() {
                @Override
                public void onClick() {
                    WifiHelper.getInstance(getApplicationContext()).openWifi();
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
                    openWifiDialog.dismiss();
                }
            });
        }
        if (!openWifiDialog.isShowing())
            openWifiDialog.show(mApplication.getTopActivity().getSupportFragmentManager(), "re_open_wifi");

    }

    private void showReconnectionDialog() {
        if (reconnectionDialog == null) {
            reconnectionDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.connection_timeout, R.string.comfirm, new NotifyDialog.OnConfirmClickListener() {
                @Override
                public void onClick() {
                    if (!(mApplication.getTopActivity() instanceof MainActivityOld)) {
                        mApplication.getTopActivity().finish();
                    }
                    BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(DeviceListFragment.class.getSimpleName());
                    if (fragment == null) {
                        fragment = new DeviceListFragment();
                    }
                    mBottomBar.selectTabAtPosition(mBottomBar.findPositionForTabWithId(R.id.tab_device));
                    changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
                    reconnectionDialog.dismiss();
                }
            });
        }
        if (!reconnectionDialog.isShowing()) {
            reconnectionDialog.show(mApplication.getTopActivity().getSupportFragmentManager(), "re_connection");
        }
    }


    private void sendConnectWifiMsg(String ssid, String pwd) {
        mWifiHelper.connectWifi(mApplication, ssid, pwd);
        showWaitingDialog();
    }

    private void showWaitingDialog() {
        if (waitingDialog == null) {
            waitingDialog = new WaitingDialog();
            waitingDialog.setNotifyContent(getString(R.string.connecting));
            waitingDialog.setCancelable(false);
            waitingDialog.setOnWaitingDialog(new WaitingDialog.OnWaitingDialog() {
                @Override
                public void onCancelDialog() {

                }
            });
        }
        if (!waitingDialog.isShowing()) {
            waitingDialog.show(getSupportFragmentManager(), "waiting_dialog");
        }
    }

    private void dismissWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
            waitingDialog = null;
        }
    }

    private void removeDeviceWifiMsg(){
        String saveSSID = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(CURRENT_WIFI_SSID, null);
        if (!TextUtils.isEmpty(saveSSID)) {
            PreferencesHelper.remove(getApplicationContext(), saveSSID);
            PreferencesHelper.remove(getApplicationContext(), CURRENT_WIFI_SSID);
        }
    }

    @Override
    public void onConnected(WifiInfo info) {
        String ssid = WifiHelper.formatSSID(info.getSSID());
        //Dbug.w(tag, "onConnected: SSID=" + ssid + ", isReConnectDev=" + isReConnectDev);
        if (!TextUtils.isEmpty(ssid) && ssid.contains(WIFI_PREFIX)) {
            isReConnectDev = false;
            reConnectNum = 0;
            connectDevice(mWifiHelper.getGateWay(mApplication));
        }else{
            if(isReConnectDev){
                mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
            }
        }
        dismissWaitingDialog();
    }

    @Override
    public void onError(int errCode) {
        Dbug.e(tag, "onError >>> errCode = " + errCode);
        dismissWaitingDialog();
        switch (errCode) {
            case ERROR_WIFI_IS_CONNECTED:
                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = null;
                if (connectivityManager != null)
                    info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.getExtraInfo().contains(WIFI_PREFIX) && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    connectDevice(mWifiHelper.getGateWay(mApplication));
                }
                break;
            case ERROR_NETWORK_NOT_OPEN:
                Dbug.e(tag, "Wi-Fi is disable !!");
                break;
            case ERROR_WIFI_PWD_NOT_MATCH:
                String ssid = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(CURRENT_WIFI_SSID, "");
                if (ssid.contains(WIFI_PREFIX))
                    showToastShort(R.string.pwd_not_match_error);
                break;
        }
    }

    public void changeAccountAction()
    {
        Dbug.e(tag,"changeAccountAction");
        ClientManager.getClient().disconnect();
       // mBottomBar.selectTabAtPosition(mBottomBar.findPositionForTabWithId(R.id.tab_device));
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DeviceListFragment.class.getSimpleName());
        if (fragment == null) {
            fragment = new DeviceListFragment();
        }
        changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
    }

    private IWifiDirectListener mIWifiDirectListener = new IWifiDirectListener() {
        @Override
        public void onCallP2pStateChanged(int state) {
            if(state != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {  //直连关闭
                mHandler.sendEmptyMessage(MSG_STOP_RECONNECTION_DEVICE);
            }
        }

        @Override
        public void onCallP2pPeersChanged(List<WifiP2pDevice> peerList) {
            if(peerList != null && mApplication.getSearchMode() == IConstant.AP_SEARCH_MODE){
                if(peerList.size() > 0 && isReConnectDev){
                    WifiP2pDevice mReadyConnectP2pDevice = null;
                    String saveSSID = PreferencesHelper.getSharedPreferences(mApplication).getString(CURRENT_WIFI_SSID, null);
                    if(!TextUtils.isEmpty(saveSSID)){
                        for (WifiP2pDevice device : peerList){
                            Dbug.i(tag, "-onCallP2pPeersChanged- device : " + device + ", isGroupOwner : "+device.isGroupOwner());
                            String deviceName = device.deviceName;
                            if(saveSSID.equals(deviceName)){
                                mReadyConnectP2pDevice = device;
                                break;
                            }
                        }
                        if(mReadyConnectP2pDevice != null){
                            mWifiP2pHelper.connectP2pDevice(mReadyConnectP2pDevice, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Dbug.i(tag, "-onCallP2pPeersChanged- connectP2pDevice ok.");
                                }

                                @Override
                                public void onFailure(int i) {
                                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
                                }
                            });
                        }
                    }else{
                        mHandler.sendEmptyMessage(MSG_STOP_RECONNECTION_DEVICE);
                        return;
                    }
                }
            }
            if(isReConnectDev){
                mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
            }
        }

        @Override
        public void onCallP2pConnectionChanged(WifiP2pInfo wifiP2pInfo) {
            if(wifiP2pInfo != null) {
                Dbug.i(tag, "-onCallP2pConnectionChanged- : " + wifiP2pInfo);
                if(isReConnectDev && mApplication.isWifiDirectGO() && wifiP2pInfo.groupFormed){
                    InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
                    if(groupOwnerAddress != null){
                        if(!ClientManager.getClient().isConnected()){
                            connectDevice(groupOwnerAddress.getHostAddress());
                        }
                    }
                }
            }
        }

        @Override
        public void onCallP2pDeviceChanged(WifiP2pDevice wifiP2pDevice) {
            Dbug.i(tag, "-onCallP2pDeviceChanged- : " + wifiP2pDevice + "\n isGroupOwner : " +  wifiP2pDevice.isGroupOwner());
        }
    };
}
