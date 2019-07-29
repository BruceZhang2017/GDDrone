package com.jieli.stream.dv.gdxxx.ui.activity.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DeviceAdvancedSettingFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DeviceCameraModeFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DeviceNameFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DevicePhotoQualityFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DevicePwdFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DeviceSettingFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DeviceStaModeFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DeviceStorageManageFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.DeviceVolumeFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.settings.RecordQualityFragment;
import com.jieli.stream.dv.gdxxx.util.Dbug;

public class DeviceSettingActivity extends BaseActivity {
    private String tag = getClass().getSimpleName();

    public static void start(Context context,int iItemType) {
        Intent intent = new Intent(context, DeviceSettingActivity.class);
        intent.putExtra("itemtype",iItemType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_generic);
        int iItemType=getIntent().getIntExtra("itemtype",0);
        if(iItemType==1){
            toDevicePictureQualityFragment();
        }else if(iItemType==2){
            toDeviceRecordQualityFragment();
        }else {
            toDeviceSettingFragment();
        }
    }

    public void toDeviceSettingFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DeviceSettingFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DeviceSettingFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DeviceSettingFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment);
    }

    public void toDeviceNameFragment() {
        Dbug.e(tag, "toDeviceNameFragment");
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DeviceNameFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DeviceNameFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DeviceNameFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }

    public void toDevicePwdFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DevicePwdFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DevicePwdFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DevicePwdFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }


    public void toDeviceVolumeFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DeviceVolumeFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DeviceVolumeFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DeviceVolumeFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }

    public void toDevicePictureQualityFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DevicePhotoQualityFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DevicePhotoQualityFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DevicePhotoQualityFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }

    public void toDeviceRecordQualityFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof RecordQualityFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(RecordQualityFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new RecordQualityFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }

    public void toDeviceCameraModeFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DeviceCameraModeFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DeviceCameraModeFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DeviceCameraModeFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }


    public void toDeviceAdvancedSettingFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DeviceAdvancedSettingFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DeviceAdvancedSettingFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DeviceAdvancedSettingFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }


    public void toDeviceStorageManageFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DeviceStorageManageFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DeviceStorageManageFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DeviceStorageManageFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }

    public void toDeviceStaModeFragment(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
        if (!(fragment instanceof DeviceStaModeFragment)) {
            fragment = getSupportFragmentManager().findFragmentByTag(DeviceStaModeFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = new DeviceStaModeFragment();
            }
        }
        changeFragment(R.id.generic_fragment_layout, fragment, fragment.getClass().getSimpleName());
    }

    public void returnBtnClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else
            super.onBackPressed();
    }

}
