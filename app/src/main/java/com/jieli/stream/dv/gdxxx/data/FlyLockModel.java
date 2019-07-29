package com.jieli.stream.dv.gdxxx.data;

import android.support.v4.util.ArrayMap;
import android.util.Log;

public class FlyLockModel {
    public static byte[] mSerialData = new byte[8];

    private static byte checkSum(byte[] arry) {

        byte mSum = 0x00;
        for (int i = 3; i < arry.length - 1; i++) {
            mSum ^= arry[i] & 0xff;
        }
        return mSum;
    }


    public static ArrayMap getFlyCtrlDataMap(byte value){

        mSerialData[0] = 0x46;
        mSerialData[1] = 0x48;
        mSerialData[2] = 0x3c;
        mSerialData[3] = 0x67;
        mSerialData[4] = 0x01;
        mSerialData[5] = 0x00;
        mSerialData[6] = value;
        mSerialData[7] = checkSum(mSerialData);

        ArrayMap aParam = new ArrayMap();
        aParam.put("D0",mSerialData[0]+"");
        aParam.put("D1", mSerialData[1]+"");
        aParam.put("D2", mSerialData[2]+"");
        aParam.put("D3", mSerialData[3]+"");
        aParam.put("D4", mSerialData[4]+"");
        aParam.put("D5", mSerialData[5]+"");
        aParam.put("D6", mSerialData[6]+"");
        aParam.put("D7", mSerialData[7]+"");

        for(int i=0;i<8;i++){
            String string = (String) aParam.get("D"+i);
            Log.d("data","D"+i+":"+string);
        }

        return aParam;
    }
}
