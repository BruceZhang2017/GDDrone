package com.jieli.stream.dv.gdxxx.bean;

import com.jieli.stream.dv.gdxxx.ui.widget.SwitchButton;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class SettingItem<T> {
    private String name;
    private T value;
    private int marginTop;
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public OnSwitchListener getOnSwitchListener() {
        return onSwitchListener;
    }

    public void setOnSwitchListener(OnSwitchListener onSwitchListener) {
        this.onSwitchListener = onSwitchListener;
    }

    private OnSwitchListener onSwitchListener;

    public interface OnSwitchListener {
        void onSwitchListener(SwitchButton v, SettingItem<Boolean> item, boolean isChecked);
    }
}
