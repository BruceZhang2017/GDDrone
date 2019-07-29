package com.jieli.stream.dv.gdxxx.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.SettingItem;
import com.jieli.stream.dv.gdxxx.ui.widget.SwitchButton;

import java.util.List;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */


public class SettingAdapter extends BaseAdapter {
    private List<SettingItem> items;

    private Context mContext;

    public SettingAdapter(Context context, List<SettingItem> items) {
        this.mContext = context;
        this.items = items;

    }

    public void setList(List<SettingItem> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(int position) {
        SettingItem item = null;
        if (items != null && position < items.size()) {
            item = items.get(position);
        }
        return item;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        SettingItem item = (SettingItem) getItem(position);
        if (item != null)
            return item.getType();
        else
            return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        final SettingItem item = (SettingItem) getItem(position);
        switch (type) {
            case 0:
                ViewHelper viewHelper;
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device_setting_one, parent, false);
                    convertView.setPadding(0,item.getMarginTop(),0,0);
                    viewHelper = new ViewHelper();
                    viewHelper.tv1 = (TextView) convertView.findViewById(R.id.item_setting_one_tv1);
                    viewHelper.tv2 = (TextView) convertView.findViewById(R.id.item_setting_one_tv2);
                    convertView.setTag(viewHelper);
                } else {
                    viewHelper = (ViewHelper) convertView.getTag();
                }
                if (!TextUtils.isEmpty(item.getName())) {
                    viewHelper.tv1.setText(item.getName());
                    viewHelper.tv2.setText((String) item.getValue());
                }
                break;
            case 1:
                final ViewHelper1 viewHelper1;
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device_setting_two, parent, false);
                    convertView.setPadding(0,item.getMarginTop(),0,0);
                    viewHelper1 = new ViewHelper1();
                    viewHelper1.tv1 = (TextView) convertView.findViewById(R.id.item_setting_three_tv1);
                    viewHelper1.switchButton = (SwitchButton) convertView.findViewById(R.id.item_setting_three_icon);
                    convertView.setTag(viewHelper1);
                    viewHelper1.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (item.getOnSwitchListener() != null) {
                                item.setValue(isChecked);
                                item.getOnSwitchListener().onSwitchListener(viewHelper1.switchButton,(SettingItem<Boolean>) getItem(position),isChecked);
                            }
                        }
                    });

                } else {
                    viewHelper1 = (ViewHelper1) convertView.getTag();
                }
                if (!TextUtils.isEmpty(item.getName())) {
                    viewHelper1.tv1.setText(item.getName());
                    viewHelper1.switchButton.setCheckedImmediatelyNoEvent((Boolean) item.getValue());
                }
                break;
            case 2:
                ViewHelper viewHelper2;
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device_setting_three, parent, false);
                    convertView.setPadding(0,item.getMarginTop(),0,0);
                    viewHelper2 = new ViewHelper();
                    viewHelper2.tv1 = (TextView) convertView.findViewById(R.id.item_setting_three_tv1);
                    viewHelper2.tv2 = (TextView) convertView.findViewById(R.id.item_setting_three_tv2);
                    convertView.setTag(viewHelper2);
                } else {
                    viewHelper2 = (ViewHelper) convertView.getTag();
                }
                if (!TextUtils.isEmpty(item.getName())) {
                    viewHelper2.tv1.setText(item.getName());
                    viewHelper2.tv2.setText((String) item.getValue());
                }
                break;
        }

        return convertView;
    }

    private class ViewHelper {
        private TextView tv1;
        private TextView tv2;
//            private ImageView icon;
    }

    private class ViewHelper1 {
        private TextView tv1;
        private SwitchButton switchButton;
    }
}
