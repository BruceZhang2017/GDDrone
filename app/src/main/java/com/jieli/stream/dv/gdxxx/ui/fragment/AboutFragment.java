package com.jieli.stream.dv.gdxxx.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.SettingItem;
import com.jieli.stream.dv.gdxxx.interfaces.OnSelectedListener;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.activity.GenericActivity;
import com.jieli.stream.dv.gdxxx.ui.adapter.SettingAdapter;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.BrowseFirmwareDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.FTPClientUtil;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 关于界面
 *@author zqjasonzhong
 */
public class AboutFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();
    private TextView tvAppVersionName;
    private ListView mListView;
    private SettingAdapter mAdapter;

    private CheckAppUpgrade mCheckAppUpgrade;
    private NotifyDialog upgradeNotifyDialog;
    private WaitingDialog mWaitingDialog;
    private NotifyDialog mUploadDialog;

    private int pressCount = 0;
    private long mBackPressedTimes;
    private static final int TIME_INTERVAL = 2000;

    public static AboutFragment newInstance(){
        return new AboutFragment();
    }

    public AboutFragment() {
        // Required empty public constructor
    }

    private final static int MSG_UPGRADE_FILE = 0x1234;
    private final static int MSG_UPLOAD_FINISH = 0x100;
    private final static int MSG_UPLOAD_FAILED = 0x101;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message != null && getActivity() != null && !getActivity().isDestroyed()){
                switch (message.what){
                    case MSG_UPGRADE_FILE:{
                        Bundle bundle = message.getData();
                        if (bundle != null) {
                            ArrayList<String> pathList = bundle.getStringArrayList(UPDATE_PATH);
                            if (pathList != null && pathList.size() > 0) {
                                int updateType = bundle.getInt(UPDATE_TYPE);
                                String descTxt = null;
                                if (updateType == UPGRADE_APK_TYPE) {
                                    if (pathList.size() > 1) {
                                        descTxt = AppUtils.readTxtFile(pathList.get(1));
                                    }
                                } else {
                                    descTxt = getString(R.string.firmware_upgrade_tip);
                                }
                                if (!TextUtils.isEmpty(descTxt)) {
                                    showNotifyDialog(descTxt, bundle);
                                }
                            }
                        }
                        break;
                    }
                    case MSG_UPDATE_UPLOAD_PROGRESS:
                        int progress1 = message.arg1;
                        if (mUploadDialog != null) {
                            mUploadDialog.setProgress(progress1);
                        }
                        break;
                    case MSG_UPLOAD_FINISH:
                        if (mUploadDialog != null && mUploadDialog.isShowing())
                            mUploadDialog.dismiss();
                        break;
                    case MSG_UPLOAD_FAILED:
                        if (mUploadDialog != null && mUploadDialog.isShowing())
                            mUploadDialog.dismiss();
                        a.getApplication().showToastLong(R.string.upload_failed);
                        break;
                }
            }
            return false;
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_about, container, false);
        tvAppVersionName = (TextView) view.findViewById(R.id.about_app_version);
        mListView = (ListView) view.findViewById(R.id.about_list_view);
        ImageView ivBack = (ImageView) view.findViewById(R.id.about_return);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().onBackPressed();
                }
            }
        });
        mListView.setOnItemClickListener(mOnItemClickListener);
        tvAppVersionName.setOnClickListener(mOnClickListener);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initVersion();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissWaitingDialog();
        dismissNotifyDialog();
        if(mCheckAppUpgrade != null){
            mCheckAppUpgrade.cancel(true);
            mCheckAppUpgrade = null;
        }
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void initVersion(){
        tvAppVersionName.setText(a.getApplication().getAppVersionName());
        String[] items = getResources().getStringArray(R.array.about_list);
        List<SettingItem> itemList = new ArrayList<>();
        for (String item : items){
            if(!TextUtils.isEmpty(item)){
                SettingItem<String> settingItem = new SettingItem<>();
                settingItem.setType(0);
                settingItem.setName(item);
                itemList.add(settingItem);
            }
        }
        mAdapter = new SettingAdapter(getContext(), itemList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(mAdapter != null){
                SettingItem item = (SettingItem)mAdapter.getItem(i);
                if(item != null){
                    String name = item.getName();
                    if(getString(R.string.check_app_upgrade).equals(name)){
                        if(mCheckAppUpgrade == null){
                            mCheckAppUpgrade = new CheckAppUpgrade();
                            mCheckAppUpgrade.execute();
                        }

                    } else if (getString(R.string.upload_firmware).equals(name)) {
                        if (ClientManager.getClient().isConnected()) {
                            BrowseFirmwareDialog browseFileDialog = new BrowseFirmwareDialog();
                            browseFileDialog.setOnSelectedListener(onSelectedFileListener);
                            browseFileDialog.show(getFragmentManager(), "browse_firmware_file_dialog");
                        } else
                            a.getApplication().showToastShort(getString(R.string.please_connect_device_to_use));
                    }
                }
            }
        }
    };

    private OnSelectedListener<String> onSelectedFileListener = new OnSelectedListener<String>() {
        @Override
        public void onSelected(final String select) {
            Dbug.i(tag, "path=" + select );
            if (TextUtils.isEmpty(select))
                return;
            if (mUploadDialog == null) {
                mUploadDialog = NotifyDialog.newInstance(R.string.dialog_tips, NotifyDialog.PB_STYLE_HORIZONTAL, R.string.uploading);
            }
            if (mUploadDialog != null && !mUploadDialog.isShowing()) {
                mUploadDialog.show(getFragmentManager(), "Upload_Firmware");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean ret = FTPClientUtil.getInstance().uploadFile("JL_AC54.bfu", select, mHandler);
                    if (ret)
                        mHandler.sendEmptyMessage(MSG_UPLOAD_FINISH);
                    else
                        mHandler.sendEmptyMessage(MSG_UPLOAD_FAILED);
                }
            }).start();
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        if (mUploadDialog != null && mUploadDialog.isShowing()) {
            mUploadDialog.dismiss();
            mUploadDialog = null;
        }
        if (a.isFactoryMode) ClientManager.getClient().unregisterNotifyListener(onNotifyListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (a.isFactoryMode) ClientManager.getClient().registerNotifyListener(onNotifyListener);
    }

    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                return;
            }
            switch (data.getTopic()) {
                case Topic.DEVICE_UPGRADE_SUCCESS:
                    showUpgradeCompleteDialog();
                    break;
            }
        }
    };

    private NotifyDialog mUpgradeCompleteDialog;
    private void showUpgradeCompleteDialog() {
        if (mUpgradeCompleteDialog == null) {
            mUpgradeCompleteDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.upgrade_step_6,
                    R.string.comfirm, new NotifyDialog.OnConfirmClickListener() {
                        @Override
                        public void onClick() {
                            mUpgradeCompleteDialog.dismiss();
                        }
                    });
        }
        if (!mUpgradeCompleteDialog.isShowing()) {
            mUpgradeCompleteDialog.show(getActivity().getSupportFragmentManager(), "mUpgradeCompleteDialog");
        }
    }

    private void showNotifyDialog(String content, Bundle bundle) {
        if (upgradeNotifyDialog == null) {
            upgradeNotifyDialog = NotifyDialog.newInstance(getString(R.string.upgrade_desc), content,
                    R.string.dialog_cancel, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            dismissNotifyDialog();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            Bundle mBundle = upgradeNotifyDialog.getBundle();
                            if (mBundle != null && getActivity() != null) {
                                Intent it = new Intent(getActivity(), GenericActivity.class);
                                it.putExtra(KEY_FRAGMENT_TAG, UPGRADE_FRAGMENT);
                                it.putExtra(KEY_DATA, mBundle);
                                startActivity(it);
                            }
                            dismissNotifyDialog();
                        }
                    });
            upgradeNotifyDialog.setContentTextLeft(true);
        }
        if (!TextUtils.isEmpty(content)) {
            upgradeNotifyDialog.setContent(content);
        }
        if(bundle != null){
            upgradeNotifyDialog.setBundle(bundle);
        }
        if (!upgradeNotifyDialog.isShowing()) {
            upgradeNotifyDialog.show(getFragmentManager(), "notify_dialog");
        }
    }

    private void dismissNotifyDialog(){
        if(upgradeNotifyDialog != null){
            if(upgradeNotifyDialog.isShowing() && !isDetached()){
                upgradeNotifyDialog.dismiss();
            }
            upgradeNotifyDialog = null;
        }
    }

    private void showWaitingDialog(){
        if(mWaitingDialog == null){
            mWaitingDialog = new WaitingDialog();
            mWaitingDialog.setCancelable(false);
            mWaitingDialog.setNotifyContent(getString(R.string.check_app_upgrade));
            mWaitingDialog.setOnWaitingDialog(new WaitingDialog.OnWaitingDialog() {
                @Override
                public void onCancelDialog() {
                    dismissWaitingDialog();
                }
            });
        }
        if(!mWaitingDialog.isShowing()){
            mWaitingDialog.show(getFragmentManager(), "waiting_dialog");
        }
    }

    public void dismissWaitingDialog(){
        if(mWaitingDialog != null){
            if(mWaitingDialog.isShowing()){
                mWaitingDialog.dismiss();
            }
            mWaitingDialog = null;
        }
    }

    private static final int LIMIT_TIME = 60000;

    private class CheckAppUpgrade extends AsyncTask<Void, Void, ArrayList<String>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showWaitingDialog();
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            dismissWaitingDialog();
            mCheckAppUpgrade = null;
            if(strings != null && strings.size() > 0){
                if(strings.size() > 1){
                    if(mHandler != null) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(UPDATE_TYPE, UPGRADE_APK_TYPE);
                        bundle.putStringArrayList(UPDATE_PATH, strings);
                        Message message = mHandler.obtainMessage(MSG_UPGRADE_FILE);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }else{
                    a.getApplication().showToastShort(strings.get(0));
                }
            }else{
                a.getApplication().showToastShort(R.string.upgrade_failed_tip);
            }
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> result = null;
            if(getActivity() != null){
                int connectTotalTime = 0;
                while (!AppUtils.checkNetworkIsAvailable()){
                    //connect to internet
                    mApplication.switchWifi();
                    SystemClock.sleep(6000);
                    connectTotalTime += 6000;
                    if(connectTotalTime > LIMIT_TIME){
                        break;
                    }
                }
                if (AppUtils.checkNetworkIsAvailable()) {
                    String upgradePath = AppUtils.checkUpdateFilePath(mApplication, UPGRADE_APK_TYPE);
                    if (!TextUtils.isEmpty(upgradePath)) {
                        result = new ArrayList<>();
                        if(upgradePath.equals(getString(R.string.latest_version))){
                            result.add(upgradePath);
                        }else{
                            Dbug.w(TAG, "有APK更新,更新路径：" + upgradePath);
                            result.add(upgradePath);
                            List<String> pathList = FTPClientUtil.getInstance().downLoadUpdateFile(upgradePath,
                                    UPGRADE_APK_TYPE, IConstant.FILE_DESC_TXT, null);
                            if (pathList != null && pathList.size() > 1) {
                                result.add(pathList.get(1));
                            }
                        }
                    } else Dbug.w(tag, "upgradePath=" +upgradePath);
                } else Dbug.w(tag, "Network is unavailable");
            }
            return result;
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view == tvAppVersionName){
                if (mBackPressedTimes + TIME_INTERVAL > System.currentTimeMillis()) {
                    pressCount++;
                    if(pressCount == 3){
                        pressCount = 0;
                        boolean isDebugOpen = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(IConstant.DEBUG_SETTINGS, false);
                        PreferencesHelper.putBooleanValue(mApplication, IConstant.DEBUG_SETTINGS, !isDebugOpen);
                        mApplication.showToastShort(R.string.open_debug_ok);
                    }else{
                        String tip = String.format(Locale.getDefault(), getString(R.string.open_debug_tip), (3 - pressCount));
                        mApplication.showToastShort(tip);
                    }
                } else {
                    if(pressCount != 0){
                        pressCount = 0;
                    }
                    pressCount++;
                    String tip = String.format(Locale.getDefault(), getString(R.string.open_debug_tip), (3 - pressCount));
                    mApplication.showToastShort(tip);
                }
                mBackPressedTimes = System.currentTimeMillis();
            }
        }
    };
}
