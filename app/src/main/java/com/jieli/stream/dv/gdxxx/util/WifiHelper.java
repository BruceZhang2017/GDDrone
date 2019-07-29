package com.jieli.stream.dv.gdxxx.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.jieli.lib.dv.control.mssdp.Discovery;
import com.jieli.stream.dv.gdxxx.interfaces.OnWifiCallBack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class WifiHelper implements IConstant{
    private static String tag = "WifiHelper";
    private static WifiHelper instance = null;
    private WifiManager mWifiManager;
    private WifiManager.WifiLock wifiLock;
    private static String otherWifiSSID = null;
    private Set<OnWifiCallBack> wifiCallBackSet;

    public static WifiHelper getInstance(Context context) {
        synchronized (WifiHelper.class){
            if (instance == null) {
                instance = new WifiHelper(context);
            }
        }
        return instance;
    }

    private WifiHelper(Context c) {
        if (c != null) {
            mWifiManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiCallBackSet = new HashSet<>();

            this.startScan();
        }
    }

    public void registerOnWifiCallback(OnWifiCallBack onWifiCallBack){
        if(wifiCallBackSet != null && onWifiCallBack != null){
            wifiCallBackSet.add(onWifiCallBack);
        }
    }

    public void unregisterOnWifiCallback(OnWifiCallBack onWifiCallBack){
        if(wifiCallBackSet != null && onWifiCallBack != null){
            wifiCallBackSet.remove(onWifiCallBack);
        }
    }

    public void clearAllOnWifiCallback(){
        if(wifiCallBackSet != null){
            wifiCallBackSet.clear();
        }
    }

    public void notifyWifiConnect(WifiInfo wifiInfo){
        if(wifiCallBackSet != null && wifiInfo != null){
            for (OnWifiCallBack listener : wifiCallBackSet){
                listener.onConnected(wifiInfo);
            }
        }
    }

    public void notifyWifiError(int errorCode){
        if(wifiCallBackSet != null){
            for (OnWifiCallBack listener : wifiCallBackSet){
                listener.onError(errorCode);
            }
        }
    }

    /**
     * WIFI 是否打开
     *
     */
    public boolean isWifiOpen() {
        return mWifiManager != null && mWifiManager.isWifiEnabled();
    }

    /**
     * 获取当前网络信息
     * @param context  上下文
     * @return  NetworkInfo
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager != null) {
            return manager.getActiveNetworkInfo();
        }
        return null;
    }

    /**
     * 判断Wifi是否连接
     * @param context   上下文
     */
    public static boolean isNetWorkConnectedType(Context context, int type) {
        if (context == null) {
            return false;
        }
        Context mContext = context.getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        return anInfo.getType() == type;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断外部网络
     * @param tag   网络标识头
     */
    public boolean isOutSideWifi(String tag){
        WifiInfo info = getWifiConnectionInfo();
        if(info != null){
            String ssid = info.getSSID();
            ssid = formatSSID(ssid);
            if(!TextUtils.isEmpty(ssid) && ssid.startsWith(tag)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取Wifi状态
     * @param context   上下文
     */
    public int getWifiState(Context context) {
        if (context == null) {return WIFI_UNKNOWN_ERROR;}
        NetworkInfo wifiNetworkInfo = getNetworkInfo(context);
        if(wifiNetworkInfo == null) return WIFI_UNKNOWN_ERROR;
        if (wifiNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.OBTAINING_IPADDR
                || wifiNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTING) {
            return WIFI_CONNECTING;
        } else if (wifiNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            return WIFI_CONNECTED;
        } else {
            return WIFI_CONNECT_FAILED;
        }
    }

    /**
     * 截断字符串(以空字符截断)
     * @param s   字符串
     */
    public static String interceptChar0Before(String s) {
        if (s == null) {
            return null;
        }
        char[] chars = s.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            Character ch = c;
            if (0 == ch.hashCode()) { //遇到空字符就跳出循环
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 查看WIFI当前是否处于打开状态
     *
     * @return true 处于打开状态；false 处于非打开状态(包括UnKnow状态)。
     */
    public boolean isWifiClosed() {
        int wifiState = getWifiState();
        return wifiState == WifiManager.WIFI_STATE_DISABLED
                || wifiState == WifiManager.WIFI_STATE_DISABLING;
    }

    /**
     * 查看WIFI当前是否处于关闭状态
     *
     * @return true 处于关闭状态；false 处于非关闭状态(包括UNKNOWN状态)
     */
    public boolean isWifiOpened() {
        int wifiState = getWifiState();
        return wifiState == WifiManager.WIFI_STATE_ENABLED
                || wifiState == WifiManager.WIFI_STATE_ENABLING;
    }

    /**
     * 如果WIFI当前处于关闭状态，则打开WIFI
     */
    public void openWifi() {
        if (mWifiManager != null && isWifiClosed()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 如果WIFI当前处于打开状态，则关闭WIFI
     */
    public void closeWifi() {
        if (mWifiManager != null && isWifiOpened()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 获取当前Wifi的状态编码
     *
     * @return WifiManager.WIFI_STATE_ENABLED，WifiManager.WIFI_STATE_ENABLING，
     * WifiManager.WIFI_STATE_DISABLED，WifiManager.WIFI_STATE_DISABLING，
     * WifiManager.WIFI_STATE_UnKnown 中间的一个
     */
    public int getWifiState() {
        if (mWifiManager != null) {
            return mWifiManager.getWifiState();
        }
        return 0;
    }

    /**
     * 获取系统保存的Wifi网络配置列表
     */
    public List<WifiConfiguration> getSavedWifiConfiguration() {
        if(mWifiManager != null){
            return mWifiManager.getConfiguredNetworks();
        }
        return null;
    }

    /**
     * 获取扫描到的网络信息列表
     */
    public List<ScanResult> getWifiScanResult() {
        if(mWifiManager != null){
            return mWifiManager.getScanResults();
        }
        return null;
    }

    /**
     * 执行一次Wifi的扫描
     */
    public synchronized void startScan() {
        if (mWifiManager != null) {
            mWifiManager.startScan();
        }
    }

    /**
     * 通过netWorkId来连接一个已经保存好的Wifi网络
     *
     * @param netWorkId network specific id
     */
    public void connectionConfiguration(int netWorkId) {
        if (mWifiManager != null) {
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(netWorkId, true);
//            mWifiManager.reconnect();
        }
    }

    /**
     * 断开一个指定ID的网络
     */
    public void disconnectionConfiguration(int netWorkId) {
        if (mWifiManager != null) {
            mWifiManager.disableNetwork(netWorkId);
            mWifiManager.disconnect();
        }
    }

    /**
     * 检测尝试连接某个网络时，查看该网络是否已经在保存的队列中间
     *
     * @param netWorkId network specific id
     * @return true : network save in configuration
     */
    private boolean configurationNetWorkIdCheck(int netWorkId) {
        List<WifiConfiguration> wifiConfigurationList = getSavedWifiConfiguration();
        if(null == wifiConfigurationList){
            return false;
        }
        for (WifiConfiguration temp : wifiConfigurationList) {
            if (null != temp && temp.networkId == netWorkId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取Wifi的信息化
     *
     */
    public WifiInfo getWifiConnectionInfo() {
        return mWifiManager.getConnectionInfo();
    }

    /**
     * 创建一个WifiLock
     */
    public void createWifiLock(String lockName) {
        if (mWifiManager != null) {
            if(TextUtils.isEmpty(lockName)){lockName = "wifiLock";}
            wifiLock = mWifiManager.createWifiLock(lockName);
        }
    }

    /**
     * 锁定WIFI，使得在熄屏状态下，仍然可以使用WIFI
     */
    public void acquireWifiLock() {
        if (wifiLock != null) {
            wifiLock.acquire();
        }else{
            createWifiLock("wifiLock");
            if (wifiLock != null) {
                wifiLock.acquire();
            }
        }
    }

    /**
     * 解锁WIFI
     */
    public void releaseWifiLock() {
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.acquire();
            }
        }
    }

    /**
     * 保存一个新的网络
     *
     * @param _wifiConfiguration   网络配置
     */
    public int addNetWork(WifiConfiguration _wifiConfiguration) {
        int netWorkId = -255;
        if (_wifiConfiguration != null && mWifiManager != null) {
            netWorkId = mWifiManager.addNetwork(_wifiConfiguration);
        }
        return netWorkId;
    }

    /**
     * 保存并连接到一个新的网络
     *
     * @param _wifiConfiguration  网络配置
     */
    public void addNetWorkAndConnect(WifiConfiguration _wifiConfiguration) {
        int netWorkId = addNetWork(_wifiConfiguration);
        if (mWifiManager != null && netWorkId != -255) {
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(netWorkId, true);
        }
    }

    /**
     * 获取当前连接状态中的Wifi的信号强度
     * @return  有效值： 0 ~ -100  , 1 : 数据错误
     * 0 ~ -50 : 信号最好
     * -50 ~ -60 ：信号一般
     * -60 ~ -70 ：信号较差
     * -70 ~ -100 : 信号最差
     */
    public int getConnectedWifiLevel() {
        WifiInfo wifiInfo = getWifiConnectionInfo();
        if (wifiInfo != null) {
            String connectedWifiSSID = wifiInfo.getSSID();
            connectedWifiSSID = formatSSID(connectedWifiSSID);
            List<ScanResult> scanResultList = getWifiScanResult();
            if (scanResultList != null) {
                for (ScanResult temp : scanResultList) {
                    if(temp != null){
                        String tempSSID = formatSSID(temp.SSID);
                        if (!TextUtils.isEmpty(tempSSID) && tempSSID.equals(connectedWifiSSID)) {
                            return temp.level;
                        }
                    }
                }
            }
        }
        return 1;
    }

    /**
     * 删除指定SSID的网络
     * @param ssid   指定网络的SSID
     * @return  删除指定网络的结果
     */
    public boolean removeSavedNetWork(String ssid){
        if(!TextUtils.isEmpty(ssid) && mWifiManager != null){
            boolean result = false;
            List<WifiConfiguration> saveWifiConfigList = getSavedWifiConfiguration();
            if(saveWifiConfigList == null){
                return false;
            }
            for (WifiConfiguration wifiConfig : saveWifiConfigList){
                if(wifiConfig != null){
                    String saveSSID = wifiConfig.SSID;
                    saveSSID = formatSSID(saveSSID);
                    ssid = formatSSID(ssid);
                    if(!TextUtils.isEmpty(ssid) && ssid.equals(saveSSID)){
                        result = mWifiManager.removeNetwork(wifiConfig.networkId);
                        break;
                    }
                }
            }
            return result;
        }
        return false;
    }

    /**
     * 删除一个已经保存的网络
     *
     * @param netWorkId  网络ID
     */
    public void removeNetWork(int netWorkId) {
        if (mWifiManager != null) {
            mWifiManager.removeNetwork(netWorkId);
        }
    }

    /**
     * Wifi加密类型的描述类
     */
    public enum WifiCipherType {
        NONE, IEEE8021XEAP, WEP, WPA, WPA2, WPAWPA2
    }

    /**
     * 连接一个WIFI
     * @param ssid  network ssid
     * @param password network password
     * @param wifiCipherType network type
     */
    public void addNetWorkAndConnect(String ssid, String password, WifiCipherType wifiCipherType) {
        if (mWifiManager != null && wifiCipherType != null) {
            WifiConfiguration temp = isWifiConfigurationSaved(ssid, wifiCipherType);
            if(null != temp){
//                remoteNetWork(temp.networkId);
                connectionConfiguration(temp.networkId);
            }else{
                WifiConfiguration wifiConfig = createWifiConfiguration(ssid, password, wifiCipherType);
//                addNetWorkAndConnect(wifiConfig);
                addNetWork(wifiConfig);
                List<WifiConfiguration> configurationList = getSavedWifiConfiguration();
                if(configurationList != null){
                    for (WifiConfiguration config : configurationList){
                        if(config != null){
                            String tempSSID = config.SSID;
                            tempSSID = formatSSID(tempSSID);
                            ssid = formatSSID(ssid);
                            if(!TextUtils.isEmpty(tempSSID) && tempSSID.equals(ssid)){
                                mWifiManager.disconnect();
                                mWifiManager.enableNetwork(config.networkId, true);
//                                mWifiManager.reconnect();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查Wifi配置是否存在保存列表
     * @param SSID   WIFI的SSID
     * @return 保存的WifiConfiguration
     */
    private WifiConfiguration isWifiConfigurationSaved(String SSID, WifiCipherType wifiCipherType) {
        List<WifiConfiguration> wifiConfigurationList = getSavedWifiConfiguration();
        if (wifiConfigurationList == null) {
            return null;
        }
        String tagetSSID = formatSSID(SSID);
        for (WifiConfiguration temp : wifiConfigurationList) {
            if(temp != null){
                String tempSSID = temp.SSID;
                tempSSID  = formatSSID(tempSSID);
                if(!TextUtils.isEmpty(tempSSID) && tempSSID.equals(tagetSSID)){
                    String keyMgmt = null;
                    for (int k = 0; k < temp.allowedKeyManagement.size(); k++) {
                        if (temp.allowedKeyManagement.get(k)) {
                            if (k < WifiConfiguration.KeyMgmt.strings.length) {
                                keyMgmt = WifiConfiguration.KeyMgmt.strings[k];
                            }
                        }
                    }
                    Dbug.e(tag, "isWifiConfigurationSaved  keyMgmt = " + keyMgmt + " , wifiCipherType : " +wifiCipherType);
                    if((wifiCipherType == WifiCipherType.WPA && KEY_WPA.equals(keyMgmt)) ||
                            (wifiCipherType == WifiCipherType.NONE && KEY_NONE.equals(keyMgmt))){
                        Dbug.e(tag, "isWifiConfigurationSaved return object, network id : " +temp.networkId);
                        return temp;
                    }
                }
            }

        }
        return null;
    }

    /**
     * 创建网络配置
     * @param SSID         wifi的ssid
     * @param password     wifi的密码
     * @param type         wifi的加密类型
     */
    private WifiConfiguration createWifiConfiguration(String SSID, String password, WifiCipherType type) {
        WifiConfiguration newWifiConfiguration = new WifiConfiguration();
        newWifiConfiguration.allowedAuthAlgorithms.clear();
        newWifiConfiguration.allowedGroupCiphers.clear();
        newWifiConfiguration.allowedKeyManagement.clear();
        newWifiConfiguration.allowedPairwiseCiphers.clear();
        newWifiConfiguration.allowedProtocols.clear();
        newWifiConfiguration.SSID = "\"" + SSID + "\"";

        switch (type) {
            case NONE:
                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case IEEE8021XEAP:
                break;
            case WEP:
                newWifiConfiguration.hiddenSSID = true;
                newWifiConfiguration.wepKeys[0] = "\"" + password + "\"";
                newWifiConfiguration.wepTxKeyIndex = 0;
                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                newWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                break;
            case WPA:
                newWifiConfiguration.preSharedKey = "\"" + password + "\"";
                newWifiConfiguration.hiddenSSID = true;
                newWifiConfiguration.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                newWifiConfiguration.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                newWifiConfiguration.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                newWifiConfiguration.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
//                newWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                newWifiConfiguration.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                newWifiConfiguration.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                newWifiConfiguration.status = WifiConfiguration.Status.ENABLED;

                break;
            case WPA2:
                newWifiConfiguration.preSharedKey = "\"" + password + "\"";
                newWifiConfiguration.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                newWifiConfiguration.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                newWifiConfiguration.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                newWifiConfiguration.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                newWifiConfiguration.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                newWifiConfiguration.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                newWifiConfiguration.allowedProtocols
                        .set(WifiConfiguration.Protocol.RSN);
                newWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                break;
            default:
                return null;
        }
        return newWifiConfiguration;
    }

    /**
     *
     * @param context  上下文
     */
    public static int getNetWorkType(Context context) {
        if (!isNetWorkConnectedType(context, ConnectivityManager.TYPE_WIFI)) {
            return 0;
        }

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        String type = networkInfo.getTypeName();

        if (type.equalsIgnoreCase("WIFI")) {
            return 1;
        } else if (type.equalsIgnoreCase("MOBILE")) {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_EVDO_0:// ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:// ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:// ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:// ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:// ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B:// ~ 5 Mbps
                    return 1;
                case TelephonyManager.NETWORK_TYPE_1xRTT:// ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:// ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:// ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:// ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_IDEN:// ~25 kbps
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return 0;
            }
        }

        return 0;
    }

    /**
     * 枚举网络状态
     * NET_NO：没有网络
     * NET_2G:2g网络
     * NET_3G：3g网络
     * NET_4G：4g网络
     * NET_WIFI：wifi
     * NET_UNKNOWN：未知网络
     */
    public enum NetState {
        NET_NO, NET_2G, NET_3G, NET_4G, NET_WIFI, NET_UNKNOWN
    }

    /**
     * 判断当前网络连接类型
     *
     * @param context 上下文
     * @return 状态码
     */
    public static NetState getConnectedType(Context context) {
        NetState stateCode = NetState.NET_NO;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            switch (ni.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    stateCode = NetState.NET_WIFI;
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    switch (ni.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS: //联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: //电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: //移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11


                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: //电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                        case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                        case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                            stateCode = NetState.NET_3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:   //api<11 : replace by 13
                            stateCode = NetState.NET_4G;
                            break;
                        default:
                            // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                            if (ni.getSubtypeName().equalsIgnoreCase("TD-SCDMA") ||
                                    ni.getSubtypeName().equalsIgnoreCase("WCDMA") ||
                                    ni.getSubtypeName().equalsIgnoreCase("CDMA2000")) {
                                stateCode = NetState.NET_3G;
                            }else{
                                stateCode = NetState.NET_UNKNOWN;
                            }

                    }
                    break;
                default:
                    stateCode = NetState.NET_UNKNOWN;
            }

        }
        return stateCode;
    }

    /**
     * 获取Wifi的Ip
     * @param context   上下文
     */
    public static String getWifiIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiinfo = wifiManager.getConnectionInfo();
        String ip = formatIpAddress(wifiinfo.getIpAddress());
        if(TextUtils.isEmpty(ip)) return null;
        if (ip.equals("0.0.0.0")) {
            ip = getLocalIpAddress();
            if (ip.equals("0.0.0.0")) {
                Dbug.e(tag, "WIFI IP Error");
            }
        }
        return ip;
    }

    /**
     * 获取本地IP地址
     */
    private static String getLocalIpAddress() {
        try {
            String ipv4;
            List<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nilist) {
                List<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address : ialist) {
                    ipv4 = address.getHostAddress();
//                    if (!address.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4)) {
                    if(!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        return ipv4;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "0.0.0.0";
    }

    /**
     * 获取当前网关
     * @return 网关IP
     */
    public String getGateWay(Context context) {
        WifiManager wifiService = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiService == null) return null;

        DhcpInfo dhcpInfo = wifiService.getDhcpInfo();
        return Formatter.formatIpAddress(dhcpInfo.gateway);
    }

    /**
     * 获取WifiInfo的IP地址
     * @param wifiInfo  WifiInfo对象
     */
    public static String getWifiIP(WifiInfo wifiInfo){
        String ip = null;
        if(wifiInfo != null){
            int ipAddress = wifiInfo.getIpAddress();
            ip = formatIpAddress(ipAddress);
        }
        return ip;
    }

    /**
     * 格式化IP地址
     * @param ipAddress  ip地址
     */
    private static String formatIpAddress(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    /**
     * 获取指定标识头的wifi 列表
     * @param specified   指定标识头
     */
    public List<ScanResult> getSpecifiedSSIDList(String specified) {
        List<ScanResult> list = new ArrayList<>();
        startScan();
        List<ScanResult> scanResultList = getWifiScanResult();
        if (scanResultList == null){
            Dbug.e(tag, "scanResultList is null");
            return null;
        }
        for (ScanResult scanResult : scanResultList) {
//                Dbug.d(tag, "scanResult.SSID=" + scanResult.SSID+ ", capabilities:" + scanResult.capabilities);
            String ssid = formatSSID(scanResult.SSID);
            if (!TextUtils.isEmpty(ssid) && ssid.startsWith(specified)) {
                list.add(scanResult);
            }
        }
        return list;
    }

    /**
     * 连接除指定标识头外的wifi
     * @param exceptSpecified   指定标识头
     */
    public void connectOtherWifi(String exceptSpecified){
        startScan();
        otherWifiSSID = null;
        boolean isConnect = false;
        List<WifiConfiguration> saveWifiList = getSavedWifiConfiguration();
        List<ScanResult> scanResultList = getWifiScanResult();
        if(scanResultList == null || saveWifiList == null){
            Dbug.e(tag, "scanResultList or saveWifiList is null");
            return;
        }

        for (ScanResult scanResult : scanResultList){
            String saveNetWorkName = scanResult.SSID;
            saveNetWorkName = formatSSID(saveNetWorkName);
            if (TextUtils.isEmpty(saveNetWorkName) || saveNetWorkName.startsWith(exceptSpecified)){
                continue;
            }
//            Dbug.e(tag,"scanResult.SSID-> " + scanResult.SSID);
            for(WifiConfiguration config : saveWifiList){
                String networkName = config.SSID;
                networkName = formatSSID(networkName);
                if(TextUtils.isEmpty(networkName)){
                    continue;
                }
                if (saveNetWorkName.equals(networkName)) {
                    Dbug.e(tag, "Save networkName-> " + saveNetWorkName + " network_id -> " + config.networkId + " networkName : " +networkName);
                    if (mWifiManager != null) {
                        mWifiManager.disconnect();
                        isConnect = mWifiManager.enableNetwork(config.networkId, true);
                    }
                    otherWifiSSID = config.SSID;
                    break;
                }
            }
            if(isConnect){
                break;
            }
        }
    }

    /**
     * 连接外部Wifi的ssid
     */
    public String getOtherWifiSSID(){
        return otherWifiSSID;
    }

    /**
     * 格式化SSID
     * @param ssid  原始SSID
     */
    public static String formatSSID(String ssid){
        //if(TextUtils.isEmpty(ssid)) return null;
        if(ssid.contains("\"")){
            ssid = ssid.replace("\"", "");
        }
//        if(ssid.contains(" ")){
//            ssid = ssid.replace(" ", "");
//        }
        return ssid;
    }

    /**
     * 释放资源
     */
    public void release(){
        instance = null;
        otherWifiSSID = null;
        releaseWifiLock();
        mWifiManager = null;
    }
    /**
     * 检查是否开启Wifi热点
     *
     * @return 是否打开
     */
    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (boolean) method.invoke(mWifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
        if (isWifiApEnabled()) {
            return InetAddress.getByName("192.168.43.255");
        }
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null)
            return InetAddress.getByName(Discovery.BROADCAST_IP);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) {
            return InetAddress.getByName(Discovery.BROADCAST_IP);
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

    /**
     * 连接指定Wifi
     *
     * @param SSID     Wifi的SSID
     * @param password wifi的密码
     */
    public void connectWifi(Context context, String SSID, String password) {
        if (TextUtils.isEmpty(SSID)) {
            Dbug.e(tag, "parameter is empty!");
            return;
        }
        //Dbug.i(tag, "-connectDeviceWifi- SSID : " + SSID + " ,password : " + password);
        SSID = WifiHelper.formatSSID(SSID);
        if (!TextUtils.isEmpty(SSID)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager == null) return;
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            WifiInfo wifiInfo = getWifiConnectionInfo();
            String ssid = null;
            if (wifiInfo != null) {
                ssid = wifiInfo.getSSID();
                ssid = WifiHelper.formatSSID(ssid);
            }
            if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED && SSID.equals(ssid)) {
                notifyWifiError(ERROR_WIFI_IS_CONNECTED);
            } else {
                if (TextUtils.isEmpty(password)) {
                    addNetWorkAndConnect(SSID, WifiConfiguration.KeyMgmt.NONE + "", WifiHelper.WifiCipherType.NONE);
                } else {
                    addNetWorkAndConnect(SSID, password, WifiHelper.WifiCipherType.WPA);
                }
            }
        }
    }

    /**
     * 删除当前连接网络并尝试连接别的网络
     * @param context 上下文
     */
    public void removeCurrentNetwork(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Dbug.e(tag, "WifiManager is null");
            return;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getSSID())) {
            Dbug.e(tag, "-=-=-=wifiInfo is null or  wifiInfo.getSSID() is null");
            return;
        }
        String mSSID = WifiHelper.formatSSID(wifiInfo.getSSID());
        if (!TextUtils.isEmpty(mSSID) && mSSID.startsWith(WIFI_PREFIX)) {
            //Dbug.w(tag, "Remove networkId:" + wifiInfo.getNetworkId());
            removeNetWork(wifiInfo.getNetworkId());
            connectOtherWifi(WIFI_PREFIX);
        }
    }
    public void removeCurrentNetworkEx(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Dbug.e(tag, "WifiManager is null");
            return;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getSSID())) {
            Dbug.e(tag, "-=-=-=wifiInfo is null or  wifiInfo.getSSID() is null");
            return;
        }
        String mSSID = WifiHelper.formatSSID(wifiInfo.getSSID());
        if (!TextUtils.isEmpty(mSSID) && mSSID.startsWith(WIFI_PREFIX)) {
            //Dbug.w(tag, "Remove networkId:" + wifiInfo.getNetworkId());
            removeNetWork(wifiInfo.getNetworkId());
            connectOtherWifi(WIFI_PREFIX);
        }
    }

    public String getCurrentConnectedSsid() {
        if (mWifiManager != null) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String s = wifiInfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return null;
    }
}
