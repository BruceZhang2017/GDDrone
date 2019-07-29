package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

import java.io.UnsupportedEncodingException;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class DeviceNameFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private Button saveBtn;
    private EditText nameEditText;

    private NotifyDialog notifyDialog;
    private WaitingDialog waitingDialog;
    private TextView tipTv;
    private final int prefixLen = IConstant.WIFI_PREFIX.getBytes().length;
    private String tipString;
    private final static int NAME_MAX_LEN = 31;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_setting_name, container, false);
        saveBtn = (Button) view.findViewById(R.id.device_setting_name_save_btn);
        nameEditText = (EditText) view.findViewById(R.id.device_setting_name_et);
        tipTv = (TextView) view.findViewById(R.id.tip_tv);
        final TextView wifiPrefixTextView = (TextView) view.findViewById(R.id.wifi_ssid_prefix_tv);
        wifiPrefixTextView.setText(IConstant.WIFI_PREFIX);
        String ssid = WifiHelper.formatSSID(mWifiHelper.getWifiConnectionInfo().getSSID());
        if (!TextUtils.isEmpty(ssid)) {
            nameEditText.setText(ssid.substring(prefixLen));
        }
        try {
            tipString = String.format(getString(R.string.device_name_input_limit), (NAME_MAX_LEN - prefixLen), nameEditText.getText().toString().getBytes("gbk").length);
            tipTv.setText(tipString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //  Dbug.e(tag, "beforeTextChanged start=" + start + " after=" + after + " count=" + count);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Dbug.e(tag, "onTextChanged start=" + start + " before=" + before + " count=" + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Dbug.e(tag, "onTextChanged length=" + length);
                if (s.toString().contains("\n")) {
                    String str = s.toString().replaceAll("\n", "");
                    nameEditText.setText(str);
                    nameEditText.setSelection(str.length());
                    return;
                }
//                if (s.toString().contains(" ") || s.toString().contains("\n")) {
//                    String str = s.toString().replaceAll(" ", "");
//                    str = str.toString().replaceAll("\n", "");
//                    nameEditText.setText(str);
//                    nameEditText.setSelection(str.length());
//                    return;
//                }
                int length = 0;
                try {
                    length = nameEditText.getText().toString().getBytes("gbk").length + prefixLen;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (length > NAME_MAX_LEN) {
                    int start = NAME_MAX_LEN - prefixLen - 1;
                    if (start <= s.length())
                        s.delete(start, s.length());
                } else {
                    tipTv.setText(R.string.device_name_input_limit);
                    tipTv.setTextColor(getResources().getColor(R.color.text_press_gray));
                }
                try {
                    tipString = String.format(getString(R.string.device_name_input_limit), (NAME_MAX_LEN - prefixLen), s.toString().getBytes("gbk").length);
                    tipTv.setText(tipString);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameEditText.getText().toString();
                int length = name.length();
                if (length < 1 || length > (NAME_MAX_LEN - prefixLen)) {
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.name_format_error));
                } else {
                    showChoseDialog();
                }

            }
        });
        return view;
    }

    /**
     * 设置密码是否立即生效
     *
     * @param immediateEffect
     */
    private void changeName(final boolean immediateEffect) {
        final String preSsid = PreferencesHelper.getSharedPreferences(getContext()).getString(CURRENT_WIFI_SSID, "");
        String pwd = PreferencesHelper.getSharedPreferences(getContext()).getString(preSsid, "");
        ClientManager.getClient().tryToSetApAccount(WIFI_PREFIX + nameEditText.getText().toString().trim(),
                pwd, immediateEffect, new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        if (code != SEND_SUCCESS) {
                            ((BaseActivity) getActivity()).showToastShort(getString(R.string.save_fail));
                        } else {
                            if (immediateEffect) {
                                getActivity().sendBroadcast(new Intent(IActions.ACTION_ACCOUT_CHANGE));
                                PreferencesHelper.remove(getContext(), preSsid);
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            //延时10s
                                            Thread.sleep(5000);//100000
                                            WifiHelper.getInstance(getContext()).removeSavedNetWork(preSsid);
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
    }

    private void showChoseDialog() {
        if (notifyDialog == null) {
            notifyDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.immediate_effect, R.string.dialog_no, R.string.dialog_yes, new NotifyDialog.OnNegativeClickListener() {
                @Override
                public void onClick() {
                    changeName(false);
                    notifyDialog.dismiss();
                }
            }, new NotifyDialog.OnPositiveClickListener() {
                @Override
                public void onClick() {
                    changeName(true);
                    notifyDialog.dismiss();
                    if (waitingDialog == null) {
                        waitingDialog = new WaitingDialog();
                        waitingDialog.setNotifyContent(getString(R.string.dialod_wait));
                    }
                    waitingDialog.show(getFragmentManager(), "change_name_wait_dialog");
                }
            });
        }
        notifyDialog.show(getFragmentManager(), "change_name_dialog");
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
    }

    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                if (notifyDialog != null && notifyDialog.isShowing()) {
                    notifyDialog.dismiss();
                }
                ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                return;
            }
            switch (data.getTopic()) {
                case Topic.AP_SSID_INFO:
                    Dbug.d(tag, getString(R.string.setting_successed));
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_successed));
                    String preSsid = PreferencesHelper.getSharedPreferences(getContext()).getString(CURRENT_WIFI_SSID, "");
                    WifiHelper.getInstance(getContext()).removeSavedNetWork(preSsid);
                    PreferencesHelper.putStringValue(getActivity().getApplicationContext(), CURRENT_WIFI_SSID, WIFI_PREFIX + nameEditText.getText().toString().trim());
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

}
