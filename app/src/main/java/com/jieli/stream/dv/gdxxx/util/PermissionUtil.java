package com.jieli.stream.dv.gdxxx.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;


public class PermissionUtil {
    private static int REQUEST_EXTERNAL_STORAGE = 1;
    public final static int PERMISSIONS_REQUEST_CODE = 3;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    private static String[] BASIC_PERMISSIONS = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_PHONE_STATE",
    };
//     "android.permission.WRITE_SETTINGS"

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity,
                        PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestBasicPermissions(Activity activity){
        for(int i = 0; i< BASIC_PERMISSIONS.length; i++){
            int permission = ActivityCompat.checkSelfPermission(activity, BASIC_PERMISSIONS[i]);
            if(permission != PackageManager.PERMISSION_GRANTED){
                //申请权限
                ActivityCompat.requestPermissions(activity, BASIC_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
                break;
            }
        }
    }

    public static boolean checkBasicPermissions(Activity activity){
        for(int i = 0; i< BASIC_PERMISSIONS.length; i++){
            int permission = ActivityCompat.checkSelfPermission(activity, BASIC_PERMISSIONS[i]);
            if(permission != PackageManager.PERMISSION_GRANTED){
                //申请权限
                ActivityCompat.requestPermissions(activity, BASIC_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    public static boolean isVerfiedBasicPermissions(Activity activity){
        for(int i = 0; i< BASIC_PERMISSIONS.length; i++){
            int permission = ActivityCompat.checkSelfPermission(activity, BASIC_PERMISSIONS[i]);
            if(permission != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

}
