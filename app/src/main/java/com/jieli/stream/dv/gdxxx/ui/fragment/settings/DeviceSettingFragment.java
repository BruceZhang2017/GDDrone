package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.SettingItem;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.activity.me.DeviceSettingActivity;
import com.jieli.stream.dv.gdxxx.ui.adapter.SettingAdapter;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

import java.util.ArrayList;
import java.util.List;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class DeviceSettingFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private ListView listView;
    private NotifyDialog notifyDialog;
    private NotifyDialog mNotifyDialog;
    private NotifyDialog mResetDialog;
    private WaitingDialog mWaitingDialog;
    private final int DEV_ADVANCED_SETTING = 0;
    private final int FACTORY_RESET = 1;

    private static final int MSG_FINISH_FRAGMENT = 0x6352;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message != null){
                switch (message.what){
                    case MSG_FINISH_FRAGMENT:
                        dismissWaitingDialog();
                        if(getActivity() != null){
                            getActivity().finish();
                        }
                        break;
                }
            }
            return false;
        }
    });

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_setting, container, false);
        listView = (ListView) view.findViewById(R.id.device_setting_list_view);
        initUI();
        return view;
    }

    private void initUI() {
        String[] names;
        if(mApplication.getSearchMode() == STA_SEARCH_MODE){
            names = getResources().getStringArray(R.array.device_setting_list_sta);
        }else{
            names = getResources().getStringArray(R.array.device_setting_list);
        }
        List<SettingItem> items = new ArrayList<>();
        int marginTop = (int) getResources().getDimension(R.dimen.list_marginTop);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (!TextUtils.isEmpty(name)) {
                SettingItem item = new SettingItem<>();
                if (    name.equals(getString(R.string.photo_quality)) ||
                        name.equals(getString(R.string.setting_record_quality)) ||
                        name.equals(getString(R.string.advanced_settings)) ||
                        name.equals(getString(R.string.device_storage_manage)) ||
                        name.equals(getString(R.string.factory_reset)) ||
                        name.equals(getString(R.string.switch_sta_mode)) ||
                        name.equals(getString(R.string.switch_ap_mode))) {
                } /*else if (name.equals(getString(R.string.volume_adjustment))) {
                    item.setType(0);
                    item.setValue(PreferencesHelper.getSharedPreferences(getContext()).getInt(KEY_VOLUME, 0) + "%");
                }*/ else if (name.equals(getString(R.string.device_name))) {
                    item.setType(0);
                    //String devName = PreferencesHelper.getSharedPreferences(getContext()).getString(CURRENT_WIFI_SSID, "");
                    //String sName=getDeviceName();
                    item.setValue(getDeviceName());
                }else if (name.equals(getString(R.string.device_password))) {
                    item.setType(0);
                    String sPass=mApplication.WiFiPassword;
                    if(sPass==null || sPass.contains("(null)")){
                        sPass="";
                    }
                    item.setValue(sPass);
                }
                if (i == 0 || i == 2 || i == 5)
                    item.setMarginTop(marginTop);
                item.setName(name);
                items.add(item);
            }
        }

        listView.setAdapter(new SettingAdapter(getActivity().getApplicationContext(), items));
        listView.setOnItemClickListener(onItemClickListener);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SettingItem item = (SettingItem) listView.getAdapter().getItem(position);
            if (item.getType() == 0) {
                //获取显示value的textview
                TextView textView = (TextView) view.findViewById(R.id.item_setting_one_tv2);
                DeviceSettingActivity activity = (DeviceSettingActivity) getActivity();
                if (item.getName().equals(getString(R.string.device_name))) {
                    activity.toDeviceNameFragment();
                } else if (item.getName().equals(getString(R.string.device_password))) {
                    String ssid = getDeviceName();
                    if (TextUtils.isEmpty(ssid)) {
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.name_empty_error));
                    } else {
                        activity.toDevicePwdFragment();
                    }
                } else if (item.getName().equals(getString(R.string.volume_adjustment))) {
                    activity.toDeviceVolumeFragment();
                } else if (item.getName().equals(getString(R.string.photo_quality))) {
                    activity.toDevicePictureQualityFragment();
                } else if (item.getName().equals(getString(R.string.setting_record_quality))) {
                    activity.toDeviceRecordQualityFragment();
                } else if (item.getName().equals(getString(R.string.camera_model))) {
                    activity.toDeviceCameraModeFragment();
                } else if (item.getName().equals(getString(R.string.advanced_settings))) {
                    if (STATUS_RECORDING == mApplication.getDeviceSettingInfo().getRecordState()) {
                        showStopRecordingDialog(activity, DEV_ADVANCED_SETTING);
                    } else {
                        activity.toDeviceAdvancedSettingFragment();
                    }
                } else if (item.getName().equals(getString(R.string.device_storage_manage))) {
                    if (mApplication.isSdcardExist())
                        activity.toDeviceStorageManageFragment();
                    else {
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.sdcard_offline));
                    }
                } else if(item.getName().equals(getString(R.string.switch_sta_mode))){
                    activity.toDeviceStaModeFragment();
                } else if(item.getName().equals(getString(R.string.switch_ap_mode))){
                    showNotifyDialog();
                } else if (item.getName().equals(getString(R.string.factory_reset))) {
                    if (STATUS_RECORDING == mApplication.getDeviceSettingInfo().getRecordState()) {
                        showStopRecordingDialog(activity, FACTORY_RESET);
                    } else {
                        showFactoryResetDialog();
                    }
                }
            }
        }
    };

    private String getDeviceName() {
        return WifiHelper.formatSSID(mWifiHelper.getWifiConnectionInfo().getSSID());
    }

    private void tryToSendFactoryReset() {
        ClientManager.getClient().tryToFactoryReset(new SendResponse() {
            @Override
            public void onResponse(Integer code) {
                if (code != SEND_SUCCESS) {
                    Dbug.e(tag, "Send failed");
                } else {
                    mResetDialog.dismiss();
                    removeDeviceWifiMsg();
                    if(getActivity() != null){
                        getActivity().sendBroadcast(new Intent(IActions.ACTION_ACCOUT_CHANGE));
                    }
                    showWaitingDialog();
                    if(mHandler != null){
                        mHandler.sendEmptyMessageDelayed(MSG_FINISH_FRAGMENT, 3000);
                    }
                }
            }
        });
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
            mResetDialog.show(getActivity().getSupportFragmentManager(), "notify_dialog");
        }
    }

    private void showStopRecordingDialog(final DeviceSettingActivity activity, final int operation) {
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
                                            case DEV_ADVANCED_SETTING:
                                                if (activity != null)
                                                    activity.toDeviceAdvancedSettingFragment();
                                                break;
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
            mNotifyDialog.show(getActivity().getSupportFragmentManager(), "notify_dialog");
        }
    }

    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                return;
            }
            switch (data.getTopic()) {
                //TODO 处理语言切换后的状态
                case Topic.VIDEO_MIC:
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_successed));
//                    Dbug.e(tag, "MIC=" + data.getParams().get(TopicKey.MIC));
                    break;
            }
        }
    };

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


    @Override
    public void onStart() {
        super.onStart();
        ClientManager.getClient().registerNotifyListener(onNotifyListener);
    }

    @Override
    public void onStop() {
        dismissNotifyDialog();
        if (mNotifyDialog != null && mNotifyDialog.isShowing()) {
            mNotifyDialog.dismiss();
        }
        mNotifyDialog = null;
        if ((mResetDialog != null && mResetDialog.isShowing())) {
            mResetDialog.dismiss();
        }
        mResetDialog = null;
        super.onStop();
        ClientManager.getClient().unregisterNotifyListener(onNotifyListener);
    }

    @Override
    public void onDetach() {
        dismissWaitingDialog();
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDetach();
    }

    private void showNotifyDialog(){
        if(notifyDialog == null){
            notifyDialog = NotifyDialog.newInstance(R.string.dialog_warning, R.string.waring_operation_tip,
                    R.string.dialog_cancel, R.string.dialog_confirm,
                    new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            dismissNotifyDialog();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            SharedPreferences preferences = PreferencesHelper.getSharedPreferences(mApplication);
                            String uuid = mApplication.getUUID();
                            if(!TextUtils.isEmpty(uuid)) {
                                String saveApSSID = preferences.getString(uuid, "");
                                String savePwd = preferences.getString(saveApSSID, "");
                                ClientManager.getClient().tryToSetApAccount(saveApSSID, savePwd, true, new SendResponse() {
                                    @Override
                                    public void onResponse(Integer code) {
                                        if(code == Constants.SEND_SUCCESS){
                                            (new Handler()).postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    searchApMode();
                                                }
                                            }, 500);
                                        }
                                    }
                                });
                            }
                            dismissNotifyDialog();
                        }
                    });
            notifyDialog.setCancelable(false);
        }
        if(!notifyDialog.isShowing() && !isDetached()){
            notifyDialog.show(getFragmentManager(), "notify_dialog");
        }
    }

    private void dismissNotifyDialog(){
        if(notifyDialog != null){
            if(notifyDialog.isShowing() && !isDetached()){
                notifyDialog.dismiss();
            }
            notifyDialog = null;
        }
    }

    private void searchApMode(){
        dismissNotifyDialog();
        mApplication.setSearchMode(AP_SEARCH_MODE);
        ClientManager.getClient().disconnect();
        DeviceSettingActivity activity = (DeviceSettingActivity) getActivity();
        if(activity != null){
            activity.onBackPressed();
        }
    }

    private void showWaitingDialog(){
        if(mWaitingDialog == null){
            mWaitingDialog = new WaitingDialog();
            mWaitingDialog.setNotifyContent(getString(R.string.dialod_wait));
        }
        if(!mWaitingDialog.isShowing()){
            mWaitingDialog.show(getFragmentManager(), "wait_dialog");
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
