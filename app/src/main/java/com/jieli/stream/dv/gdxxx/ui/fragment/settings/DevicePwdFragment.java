package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.PasswordConfirmDialog;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class DevicePwdFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private Button saveBtn;
    private EditText pwdEditText;
    private EditText pwdComfirEditText;
    private PasswordConfirmDialog changePwdDialog;
    private NotifyDialog notifyDialog;
    private WaitingDialog waitingDialog;
    private String currentPwd;
    private String currentSsid;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_setting_pwd, container, false);
        saveBtn = (Button) view.findViewById(R.id.pwd_setting_save_btn);
        pwdEditText = (EditText) view.findViewById(R.id.pwd_setting_et);
        pwdComfirEditText = (EditText) view.findViewById(R.id.pwd_setting_comfir_et);
        currentSsid = PreferencesHelper.getSharedPreferences(getContext()).getString(CURRENT_WIFI_SSID, "");
        currentPwd = PreferencesHelper.getSharedPreferences(getContext()).getString(currentSsid, "");
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = pwdEditText.getText().toString();
                String pwd2 = pwdComfirEditText.getText().toString();
                if (pwd.length() == 0 && pwd2.length() == 0) {
                    if (TextUtils.isEmpty(currentPwd)) {
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.pwd_is_empty_now));
                    } else {
                        showChoseDialog();
                    }
                } else if (!pwd.equals(pwd2)) {
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.pwd_check_fail));
                } else {
                    if (pwd.length() < 8) {
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.pwd_lenth_limits));
                    } else {
                        showChoseDialog();
                    }
                }
            }
        });
        return view;
    }

    /**
     * @param immediateEffect 设置密码是否立即生效
     */
    private void showPwdInputDialog(final boolean immediateEffect) {
        if (changePwdDialog == null) {
            changePwdDialog = PasswordConfirmDialog.newInstance(getString(R.string.input_old_pwd));
            changePwdDialog.setOnInputCompletionListener(new PasswordConfirmDialog.OnInputCompletionListener() {
                @Override
                public void onCompletion(String ssid, String password) {
                    if (password.equals(currentPwd)) {
                        if (immediateEffect) {
                            if (waitingDialog == null) {
                                waitingDialog = new WaitingDialog();
                                waitingDialog.setNotifyContent(getString(R.string.dialod_wait));
                            }
                            waitingDialog.show(getFragmentManager(), "change_name_wait_dialog");
                        }
                        ClientManager.getClient().tryToSetApAccount(currentSsid, pwdEditText.getText().toString().trim(), immediateEffect, new SendResponse() {
                            @Override
                            public void onResponse(Integer code) {
                                if (code != SEND_SUCCESS) {
                                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                                } else {
                                    if (immediateEffect) {
                                        getActivity().sendBroadcast(new Intent(IActions.ACTION_ACCOUT_CHANGE));
                                        PreferencesHelper.remove(getContext(), currentSsid);
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                super.run();
                                                try {
                                                    mApplication.switchWifi();
                                                    //延时10s
                                                    Thread.sleep(5000);//10000
                                                    WifiHelper.getInstance(getContext()).removeSavedNetWork(currentSsid);
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (waitingDialog != null && waitingDialog.isShowing()) {
                                                                waitingDialog.dismiss();
                                                            }
                                                            getActivity().finish();
                                                        }
                                                    });
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                }
                            }
                        });
                    } else {
                        Dbug.e(tag, "current pwd="+ currentPwd+", password="+password);
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.input_pld_pws_error));
                    }
                }
            });
        }
        changePwdDialog.show(getFragmentManager(), "changePwdDialog");
    }

    private void showChoseDialog() {
        if (notifyDialog == null) {
            notifyDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.immediate_effect, R.string.dialog_no, R.string.dialog_yes, new NotifyDialog.OnNegativeClickListener() {
                @Override
                public void onClick() {
                    showPwdInputDialog(false);
                    notifyDialog.dismiss();
                }
            }, new NotifyDialog.OnPositiveClickListener() {
                @Override
                public void onClick() {
                    showPwdInputDialog(true);
                    notifyDialog.dismiss();
                }
            });
        }
        notifyDialog.show(getFragmentManager(), "change_name_dialog");
    }


    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                return;

            }
            switch (data.getTopic()) {
                case Topic.AP_SSID_INFO:
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_successed));
                    String ssid = PreferencesHelper.getSharedPreferences(getContext()).getString(CURRENT_WIFI_SSID, "");
                    PreferencesHelper.putStringValue(getActivity().getApplicationContext(), ssid, pwdEditText.getText().toString().trim());
                    currentPwd = pwdEditText.getText().toString().trim();
                    break;
            }
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        ClientManager.getClient().registerNotifyListener(onNotifyListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        ClientManager.getClient().unregisterNotifyListener(onNotifyListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notifyDialog != null && notifyDialog.isShowing())
            notifyDialog.dismiss();
        if (waitingDialog != null && waitingDialog.isShowing())
            waitingDialog.dismiss();
        notifyDialog = null;
        waitingDialog = null;
        if (changePwdDialog != null && changePwdDialog.isShowing())
            changePwdDialog.dismiss();
        changePwdDialog = null;
    }
}
