package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class DeviceVolumeFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private SeekBar seekBar;
    private ImageButton volumePlusImageButton;
    private ImageButton volumeDownImageButton;
    private TextView volumeTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_setting_volume, container, false);
        seekBar = (SeekBar) view.findViewById(R.id.volume_seek_bar);
        volumeDownImageButton = (ImageButton) view.findViewById(R.id.volume_down_img_btn);
        volumePlusImageButton = (ImageButton) view.findViewById(R.id.volume_plus_img_btn);
        volumeTextView = (TextView) view.findViewById(R.id.device_volume_value_tv);
        seekBar.setProgress(PreferencesHelper.getSharedPreferences(getContext()).getInt(KEY_VOLUME, 0));
        volumeTextView.setText(String.format(getResources().getString(R.string.device_volume_value), seekBar.getProgress()));
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        /**
         * TODO 处理音量减逻辑
         */
        volumeDownImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getProgress() - 10);
            }
        });
        /**
         * TODO 处理音量加逻辑
         */
        volumePlusImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getProgress() + 10);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                PreferencesHelper.putIntValue(getContext(), KEY_VOLUME, progress);
                String string = String.format(getResources().getString(R.string.device_volume_value), seekBar.getProgress());
                volumeTextView.setText(string);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return view;
    }


    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                ((BaseActivity) getActivity()).showToastShort(getString(R.string.save_fail));
                return;
            }
            switch (data.getTopic()) {
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
