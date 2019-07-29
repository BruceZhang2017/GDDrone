package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.lib.dv.control.utils.TopicParam;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.DeviceSettingInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

public class RecordQualityFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private RadioGroup radioGroup;
    private RadioGroup mRearRadioGroup;
    private boolean isRecoding = false;
    private boolean isLastRecoding = false;
    private boolean isModified = false;
    private boolean isRearModified = false;
    private int mSelectedLevel;
    private int mRearSelectedLevel;
    private TextView mRearDeviceName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_resolution, container, false);
        radioGroup = (RadioGroup) view.findViewById(R.id.record_quality_radio_group);
        mRearRadioGroup = (RadioGroup) view.findViewById(R.id.rear_record_res_radio_group);
        mRearRadioGroup.setOnCheckedChangeListener(mRearDeviceOnCheckedChangeListener);
        mRearDeviceName = (TextView) view.findViewById(R.id.rear_camera_name);

        DeviceSettingInfo deviceSettingInfo = mApplication.getDeviceSettingInfo();
        if (deviceSettingInfo != null && deviceSettingInfo.isExistRearView()) {
            showRearCameraUI();
        } else {
            hideRearCameraUI();
        }
        //Current record resolution
        int currentLevel = getCameraLevel(DeviceClient.CAMERA_FRONT_VIEW);
        if (radioGroup.getChildCount() > 0)
            ((RadioButton) radioGroup.getChildAt(currentLevel)).setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                final int index = group.indexOfChild(radioGroup.findViewById(checkedId));
                Dbug.e(tag, "index " + index);
                if (!isModified) {
                    isModified = true;
                    mSelectedLevel = index;

                    switchResolution();
                }
            }
        });
        return view;
    }

    private final RadioGroup.OnCheckedChangeListener mRearDeviceOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            final int index = group.indexOfChild(mRearRadioGroup.findViewById(checkedId));
            Dbug.e(tag, "rear index " + index);
            if (!isRearModified) {
                isRearModified = true;
                mRearSelectedLevel = index;
                switchResolution();
            }
        }
    };

    private void switchResolution() {
        isLastRecoding = isRecoding;
        if (isRecoding) {//Process: Close --> open
            ClientManager.getClient().tryToRecordVideo(false, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if (code != SEND_SUCCESS) {
                        Dbug.e(tag, "Send failed");
                    }
                }
            });
        } else {
            sendRecordParam();
        }
    }

    private int getCameraLevel(int cameraType){
        int level = IConstant.RTS_LEVEL_HD;
        DeviceSettingInfo settingInfo = a.getApplication().getDeviceSettingInfo();
        if(settingInfo != null){
            if(cameraType == DeviceClient.CAMERA_REAR_VIEW){
                level = settingInfo.getRearLevel();
            }else{
                level = settingInfo.getFrontLevel();
            }
        }
        return level;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        a mainApplication = a.getApplication();
        DeviceSettingInfo deviceSettingInfo = mainApplication.getDeviceSettingInfo();
        if (deviceSettingInfo != null && deviceSettingInfo.getRecordState() == STATUS_RECORDING) {
            isRecoding = true;
        } else {
            isRecoding = false;
        }
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
                return;
            }
            //Dbug.e(tag, "data " + data);
            switch (data.getTopic()) {
                case Topic.VIDEO_CTRL:  // video start
                    if (null == data.getParams()) {
                        return;
                    }
                    String state = data.getParams().get(TopicKey.STATUS);
                    if (!TextUtils.isEmpty(state)) {
                        if (!TopicParam.OPEN.equals(state) && isLastRecoding && (isModified||isRearModified)) {
                            sendRecordParam();
                        }
                    }
                    break;
                case Topic.PULL_VIDEO_STATUS:
                    Dbug.e(tag, "PULL_VIDEO_STATUS:" + data);
                    Dbug.e(tag, "width: " + data.getParams().get(TopicKey.WIDTH));
                    boolean isExistRearCamera = TopicParam.TF_ONLINE.equals(data.getParams().get(TopicKey.STATUS));
                    if (isExistRearCamera) {
                        showRearCameraUI();
                    } else {
                        hideRearCameraUI();
                    }
                    break;
                case Topic.VIDEO_PARAM:
                case Topic.PULL_VIDEO_PARAM:
                    if ((isModified&&!isRearModified) || (!isModified&&isRearModified)) {
                        if (isModified) isModified = false;
                        if (isRearModified) isRearModified = false;
                        String rtsWidth = data.getParams().get(TopicKey.WIDTH);
                        String rtsHeight = data.getParams().get(TopicKey.HEIGHT);
                        Dbug.w(tag, "isLastRecoding "+ isLastRecoding + ", set success: w " + rtsWidth +", h " + rtsHeight);

                        if (isLastRecoding) {
                            ClientManager.getClient().tryToRecordVideo(true, new SendResponse() {
                                @Override
                                public void onResponse(Integer code) {
                                    if (code != SEND_SUCCESS) {
                                        Dbug.e(tag, "Send failed");
                                    } else {
                                        isLastRecoding = false;
                                    }
                                }
                            });
                        }
                    } else {
                       Dbug.w(tag, "Not modified ");
                    }
                    break;
            }
        }
    };

    private void sendRecordParam() {
        int[] params;
        if (isRearModified) {
            params = AppUtils.getRtsResolution(mRearSelectedLevel);
            int format = mApplication.getDeviceSettingInfo().getRearFormat();
            ClientManager.getClient().tryToSetRearVideoParams(params[0], params[1], format, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    String sSetText="";
                    if(mSelectedLevel==0)
                        sSetText="VGA";
                    else if(mSelectedLevel==1)
                        sSetText="720P";
                    else if(mSelectedLevel==2)
                        sSetText="1080P";
                    if(code != SEND_SUCCESS){
                        Dbug.e(tag, "Send failed");
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed)+"("+sSetText+")");
                    }else{
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_successed)+"("+sSetText+")");
                    }
                }
            });
        } else if (isModified) {
            params = AppUtils.getRtsResolution(mSelectedLevel);
            int format = mApplication.getDeviceSettingInfo().getFrontFormat();
            int fps = mApplication.getDeviceSettingInfo().getFrontRate();
            ClientManager.getClient().tryToSetFrontVideoParams(params[0], params[1], format, fps, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    String sSetText="";
                    if(mSelectedLevel==0)
                        sSetText="VGA";
                    else if(mSelectedLevel==1)
                        sSetText="720P";
                    else if(mSelectedLevel==2)
                        sSetText="1080P";
                    if(code != SEND_SUCCESS){
                        Dbug.e(tag, "Send failed");
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_failed)+"("+sSetText+")");
                    }else{
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.setting_successed)+"("+sSetText+")");
                    }
                }
            });
        } else {
            Dbug.e(tag, "Not supported");
        }
    }

    private void hideRearCameraUI() {
        mRearDeviceName.setVisibility(View.GONE);
        mRearRadioGroup.setVisibility(View.GONE);
    }

    private void showRearCameraUI() {
        int currentRearLevel = getCameraLevel(DeviceClient.CAMERA_REAR_VIEW);
        if (mRearRadioGroup.getChildCount() > 0)
            ((RadioButton) mRearRadioGroup.getChildAt(currentRearLevel)).setChecked(true);
        mRearRadioGroup.setOnCheckedChangeListener(mRearDeviceOnCheckedChangeListener);
        mRearDeviceName.setVisibility(View.VISIBLE);
        mRearRadioGroup.setVisibility(View.VISIBLE);
    }
}
