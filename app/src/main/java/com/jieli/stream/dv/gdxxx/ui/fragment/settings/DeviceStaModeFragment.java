package com.jieli.stream.dv.gdxxx.ui.fragment.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

/**
 *
 */
public class DeviceStaModeFragment extends BaseFragment {

    private EditText editWifiSSID;
    private EditText editWifiPwd;
    private ImageView ivShowOrHidePwd;
    private CheckBox mSaveSTAMsgCheckbox;
    private Button btnSwitch;

    private boolean isShowPwd;
    private boolean isSaveMsg;

    private Handler mHandler = new Handler();

    public DeviceStaModeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_sta_mode, container, false);
        editWifiSSID = (EditText) view.findViewById(R.id.edit_wifi_ssid);
        editWifiPwd = (EditText) view.findViewById(R.id.edit_wifi_pwd);
        ivShowOrHidePwd = (ImageView) view.findViewById(R.id.show_or_hide_pwd);
        mSaveSTAMsgCheckbox = (CheckBox) view.findViewById(R.id.save_sta_msg);
        btnSwitch = (Button) view.findViewById(R.id.switch_sta_btn);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isSaveMsg = mSaveSTAMsgCheckbox.isChecked();
        ivShowOrHidePwd.setOnClickListener(mOnClickListener);
        btnSwitch.setOnClickListener(mOnClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!isDetached() && view != null){
                switch (view.getId()){
                    case R.id.switch_sta_btn:
                        sendRouterInformation();
                        break;
                    case R.id.show_or_hide_pwd:{
                        isShowPwd = !isShowPwd;
                        handlerPwdUI();
                        break;
                    }
                }
            }
        }
    };

    private void sendRouterInformation() {
        String ssid = editWifiSSID.getText().toString().trim();
        String pwd = editWifiPwd.getText().toString().trim();
        isSaveMsg = mSaveSTAMsgCheckbox.isChecked();
        if(TextUtils.isEmpty(ssid)){
            mApplication.showToastShort(R.string.wifi_ssid_empty_tip);
        }else{
            if(TextUtils.isEmpty(pwd)){
                pwd = "";
            }else{
                if(pwd.length() < 8){
                    mApplication.showToastShort(R.string.pwd_lenth_limits);
                    return;
                }
            }
            final String mSSID = ssid;
            final String mPwd = pwd;
            ClientManager.getClient().tryToSetSTAAccount(ssid, pwd, isSaveMsg, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if(code == SEND_SUCCESS){
                        Dbug.i(TAG, "send set sta cmd ok");
                        ClientManager.getClient().disconnect();
                        saveApMsg();
                        if(mHandler != null){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mApplication.setSearchMode(STA_SEARCH_MODE);
                                    mWifiHelper.connectWifi(mApplication, mSSID, mPwd);
                                    if(getActivity() != null){
                                        getActivity().finish();
                                    }
                                }
                            }, 1000);
                        }
                    }
                }
            });
        }
    }

    private void handlerPwdUI(){
        if(isShowPwd){
            editWifiPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivShowOrHidePwd.setImageResource(R.drawable.dbg_show_pwd_selector);
        }else{
            editWifiPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            editWifiPwd.requestFocus();
            ivShowOrHidePwd.setImageResource(R.mipmap.ic_hide_pwd);
        }
    }

    private void saveApMsg(){
        SharedPreferences preferences = PreferencesHelper.getSharedPreferences(a.getApplication());
        String saveSSID = preferences.getString(CURRENT_WIFI_SSID, null);
        String uuid = a.getApplication().getUUID();
        if(!TextUtils.isEmpty(saveSSID) && !TextUtils.isEmpty(uuid)){
            String savePwd = preferences.getString(saveSSID, null);
            PreferencesHelper.putStringValue(a.getApplication(), uuid, saveSSID);
            PreferencesHelper.putStringValue(a.getApplication(), saveSSID, savePwd);
        }
    }
}
