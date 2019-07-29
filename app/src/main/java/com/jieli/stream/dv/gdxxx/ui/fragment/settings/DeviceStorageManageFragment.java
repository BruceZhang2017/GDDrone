package com.jieli.stream.dv.gdxxx.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.lib.dv.control.utils.TopicParam;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.ThumbLoader;
import com.jieli.stream.dv.gdxxx.util.json.JSonManager;

import java.util.ArrayList;
import java.util.Locale;

import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

/**
 * Created by 陈森华 on 2017/7/17.
 * 功能：用一句话描述
 */

public class DeviceStorageManageFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private PieChart mChart;
    private ImageButton delPhotoIbtn;
    private ImageButton delVideoIbtn;
    private TextView tfCapTextView;
    private Button formatBtn;
    private NotifyDialog formatDialog;
    private NotifyDialog mNotifyDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_storage_manage, container, false);
        delPhotoIbtn = (ImageButton) view.findViewById(R.id.photo_del_ibtn);
        delVideoIbtn = (ImageButton) view.findViewById(R.id.video_del_ibtn);
        formatBtn = (Button) view.findViewById(R.id.device_storage_format_btn);
        tfCapTextView = (TextView) view.findViewById(R.id.tf_cap_tv);
        mChart = (PieChart) view.findViewById(R.id.pie_chart);
        int left = mApplication.getDeviceSettingInfo().getLeftStorage();
        int total = mApplication.getDeviceSettingInfo().getTotalStorage();
        if (total > 1024) {
            String str = String.format(Locale.getDefault(), "%.2f", total / 1024.0f) + "GB";
            tfCapTextView.setText(str);
        } else {
            tfCapTextView.setText(total + "MB");
        }
        initChart();
        setData(left, total - left);
        formatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (STATUS_RECORDING == mApplication.getDeviceSettingInfo().getRecordState()) {
                    showStopRecordingDialog();
                } else {
                    showFormatDevice();
                }
            }
        });


        delPhotoIbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 删除照片
                ((BaseActivity) getActivity()).showToastShort(getString(R.string.save_success));
            }
        });

        delVideoIbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 删除视频
                ((BaseActivity) getActivity()).showToastShort(getString(R.string.save_success));
            }
        });

        return view;
    }


    private void initChart() {
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(0, 10, 0, 0);
        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setDrawHoleEnabled(false);
        mChart.setRotationAngle(-90);
        mChart.setRotationEnabled(false);
        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        mChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }

    private void setData(final float rest, float used) {
        mChart.clear();
        final ArrayList<PieEntry> entries = new ArrayList<>();
        PieEntry restPieEntry = new PieEntry(rest, getString(R.string.remaining_storage));
        entries.add(restPieEntry);
        PieEntry usedPieEntry = new PieEntry(used, getString(R.string.used_storage));
        entries.add(usedPieEntry);
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSelectionShift(0f);
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.bg_pie_chart_rest));
        colors.add(getResources().getColor(R.color.bg_pie_chart_used));
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new IValueFormatter() {

            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                String result;
                if (v > 1024) {
                    result = String.format(Locale.getDefault(), "%.2f", v / 1024.0f) + "GB";
                } else if (v < 0.01f) {
                    result = "";
                } else {
                    result = (int) v + "MB";
                }

                return result;
            }
        });
        data.setValueTextSize(16);
        data.setValueTextColor(getResources().getColor(R.color.text_white));
        mChart.setEntryLabelTextSize(0);
        mChart.setData(data);
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();
        ClientManager.getClient().registerNotifyListener(onNotifyListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mNotifyDialog != null && mNotifyDialog.isShowing()) {
            mNotifyDialog.dismiss();
        }
        mNotifyDialog = null;
        ClientManager.getClient().unregisterNotifyListener(onNotifyListener);
    }

    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                switch (data.getTopic()) {
                    case Topic.FORMAT_TF_CARD:
                        ((BaseActivity) getActivity()).showToastShort(getString(R.string.format_failed));
                        break;

                }
                return;
            }
            switch (data.getTopic()) {
                case Topic.VIDEO_CTRL:
                    if(mNotifyDialog != null && mNotifyDialog.isShowing())
                        mNotifyDialog.dismiss();
                    showFormatDevice();
                    break;
                case Topic.FORMAT_TF_CARD:
                    ((BaseActivity) getActivity()).showToastShort(getString(R.string.format_successed));
                    mApplication.getDeviceSettingInfo().setLeftStorage(mApplication.getDeviceSettingInfo().getTotalStorage());
                    int left = mApplication.getDeviceSettingInfo().getLeftStorage();
                    int total = mApplication.getDeviceSettingInfo().getTotalStorage();
                    setData(left, total - left);
                    JSonManager.getInstance().clearData();
                    ThumbLoader.getInstance().clearCache();
                    break;
                case Topic.TF_STATUS:
                    if (!TopicParam.TF_ONLINE.equals(data.getParams().get(TopicKey.ONLINE))) {
                        if (getActivity() != null)
                            getActivity().onBackPressed();
                    }
                    break;

            }
        }
    };

    private void showFormatDevice() {
        if (formatDialog == null) {
            formatDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.format, R.string.dialog_cancel, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                @Override
                public void onClick() {
                    formatDialog.dismiss();
                }
            }, new NotifyDialog.OnPositiveClickListener() {
                @Override
                public void onClick() {
                    ClientManager.getClient().tryToFormatTFCard(new SendResponse() {
                        @Override
                        public void onResponse(Integer code) {
                            if (code != SEND_SUCCESS) {
                                ((BaseActivity) getActivity()).showToastShort(getString(R.string.format_failed));
                            }
                        }
                    });
                    formatDialog.dismiss();
                }
            });
        }
        if (!formatDialog.isShowing())
            formatDialog.show(getFragmentManager(), "formatDialog");
    }

    private void showStopRecordingDialog() {
        if(mNotifyDialog == null){
            mNotifyDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.stop_recording_tips,
                    R.string.dialog_cancel, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            mNotifyDialog.dismiss();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            ClientManager.getClient().tryToRecordVideo(false, new SendResponse() {
                                @Override
                                public void onResponse(Integer code) {
                                    if (code != SEND_SUCCESS) {
                                        mNotifyDialog.dismiss();
                                        a.getApplication().showToastShort(getString(R.string.operation_failed_and_try_again));
                                        Dbug.e(tag, "Send failed");
                                    }
                                }
                            });
                        }
                    });
            mNotifyDialog.setCancelable(false);
        }
        if(!mNotifyDialog.isShowing()){
            mNotifyDialog.show(getActivity().getSupportFragmentManager(), "notify_dialog");
        }
    }
}
