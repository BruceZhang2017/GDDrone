package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class DeviceCameraModeFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private RadioGroup radioGroup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_setting_camera_mode, container, false);
        radioGroup = (RadioGroup) view.findViewById(R.id.camera_mode_radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                //TODO 摄像模式状态处理
                switch (checkedId) {
                    case R.id.full_screen_mode_rbtn:
                        break;
                    case R.id.width_screen_mode_rbtn:
                        break;
                }

            }
        });
        return view;
    }

}
