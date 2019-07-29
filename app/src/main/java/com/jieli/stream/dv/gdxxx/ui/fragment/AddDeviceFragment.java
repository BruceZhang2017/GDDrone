package com.jieli.stream.dv.gdxxx.ui.fragment;


import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.WifiBean;
import com.jieli.stream.dv.gdxxx.interfaces.OnWifiCallBack;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加设备
 * date : 2017/3/6
 */
public class AddDeviceFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, OnWifiCallBack {
    private TextView selectWifiSSIDTv;
    private EditText wifiPwdEdit;
    private ListView searchWifiListView;
    private WifiListAdapter adapter;

    private List<WifiBean> wifiBeanList;
    private ScanResult scanResult;
    private boolean isAddDev = false;


    public AddDeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_device, container, false);
        ImageView returnBtn = (ImageView) view.findViewById(R.id.add_dev_return_btn);
        ImageView refreshBtn = (ImageView) view.findViewById(R.id.add_dev_refresh_btn);
        selectWifiSSIDTv = (TextView) view.findViewById(R.id.add_dev_wifi_name);
        wifiPwdEdit = (EditText) view.findViewById(R.id.add_dev_wifi_pwd_edit);
        TextView addBtn = (TextView) view.findViewById(R.id.add_dev_btn);
        searchWifiListView = (ListView) view.findViewById(R.id.add_dev_list_view);

        returnBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        searchWifiListView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() == null) return;
        refresh();
        WifiHelper.getInstance(getActivity().getApplicationContext()).registerOnWifiCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAddDev = false;
        WifiHelper.getInstance(getActivity().getApplicationContext()).unregisterOnWifiCallback(this);
    }


    @Override
    public void onClick(View view) {
        if(null == getActivity()) return;
        switch (view.getId()){
            case R.id.add_dev_return_btn:
                getActivity().onBackPressed();
                break;
            case R.id.add_dev_refresh_btn:
                refresh();
                break;
            case R.id.add_dev_btn:
                String ssid = selectWifiSSIDTv.getText().toString().trim();
                String pwd = wifiPwdEdit.getText().toString().trim();
                isAddDev = false;
                if(!TextUtils.isEmpty(ssid)){
                    Log.w(TAG, "ssid : " + ssid + " ,pwd : " +pwd);
                    PreferencesHelper.putStringValue(getActivity().getApplicationContext(), CURRENT_WIFI_SSID, ssid);
                    if(TextUtils.isEmpty(pwd)){
                        mWifiHelper.addNetWorkAndConnect(ssid, WifiConfiguration.KeyMgmt.NONE + "", WifiHelper.WifiCipherType.NONE);
                        PreferencesHelper.putStringValue(getActivity().getApplicationContext(), ssid, null);
                        isAddDev = true;
                    }else{
                        if(pwd.length() >= 8){
                            mWifiHelper.addNetWorkAndConnect(ssid, pwd, WifiHelper.WifiCipherType.WPA);
                            PreferencesHelper.putStringValue(getActivity().getApplicationContext(), ssid, pwd);
                            isAddDev = true;
                        }else{
                            a.getApplication().showToastShort(R.string.wifi_pwd_length_not_allow);
                        }
                    }
                }else{
                    a.getApplication().showToastShort(R.string.wifi_pwd_not_empty);
                }
                break;
        }
    }

    private void refresh(){
        if(mWifiHelper != null){
            mWifiHelper.startScan();
            List<ScanResult> scanResultList = mWifiHelper.getWifiScanResult();
            if(scanResultList != null){
                if(wifiBeanList == null){
                    wifiBeanList = new ArrayList<>();
                }else{
                    wifiBeanList.clear();
                }
                for (ScanResult result : scanResultList){
                    if(result != null){
                        String ssid = WifiHelper.formatSSID(result.SSID);
                        if(!TextUtils.isEmpty(ssid) && ssid.startsWith(WIFI_PREFIX)){
                            if(wifiBeanList.size() == 0){
                                scanResult = result;
                            }
                            WifiBean wifiBean = new WifiBean();
                            wifiBean.setSSID(ssid);
                            wifiBean.setSelect(false);
                            wifiBean.setState(0);
                            wifiBeanList.add(wifiBean);
                        }
                    }
                }
                if(wifiBeanList != null && wifiBeanList.size() > 0){
                    WifiBean bean =  wifiBeanList.get(0);
                    if(bean != null){
                        selectWifiSSIDTv.setText(bean.getSSID());
                        bean.setSelect(true);
                    }
                }
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }else{
                    adapter = new WifiListAdapter(wifiBeanList);
                }
                searchWifiListView.setAdapter(adapter);
                updateEditView(scanResult);
            }
        }
    }

    private void updateEditView(ScanResult scanResult){
        if(scanResult != null){
            if(scanResult.capabilities.equals("WPA")){
                wifiPwdEdit.setEnabled(true);
            }else{
                wifiPwdEdit.setText("");
                wifiPwdEdit.setEnabled(false);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(getActivity() == null ) return;
        if(adapter != null){
            WifiBean wifiBean = (WifiBean) adapter.getItem(i);
            if(wifiBean != null){
                wifiBean.setSelect(true);
                selectWifiSSIDTv.setText(wifiBean.getSSID());
                adapter.notifyDataSetChanged();
                ScanResult temp = getScanResultForList(wifiBean);
                if(temp != null){
                    scanResult = temp;
                }
                updateEditView(scanResult);
            }else{
                refresh();
            }
        }
    }

    @Override
    public void onConnected(WifiInfo info) {
        if(info != null){
            String SSID  = WifiHelper.formatSSID(info.getSSID());
            String saveSSID = PreferencesHelper.getSharedPreferences(getActivity().
                    getApplicationContext()).getString(CURRENT_WIFI_SSID, null);
            saveSSID = WifiHelper.formatSSID(saveSSID);
            if(isAddDev && !TextUtils.isEmpty(saveSSID) && saveSSID.equals(SSID)){
                a.getApplication().showToastShort(R.string.connected_wifi_tip);
                isAddDev = false;
            }
        }
    }

    @Override
    public void onError(int errCode) {
        if(isAddDev){
            isAddDev = false;
        }
    }

    private ScanResult getScanResultForList(WifiBean wifiBean){
        if(getActivity() == null) return null;
        if(wifiBean != null){
            String ssid = wifiBean.getSSID();
            if(!TextUtils.isEmpty(ssid)){
                if(mWifiHelper == null) mWifiHelper = WifiHelper.getInstance(getActivity().getApplicationContext());
                List<ScanResult> scanResults = mWifiHelper.getWifiScanResult();
                if(scanResults != null){
                    for (ScanResult scanResult : scanResults){
                        if(scanResult != null){
                            String tempSSID = WifiHelper.formatSSID(scanResult.SSID);
                            if(!TextUtils.isEmpty(tempSSID) && tempSSID.equals(ssid)){
                                return scanResult;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private class WifiListAdapter extends BaseAdapter{
        private List<WifiBean> wifiList;

        WifiListAdapter(List<WifiBean> list){
            this.wifiList = list;
        }
        @Override
        public int getCount() {
            return wifiList == null? 0 : wifiList.size();
        }

        @Override
        public Object getItem(int position) {
            WifiBean bean = null;
            if(wifiList != null && position < wifiList.size()){
                bean = wifiList.get(position);
            }
            return bean;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if(null == getActivity()) return view;
            ViewHolder viewHolder;
            if(view == null){
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_wifi_list, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.wifiIcon = (ImageView) view.findViewById(R.id.item_wifi_icon);
                viewHolder.wifiNameTv = (TextView) view.findViewById(R.id.item_wifi_name);
                viewHolder.wifiStateTv = (TextView) view.findViewById(R.id.item_wifi_state);
                viewHolder.wifiSelectIcon = (ImageView) view.findViewById(R.id.item_wifi_select_state);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
            WifiBean wifiBean = (WifiBean) getItem(position);
            if(wifiBean != null){
                String ssid = wifiBean.getSSID();
                int state = wifiBean.getState();
                boolean isSelect = wifiBean.isSelect();
                if(!TextUtils.isEmpty(ssid)){
                    viewHolder.wifiNameTv.setText(ssid);
                }
                if(state == 1){
                    viewHolder.wifiStateTv.setText(R.string.saved);
                }
                if(isSelect){
                    viewHolder.wifiSelectIcon.setImageResource(R.mipmap.ic_check_round_blue);
                }else{
                    viewHolder.wifiSelectIcon.setImageResource(R.mipmap.ic_uncheck_round_blue);
                }
            }
            return view;
        }

        private class ViewHolder{
            private ImageView wifiIcon;
            private TextView wifiNameTv;
            private TextView wifiStateTv;
            private ImageView wifiSelectIcon;
        }
    }


}
