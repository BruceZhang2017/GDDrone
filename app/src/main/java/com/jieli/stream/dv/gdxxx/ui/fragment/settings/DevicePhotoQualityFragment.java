package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class DevicePhotoQualityFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private RadioGroup radioGroup;
    private TextView photoQulityTextView;
    private TextView timeTextView;

    String sSetText="";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_setting_photo_qulity, container, false);
        radioGroup = (RadioGroup) view.findViewById(R.id.photo_qulity_radio_group);
        photoQulityTextView = (TextView) view.findViewById(R.id.photo_qulity_value);
        timeTextView = (TextView) view.findViewById(R.id.recording_time);
        final String[] values = getResources().getStringArray(R.array.photo_qulity);
        //设置当前图片质量
        final int currentQualityIndex = mApplication.getDeviceSettingInfo().getPhotoQualityIndex();
        ((RadioButton) radioGroup.getChildAt(currentQualityIndex)).setChecked(true);
        String quality = String.format(getString(R.string.photo_qulity_value), values[currentQualityIndex]);
        photoQulityTextView.setText(quality);
        //剩余可录制时间
        String time = "12:12:12";
        time = String.format(getString(R.string.recorded_time), time);
        timeTextView.setText(time);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                final int index = group.indexOfChild(radioGroup.findViewById(checkedId));
                sSetText=values[index];
                String quality = String.format(getString(R.string.photo_qulity_value), values[index]);
                photoQulityTextView.setText(quality);
                ClientManager.getClient().tryToSetPhotoQuality(index, new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        //设置失败,恢复radiobutton的选择状态
                        if (code != SEND_SUCCESS) {
                            ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                            ((RadioButton) radioGroup.getChildAt(currentQualityIndex)).setChecked(true);
                        }
                    }
                });

            }
        });
        return view;
    }

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

    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                switch (data.getTopic()) {
                    case Topic.PHOTO_QUALITY:
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed)+"("+sSetText+")");
                        break;
                }
                return;
            }
            switch (data.getTopic()) {
                case Topic.PHOTO_QUALITY:
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_successed)+"("+sSetText+")");
                    Dbug.e(tag, "qua=" + Integer.valueOf(data.getParams().get(TopicKey.QUA)));
                    break;
            }
        }
    };

}
