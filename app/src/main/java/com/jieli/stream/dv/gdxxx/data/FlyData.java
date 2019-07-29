package com.jieli.stream.dv.gdxxx.data;

import android.support.v4.util.ArrayMap;
import android.util.Log;

public class FlyData {
    public static int MAX = 256;
    public static int MIDDLE = MAX/2;

    public static byte[] mSerialData = new byte[11];
    public static int[] rudderData = new int[5];//0:specific item,1:leftright,2:updown,3:oil,4:LR_rotate

    public static boolean isTouched = false;

    public FlyData(){
        init();
    }
    
    public static void init() {
        rudderData[1] = 128;
        rudderData[2] = 128;
        rudderData[4] = 128;

        //Oil,LR_Rotate,Trim_LR_Rotate,FB,LR,Trim_FB,Trim_LR,SpecialByte
        mSerialData[0] = 0;
        mSerialData[1] = 0;             //Oil
        mSerialData[2] = 0;             //LR_Rotate
        mSerialData[3] = 0;             //Trim_LR_Rotate
        mSerialData[4] = 0;             //FB
        mSerialData[5] = 0;             //LR
        mSerialData[6] = 0;             //Trim_FB
        mSerialData[7] = 0;             //Trim_LR
        mSerialData[8] = 0;             //SpecialByte
        mSerialData[9] = 0;
        mSerialData[10] = 0;
    }

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
        mSerialData[3] = 0x6a;
        mSerialData[4] = 0x04;
        mSerialData[5] = 0x00;
        mSerialData[6] = (byte) rudderData[2]; // pitch
        mSerialData[7] = (byte) rudderData[1]; // roll
        if (rudderData[3] < 25) {
            mSerialData[8] = 0;
        } else {
            mSerialData[8] = (byte) rudderData[3];
        }
        mSerialData[9] = (byte) rudderData[4]; // yaw
        mSerialData[10] = checkSum(mSerialData);

        ArrayMap aParam = new ArrayMap();
        aParam.put("D0",mSerialData[0]+"");
        aParam.put("D1", mSerialData[1]+"");
        aParam.put("D2", mSerialData[2]+"");
        aParam.put("D3", mSerialData[3]+"");
        aParam.put("D4", mSerialData[4]+"");
        aParam.put("D5", mSerialData[5]+"");
        aParam.put("D6", mSerialData[6]+"");
        aParam.put("D7", mSerialData[7]+"");
        aParam.put("D8", mSerialData[8]+"");
        aParam.put("D9", mSerialData[9]+"");
        aParam.put("D10",mSerialData[10]+"");

        for(int i=0;i<11;i++){
            Log.d("data","D"+i+":"+byteToHexStr(mSerialData[i]));
        }

        return aParam;
    }

    public static String byteToHexStr(byte aByte) {
        String hexStr = String.format("%02x", Integer.valueOf(aByte & 255));
        return hexStr;
    }


    public static String byteArrToHexStr(byte[] arry) {
        StringBuffer sb = new StringBuffer();
        int i = 0;

        for(int len = arry.length; i < len; ++i) {
            String str1 = String.format("%02x", Integer.valueOf(arry[i] & 255));
            sb.append(str1 + " ");
        }

        return sb.toString();
    }

}