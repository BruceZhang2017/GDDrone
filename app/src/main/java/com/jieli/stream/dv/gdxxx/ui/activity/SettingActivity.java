package com.jieli.stream.dv.gdxxx.ui.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.SettingCmd;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.SettingItem;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.activity.me.APPAdvancedSettingActivity;
import com.jieli.stream.dv.gdxxx.ui.activity.me.AppStorageManageActivity;
import com.jieli.stream.dv.gdxxx.ui.activity.me.DeviceSettingActivity;
import com.jieli.stream.dv.gdxxx.ui.activity.me.LanguageActivity;
import com.jieli.stream.dv.gdxxx.ui.adapter.SettingAdapter;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.ui.widget.SwitchButton;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

import java.util.ArrayList;
import java.util.List;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

//wifi设置
//sensor:亮度。


//1.语言 ：中文（默认），英文
//        2.WIFI参数 -》名称，密码
//        3.存储容量-》存储容量，存储容量饼状图
//        4.拍照像素：30W，100W，200W
//        5.录像分辩率：VGA，720P，1080P
//        6.时间水印：开关按钮
//        7.恢复出厂：选择性对话框
//        8.关于-》参照原关于

//录像时间，语言设置等。返回时连接不成功，自动旋转录像预览。面板。顶上的参数等。

//1.时间；2.sensor参数，左边和GSensor,3.重试次数等。4.水平仪。5.未连接上多尝试三次。6.语言设置。7.发送起飞等命令。8.禁止屏幕两边旋转都可以，只能一边等形式。


//买一个高级电铬铁。连主机头一起吗？


/**
 * 设置
 * date : 2017/3/3
 */
