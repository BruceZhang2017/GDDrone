package com.jieli.stream.dv.gdxxx.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.UpgradeStep;
import com.jieli.stream.dv.gdxxx.interfaces.OnWifiCallBack;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.FTPClientUtil;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 升级界面
 */
public class UpgradeFragment extends BaseFragment{
    private TextView tvTip;
    private ListView mListView;
    private StateAdapter mAdapter;
    private ProgressBar mProgressBar;
    private ProgressBar mUploadPb;

    private ExecutorService service;
    private SharedPreferences sharedPreferences;
    private UploadFileThread uploadFileThread;


    private List<UpgradeStep> dataList;
    private List<String> upgradePathList;
    private int upgradeType = UPGRADE_APK_TYPE;
    private String upgradePath;
    private boolean isConnectDev;

    private static final int LIMIT_TIME = 60000;
    private static final int MSG_UPDATE_STEP_UI = 0x1001;
    private static final int MSG_UPGRADE_APK = 0x1002;
    private static final int MSG_UPGRADE_SDK = 0x1003;
    private static final int MSG_UPGRADE_RESULT = 0x1004;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(getActivity() != null && message != null){
                switch (message.what) {
                    case MSG_UPDATE_STEP_UI:
                        int position = message.arg1;
                        int state = message.arg2;
                        if (position < dataList.size()) {
                            dataList.get(position).setState(state);
                            tvTip.setVisibility(View.VISIBLE);
                            tvTip.setText(getString(R.string.executing_step, dataList.get(position).getDescription()));
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        break;
                    case MSG_UPDATE_DOWNLOAD_PROGRESS:
                        int progress = message.arg1;
                        if (mProgressBar != null) {
                            mProgressBar.setProgress(progress);
                        }
                        break;
                    case MSG_UPDATE_UPLOAD_PROGRESS:
                        int progress1 = message.arg1;
                        if(mUploadPb != null){
                            mUploadPb.setProgress(progress1);
                        }
                        break;
                    case MSG_UPGRADE_APK:{
                        List<String> upgradePath = (List<String>) message.obj;
                        if (upgradePath != null && upgradePath.size() > 1) {
                            upgradePathList = upgradePath;
                            sendHandlerMsg(mHandler, 2, 2, null, 0);
                            Intent updateAPK = new Intent(Intent.ACTION_VIEW);
                            updateAPK.setDataAndType(Uri.parse("file://" + upgradePathList.get(1)), "application/vnd.android.package-archive");
                            startActivityForResult(updateAPK, CODE_UPGRADE_APK);
                            getActivity().finish();
                        }
                        break;
                      }
                    case MSG_UPGRADE_SDK: {
                        List<String> upgradePath = (List<String>) message.obj;
                        if (upgradePath != null && upgradePath.size() > 1) {
                            upgradePathList = upgradePath;
                            String ssid = sharedPreferences.getString(CURRENT_WIFI_SSID, null);
                            if(!TextUtils.isEmpty(ssid)){
                                String pwd = sharedPreferences.getString(ssid, null);
                                mWifiHelper.connectWifi(mApplication, ssid, pwd);
                                isConnectDev = true;
                            }
                        }
                        break;
                    }
                    case MSG_UPGRADE_RESULT:
                        int result = message.arg1;
                        if(result != 1){
                            a.getApplication().showToastLong(R.string.upgrade_failed_tip);
                        }else{
                            if(dataList != null && dataList.size() > 5){
                                sendHandlerMsg(mHandler, 4, 2, null, 0);
                                sendHandlerMsg(mHandler, 5, 2, null, 100);
                            }
                            getActivity().sendBroadcast(new Intent(ACTION_SDK_UPGRADE_SUCCESS));
                        }
                        getActivity().finish();
                        break;
                }
            }
            return false;
        }
    });

    public UpgradeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upgrade, container, false);
        tvTip = (TextView) view.findViewById(R.id.upgrade_tip);
        tvTip.setVisibility(View.GONE);
        mListView = (ListView) view.findViewById(R.id.upgrade_list_view);
        mListView.setEnabled(false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null){
            service = Executors.newFixedThreadPool(2);
            sharedPreferences = PreferencesHelper.getSharedPreferences(getActivity().getApplicationContext());
            mApplication.setUpgrading(true);
            Bundle bundle = getBundle();
            if(bundle != null){
                upgradeType = bundle.getInt(UPDATE_TYPE);
                ArrayList<String> pathList = bundle.getStringArrayList(UPDATE_PATH);
                if(pathList != null && pathList.size() > 0){
                    upgradePath = pathList.get(0);
                }
            }
            WifiHelper.getInstance(getActivity().getApplicationContext()).registerOnWifiCallback(wifiCallBack);
            initListView();

            if(upgradeType == UPGRADE_APK_TYPE){
                service.submit(new UpgradeAPK(mHandler));
            }else{
                if(mApplication.isSdcardExist()){
                    service.submit(new UpgradeSDK(mHandler));
                }else{
                    mApplication.showToastShort(R.string.sdcard_online);
                    getActivity().finish();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(service != null){
            service.shutdownNow();
        }
        WifiHelper.getInstance(getActivity().getApplicationContext()).unregisterOnWifiCallback(wifiCallBack);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mApplication.setUpgrading(false);
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if(uploadFileThread != null){
            uploadFileThread.interrupt();
            uploadFileThread = null;
        }
        if(service != null){
            service.shutdownNow();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE_UPGRADE_APK){
            Dbug.w(TAG, "-resultCode- " + resultCode);
        }
    }

    private void initListView(){
        dataList = new ArrayList<>();
        String[] strs;
        if(upgradeType == UPGRADE_APK_TYPE){
            strs = getResources().getStringArray(R.array.upgrade_apk_steps);
        }else{
            strs = getResources().getStringArray(R.array.upgrade_sdk_steps);
        }
        for (int i = 0; i < strs.length; i++){
            String str = strs[i];
            UpgradeStep upgradeStep = new UpgradeStep();
            upgradeStep.setNum(i);
            upgradeStep.setDescription(str);
            if(i == 1){
                upgradeStep.setNeedPb(true);
            }else if(upgradeType == UPGRADE_SDK_TYPE && i == 3){
                upgradeStep.setNeedPb(true);
            }
            dataList.add(upgradeStep);
        }

        mAdapter = new StateAdapter(getActivity().getApplicationContext());
        mListView.setAdapter(mAdapter);
        mAdapter.setDataList(dataList);
        mAdapter.notifyDataSetChanged();
    }

    private OnWifiCallBack wifiCallBack = new OnWifiCallBack() {
        @Override
        public void onConnected(WifiInfo info) {
            String saveSSID  = sharedPreferences.getString(CURRENT_WIFI_SSID, null);
            String ssid = WifiHelper.formatSSID(info.getSSID());
            if(!TextUtils.isEmpty(saveSSID) && saveSSID.equals(ssid)){
                if(dataList.get(2).getState() == 1){
                    sendHandlerMsg(mHandler, 2, 2, null, 0);
                    sendHandlerMsg(mHandler, 3, 1, null, 100);
                    isConnectDev = false;
                    mApplication.sendCommandToService(SERVICE_CMD_CONNECT_CTP);

                    if(upgradePathList != null && upgradePathList.size() > 1){
                        if(service != null && uploadFileThread == null){
                            uploadFileThread = new UploadFileThread("JL_AC54.bfu", upgradePathList.get(1), mHandler);
                            service.submit(uploadFileThread);
                        }
                    }
                }
            }else if(isConnectDev){
                if(!TextUtils.isEmpty(saveSSID)){
                    String pwd = sharedPreferences.getString(saveSSID, null);
                    mWifiHelper.connectWifi(mApplication, saveSSID, pwd);
                }
            }
        }

        @Override
        public void onError(int errCode) {

        }
    };

    private class StateAdapter extends BaseAdapter{
        private List<UpgradeStep> dataList;
        private Context mContext;

        StateAdapter(Context context){
            mContext = context;
        }

        void setDataList(List<UpgradeStep> dataList) {
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            return dataList == null ? 0 : dataList.size();
        }

        @Override
        public Object getItem(int i) {
            UpgradeStep item = null;
            if(dataList != null && i < dataList.size()){
                item = dataList.get(i);
            }
            return item;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view == null){
                view = LayoutInflater.from(mContext).inflate(R.layout.item_upgrade, viewGroup, false);
                viewHolder = new ViewHolder(view);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
            UpgradeStep item = (UpgradeStep) getItem(i);
            if(item != null){
                viewHolder.tvStep.setText(item.getDescription());

                switch (item.getState()){
                    case 0:
                        viewHolder.ivState.setImageResource(R.mipmap.ic_gary_dot);
                        viewHolder.tvStep.setTextColor(getResources().getColor(R.color.text_gray));
                        break;
                    case 1:
                        viewHolder.ivState.setImageResource(R.mipmap.ic_refresh_green);
                        viewHolder.tvStep.setTextColor(getResources().getColor(R.color.text_black));
                        Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate_forevery);
                        LinearInterpolator lin = new LinearInterpolator();
                        operatingAnim.setInterpolator(lin);
                        viewHolder.ivState.startAnimation(operatingAnim);
                        break;
                    case 2:
                        viewHolder.tvStep.setTextColor(getResources().getColor(R.color.text_black));
                        viewHolder.ivState.clearAnimation();
                        viewHolder.ivState.setImageResource(R.mipmap.ic_complete);
                        break;
                }

                if(item.isNeedPb()){
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    if(i == 1){
                        mProgressBar = viewHolder.progressBar;
                    }else{
                        mUploadPb = viewHolder.progressBar;
                    }
                }else{
                    viewHolder.progressBar.setVisibility(View.GONE);
                }
            }
            return view;
        }

        private class ViewHolder{
            private ImageView ivState;
            private TextView tvStep;
            private ProgressBar progressBar;

            ViewHolder(View view){
                ivState = (ImageView) view.findViewById(R.id.item_upgrade_state);
                tvStep = (TextView) view.findViewById(R.id.item_upgrade_step);
                progressBar = (ProgressBar) view.findViewById(R.id.item_upgrade_pb);
                progressBar.setMax(100);

                view.setTag(this);
            }
        }
    }

    private void sendHandlerMsg(Handler handler, int position, int state, Object object, long delay){
        if(handler != null){
            if(delay > 0 ){
                handler.sendMessageDelayed(handler.obtainMessage(MSG_UPDATE_STEP_UI, position,
                        state, object), delay);
            }else{
                handler.sendMessage(handler.obtainMessage(MSG_UPDATE_STEP_UI,
                        position, state, object));
            }
        }
    }

    private class UpgradeSDK extends Thread{
        private SoftReference<Handler> softReference;

        UpgradeSDK(Handler handler){
            softReference = new SoftReference<>(handler);
        }

        @Override
        public void run() {
            super.run();
            Handler handler = softReference.get();
            int connectTotalTime = 0;
            /*first step: check the network environment*/
            dataList.get(0).setState(1);
            while (!AppUtils.checkNetworkIsAvailable()){
                //connect to internet
                mApplication.switchWifi();
                SystemClock.sleep(3000);
                connectTotalTime += 3000;
                if(connectTotalTime > LIMIT_TIME){
                    break;
                }
            }

            boolean isAvailNetWork = AppUtils.checkNetworkIsAvailable();
            if(isAvailNetWork){
                sendHandlerMsg(handler, 0, 2, null, 0);
                sendHandlerMsg(handler, 1, 1, null, 100);

                connectTotalTime = 0;
                /*second step: check and download*/
                do{
                    SystemClock.sleep(2000);
                    upgradePath = AppUtils.checkUpdateFilePath(getActivity().getApplicationContext(), upgradeType);
                    if(TextUtils.isEmpty(upgradePath)){
                        connectTotalTime += 2000;
                        if(connectTotalTime > LIMIT_TIME){
                            break;
                        }
                    }
                } while (TextUtils.isEmpty(upgradePath));

                if(!TextUtils.isEmpty(upgradePath)){
                    List<String> pathList;
                    connectTotalTime = 0;
                    do {
                        pathList = FTPClientUtil.getInstance().downLoadUpdateFile(upgradePath, upgradeType, FILE_TYPE_UPGRADE, handler);
                        if(pathList == null || pathList.size() < 1){
                            SystemClock.sleep(2000);
                            connectTotalTime += 2000;
                            if(connectTotalTime > LIMIT_TIME){
                                break;
                            }
                        }
                    }while (pathList == null || pathList.size() < 1);

                    if(pathList != null && pathList.size() > 1){
                        sendHandlerMsg(handler, 1, 2, null, 0);
                        sendHandlerMsg(handler, 2, 1, null, 100);
                        if(handler != null){
                            handler.sendMessageDelayed(handler.obtainMessage(MSG_UPGRADE_SDK, pathList), 200);
                        }
                    }else {
                        if(handler != null){
                            handler.sendMessage(handler.obtainMessage(MSG_UPGRADE_RESULT, 0, 0));
                        }
                    }
                }else{
                    if(handler != null){
                        handler.sendMessage(handler.obtainMessage(MSG_UPGRADE_RESULT, 0, 0));
                    }
                }
            }else{
                if(handler != null){
                    handler.sendMessage(handler.obtainMessage(MSG_UPGRADE_RESULT, 0, 0));
                }
            }
        }
    }

    private class UpgradeAPK extends Thread{
        private SoftReference<Handler> softReference;

        UpgradeAPK(Handler handler){
            softReference = new SoftReference<>(handler);
        }

        @Override
        public void run() {
            super.run();
            Handler handler = softReference.get();
            int connectTotalTime = 0;
            /*first step: check the network environment*/
            sendHandlerMsg(handler, 0, 1, null, 0);
            while (!AppUtils.checkNetworkIsAvailable()){
                /*connect to internet*/
                mApplication.switchWifi();
                SystemClock.sleep(6000);
                connectTotalTime += 6000;
                if(connectTotalTime > LIMIT_TIME){
                    break;
                }
            }

            boolean isAvailNetWork = AppUtils.checkNetworkIsAvailable();
            if(isAvailNetWork){
                sendHandlerMsg(handler, 0, 2, null, 0);
                sendHandlerMsg(handler, 1, 1, null, 100);
                /*second step: check and download*/
                if(TextUtils.isEmpty(upgradePath)){
                    upgradePath = AppUtils.checkUpdateFilePath(getActivity().getApplicationContext(), upgradeType);
                }
                List<String> pathList = FTPClientUtil.getInstance().downLoadUpdateFile(upgradePath, upgradeType, FILE_TYPE_UPGRADE, handler);
                if(pathList != null && pathList.size() > 1){
                    sendHandlerMsg(handler, 1, 2, null, 0);
                    sendHandlerMsg(handler, 2, 1, null, 100);
                    if(handler != null){
                        handler.sendMessageDelayed(handler.obtainMessage(MSG_UPGRADE_APK, pathList), 200);
                    }
                }else {
                    if(handler != null){
                        handler.sendMessage(handler.obtainMessage(MSG_UPGRADE_RESULT, 0, 0));
                    }
                }
            }else{
                if(handler != null){
                    handler.sendMessage(handler.obtainMessage(MSG_UPGRADE_RESULT, 0, 0));
                }
            }
        }
    }

    private class UploadFileThread extends Thread{
        private String remoteFile;
        private String localFilePath;
        private SoftReference<Handler> softReference;

        UploadFileThread(String remoteFile, String localFilePath, Handler handler){
            this.remoteFile = remoteFile;
            this.localFilePath = localFilePath;
            softReference = new SoftReference<>(handler);
        }

        @Override
        public void run() {
            super.run();
            Handler handler = softReference.get();
            SystemClock.sleep(3000);
            boolean ret = FTPClientUtil.getInstance().uploadFile(remoteFile, localFilePath, handler);
            Dbug.e(TAG, "-UploadFileThread- uploadFile ret = " +ret);
            if(!ret){
                SystemClock.sleep(3000);
                ret = FTPClientUtil.getInstance().uploadFile(remoteFile, localFilePath, handler);
            }
            if(ret){
                sendHandlerMsg(mHandler, 3, 2, null, 0);
                sendHandlerMsg(mHandler, 4, 1, null, 100);

                ClientManager.getClient().tryToResetDev(new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        if(code != Constants.SEND_SUCCESS){
                            Dbug.e(TAG, "-UploadFileThread- send reset cmd failed!");
                        }else{
                            mApplication.switchWifi();
                            if(mHandler != null){
                                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPGRADE_RESULT, 1, 0), 10000);
                            }
                        }
                    }
                });
            }
            uploadFileThread = null;
        }
    }
}
