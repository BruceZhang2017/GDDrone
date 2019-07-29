package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.bean.SettingItem;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.adapter.SettingAdapter;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.widget.SwitchButton;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.json.JSonManager;

import java.util.ArrayList;
import java.util.List;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class DeviceAdvancedSettingFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private ListView listView;
    private TextView hourTextView;
    private TextView senorTextView;
    private String[] senors;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_advanced_settings, container, false);
        listView = (ListView) view.findViewById(R.id.device_advanced_setting_list_view);
        hourTextView = (TextView) view.findViewById(R.id.hour_tv);
        senorTextView = (TextView) view.findViewById(R.id.senor_tv);
        initUI();
        return view;
    }


    private void initUI() {
        String[] names = getResources().getStringArray(R.array.device_advanced_setting_list);
        List<SettingItem> items = new ArrayList<>();
        int marginTop = (int) getResources().getDimension(R.dimen.list_marginTop);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (!TextUtils.isEmpty(name)) {
                //本页不用
//                Log.e("AppVerion-ItemName","--"+name);
//                if (name.equals(getString(R.string.about_app))) {
//                    SettingItem<String> item = new SettingItem<>();
//                    item.setName(name);
//                    item.setType(0);
//                    String sVersion=a.getApplication().getAppVersionName();
//                    item.setValue(sVersion);
//                    items.add(item);
//                    Log.e("AppVerion","AppVerion"+sVersion);
//
//                    continue;
//                }

                SettingItem<Boolean> item = new SettingItem<>();
                item.setName(name);
                item.setType(1);
                item.setValue(true);
                items.add(item);
                if (name.equals(getString(R.string.parking_monitoring))) {
                    item.setMarginTop(marginTop);
                    item.setValue(mApplication.getDeviceSettingInfo().isVideoParCar());
                    item.setOnSwitchListener(parkingMonitoringOnSwitchListener);
                } else if (name.equals(getString(R.string.time_the_watermark))) {
                    //水印修改为本机保存
                    item.setValue(mApplication.Camera_Watermark);//item.setValue(mApplication.getDeviceSettingInfo().isVideoDate());
                    item.setOnSwitchListener(timeTheWatermarkOnSwitchListener);
                } else if (name.equals(getString(R.string.boot_prompt))) {
                    item.setValue(mApplication.getDeviceSettingInfo().isOpenBootSound());
                    item.setOnSwitchListener(bootPromptoOnSwitchListener);
                } else if (STR_RTSP.equals(name)) {
                    item.setValue(PreferencesHelper.getSharedPreferences(mApplication).getBoolean(KEY_RTSP, false));
                    item.setOnSwitchListener(rtspOnSwitchListener);
                } else if (name.equals(getString(R.string.sound_recording))) {
                    item.setValue(mApplication.getDeviceSettingInfo().isVideoMic());
                    item.setOnSwitchListener(recordingOnSwitchListener);
                }
            }

        }
        listView.setAdapter(new SettingAdapter(getActivity().getApplicationContext(), items));

        hourTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String[] items = getResources().getStringArray(R.array.after_parking_recorder_time);
                showPopupWindow((TextView) v, items, new OnselectedListener() {
                    @Override
                    public void onSelected(String value, int pos) {
                        //TODO 处理停车录像时长
                        hourTextView.setText(String.format(getString(R.string.hour), value));
                    }
                });
                Drawable drawable = getResources().getDrawable(R.mipmap.ic_up);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                ((TextView) v).setCompoundDrawables(null, null, drawable, null);
            }
        });
        senors = getResources().getStringArray(R.array.collision_induction_sensitivity_value);
        senorTextView.setText(senors[mApplication.getDeviceSettingInfo().getGravitySensor()]);
        senorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                showPopupWindow((TextView) v, senors, new OnselectedListener() {
                    @Override
                    public void onSelected(String value, int pos) {
                        //TODO 处理碰撞感应
                        ClientManager.getClient().tryToSetGravitySenor(pos, new SendResponse() {
                            @Override
                            public void onResponse(Integer code) {
                                if (code != SEND_SUCCESS) {
                                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                                }
                            }
                        });
                    }
                });
                Drawable drawable = getResources().getDrawable(R.mipmap.ic_up);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                ((TextView) v).setCompoundDrawables(null, null, drawable, null);
            }
        });
    }


    /**
     * 处理停车监控逻辑
     */
    private SettingItem.OnSwitchListener parkingMonitoringOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(final SwitchButton v, SettingItem<Boolean> item, final boolean isChecked) {
            ClientManager.getClient().tryToSetVideoParkCar(isChecked ? 1 : 0, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code != SEND_SUCCESS) {
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                        v.setCheckedImmediatelyNoEvent(!isChecked);
                    }
                }
            });
        }
    };
    /**
     * 处理时间水印逻辑
     */
    private SettingItem.OnSwitchListener timeTheWatermarkOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(final SwitchButton v, SettingItem<Boolean> item, final boolean isChecked) {
            ClientManager.getClient().tryToSetTimeWatermark(isChecked, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code != SEND_SUCCESS) {
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                        v.setCheckedImmediatelyNoEvent(!isChecked);
                    }else{
                        mApplication.Camera_Watermark=isChecked;
                        PreferencesHelper.putBooleanValue(a.getApplication(), "Camera_Watermark", mApplication.Camera_Watermark);
                    }
                }
            });
        }
    };
    /**
     * 处理开机提示音逻辑
     */
    private SettingItem.OnSwitchListener bootPromptoOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(final SwitchButton v, SettingItem<Boolean> item, final boolean isChecked) {
            ClientManager.getClient().tryToToggleBootSound(isChecked, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code != SEND_SUCCESS) {
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                        v.setCheckedImmediatelyNoEvent(!isChecked);
                    }
                }
            });
        }
    };
    /**
     * 是否开启RTSP
     */
    private SettingItem.OnSwitchListener rtspOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(SwitchButton v, SettingItem<Boolean> item, boolean isChecked) {
            PreferencesHelper.putBooleanValue(mApplication, KEY_RTSP, isChecked);
        }
    };

    /**
     * 处理录像时录音逻辑
     */
    private SettingItem.OnSwitchListener recordingOnSwitchListener = new SettingItem.OnSwitchListener() {
        @Override
        public void onSwitchListener(final SwitchButton v, SettingItem<Boolean> item, final boolean isChecked) {
            ClientManager.getClient().tryToSetVideoMic(isChecked, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code != SEND_SUCCESS) {
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));
                        v.setCheckedImmediatelyNoEvent(!isChecked);
                    }
                }
            });
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

    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                switch (data.getTopic()) {
                    case Topic.GRA_SEN:
                    case Topic.VIDEO_DATE:
                    case Topic.VIDEO_PAR_CAR:
                    case Topic.BOARD_VOICE:
                    case Topic.VIDEO_MIC:
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed));

                }
                return;
            }
            switch (data.getTopic()) {
                case Topic.GRA_SEN:
                    senorTextView.setText(senors[Integer.valueOf(data.getParams().get(TopicKey.GRA))]);
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_successed));
                    break;
                case Topic.VIDEO_DATE:
                case Topic.VIDEO_PAR_CAR:
                case Topic.BOARD_VOICE:
                case Topic.VIDEO_MIC:
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_successed));
                    break;
                case Topic.VIDEO_FINISH: // video finish
                    if (null == data.getParams()) {
                        return;
                    }
                    String desc = data.getParams().get(TopicKey.DESC);
                    Dbug.w(tag, "VIDEO_FINISH: there is desc =" + desc);
                    if (!TextUtils.isEmpty(desc)) {
                        desc = desc.replaceAll("\\\\", "");
                        FileInfo fileInfo = JSonManager.parseFileInfo(desc);
                        if (fileInfo != null) {
                            int cameraType = DeviceClient.CAMERA_FRONT_VIEW;
                            if(CAMERA_TYPE_REAR.equals(fileInfo.getCameraType())){
                                cameraType = DeviceClient.CAMERA_REAR_VIEW;
                            }
                            if(cameraType == mApplication.getDeviceSettingInfo().getCameraType()) {
                                List<FileInfo> fileInfoList = JSonManager.getInstance().getInfoList();
                                if (fileInfoList != null) {
                                    fileInfoList.add(fileInfo);
                                    JSonManager.convertJson(fileInfoList);
                                }
                            }
                        }
                    }
                    break;
            }
        }
    };


    private void showPopupWindow(final TextView view, String[] items, final OnselectedListener onselectedListener) {
        // 一个自定义的布局，作为显示的内容
        LinearLayout contentView = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(view.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setLayoutParams(layoutParams);
        // contentView.setBackgroundColor(Color.GRAY);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        int i = 0;
        for (final String s : items) {
            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(view.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
            l.setMargins(0, 0, 0, 1);
            l.gravity = Gravity.CENTER;
            textView.setLayoutParams(l);
            textView.setText(s);
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(10, 10, 10, 10);
            textView.setBackgroundColor(Color.WHITE);
            contentView.addView(textView);
            final int pos = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onselectedListener.onSelected(s, pos);
                    if (popupWindow.isShowing())
                        popupWindow.dismiss();
                }
            });
            ++i;
        }
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_pupop_window));
        // 设置好参数之后再show
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Drawable drawable = getResources().getDrawable(R.mipmap.ic_down);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                view.setCompoundDrawables(null, null, drawable, null);
            }
        });
        popupWindow.showAsDropDown(view);
    }

    private interface OnselectedListener {
        void onSelected(String value, int pos);
    }
}
