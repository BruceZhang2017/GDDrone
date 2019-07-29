package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 选择wifi连接窗口
 * @author zqjasonZhong
 *         date : 2017/10/11
 */
public class SelectWifiDialog extends BaseDialogFragment {
    private TextView tvTitle;
    private Spinner mSpinner;
    private EditText mEditPwd;
    private ImageView ivShowOrHidePwd;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    private WifiListAdapter mAdapter;

    private WifiHelper mWifiHelper;
    private OnConnectWifiListener listener;

    private String mSSID;
    private boolean isShowPwd;

    public void setOnConnectWifiListener(OnConnectWifiListener listener){
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiHelper = WifiHelper.getInstance(getContext());
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_select_wifi, container);
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        tvTitle = (TextView) root.findViewById(R.id.dialog_title);
        mSpinner = (Spinner) root.findViewById(R.id.dialog_wifi_ssid_spinner);
        mEditPwd = (EditText) root.findViewById(R.id.dialog_edit_wifi_pwd);
        ivShowOrHidePwd = (ImageView) root.findViewById(R.id.dialog_show_or_hide_pwd);
        mLeftBtn = (TextView) root.findViewById(R.id.dialog_left);
        mRightBtn = (TextView) root.findViewById(R.id.dialog_right);

        mSpinner.setOnItemSelectedListener(onItemSelectedListener);
        mLeftBtn.setOnClickListener(mOnClickListener);
        mRightBtn.setOnClickListener(mOnClickListener);
        ivShowOrHidePwd.setOnClickListener(mOnClickListener);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() == null || getDialog().getWindow() == null) return;

        final WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = 100;
        params.height = 100;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.width = displayMetrics.heightPixels * 4 / 5;
            params.height = displayMetrics.heightPixels * 3 / 4;
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.width = displayMetrics.widthPixels * 4 / 5;
            params.height = displayMetrics.widthPixels * 3 / 4;
        }
        params.gravity = Gravity.CENTER;
        getDialog().getWindow().setAttributes(params);

        initSpinner();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initSpinner(){
        List<ScanResult> wifiList = mWifiHelper.getWifiScanResult();
        if(wifiList != null){
            List<ScanResult> newWifiList = new ArrayList<>();
            for (ScanResult result : wifiList){
                String ssid = WifiHelper.formatSSID(result.SSID);
                if(!TextUtils.isEmpty(ssid) && !ssid.startsWith(IConstant.WIFI_PREFIX)){
                    newWifiList.add(result);
                }
            }
            if(newWifiList.size() > 0){
                Collections.sort(newWifiList, new Comparator<ScanResult>() {
                    @Override
                    public int compare(ScanResult s1, ScanResult s2) {
                        if(s2.level > s1.level){
                            return 1;
                        }else if(s2.level == s1.level){
                            return 0;
                        }else {
                            return -1;
                        }
                    }
                });
                List<String> dataList = new ArrayList<>();
                for (ScanResult item : newWifiList){
                    dataList.add(item.SSID);
                }
                mAdapter = new WifiListAdapter(getContext());
                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mAdapter.addAll(dataList);
                mSpinner.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(mAdapter != null){
                String item = mAdapter.getItem(i);
                if(!TextUtils.isEmpty(item) && !item.equals(mSSID)){
                    mSSID = item;
                    mEditPwd.setText("");
                    mEditPwd.setSelection(0);
                    mEditPwd.requestFocus();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view != null){
                if(view == mLeftBtn){
                    if(listener != null){
                        listener.onCancel();
                    }
                    dismiss();
                }else if(view == mRightBtn){
                    String pwd = mEditPwd.getText().toString().trim();
                    if(TextUtils.isEmpty(mSSID)){
                        a.getApplication().showToastShort(R.string.wifi_ssid_empty_tip);
                    }else if(!TextUtils.isEmpty(pwd) && pwd.length() < 8){
                        a.getApplication().showToastShort(R.string.wifi_pwd_length_not_allow);
                    }else {
                        if (listener != null) {
                            listener.onSelectWifi(mSSID, pwd);
                        }
                        dismiss();
                    }
                }else if(view == ivShowOrHidePwd){
                    isShowPwd = !isShowPwd;
                    if(isShowPwd){
                        mEditPwd.setTransformationMethod(HideReturnsTransformationMethod
                                .getInstance());
                        ivShowOrHidePwd.setImageResource(R.drawable.dbg_show_pwd_selector);
                    }else{
                        mEditPwd.setTransformationMethod(PasswordTransformationMethod
                                .getInstance());
                        mEditPwd.requestFocus();
                        ivShowOrHidePwd.setImageResource(R.mipmap.ic_hide_pwd);
                    }
                }
            }
        }
    };

    private class WifiListAdapter extends ArrayAdapter<String>{

        private WifiListAdapter(@NonNull Context context) {
            super(context, 0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_select_wifi, parent, false);
                viewHolder = new ViewHolder(convertView);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String item = getItem(position);
            viewHolder.textView.setText(item);
            return convertView;
        }

        private class ViewHolder{
            private TextView textView;

            ViewHolder(View view){
                textView = (TextView) view.findViewById(R.id.item_select_wifi_tv);

                view.setTag(this);
            }
        }
    }

    public interface OnConnectWifiListener {
        void onSelectWifi(String ssid, String pwd);

        void onCancel();
    }
}