public class SettingActivity extends BaseActivity {
    private static String tag = SettingActivity.class.getSimpleName();
    private ListView listView;

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(ACTION_LANGUAAGE_CHANGE);
        a.getApplication().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mNotifyDialog != null && mNotifyDialog.isShowing()) {
            mNotifyDialog.dismiss();
        }
        mNotifyDialog = null;

        if ((mResetDialog != null && mResetDialog.isShowing())) {
            mResetDialog.dismiss();
        }
        mResetDialog = null;

        dismissWaitingDialog();

        a.getApplication().unregisterReceiver(mBroadcastReceiver);

    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            switch (action) {
                case ACTION_LANGUAAGE_CHANGE:
                    initUI();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.fragment_setting);

        listView = (ListView) findViewById(R.id.setting_list_view);
        // Dbug.e("SettingFragment",mApplication.getDeviceSettingInfo().toString());

        ImageButton btnBack=findImageButton_AutoBack(R.id.btnBack, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();
    }

    private SettingItem item__time_the_watermark=null;
    private void initUI() {
        TextView title = (TextView) findViewById(R.id.setting_top_tv);
        title.setText(R.string.tab_me);
        String[] names = getResources().getStringArray(R.array.setting_list);
        List<SettingItem> items = new ArrayList<>();
        int marginTop = (int) getResources().getDimension(R.dimen.list_marginTop);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (!TextUtils.isEmpty(name)) {
                SettingItem item = new SettingItem<>();
                if (name.equals(getString(R.string.device_setting)) ||
                        name.equals(getString(R.string.app_storage_manager)) ||
                        //name.equals(getString(R.string.app_advanced_settings)) ||
                        name.equals(getString(R.string.factory_reset)) ||
                        name.equals(getString(R.string.help))
                        ) {
                    item.setType(0);
                } else if (name.equals(getString(R.string.about_app))) {
                    item.setType(0);
                    String sVersion=a.getApplication().getAppVersionName();
                    item.setValue(sVersion);
                }else if (name.equals(getString(R.string.language))) {
                    item.setType(0);

                    //if(ClientManager.getClient().isConnected()) {
                        int index = 0;
                        String[] languages = getResources().getStringArray(R.array.language);
                        String languageCode = PreferencesHelper.getSharedPreferences(mApplication).getString(KEY_APP_LANGUAGE_CODE, "-1");
                        if (TextUtils.isDigitsOnly(languageCode)) {
                            index = Integer.parseInt(languageCode) - 1;
                        }
                        if (index > -1 && index < languages.length) item.setValue(languages[index]);
                    //}
                    /* else {
                        item.setValue(getResources().getConfiguration().locale.getDisplayName());
                        Dbug.e(tag, "DisplayName=" + getResources().getConfiguration().locale.getDisplayName());
                    }*/
                } else if (name.equals(getString(R.string.save_picture_in_phone))) {
                    item.setType(1);
                    item.setValue(PreferencesHelper.getSharedPreferences(SettingActivity.this).getBoolean(KEY_SAVE_PICTURE, false));
                    item.setOnSwitchListener(savePictureInPhoneOnSwitchListener);
                } else if (name.equals(getString(R.string.time_the_watermark))) {
                    item__time_the_watermark=item;
                    //item.setMarginTop(marginTop);
                    item.setType(1);
                    //水印修改为本机保存
//                    item.setValue(mApplication.Camera_Watermark);//item.setValue(mApplication.getDeviceSettingInfo().isVideoDate());
                    mApplication.Camera_Watermark = PreferencesHelper.getSharedPreferences(getApplicationContext()).getBoolean("Camera_Watermark", false);
                    item.setValue(mApplication.Camera_Watermark);
                    item.setOnSwitchListener(timeTheWatermarkOnSwitchListener);
                } else if (name.equals(getString(R.string.auto_download_the_photo_files))) {
                    item.setType(1);
                    item.setValue(PreferencesHelper.getSharedPreferences(SettingActivity.this).getBoolean(KEY_AUTO_DOWNLOAG_PICTURE, false));
                    item.setOnSwitchListener(downLoadPhotoOnSwitchListener);
                } else if (getString(R.string.app_advanced_settings).equals(name)) {
                    item.setType(0);
                }
                //if (i <3 || i>5) {
                    //item.setMarginTop(marginTop);//添加大空格
                //}
                item.setName(name);
                items.add(item);
            }
        }
        listView.setAdapter(new SettingAdapter(SettingActivity.this.getApplicationContext(), items));
        listView.setOnItemClickListener(onItemClickListener);
    }


    /**
     * TODO 处理保存图片到手机逻辑
     */
    private SettingItem.OnSwitchListener savePictureInPhoneOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(SwitchButton v, SettingItem<Boolean> item, boolean isChecked) {
            PreferencesHelper.putBooleanValue(SettingActivity.this, KEY_SAVE_PICTURE, isChecked);
        }
    };
    /**
     * 处理时间水印逻辑
     */
    private SettingItem.OnSwitchListener timeTheWatermarkOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(final SwitchButton v, SettingItem<Boolean> item, final boolean isChecked) {
            if (ClientManager.getClient().isConnected()) {
                ClientManager.getClient().tryToSetTimeWatermark(isChecked, new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        String sText=isChecked?getString(R.string.setting_open):getString(R.string.setting_close);
                        String sSetText=sText+"["+getString(R.string.time_the_watermark)+"]";
                        if (code != SEND_SUCCESS) {
                            SettingActivity.this.showToastShort(sSetText+getString(R.string.failed));
                            v.setCheckedImmediatelyNoEvent(!isChecked);
                        }else{
                            SettingActivity.this.showToastShort(sSetText+getString(R.string.successed));

                            //水印设置持久化
                            PreferencesHelper.putBooleanValue(SettingActivity.this,"Camera_Watermark",isChecked);
                        }
                    }
                });
            } else {
                ((BaseActivity) SettingActivity.this).showToastShort(getString(R.string.please_connect_device_to_use));
            }
        }
    };
    /**
     * TODO 处理自动下载拍照文件逻辑
     */
    private SettingItem.OnSwitchListener downLoadPhotoOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(SwitchButton v, SettingItem<Boolean> item, boolean isChecked) {
            PreferencesHelper.putBooleanValue(SettingActivity.this, KEY_AUTO_DOWNLOAG_PICTURE, isChecked);
        }
    };


    private final int DEV_ADVANCED_SETTING = 0;
    private final int FACTORY_RESET = 1;
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SettingItem item = (SettingItem) listView.getAdapter().getItem(position);
            if (item.getType() == 0) {
                //获取显示value的textview
                TextView textView = (TextView) view.findViewById(R.id.item_setting_one_tv2);
                if (item.getName().equals(getString(R.string.device_setting))) {
                    if (ClientManager.getClient().isConnected()) {
                        DeviceSettingActivity.start(SettingActivity.this,0);
                    } else {
                        ((BaseActivity) SettingActivity.this).showToastShort(getString(R.string.please_connect_device_to_use));
                    }
                } else if (item.getName().equals(getString(R.string.language))) {
                    //if (ClientManager.getClient().isConnected()) {
                        LanguageActivity.start(SettingActivity.this);
//                    } else {
//                        ((BaseActivity) SettingActivity.this).showToastShort(getString(R.string.please_connect_device_to_use));
//                    }
                } else if (item.getName().equals(getString(R.string.app_advanced_settings))) {
                    if (ClientManager.getClient().isConnected())
                        APPAdvancedSettingActivity.start(SettingActivity.this);
                    else
                        a.getApplication().showToastShort(getString(R.string.please_connect_device_to_use));
                } else if (item.getName().equals(getString(R.string.photo_quality))) {
                    if (ClientManager.getClient().isConnected()) {
                        DeviceSettingActivity.start(SettingActivity.this,1);
                        //activity.toDevicePictureQualityFragment();
                    } else {
                        ((BaseActivity) SettingActivity.this).showToastShort(getString(R.string.please_connect_device_to_use));
                    }
                } else if (item.getName().equals(getString(R.string.setting_record_quality))) {
                    if (ClientManager.getClient().isConnected()) {
                        DeviceSettingActivity.start(SettingActivity.this,2);
                        //activity.toDeviceRecordQualityFragment();
                    } else {
                        ((BaseActivity) SettingActivity.this).showToastShort(getString(R.string.please_connect_device_to_use));
                    }
                } else if (item.getName().equals(getString(R.string.factory_reset))) {
                    if (ClientManager.getClient().isConnected()) {
                        if (STATUS_RECORDING == mApplication.getDeviceSettingInfo().getRecordState()) {
                            showStopRecordingDialog(FACTORY_RESET);
                        } else {
                            showFactoryResetDialog();
                        }
                    } else {
                        ((BaseActivity) SettingActivity.this).showToastShort(getString(R.string.please_connect_device_to_use));
                    }
                }else if (item.getName().equals(getString(R.string.help))) {
                    Intent intent = new Intent(SettingActivity.this, GenericActivity.class);
                    intent.putExtra(KEY_FRAGMENT_TAG, HELP_FRAGMENT);
                    startActivity(intent);
                } else if (item.getName().equals(getString(R.string.about_app))) {
                    Intent intent = new Intent(SettingActivity.this, GenericActivity.class);
                    intent.putExtra(KEY_FRAGMENT_TAG, ABOUT_FRAGMENT);
                    startActivity(intent);
                } else if (item.getName().equals(getString(R.string.help))) {
                } else if (item.getName().equals(getString(R.string.app_storage_manager))) {
                    AppStorageManageActivity.start(SettingActivity.this);
                }
            } else {
                Dbug.e(tag, "item.getType() " + item.getType() +", position " + position);
            }
        }
    };


    private NotifyDialog mNotifyDialog;
    private NotifyDialog mResetDialog;
    private void showStopRecordingDialog(final int operation) {
        if(mNotifyDialog == null) {
            mNotifyDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.stop_recording_tips,
                    R.string.dialog_cancel, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            mNotifyDialog.dismiss();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            ClientManager.getClient().tryToRecordVideo(false, new SendResponse() {
                                @Override
                                public void onResponse(Integer code) {
                                    if (code != SEND_SUCCESS) {
                                        Dbug.e(tag, "Send failed");
                                    } else {
                                        mNotifyDialog.dismiss();
                                        switch (operation) {
                                            case FACTORY_RESET:
                                                showFactoryResetDialog();
                                                break;
                                            default:
                                                Dbug.e(tag, "Unknown OP:" +operation);
                                                break;
                                        }
                                    }
                                }
                            });
                        }
                    });
            mNotifyDialog.setCancelable(false);
        }
        if(!mNotifyDialog.isShowing()){
            mNotifyDialog.show(SettingActivity.this.getSupportFragmentManager(), "notify_dialog");
        }
    }
    private void showFactoryResetDialog() {
        if(mResetDialog == null){
            mResetDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.factory_reset_tips,
                    R.string.dialog_cancel, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            mResetDialog.dismiss();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            tryToSendFactoryReset();
                        }
                    });
            mResetDialog.setCancelable(false);
        }
        if(!mResetDialog.isShowing()){
            mResetDialog.show(SettingActivity.this.getSupportFragmentManager(), "notify_dialog");
        }
    }
    private void removeDeviceWifiMsg(){
        String saveSSID = PreferencesHelper.getSharedPreferences(a.getApplication()).getString(CURRENT_WIFI_SSID, null);
        if (!TextUtils.isEmpty(saveSSID)) {
            if (!TextUtils.isEmpty(saveSSID) && saveSSID.startsWith(WIFI_PREFIX)) {
                WifiHelper.getInstance(a.getApplication()).removeSavedNetWork(saveSSID);
            }
            PreferencesHelper.remove(a.getApplication(), saveSSID);
            PreferencesHelper.remove(a.getApplication(), CURRENT_WIFI_SSID);
        }
    }
    private void tryToSendFactoryReset() {
        setDefaultAll();
//        ClientManager.getClient().tryToFactoryReset(new SendResponse() {
//            @Override
//            public void onResponse(Integer code) {
//                if (code != SEND_SUCCESS) {
//                    Dbug.e(tag, "Send failed");
//                    SettingActivity.this.showToastShort(getString(R.string.setting_failed));
//                } else {
//                    mResetDialog.dismiss();
//                    removeDeviceWifiMsg();
//                    SettingActivity.this.sendBroadcast(new Intent(IActions.ACTION_ACCOUT_CHANGE));
//                    showWaitingDialog();
//
//                    if(mHandler != null){
//                        mHandler.sendEmptyMessageDelayed(MSG_FINISH_FRAGMENT, 10000);
//                    }
//                }
//            }
//        });
    }

    public void setDefaultAll(){

        //AP_SSID_INFO
        SettingCmd aNewCmd1 = new SettingCmd();
        aNewCmd1.setTopic("PHOTO_BRIGHTNESS");
        aNewCmd1.setOperation("PUT");
        ArrayMap aParam1 = new ArrayMap();
        aParam1.put("brightness", mApplication.Camera_brt_Default+"");
        aNewCmd1.setParams(aParam1);
        ClientManager.getClient().tryToPut(aNewCmd1, new SendResponse() {
            @Override
            public void onResponse(Integer integer) {
                if (integer != SEND_SUCCESS) {
                    Dbug.e(tag, "Send failed");
                    SettingActivity.this.showToastShort(getString(R.string.setting_failed));
                }else{
                    mApplication.Camera_brt=mApplication.Camera_brt_Default;
                    PreferencesHelper.putIntValue(a.getApplication(), "Camera_brt", mApplication.Camera_brt);

                    SettingCmd aNewCmd2 = new SettingCmd();
                    aNewCmd2.setTopic("PHOTO_EXP");
                    aNewCmd2.setOperation("PUT");
                    ArrayMap aParam2 = new ArrayMap();
                    aParam2.put("exp", mApplication.Camera_exp_Default+"");
                    aNewCmd2.setParams(aParam2);
                    ClientManager.getClient().tryToPut(aNewCmd2, new SendResponse() {
                        @Override
                        public void onResponse(Integer integer) {
                            if (integer != SEND_SUCCESS) {
                                Dbug.e(tag, "Send failed");
                                SettingActivity.this.showToastShort(getString(R.string.setting_failed));
                            }else{
                                mApplication.Camera_exp=mApplication.Camera_exp_Default;
                                PreferencesHelper.putIntValue(a.getApplication(), "Camera_exp", mApplication.Camera_exp_Default);

                                SettingCmd aNewCmd3 = new SettingCmd();
                                aNewCmd3.setTopic("PHOTO_CONTRAST");
                                aNewCmd3.setOperation("PUT");
                                ArrayMap aParam = new ArrayMap();
                                aParam.put("contrast", mApplication.Camera_ctr_Default+"");
                                aNewCmd3.setParams(aParam);
                                ClientManager.getClient().tryToPut(aNewCmd3, new SendResponse() {
                                    @Override
                                    public void onResponse(Integer integer) {
                                        if (integer != SEND_SUCCESS) {
                                            Dbug.e(tag, "Send failed");
                                            SettingActivity.this.showToastShort(getString(R.string.setting_failed));
                                        }else{
                                            mApplication.Camera_ctr=mApplication.Camera_ctr_Default;
                                            PreferencesHelper.putIntValue(a.getApplication(), "Camera_ctr", mApplication.Camera_ctr_Default);

                                            SettingCmd aNewCmd4 = new SettingCmd();
                                            aNewCmd4.setTopic("WHITE_BALANCE");
                                            aNewCmd4.setOperation("PUT");
                                            ArrayMap aParam4 = new ArrayMap();
                                            aParam4.put("wbl", mApplication.Camera_wbl_Default+"");
                                            aNewCmd4.setParams(aParam4);
                                            ClientManager.getClient().tryToPut(aNewCmd4, new SendResponse() {
                                                @Override
                                                public void onResponse(Integer integer) {
                                                    if (integer != SEND_SUCCESS) {
                                                        Dbug.e(tag, "Send failed");
                                                        SettingActivity.this.showToastShort(getString(R.string.setting_failed));
                                                    }else{
                                                        mApplication.Camera_wbl=mApplication.Camera_wbl_Default;
                                                        PreferencesHelper.putIntValue(a.getApplication(), "Camera_wbl", mApplication.Camera_wbl_Default);

                                                        //wifi名称和密码，时间水印
                                                        ClientManager.getClient().tryToSetTimeWatermark(false, new SendResponse() {
                                                            @Override
                                                            public void onResponse(Integer code) {
                                                                if (code != SEND_SUCCESS) {
                                                                    Dbug.e(tag, "Send failed");
                                                                    SettingActivity.this.showToastShort(getString(R.string.setting_failed));
                                                                }else{
                                                                    if(item__time_the_watermark!=null){
                                                                        item__time_the_watermark.setValue(false);
                                                                    }

                                                                    mResetDialog.dismiss();
                                                                    removeDeviceWifiMsg();
                                                                    SettingActivity.this.sendBroadcast(new Intent(IActions.ACTION_ACCOUT_CHANGE));
                                                                    showWaitingDialog();

                                                                    if(mHandler != null){
                                                                        mHandler.sendEmptyMessageDelayed(MSG_FINISH_FRAGMENT, 3000);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private static final int MSG_FINISH_FRAGMENT = 0x6352;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message != null){
                switch (message.what){
                    case MSG_FINISH_FRAGMENT:
                        SettingActivity.this.showToastShort(getString(R.string.setting_successed));
                        dismissWaitingDialog();
                        SettingActivity.this.finish();
                        break;
                }
            }
            return false;
        }
    });
    private WaitingDialog mWaitingDialog;
    private void showWaitingDialog(){
        if(mWaitingDialog == null){
            mWaitingDialog = new WaitingDialog();
            mWaitingDialog.setNotifyContent(getString(R.string.dialod_wait));
        }
        if(!mWaitingDialog.isShowing()){
            mWaitingDialog.show(SettingActivity.this.getSupportFragmentManager(), "wait_dialog");
        }
    }
    private void dismissWaitingDialog(){
        if(mWaitingDialog != null){
            if(mWaitingDialog.isShowing()){
                mWaitingDialog.dismiss();
            }
            mWaitingDialog = null;
        }
    }


}
