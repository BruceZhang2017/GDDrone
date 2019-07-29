package com.jieli.stream.dv.gdxxx.ui.activity.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.SettingItem;
import com.jieli.stream.dv.gdxxx.ui.adapter.SettingAdapter;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.widget.SwitchButton;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 陈森华 on 2017/7/18.
 * 功能：用一句话描述
 */

public class APPAdvancedSettingActivity extends BaseActivity {
    private String tag = getClass().getSimpleName();
    private ListView listView;
    private Button returnBtn;

    public static void start(Context context) {
        Intent intent = new Intent(context, APPAdvancedSettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_app_advanced_settings);
        listView = (ListView) findViewById(R.id.app_advanced_setting_list_view);
        initUI();
    }

    private void initUI() {
        String[] names = getResources().getStringArray(R.array.app_advanced_setting_list);
        List<SettingItem> items = new ArrayList<>();
        int marginTop = (int) getResources().getDimension(R.dimen.list_marginTop);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (!TextUtils.isEmpty(name)) {
                if (name.equals(getString(R.string.time_format))) {
                    SettingItem<String> item = new SettingItem<>();
                    item.setName(name);
                    item.setType(0);
                    items.add(item);
                } else if (name.equals(getString(R.string.using_hard_codec))) {
                    SettingItem<Boolean> item = new SettingItem<>();
                    item.setName(name);
                    item.setType(1);
                    item.setValue(PreferencesHelper.getSharedPreferences(getApplicationContext()).getBoolean(KEY_HARD_CODEC, true));
                    items.add(item);
                    item.setOnSwitchListener(usingHardCodecListener);
                } else if (name.equals(getString(R.string.open_debug))) {
                    SettingItem<Boolean> item = new SettingItem<>();
                    item.setName(name);
                    item.setType(1);
                    item.setValue(PreferencesHelper.getSharedPreferences(getApplicationContext()).getBoolean(KEY_OPEN_DEBUG,false));
                    items.add(item);
                    item.setOnSwitchListener(openDebugOnSwitchListener);
                }
                if (i == 0 || i == 2)
                    items.get(i).setMarginTop(marginTop);
            }
        }

        listView.setAdapter(new SettingAdapter(getApplicationContext(), items));
        listView.setOnItemClickListener(onItemClickListener);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SettingItem item = (SettingItem) listView.getAdapter().getItem(position);

            if (item.getType() == 0) {
                //获取显示value的textview
                TextView textView = (TextView) view.findViewById(R.id.item_setting_one_tv2);
                if (item.getName().equals(getString(R.string.time_format))) {
                    /**
                     *   处时间格式
                     */
                }
            }
        }
    };


    public void returnBtnClick(View v) {
        onBackPressed();
    }

    private SettingItem.OnSwitchListener usingHardCodecListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(SwitchButton v,SettingItem<Boolean> item, boolean isChecked) {
            PreferencesHelper.putBooleanValue(getApplicationContext(), KEY_HARD_CODEC, isChecked);
        }
    };
    /**
     *  处理打开调试模式
     */
    private SettingItem.OnSwitchListener openDebugOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(SwitchButton v, SettingItem<Boolean> item, boolean isChecked) {
            PreferencesHelper.putBooleanValue(getApplicationContext(),KEY_OPEN_DEBUG, isChecked);
        }
    };

}
