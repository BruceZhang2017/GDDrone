package com.jieli.stream.dv.gdxxx.data;

import android.support.v4.util.ArrayMap;
import android.util.Log;

public class FlyStateModel {
    public static byte[] mSerialData = new byte[10];

    private static byte checkSum(byte[] arry) {

        byte mSum = 0x00;
        for (int i = 3; i < arry.length - 1; i++) {
            mSum ^= arry[i] & 0xff;
        }
        return mSum;
    }


    public static ArrayMap getFlyCtrlDataMap(int disance){

        mSerialData[0] = 0x46;
        mSerialData[1] = 0x48;
        mSerialData[2] = 0x3c;
        mSerialData[3] = 0x65;
        mSerialData[4] = 0x03;
        mSerialData[5] = 0x00;
        mSerialData[6] = 0x01;
        mSerialData[7] = (byte) (disance & 0xff);
        mSerialData[8] = (byte)((disance >> 8) * 0xff);
        mSerialData[9] = checkSum(mSerialData);

        ArrayMap aParam = new ArrayMap();
        aParam.put("D0",btou(mSerialData[0])+"");
        aParam.put("D1", btou(mSerialData[1])+"");
        aParam.put("D2", btou(mSerialData[2])+"");
        aParam.put("D3", btou(mSerialData[3])+"");
        aParam.put("D4", btou(mSerialData[4])+"");
        aParam.put("D5", btou(mSerialData[5])+"");
        aParam.put("D6", btou(mSerialData[6])+"");
        aParam.put("D7", btou(mSerialData[7])+"");
        aParam.put("D8", btou(mSerialData[8])+"");
        aParam.put("D9", btou(mSerialData[9])+"");

        for(int i=0;i<10;i++){
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
