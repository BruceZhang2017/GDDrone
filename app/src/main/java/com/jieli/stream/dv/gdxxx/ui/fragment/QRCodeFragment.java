package com.jieli.stream.dv.gdxxx.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.activity.MainActivityOld;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.QRCodeDialog;
import com.jieli.stream.dv.gdxxx.util.json.listener.OnCompletedListener;

/**
 * Description:
 * Author:created by bob on 18-1-17.
 */
public class QRCodeFragment extends BaseFragment implements View.OnClickListener {
    private String tag = getClass().getSimpleName();
    private EditText mEditWifiSSID;
    private EditText mEditWifiPwd;
    private Button mGenerateQRCodeButton;
    private Button mBackToSearchButton;
    private Button mReturnButton;
    private ImageView mPasswordView;
    private boolean isShowPwd;
    private CheckBox mSaveInfoCheckbox;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sta_qr_code, container, false);
        mEditWifiSSID = (EditText) view.findViewById(R.id.edit_hot_spot_wifi);
        mEditWifiPwd = (EditText) view.findViewById(R.id.edit_hot_spot_pwd);
        mGenerateQRCodeButton = (Button) view.findViewById(R.id.generate_qr_code_btn);
        mPasswordView = (ImageView) view.findViewById(R.id.show_or_hide_pwd);
        mReturnButton = (Button) view.findViewById(R.id.hot_spot_return_btn);
        mSaveInfoCheckbox = (CheckBox) view.findViewById(R.id.save_sta_msg);
        mBackToSearchButton = (Button) view.findViewById(R.id.back_to_search_btn);
        mGenerateQRCodeButton.setOnClickListener(this);
        mPasswordView.setOnClickListener(this);
        mReturnButton.setOnClickListener(this);
        mBackToSearchButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v == mGenerateQRCodeButton) {
            showQRCodeDialog(mEditWifiSSID.getText().toString().trim(), mEditWifiPwd.getText().toString().trim());
        } else if (v == mPasswordView) {
            isShowPwd = !isShowPwd;
            handlerPwdUI();
        } else if (v == mReturnButton) {
            toDeviceListFragment(null);
        } else if (v == mBackToSearchButton) {
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_SEARCH_MODE, STA_SEARCH_MODE);
            toDeviceListFragment(bundle);
        }
    }

    private void handlerPwdUI(){
        if(isShowPwd){
            mEditWifiPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mPasswordView.setImageResource(R.drawable.dbg_show_pwd_selector);
        }else{
            mEditWifiPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mEditWifiPwd.requestFocus();
            mPasswordView.setImageResource(R.mipmap.ic_hide_pwd);
        }
    }

    private void showQRCodeDialog(final String wifi, final String pwd) {
        if(TextUtils.isEmpty(wifi)) {
            mApplication.showToastShort(R.string.wifi_ssid_empty_tip);
            return;
        } else {
            //Dbug.e(tag, "wifi=" + wifi +", pwd=" + pwd);
            if (!TextUtils.isEmpty(pwd) && pwd.length() < 8) {
                mApplication.showToastShort(R.string.pwd_lenth_limits);
                return;
            }
        }

        QRCodeDialog mQRCodeDialog = QRCodeDialog.newInstance(wifi, pwd, mSaveInfoCheckbox.isChecked());
        mQRCodeDialog.setOnCompletedListener(new OnCompletedListener<Boolean>() {
            @Override
            public void onCompleted(Boolean data) {
                if (data) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_SEARCH_MODE, STA_SEARCH_MODE);
                    toDeviceListFragment(bundle);
                }
            }
        });

        if (!mQRCodeDialog.isShowing()) {
            mQRCodeDialog.show(getActivity().getSupportFragmentManager(), "QRDialog");
        }
    }

    private void toDeviceListFragment(Bundle bundle) {
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);
        if (!(fragment instanceof DeviceListFragment))
            fragment = new DeviceListFragment();
        fragment.setArguments(bundle);
        ((MainActivityOld)getActivity()).changeFragment(R.id.container, fragment, fragment.getClass().getSimpleName());
    }

}
