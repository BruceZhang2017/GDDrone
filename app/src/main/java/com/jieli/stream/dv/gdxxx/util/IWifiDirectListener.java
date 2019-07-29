package com.jieli.stream.dv.gdxxx.util;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import java.util.List;

public interface IWifiDirectListener {

    void onCallP2pStateChanged(int state);

    void onCallP2pPeersChanged(List<WifiP2pDevice> peerList);

    void onCallP2pConnectionChanged(WifiP2pInfo wifiP2pInfo);

    void onCallP2pDeviceChanged(WifiP2pDevice wifiP2pDevice);
}
