package com.jieli.stream.dv.gdxxx.ui.fragment;


import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.lib.dv.control.mssdp.Discovery;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.DeviceBean;
import com.jieli.stream.dv.gdxxx.interfaces.OnWifiCallBack;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.activity.MainActivityOld;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.SelectWifiDialog;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.IWifiDirectListener;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;
import com.jieli.stream.dv.gdxxx.util.WifiP2pHelper;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.List;

public class DeviceListFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static String tag = DeviceListFragment.class.getSimpleName();
    private ListView deviceListView;
    private DeviceListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchStaDevice mSearchStaDevice;
    private boolean isRefreshing = false;//是否刷新中
    private ImageView mSwitchMode;

    private SelectWifiDialog mSelectWifiDialog;

    private final static int MSG_SEARCH_DEVICE_LIST = 0x457;
    private final static int MSG_ADD_NEW_DEVICE = 0x458;
    private final static int MSG_SWITCH_SEARCH_MODE = 0x459;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message != null && !isDetached()){
                switch (message.what){
                    case MSG_SEARCH_DEVICE_LIST:{
                        refreshList();
                        break;
                    }
                    case MSG_ADD_NEW_DEVICE:{
                        DeviceBean bean = (DeviceBean) message.obj;
                        Dbug.e(tag, "MSG_ADD_NEW_DEVICE=" + bean);
                        if(bean != null){
                            if(mApplication.getSearchMode() == IConstant.STA_SEARCH_MODE){
                                if(mAdapter != null){
                                    mAdapter.add(bean);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }else{
                                String deviceIp = bean.getWifiIP();
                                if(!TextUtils.isEmpty(deviceIp)){
                                    stopStaSearchThread();
                                    if(ClientManager.getClient().isConnected()){
                                        enterLiveVideo();
                                    }else{
                                        ((MainActivityOld) getActivity()).connectDevice(deviceIp);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case MSG_SWITCH_SEARCH_MODE:{
                        Dbug.e(tag, "MSG_SWITCH_SEARCH_MODE=" + mApplication.getSearchMode());
                        stopStaSearchThread();
                        mAdapter.clear();
                        boolean checkDeviceWifi = checkDeviceWifi();
                        Dbug.e(tag, "checkDeviceWifi= "+ checkDeviceWifi + ", mode=" + mApplication.getSearchMode());
                        switch (mApplication.getSearchMode()) {
                            case STA_SEARCH_MODE:
                                mApplication.setSearchMode(AP_SEARCH_MODE);
                                mHandler.sendEmptyMessageDelayed(MSG_SEARCH_DEVICE_LIST, 200);
                                updateSwitchUI();
                                break;
                            case AP_SEARCH_MODE:
                                mApplication.setSearchMode(STA_SEARCH_MODE);
                                toDeviceStaModeFragment();
                                break;
                            default:
                                Dbug.e(tag, "Unknown search mode=" + mApplication.getSearchMode());
                                break;
                        }
                        break;
                    }
                }
            }
            return false;
        }
    });

    public DeviceListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        WifiHelper.getInstance(getContext()).registerOnWifiCallback(mWifiCallBack);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        mSwitchMode = (ImageView) view.findViewById(R.id.device_list_switch_search_mode);
        deviceListView = (ListView) view.findViewById(android.R.id.list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        mSwitchMode.setOnClickListener(this);
        deviceListView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null) return;
        Bundle bundle = getArguments();
        if (bundle != null) {
            int mode = bundle.getInt(KEY_SEARCH_MODE, AP_SEARCH_MODE);
            mApplication.setSearchMode(mode);
            Dbug.e(tag, "onActivityCreated: mode=" + mode);
            updateSwitchUI();
        }
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.black),
                getResources().getColor(android.R.color.darker_gray),
                getResources().getColor(android.R.color.black),
                getResources().getColor(android.R.color.background_light));
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        initListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Dbug.e(tag, "Current mode=" +mApplication.getSearchMode());
        if(IConstant.isWifiP2pEnable && mApplication.getSearchMode() == IConstant.AP_SEARCH_MODE){
            WifiP2pHelper.getInstance(mApplication).registerBroadcastReceiver(mIWifiDirectListener);
            Dbug.i(tag, "registerBroadcastReceiver");
            WifiP2pHelper.getInstance(mApplication).requestPeerList();
            Dbug.i(tag, "requestPeerList");
            WifiP2pHelper.getInstance(mApplication).startDiscoverPeers(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Dbug.i(tag, "start discovery wifi direct list");
                }

                @Override
                public void onFailure(int i) {
                    Dbug.e(tag, "start discovery error, code : " +i);
                }
            });
        }
        if (WifiHelper.getInstance(getContext()).isWifiClosed()) {
            ((BaseActivity) getActivity()).showToastShort(getString(R.string.tip_open_wifi));
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
        } else {
            updateSwitchUI();
//            if (mApplication.getSearchMode() != mSpinner.getSelectedItemPosition()) {
//                mSpinner.setSelection(mApplication.getSearchMode(), false);
//            }
//            mHandler.sendEmptyMessage(MSG_SEARCH_DEVICE_LIST);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(IConstant.isWifiP2pEnable){
            WifiP2pHelper.getInstance(mApplication).stopDiscoverPeers(null);
            WifiP2pHelper.getInstance(getContext()).unregisterBroadcastReceiver(mIWifiDirectListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        WifiHelper.getInstance(getContext()).unregisterOnWifiCallback(mWifiCallBack);
    }

    private void refreshList() {
        int searchMode = mApplication.getSearchMode();
        if(IConstant.AP_SEARCH_MODE == searchMode){
            WifiHelper wifiHelper = WifiHelper.getInstance(a.getApplication());
            List<ScanResult> scanResults = wifiHelper.getSpecifiedSSIDList(a.isFactoryMode?"":WIFI_PREFIX);
            mAdapter.clear();
            Dbug.w(TAG, "scan result=" + scanResults.size());
            for (ScanResult scanResult : scanResults) {
                String scanSSID = WifiHelper.formatSSID(scanResult.SSID);
                DeviceBean bean = new DeviceBean();
                bean.setWifiSSID(scanSSID);
                bean.setWifiType(scanResult.capabilities);
                bean.setMode(DEV_AP_MODE);
                mAdapter.add(bean);
            }
            mAdapter.notifyDataSetChanged();
        }else{
            if(mSearchStaDevice != null && mSearchStaDevice.isSearching){
                Dbug.w(TAG, "SearchStaDevice is running");
                return;
            }
            if (mSearchStaDevice != null) {
                stopStaSearchThread();
            }
            mAdapter.clear();
            mSearchStaDevice = new SearchStaDevice(mHandler);
            mSearchStaDevice.start();
        }
    }

    private final SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (!isRefreshing) {
                isRefreshing = true;
                WifiP2pHelper.getInstance(getContext()).requestPeerList();
                mHandler.sendEmptyMessage(MSG_SEARCH_DEVICE_LIST);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isRefreshing = false;
                        //显示或隐藏刷新进度条
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        }
    };

    private void stopStaSearchThread(){
        if(mSearchStaDevice != null){
            mSearchStaDevice.stopSearch();
            mSearchStaDevice = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View view) {
        if (view != null && getActivity() != null) {
            switch (view.getId()) {
                case R.id.device_list_switch_search_mode:
                    mHandler.removeMessages(MSG_SWITCH_SEARCH_MODE);
                    mHandler.sendEmptyMessageDelayed(MSG_SWITCH_SEARCH_MODE, 200);
                    break;
            }
        }
    }

    private void updateSwitchUI(){
        if(mApplication.getSearchMode() == AP_SEARCH_MODE){
            mSwitchMode.setImageResource(R.drawable.drawable_ap_search_mode);
        }else{
            mSwitchMode.setImageResource(R.drawable.drawable_sta_search_mode);
        }
        if (mAdapter != null) {
            mAdapter.clear();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (getActivity() != null && mAdapter != null) {
            final DeviceBean deviceBean = mAdapter.getItem(position);
            if (deviceBean != null) {
                int mode = deviceBean.getMode();
                //Dbug.e(tag, "mode=" + mode + ", getAddress=" +ClientManager.getClient().getAddress() + ", isConnected="+ClientManager.getClient().isConnected());
                if(mode == DEV_AP_MODE){
                    boolean isConnected = checkDeviceConnected(deviceBean.getMode(), deviceBean.getWifiSSID());
                    if (isConnected) {
                        enterLiveVideo();
                    } else {
                        if (ClientManager.getClient().isConnected()) {
                            ClientManager.getClient().close();
                        }
                        ((MainActivityOld) getActivity()).tryToConnectDevice(deviceBean);
                    }
                }else{
                    stopStaSearchThread();
                    String ip = deviceBean.getWifiIP();
                    if(!TextUtils.isEmpty(ip) && !ip.equals(ClientManager.getClient().getAddress())){
                        if (ClientManager.getClient().isConnected()) {
                            ClientManager.getClient().close();
                        }
                        ((MainActivityOld) getActivity()).connectDevice(ip);
                    } else {
                        enterLiveVideo();
                    }
                }
            } else Dbug.w(tag, "Device bean is null");
        } else Dbug.w(tag, "mAdapter is null");
    }

    private void enterLiveVideo() {
        //Dbug.w(tag, "It has connected.");
        BaseFragment fragment = (BaseFragment) getActivity().getSupportFragmentManager().findFragmentByTag(VideoFragment.class.getSimpleName());
        if (fragment == null) fragment = new VideoFragment();

        ((MainActivityOld)getActivity()).changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
    }

    private void initListView() {
        if (mAdapter == null) {
            mAdapter = new DeviceListAdapter(getActivity(), R.layout.item_device_list);
        }

        deviceListView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        stopStaSearchThread();
        dismissSelectWifiDialog();
        super.onDestroyView();
    }

    private class DeviceListAdapter extends ArrayAdapter<DeviceBean> {
        private int resourceId;

        DeviceListAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
            this.resourceId = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                viewHolder = new ViewHolder(convertView);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            DeviceBean bean = getItem(position);
            if (bean != null) {
                String deviceName = bean.getWifiSSID();
                if (!TextUtils.isEmpty(deviceName)) {
                    viewHolder.devNameTv.setText(deviceName);
                } else {
                    viewHolder.devNameTv.setText(getString(R.string.unknown_device_name));
                }
                if (checkDeviceConnected(bean.getMode(), bean.getWifiSSID())) {
                    viewHolder.devStateIcon.setImageResource(R.mipmap.ic_device_selected);
                } else {
                    viewHolder.devStateIcon.setImageResource(R.mipmap.ic_device_unselected);
                }
            }
            return convertView;
        }

        private class ViewHolder {
            private ImageView devStateIcon;
            private TextView devNameTv;

            ViewHolder(View view) {
                devStateIcon = (ImageView) view.findViewById(R.id.device_state_icon);
                devNameTv = (TextView) view.findViewById(R.id.device_name_text);

                view.setTag(this);
            }
        }
    }

    private boolean checkDeviceConnected(int mode, String ssid){
        boolean result = false;
        if(mode == DEV_STA_MODE){
            if(ClientManager.getClient().isConnected() && ssid.equals(ClientManager.getClient().getConnectedIP())){
                result = true;
            }
        }else{
            if(!TextUtils.isEmpty(ssid)){
                if(mWifiHelper == null){
                    mWifiHelper = WifiHelper.getInstance(getContext());
                }
                String saveSSID = null;
                WifiP2pDevice wifiP2pDevice = WifiP2pHelper.getInstance(getContext()).getConnectWifiDevice();
                if(wifiP2pDevice != null){
                    saveSSID = wifiP2pDevice.deviceName;
                }
                if(TextUtils.isEmpty(saveSSID)){
                    WifiInfo wifiInfo = mWifiHelper.getWifiConnectionInfo();
                    if (wifiInfo != null) {
                        saveSSID = WifiHelper.formatSSID(wifiInfo.getSSID());
                    }
                }
                if(ssid.equals(saveSSID) && ClientManager.getClient().isConnected()){
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean checkDeviceWifi(){
        if(mWifiHelper == null){
            mWifiHelper = WifiHelper.getInstance(getContext());
        }
        String saveSSID = null;
        WifiInfo wifiInfo = mWifiHelper.getWifiConnectionInfo();
        if (wifiInfo != null) {
            saveSSID = WifiHelper.formatSSID(wifiInfo.getSSID());
        }
        return !TextUtils.isEmpty(saveSSID) && saveSSID.startsWith(WIFI_PREFIX);
    }

    private static class SearchStaDevice extends Thread{

        private Discovery mDiscovery;
        private WeakReference<Handler> weakReference;
        private boolean isSearching;
        private int timeCount = 0;

        SearchStaDevice(Handler mHandler){
            mDiscovery = Discovery.newInstance();
            mDiscovery.registerOnDiscoveryListener(mOnDiscoveryListener);
            weakReference = new WeakReference<>(mHandler);
        }

        private void stopSearch(){
            isSearching = false;
            if (mDiscovery != null) {
                mDiscovery.unregisterOnDiscoveryListener(mOnDiscoveryListener);
                mDiscovery.release();
                mDiscovery = null;
            }
        }

        @Override
        public synchronized void start() {
            super.start();
            isSearching = true;
        }

        @Override
        public void run() {
            super.run();
            Dbug.i(tag, "SearchStaDevice thread start running...");
            while (isSearching){
                if(mDiscovery != null){
                    mDiscovery.setFilter(true);
                    mDiscovery.doDiscovery();
                    int count = 0;
                    while (count < 10 && isSearching) {
                        SystemClock.sleep(300);
                        count++;
                    }
                    if (!isSearching) break;
                    timeCount += 3;
                    if (timeCount >= 30) {
                        break;
                    }
                }
            }
            Dbug.i(tag, "SearchStaDevice thread stop running..." + isSearching);
            isSearching = false;
            timeCount = 0;
        }

        private Discovery.OnDiscoveryListener mOnDiscoveryListener = new Discovery.OnDiscoveryListener() {
            @Override
            public void onDiscovery(String remoteAddress, String msg) {
                if(!TextUtils.isEmpty(remoteAddress)){
//                    DeviceDesc deviceDesc = AppUtils.parseDeviceDescTxt(msg);
                    DeviceBean bean = new DeviceBean();
                    bean.setWifiIP(remoteAddress);
                    bean.setMode(DEV_STA_MODE);
                    bean.setWifiSSID(remoteAddress);
                    Handler handler = weakReference.get();
                    if(handler != null){
                        handler.sendMessage(handler.obtainMessage(MSG_ADD_NEW_DEVICE, bean));
                    }
                }
            }

            @Override
            public void onError(int errCode, String errMsg) {
                Dbug.e(tag, "code : " + errCode + " , msg : " + errMsg);
                isSearching = false;
            }
        };
    }

    private OnWifiCallBack mWifiCallBack = new OnWifiCallBack() {
        @Override
        public void onConnected(WifiInfo info) {
            if(info != null && !checkDeviceWifi()){
                mHandler.sendEmptyMessageDelayed(MSG_SEARCH_DEVICE_LIST, 200);
            }
        }

        @Override
        public void onError(int errCode) {
            if(errCode == ERROR_WIFI_PWD_NOT_MATCH){
                a.getApplication().showToastShort(R.string.pwd_not_match_error);
                if(mApplication.getSearchMode() == STA_SEARCH_MODE){
                    Dbug.e(tag, "onError: ??");
                    //mSwitchMode.performClick();
                }
            }
        }
    };

    private void showSelectWifiDialog(){
        if(mSelectWifiDialog == null){
            mSelectWifiDialog = new SelectWifiDialog();
            mSelectWifiDialog.setOnConnectWifiListener(mSelectWifiListener);
        }
        if(!mSelectWifiDialog.isShowing() && !isDetached()){
            mSelectWifiDialog.show(getFragmentManager(), "select_wifi_dialog");
        }
    }

    private void dismissSelectWifiDialog(){
        if(mSelectWifiDialog != null){
            if(mSelectWifiDialog.isShowing() && !isDetached()){
                mSelectWifiDialog.dismiss();
            }
            mSelectWifiDialog = null;
        }
    }

    private SelectWifiDialog.OnConnectWifiListener mSelectWifiListener = new SelectWifiDialog.OnConnectWifiListener() {
        @Override
        public void onSelectWifi(String ssid, String pwd) {
            if(ClientManager.getClient().isConnected()){
                ClientManager.getClient().disconnect();
            }
            mWifiHelper.connectWifi(mApplication, ssid, pwd);
        }

        @Override
        public void onCancel() {
            //mSwitchMode.performClick();
            Dbug.e(tag, "onCancel: ??");
        }
    };

    private void toDeviceStaModeFragment() {
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment == null || !(fragment instanceof QRCodeFragment)) {
            fragment = new QRCodeFragment();
        }
        ((MainActivityOld)getActivity()).changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
    }

    private IWifiDirectListener mIWifiDirectListener = new IWifiDirectListener() {
        @Override
        public void onCallP2pStateChanged(int state) {
            Dbug.i(tag, "-onCallP2pStateChanged- state : " + state);
        }

        @Override
        public void onCallP2pPeersChanged(List<WifiP2pDevice> peerList) {
            if(peerList != null && mApplication.getSearchMode() == IConstant.AP_SEARCH_MODE){
                Dbug.i(tag, "-onCallP2pPeersChanged- size : " + peerList.size());
                if(peerList.size() > 0){
                   WifiP2pDevice mConnectP2pDevice = null;
                    for (WifiP2pDevice device : peerList){
                        Dbug.i(tag, "-onCallP2pPeersChanged- device : " + device + ", isGroupOwner : "+device.isGroupOwner());
                        if((!device.isGroupOwner() && device.status != WifiP2pDevice.AVAILABLE) ||
                                (device.isGroupOwner() || device.status == WifiP2pDevice.CONNECTED)){
                            mConnectP2pDevice = device;
                            break;
                        }
                    }
                    if(mConnectP2pDevice != null){
                        String connectName = mConnectP2pDevice.deviceName;
                        if(!TextUtils.isEmpty(connectName) && connectName.startsWith(IConstant.WIFI_PREFIX)){
                            WifiP2pHelper.getInstance(getContext()).setConnectWifiDevice(mConnectP2pDevice);
                            if(ClientManager.getClient().isConnected()){
                                enterLiveVideo();
                            }else{
                                PreferencesHelper.putStringValue(mApplication, CURRENT_WIFI_SSID, connectName);
                                PreferencesHelper.putIntValue(mApplication, RECONNECT_TYPE, RECONNECT_TYPE_WIFI_DIRECT);
                                if(mApplication.isWifiDirectGO()){
                                    WifiP2pHelper.getInstance(getContext()).requestConnectionInfo();
                                }else{
                                    stopStaSearchThread();
                                    mSearchStaDevice = new SearchStaDevice(mHandler);
                                    mSearchStaDevice.start();
                                }
//                                ((MainActivityOld) getActivity()).connectDevice(IConstant.DEFAULT_DEV_IP);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onCallP2pConnectionChanged(WifiP2pInfo wifiP2pInfo) {
            if(wifiP2pInfo != null){
                Dbug.i(tag, "-onCallP2pConnectionChanged- : " + wifiP2pInfo);
                if(mApplication.getSearchMode() == IConstant.DEV_AP_MODE
                        && mApplication.isWifiDirectGO() && wifiP2pInfo.groupFormed){
                    InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
                    if(groupOwnerAddress != null){
                        if(ClientManager.getClient().isConnected()){
                            enterLiveVideo();
                        }else{
                            ((MainActivityOld) getActivity()).connectDevice(groupOwnerAddress.getHostAddress());
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
