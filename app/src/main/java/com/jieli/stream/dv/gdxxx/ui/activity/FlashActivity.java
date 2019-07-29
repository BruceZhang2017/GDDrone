package com.jieli.stream.dv.gdxxx.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.PermissionUtil;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;

/**
 * 动画界面  检测请求存储权限，GPS权限等。设置权限等。
 */
public class FlashActivity extends BaseActivity{

    private NotifyDialog notifyDialog;
    private NotifyDialog notifyGpsDialog;
    private NotifyDialog requestPermissionDialog;
    private Handler handler = new Handler();
    private Intent toWriteSettingIntent;

    @Override
    protected void onStart() {
        super.onStart();
        String lan = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_APP_LANGUAGE_CODE, "-1");
        if (!"-1".equals(lan))
            AppUtils.changeAppLanguage(getApplicationContext(), lan);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_flash);
        if (mWifiHelper.isWifiOpen()) {
            checkPermissions();
//            if(PermissionUtil.checkBasicPermissions(this)){
//                toMainActivity();
//            }
        }else{
            showNotifyDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 跳转到主界面
     */
    private void toMainActivity() {
        enter(1);//2000
    }

    private void enter(long delay) {
        if(Build.VERSION.SDK_INT >= 23){
            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            if(!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                showNotifyGPSDialog();
//            }else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(FlashActivity.this, MainActivity.class));
                        finish();
                    }
                }, delay);
//            }
        }else{
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(FlashActivity.this, MainActivity.class));
                    finish();
                }
            }, delay);
        }
    }

    /**
     * 动态权限管理
     */
