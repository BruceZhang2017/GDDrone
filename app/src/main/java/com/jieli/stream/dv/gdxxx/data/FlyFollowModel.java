package com.jieli.stream.dv.gdxxx.data;

import android.support.v4.util.ArrayMap;
import android.util.Log;

public class FlyFollowModel {

    public static byte[] mSerialData = new byte[15];

    private static byte checkSum(byte[] arry) {

        byte mSum = 0x00;
        for (int i = 3; i < arry.length - 1; i++) {
            mSum ^= arry[i] & 0xff;
        }
        return mSum;
    }


    public static ArrayMap getFlyCtrlDataMap(int latitude, int longitude){

        mSerialData[0] = 0x46;
        mSerialData[1] = 0x48;
        mSerialData[2] = 0x3c;
        mSerialData[3] = 0x6d;
        mSerialData[4] = 0x08;
        mSerialData[5] = 0x00;
        mSerialData[6] = (byte) (latitude & 0xff);
        mSerialData[7] = (byte) (latitude >> 8 & 0xff);
        mSerialData[8] = (byte) (latitude >> 16 & 0xff);
        mSerialData[9] = (byte) (latitude >> 24 & 0xff);
        mSerialData[10] = (byte) (longitude & 0xff);
        mSerialData[11] = (byte) (longitude >> 8 & 0xff);
        mSerialData[12] = (byte) (longitude >> 16 & 0xff);
        mSerialData[13] = (byte) (longitude >> 24 & 0xff);
        mSerialData[14] = checkSum(mSerialData);

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
        aParam.put("D10",btou(mSerialData[10])+"");
        aParam.put("D11",btou(mSerialData[11])+"");
        aParam.put("D12",btou(mSerialData[12])+"");
        aParam.put("D13",btou(mSerialData[13])+"");
        aParam.put("D14",btou(mSerialData[14])+"");

        for(int i=0;i<15;i++){
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
