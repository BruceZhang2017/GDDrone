package com.jieli.stream.dv.gdxxx.data;

import android.support.v4.util.ArrayMap;
import android.util.Log;

public class FlyInfoModel {
    public static byte[] mSerialData = new byte[8];

    private static byte checkSum(byte[] arry) {

        byte mSum = 0x00;
        for (int i = 3; i < arry.length - 1; i++) {
            mSum ^= arry[i] & 0xff;
        }
        return mSum;
    }


    public static ArrayMap getFlyCtrlDataMap(){

        mSerialData[0] = 0x46;
        mSerialData[1] = 0x48;
        mSerialData[2] = 0x3c;
        mSerialData[3] = 0x64;
        mSerialData[4] = 0x01;
        mSerialData[5] = 0x00;
        mSerialData[6] = 0x01;
        mSerialData[7] = checkSum(mSerialData);

        ArrayMap aParam = new ArrayMap();
        aParam.put("D0",btou(mSerialData[0])+"");
        aParam.put("D1", btou(mSerialData[1])+"");
        aParam.put("D2", btou(mSerialData[2])+"");
        aParam.put("D3", btou(mSerialData[3])+"");
        aParam.put("D4", btou(mSerialData[4])+"");
        aParam.put("D5", btou(mSerialData[5])+"");
        aParam.put("D6", btou(mSerialData[6])+"");
        aParam.put("D7", btou(mSerialData[7])+"");

        for(int i=0;i<8;i++){
            String string = (String) aParam.get("D"+i);
            Log.d("data","D"+i+":"+string);
        }

        return aParam;
    }

    public static final int btou(byte b) {
        if (b >= 0)
            return (b + 0);
        else
            return (256 + b);
    }
}
