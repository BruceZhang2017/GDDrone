package com.jieli.stream.dv.gdxxx.ui.activity.me;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;

import java.util.Locale;

/**
 * Created by 陈森华 on 2017/7/18.
 * 功能：用一句话描述
 */

public class LanguageActivity extends BaseActivity {
    private String tag = getClass().getSimpleName();
    private RadioGroup radioGroup;
    private String mLastLanguage = "-1";
    private int mSelectLanguage;

    public static void start(Context context) {
        Intent intent = new Intent(context, LanguageActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_language);
        radioGroup = (RadioGroup) findViewById(R.id.language_radio_group);
        radioGroup.removeAllViews();


        String lan = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_APP_LANGUAGE_CODE, "-1");
        mLastLanguage = lan;
        int index = 0, i = 0;
        if (TextUtils.isDigitsOnly(lan)) {
            index = Integer.parseInt(lan);
            if (index > 0) index --;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        String[] lans = getResources().getStringArray(R.array.language);
        for (String language : lans) {
            if (i == 0 || i == 4) {
                RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.item_radiobuttom, radioGroup, false);
                radioButton.setText(language);
                if (index == i) radioButton.setChecked(true);
                radioButton.setId(i);
                radioGroup.addView(radioButton);
            }
            i++;
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                mSelectLanguage = checkedId + 1;
                //Dbug.e(tag, "checkedId=" + checkedId + ", mSelectLanguage=" +mSelectLanguage + ", mLastLanguage=" + mLastLanguage);
            }
        });
    }

//    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
//        @Override
//        public void onNotify(NotifyInfo data) {
//            if (data.getErrorType() != Code.ERROR_NONE) {
//                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
//                return;
//            }
//            switch (data.getTopic()) {
//                case Topic.LANGUAGE:
//                    setLanguage(getLanguage(data.getParams().get(TopicKey.LAG)));
//                    TextView textView = (TextView) findViewById(R.id.device_setting_title);
//                    textView.setText(R.string.language);
//                    Button button = (Button) findViewById(R.id.lan_confirm);
//                    button.setText(R.string.comfirm);
//                    sendBroadcast(new Intent(IActions.ACTION_LANGUAAGE_CHANGE));
//                    showToastShort(getString(R.string.setting_successed));
//                    //finish();
//                    onBackPressed();
//                    break;
//            }
//        }
//    };

    public void returnBtnClick(View v) {
        onBackPressed();
    }

    public void confirmBtnClick(View v) {
        if (mSelectLanguage > 0) {
//            if (ClientManager.getClient().isConnected()) {
//                ClientManager.getClient().tryToSetLanguage(mSelectLanguage, new SendResponse() {
//                    @Override
//                    public void onResponse(Integer code) {
//                        if (code != SEND_SUCCESS) {
//                            showToastShort(getString(R.string.setting_failed));
//                        }
//                    }
//                });
//            } else {
                String preIndex = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_APP_LANGUAGE_CODE, "-1");
                if (!preIndex.equals(mLastLanguage)) {
                    AppUtils.changeAppLanguage(getApplicationContext(), mLastLanguage);
                    sendBroadcast(new Intent(IActions.ACTION_LANGUAAGE_CHANGE));
                    PreferencesHelper.putStringValue(getApplicationContext(), KEY_APP_LANGUAGE_CODE, mLastLanguage);
                }
                showToastShort(getString(R.string.setting_successed));
            //}
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //ClientManager.getClient().registerNotifyListener(onNotifyListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        //ClientManager.getClient().unregisterNotifyListener(onNotifyListener);
    }

    private void setLanguage(Locale locale){
        if (locale == null){
            return;
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getApplicationContext().getResources().updateConfiguration(config, null);
    }
}
