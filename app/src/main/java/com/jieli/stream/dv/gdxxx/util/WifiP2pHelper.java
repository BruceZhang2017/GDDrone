package com.jieli.stream.dv.gdxxx.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiP2pHelper {

    private static WifiP2pHelper instance;
    private Context mMainContext;
    private WifiP2pManager mP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDevice mConnectWifiDevice;

    private WiFiDirectBroadcastReceiver mBroadcastReceiver;
    private Set<IWifiDirectListener> mWifiDirectListeners;
    private List<WifiP2pDevice> peers = new ArrayList<>();

    private MyWifiHandler mWifiHandler;

    private WifiP2pHelper(Context context){
        if(context == null){
            throw new NullPointerException("Context can not be empty.");
        }
        mMainContext = context;
        mP2pManager = (WifiP2pManager) mMainContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mP2pManager.initialize(mMainContext, mMainContext.getMainLooper(), null);
        mWifiHandler = new MyWifiHandler(mMainContext.getMainLooper());
    }

    public static WifiP2pHelper getInstance(Context context){
        if(instance == null){
            synchronized (WifiP2pHelper.class){
                if(instance == null){
                    instance = new WifiP2pHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * 注册Wifi直连的广播
     *
     * @param listener Wifi直连的监听器
     */
    public void registerBroadcastReceiver(IWifiDirectListener listener){
        if(mWifiDirectListeners == null){
            mWifiDirectListeners = new HashSet<>();
        }
        mWifiDirectListeners.add(listener);
        if(mBroadcastReceiver == null && mMainContext != null){
            IntentFilter mIntentFilter = new IntentFilter();
            //  Indicates a change in the Wi-Fi P2P status.
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            // Indicates a change in the list of available peers.
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            // Indicates the state of Wi-Fi P2P connectivity has changed.
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            // Indicates this device's details have changed.
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            mBroadcastReceiver = new WiFiDirectBroadcastReceiver();
            mMainContext.registerReceiver(mBroadcastReceiver, mIntentFilter);
        }
    }

    /**
     * 注销Wifi直连的广播
     *
     * @param listener Wifi直连的监听器
     */
    public void unregisterBroadcastReceiver(IWifiDirectListener listener){
        if(mWifiDirectListeners != null){
            mWifiDirectListeners.remove(listener);
        }
        if(mMainContext != null && mBroadcastReceiver != null){
            mMainContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * 开始搜索Wifi直连设备
     *
     * @param actionListener
     */
    public void startDiscoverPeers(WifiP2pManager.ActionListener actionListener){
        if(mP2pManager != null && mChannel != null){
            mP2pManager.discoverPeers(mChannel, actionListener);
        }
    }

    /**
     * 停止搜索Wifi直连设备
     *
     * @param actionListener
     */
    public void stopDiscoverPeers(WifiP2pManager.ActionListener actionListener){
        if(mP2pManager != null && mChannel != null){
            mP2pManager.stopPeerDiscovery(mChannel, actionListener);
        }
    }

    /**
     * 连接Wifi直连设备
     *
     * @param device    Wifi直连设备
     * @param actionListener
     */
    public  void connectP2pDevice(WifiP2pDevice device, WifiP2pManager.ActionListener actionListener){
        if(mP2pManager != null && device != null){
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            mP2pManager.connect(mChannel, config, actionListener);
        }
    }

    /**
     * 以组的形式连接Wifi直连设备
     *
     * @param device   Wifi直连设备
     * @param actionListener
     */
    public void connectP2pDeviceForGroup(final WifiP2pDevice device, final WifiP2pManager.ActionListener actionListener){
        if(mP2pManager != null && device != null){
            mP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;
                    config.wps.setup = WpsInfo.PBC;
                    mP2pManager.connect(mChannel, config, actionListener);
                }

                @Override
                public void onFailure(int i) {
                    if(actionListener != null){
                        actionListener.onFailure(i);
                    }
                }
            });
        }
    }

    /**
     * 取消连接设备
     *
     * @param actionListener
     */
    public void disconnectP2pDevice(WifiP2pManager.ActionListener actionListener){
        if(mP2pManager != null && mChannel != null){
            mP2pManager.cancelConnect(mChannel, actionListener);
        }
    }

    /**
     *
     * @param actionListener
     */
    public void disconnectP2pForGroup(final WifiP2pManager.ActionListener actionListener){
        if(mP2pManager != null && mChannel != null){
            mP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    disconnectP2pDevice(actionListener);
                }

                @Override
                public void onFailure(int i) {
                    if(actionListener != null){
                        actionListener.onFailure(i);
                    }
                }
            });
        }
    }

    /**
     * 获取Wifi直连搜索列表
     */
    public List<WifiP2pDevice> getPeerList() {
        return peers;
    }

    /**
     * 请求连接设备的信息
     */
    public void requestConnectionInfo(){
        if(mP2pManager != null && mChannel != null){
            mP2pManager.requestConnectionInfo(mChannel, mConnectionInfoListener);
        }
    }

    /**
     * 请求设备列表
     */
    public void requestPeerList(){
        if (mP2pManager != null && mChannel != null) {
            mP2pManager.requestPeers(mChannel, peerListListener);
        }
    }

    public void setConnectWifiDevice(WifiP2pDevice connectWifiDevice) {
        mConnectWifiDevice = connectWifiDevice;
    }

    public WifiP2pDevice getConnectWifiDevice() {
        return mConnectWifiDevice;
    }

    /**
     * 释放资源
     */
    public void release(){
        if(mWifiHandler != null){
            mWifiHandler.removeCallbacksAndMessages(null);
        }
        stopDiscoverPeers(null);
//        disconnectP2pForGroup(null);
//        disconnectP2pDevice(null);
        if(mWifiDirectListeners != null){
            mWifiDirectListeners.clear();
        }
        if(peers != null){
            peers.clear();
        }
        mConnectWifiDevice = null;
        mMainContext = null;
        mP2pManager = null;
        mChannel = null;
    }

    private class WiFiDirectBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                String action = intent.getAction();
                if(!TextUtils.isEmpty(action)){
                    switch (action){
                        case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                            // Determine if Wifi P2P mode is enabled or not.
                            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                            notifyP2pStateChanged(state);
                            if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                                if(mConnectWifiDevice != null){
                                    mConnectWifiDevice = null;
                                }
                            }
                            break;
                        }
                        case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:{
                            // The peer list has changed!
                            requestPeerList();
                            break;
                        }
                        case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:{
                            // Connection state changed!
                            if(mP2pManager != null){
                                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                                if(networkInfo != null && networkInfo.isConnected()){
                                    mP2pManager.requestConnectionInfo(mChannel, mConnectionInfoListener);
                                }
                            }
                            break;
                        }
                        case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:{
                            //this device's details have changed.
                            WifiP2pDevice mWifiP2pDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                            notifyP2pDeviceChanged(mWifiP2pDevice);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void notifyP2pStateChanged(final int state){
        if(mWifiHandler != null && mWifiDirectListeners != null){
            mWifiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IWifiDirectListener listener : mWifiDirectListeners){
                        listener.onCallP2pStateChanged(state);
                    }
                }
            });
        }
    }

    private void notifyP2pPeerListChanged(final List<WifiP2pDevice> peerList){
        if(mWifiHandler != null && mWifiDirectListeners != null){
            mWifiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IWifiDirectListener listener : mWifiDirectListeners){
                        listener.onCallP2pPeersChanged(peerList);
                    }
                }
            });
        }
    }

    private void notifyP2pConnectionChanged(final WifiP2pInfo wifiP2pInfo){
        if(mWifiHandler != null && mWifiDirectListeners != null){
            mWifiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IWifiDirectListener listener : mWifiDirectListeners){
                        listener.onCallP2pConnectionChanged(wifiP2pInfo);
                    }
                }
            });
        }
    }

    private void notifyP2pDeviceChanged(final WifiP2pDevice wifiP2pDevice){
        if(mWifiHandler != null && mWifiDirectListeners != null){
            mWifiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IWifiDirectListener listener : mWifiDirectListeners){
                        listener.onCallP2pDeviceChanged(wifiP2pDevice);
                    }
                }
            });
        }
    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            peers.clear();
            if(wifiP2pDeviceList != null) {
                peers.addAll(wifiP2pDeviceList.getDeviceList());
                notifyP2pPeerListChanged(peers);
            }
        }
    };

    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            notifyP2pConnectionChanged(wifiP2pInfo);
        }
    };

    private class MyWifiHandler extends Handler {

        private MyWifiHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
}