//    private void checkPermissions(){
//        if(Build.VERSION.SDK_INT >= 23){
//            String[] permissions;
//            if(!checkPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
//                permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
//                ActivityCompat.requestPermissions(FlashActivity.this, permissions, PERMISSION_LOCATION_CODE);
//            }else if(!checkPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
//                }else{
//                    permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
//                }
//                ActivityCompat.requestPermissions(FlashActivity.this, permissions, PERMISSION_STORAGE_CODE);
//            }
//            /*else if(!checkPermissionGranted(Manifest.permission.GET_ACCOUNTS)){
//                permissions = new String[]{Manifest.permission.GET_ACCOUNTS};
//                ActivityCompat.requestPermissions(FlashActivity.this, permissions, PERMISSION_CONTACTS_CODE);
//            }*/
//            else if(!checkPermissionGranted(Manifest.permission.WRITE_SETTINGS)
//                    && !Settings.System.canWrite(getApplicationContext())){
//                if(toWriteSettingIntent == null){
//                    toWriteSettingIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                    toWriteSettingIntent.setData(Uri.parse("package:" + getPackageName()));
//                    startActivityForResult(toWriteSettingIntent, PERMISSION_SETTING_CODE );
//                }
//            } else if(!checkPermissionGranted(Manifest.permission.RECORD_AUDIO)){
//                permissions = new String[]{Manifest.permission.RECORD_AUDIO};
//                ActivityCompat.requestPermissions(FlashActivity.this, permissions, PERMISSION_MICROPHONE_CODE);
//            }else{
//                toMainActivity();
//            }
//        }else{
//            toMainActivity();
//        }
//    }


    private void checkPermissions(){
        if(Build.VERSION.SDK_INT >= 23){
            String[] permissions;
            if(!checkPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                }else{
                    permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                }
                ActivityCompat.requestPermissions(FlashActivity.this, permissions, PERMISSION_STORAGE_CODE);
            }
            else if(!checkPermissionGranted(Manifest.permission.WRITE_SETTINGS)
                    && !Settings.System.canWrite(getApplicationContext())){
                if(toWriteSettingIntent == null){
                    toWriteSettingIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    toWriteSettingIntent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(toWriteSettingIntent, PERMISSION_SETTING_CODE );
                }

//                ActivityCompat.requestPermissions(FlashActivity.this, new String[]{Manifest.permission.WRITE_SETTINGS},PERMISSION_SETTING_CODE);

            }
//            else if(!checkPermissionGranted(Manifest.permission.RECORD_AUDIO)){
//                permissions = new String[]{Manifest.permission.RECORD_AUDIO};
//                ActivityCompat.requestPermissions(FlashActivity.this, permissions, PERMISSION_MICROPHONE_CODE);
//            }
            else{
                toMainActivity();
            }
        }else{
            toMainActivity();
        }
    }








    /**
     * 判断权限是否授予
     * @param permission  权限
     */
    private boolean checkPermissionGranted(String permission){
        return ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 显示打开定位服务(gps)提示框
     */
    private void showNotifyGPSDialog(){
        if(notifyGpsDialog == null){
            notifyGpsDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.open_gpg_tip,
                    R.string.dialog_exit, R.string.comfirm,
                    new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            notifyGpsDialog.dismiss();
                            notifyGpsDialog = null;
                            finish();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            notifyGpsDialog.dismiss();
                            notifyGpsDialog = null;
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), CHECK_GPS_CODE);
                        }
                    });
        }
        if(!notifyGpsDialog.isShowing()){
            notifyGpsDialog.show(getSupportFragmentManager(), "notify_gps_dialog");
        }
    }

    /**
     * 显示打开wifi提示框
     */
    private void showNotifyDialog(){
        if(notifyDialog == null){
            notifyDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.open_wifi, R.string.dialog_no,R.string.dialog_yes,
                    new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            notifyDialog.dismiss();
                            notifyDialog = null;
                            checkPermissions();
                            /*handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDialog.dismiss();
                                    notifyDialog = null;
                                    checkPermissions();
                                }
                            }, 1000);
                            */
                        }
                    },
                    new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            notifyDialog.dismiss();
                            notifyDialog = null;
                            mWifiHelper.openWifi();
                            checkPermissions();
                        }
                    });
        }
        if(!notifyDialog.isShowing()){
            notifyDialog.show(getSupportFragmentManager(), "openWifi");
        }
    }

    /**
     * 显示权限请求提示框
     * @param permission    权限
     * @param requestCode   请求码
     */
    private void showRequestPermissionDialog(String permission, int requestCode){
        if(TextUtils.isEmpty(permission)) return;
        String explanation = "";
        switch (requestCode){
            case PERMISSION_LOCATION_CODE:
                explanation = getString(R.string.request_location_permission);
                break;
            case PERMISSION_STORAGE_CODE:
                explanation = getString(R.string.request_sdcard_permission);
                break;
            case PERMISSION_CONTACTS_CODE:
                explanation = getString(R.string.request_contacts_permission);
                break;
            case PERMISSION_MICROPHONE_CODE:
                explanation = getString(R.string.request_microphone_permission);
                break;
            case PERMISSION_SETTING_CODE:
                explanation = getString(R.string.request_write_setting_permission);
                break;
        }
        if(requestPermissionDialog == null){
            requestPermissionDialog = NotifyDialog.newInstance(getString(R.string.dialog_tips), explanation, R.string.dialog_exit, R.string.grant,
                    new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            requestPermissionDialog.dismiss();
                            requestPermissionDialog = null;
                            finish();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            requestPermissionDialog.dismiss();
                            requestPermissionDialog = null;
                            checkPermissions();
                        }
                    });
        }
        if(!requestPermissionDialog.isShowing() && !isFinishing()){
            requestPermissionDialog.setContent(explanation);
            requestPermissionDialog.show(getSupportFragmentManager(), "request_permission_dialog");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler = null;
        if(notifyDialog != null){
            if(notifyDialog.isShowing()){
                notifyDialog.dismiss();
            }
            notifyDialog = null;
        }
        if(notifyGpsDialog != null){
            if(notifyGpsDialog.isShowing()){
                notifyGpsDialog.dismiss();
            }
            notifyGpsDialog = null;
        }
        if(requestPermissionDialog != null){
            if(requestPermissionDialog.isShowing()){
                requestPermissionDialog.dismiss();
            }
            requestPermissionDialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHECK_GPS_CODE){
            toMainActivity();
        }else if(requestCode == PERMISSION_SETTING_CODE){
            toWriteSettingIntent = null;
            if(Build.VERSION.SDK_INT >= 23 && Settings.System.canWrite(getApplicationContext())){
                checkPermissions();
            }else{
                showRequestPermissionDialog(Manifest.permission.WRITE_SETTINGS, requestCode);
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if(grantResults.length == 0){
//            return;
//        }
//        boolean isOk = true;
//        for (int i = 0; i < grantResults.length; i++){
//            int result = grantResults[i];
//            Dbug.w("FlashActivity", "requestCode : " + requestCode +" ,result = " +result);
//            if(result != PackageManager.PERMISSION_GRANTED){
//                String permission = null;
//                if(i < permissions.length){
//                    permission = permissions[i];
//                }
//                if(!TextUtils.isEmpty(permission)){
//                    Dbug.w("FlashActivity", "permission : " + permission);
//                    if(!ActivityCompat.shouldShowRequestPermissionRationale(FlashActivity.this, permission)){
//                        finish();
//                    }else{
//                        showRequestPermissionDialog(permission, requestCode);
//                    }
//                    isOk = false;
//                    break;
//                }
//            }
//        }
//        if(isOk){
//            checkPermissions();
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length == 0){
            return;
        }
        boolean isOk = true;
        for (int i = 0; i < grantResults.length; i++){
            int result = grantResults[i];
            Dbug.w("FlashActivity", "requestCode : " + requestCode +" ,result = " +result);
            if(result != PackageManager.PERMISSION_GRANTED){
                String permission = null;
                if(i < permissions.length){
                    permission = permissions[i];
                }
                if(!TextUtils.isEmpty(permission)){
                    Dbug.w("FlashActivity", "permission : " + permission);
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(FlashActivity.this, permission)){
                        finish();
                    }else{
                        showRequestPermissionDialog(permission, requestCode);
                    }
                    isOk = false;
                    break;
                }
            }


            if(result != PackageManager.PERMISSION_GRANTED){
                if(PermissionUtil.checkBasicPermissions(FlashActivity.this)){
                    toMainActivity();
                }
            }
        }
        if(isOk){
            checkPermissions();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
