package com.jieli.stream.dv.gdxxx.util;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.stream.dv.gdxxx.ui.a;

public class UDPClientManager {
    private String tag = getClass().getSimpleName();
    private static DeviceClient instance;

    public static DeviceClient getClient() {

        if (instance == null) {
            synchronized (UDPClientManager.class) {
                if (instance == null) {
//                    Log.e("ClientImpl ClientManager", "create client instance");
                    instance = new DeviceClient(a.getApplication(), DeviceClient.PROTOCOL_UDP);
                }
            }
        }
        return instance;
    }

    public static void release() {
        if (instance != null) {
            instance.release();
            instance = null;
        }
    }
}
