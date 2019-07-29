package com.jieli.stream.dv.gdxxx.ui.fragment.browse;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.bean.ItemBean;
import com.jieli.stream.dv.gdxxx.bean.MediaTaskInfo;
import com.jieli.stream.dv.gdxxx.task.MediaTask;
import com.jieli.stream.dv.gdxxx.ui.activity.GenericActivity;
import com.jieli.stream.dv.gdxxx.ui.adapter.TimeLineAdapter;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.layout.BaseFooterView;
import com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.view.ExpandFooterView;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.ThumbLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 紧急视频界面
 * @author zqjasonzhong
 * date : 2017/07/31
 */
public class EmergencyVideoFragment extends BaseFragment implements BaseFooterView.OnLoadListener,
        TimeLineAdapter.OnSubViewItemClickListener{
    private ListView mListView;
    private LinearLayout emptyView;
    private ExpandFooterView footerView;
    private TimeLineAdapter mAdapter;

    private BrowseFileFragment browseFileFragment;
    private MediaTask emergencyVideoTask;
    private EmergencyVideoBroadCastReceiver mReceiver;

    private NotifyDialog notifyDialog;

    private List<FileInfo> allDataList;
    private List<FileInfo> dataList;
    private List<FileInfo> selectedList;
    private boolean isSelectAll;
    private boolean isLoading;
    private boolean isOpenTask;
    private int retryNum;
    private int mOp;

    private static final int MSG_LOAD_DATA = 0x0101;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(getActivity() != null && message != null){
                int result;
                switch (message.what){
                    case RESULT_DELETE_FILE:
                        result = message.arg1;
                        switch (result){
                            case RESULT_SUCCESS:
                                handlerTaskList(OP_DELETE_FILES, true);
                                break;
                            case RESULT_FALSE:
                                handlerTaskList(OP_DELETE_FILES, false);
                                break;
                        }
                        break;
                    case MSG_LOAD_DATA:
                        if(dataList != null){
                            loadMoreData(dataList.size());
                        }
                        onStopLoad();
                        break;
                }
            }
            return false;
        }
    });

    private class EmergencyVideoBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(getActivity() != null && context != null && intent != null && isVisible() &&
                    (browseFileFragment != null && browseFileFragment.currentFragment() instanceof EmergencyVideoFragment)){
                String action = intent.getAction();
                if(!TextUtils.isEmpty(action)){
                    if(emergencyVideoTask != null){
                        emergencyVideoTask.setUIHandler(mHandler);
                    }
                    switch (action){
                        case ACTION_BROWSE_FILE_OPERATION:
                            int op = intent.getIntExtra(KEY_BROWSE_OPERATION, -1);
                            Dbug.w(TAG, "receive op : " + op);
                            switch (op){
                                case OP_ENTER_EDIT_MODE:
                                    if(allDataList != null){
                                        if(allDataList.size() > 0){
                                            sendStateChange(TYPE_EDIT, true);
                                            if(mAdapter != null){
                                                mAdapter.setEditMode(true);
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }else{
                                            mApplication.showToastShort(R.string.no_data_tip);
                                            sendStateChange(TYPE_EDIT, false);
                                        }
                                    }
                                    break;
                                case OP_EXIT_EDIT_MODE:
                                    isSelectAll = false;
                                    isOpenTask = false;
                                    selectedList.clear();
                                    sendStateChange(TYPE_SELECT_ALL, false);
                                    if(mAdapter != null){
                                        mAdapter.setEditMode(false);
                                        if(mOp == OP_DELETE_FILES){
                                            mAdapter.clear();
                                            loadMoreData(0);
                                        }else{
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                    if(browseFileFragment != null){
                                        browseFileFragment.dismissWaitingDialog();
                                    }
                                    mOp = 0;
                                    break;
                                case OP_DELETE_FILES:
                                    if(selectedList != null && selectedList.size() > 0){
                                        showNotifyDialog();
                                    }else{
                                        mApplication.showToastShort(R.string.selected_file_empty_tip);
                                    }
                                    break;
                                case OP_SHARE_FILES:
                                    if(selectedList != null){
                                        int size = selectedList.size();
                                        if(size > 0){
                                            if(size == 1){
                                                FileInfo fileInfo = selectedList.get(0);
                                                Intent share = new Intent(Intent.ACTION_SEND);
                                                share.setType("video/*");
                                                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(fileInfo.getPath()));
                                                startActivityForResult(Intent.createChooser(share, getString(R.string.tab_share)), CODE_SHARE_FILES);
                                            }else{
                                                ArrayList<Uri> imageUris = new ArrayList<>();
                                                for (FileInfo info : selectedList){
                                                    imageUris.add(Uri.parse(info.getPath()));
                                                }
                                                Intent multShare = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                                multShare.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                                                multShare.setType("video/*");
                                                startActivityForResult(Intent.createChooser(multShare, getString(R.string.tab_share)), CODE_SHARE_FILES);
                                            }
                                        }
                                    }
                                    break;
                                case OP_SELECT_ALL:
                                    isSelectAll = true;
                                    selectedList.clear();
                                    if(allDataList != null) {
                                        for (FileInfo fileInfo : allDataList) {
                                            if (fileInfo != null) {
                                                fileInfo.setSelected(true);
                                                selectedList.add(fileInfo);
                                            }
                                        }
                                    }
                                    if(dataList != null) {
                                        for (FileInfo info : dataList) {
                                            if (info != null) {
                                                info.setSelected(true);
                                            }
                                        }
                                    }
                                    sendMsg(selectedList.size());
                                    if(mAdapter != null){
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    break;
                                case OP_CANCEL_SELECT_ALL:
                                    isSelectAll = false;
                                    selectedList.clear();
                                    if(allDataList != null) {
                                        for (FileInfo fileInfo : allDataList) {
                                            if (fileInfo != null) {
                                                fileInfo.setSelected(false);
                                            }
                                        }
                                    }
                                    if(dataList != null) {
                                        for (FileInfo info : dataList) {
                                            if (info != null) {
                                                info.setSelected(false);
                                            }
                                        }
                                    }
                                    sendMsg(selectedList.size());
                                    if(mAdapter != null){
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    break;
                                case OP_CANCEL_TASK:
                                    if(selectedList != null){
                                        selectedList.clear();
                                    }
                                    if(emergencyVideoTask != null){
                                        emergencyVideoTask.tryToStopTask();
                                    }
                                    handlerTaskList(mOp, true);
                                    break;
                            }
                            break;
                        case ACTION_LANGUAAGE_CHANGE:
                            updateTextUI();
                            break;
                    }
                }
            }
        }
    }

    public EmergencyVideoFragment() {
        // Required empty public constructor
    }

    private void updateTextUI() {
        if (emptyView != null) {
            TextView textView = (TextView) emptyView.findViewById(R.id.text_empty_tips);
            textView.setText(R.string.no_data_tip);
        }
    }
    public void setParentFragment(BrowseFileFragment fragment){
        browseFileFragment = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_video, container, false);
        mListView = (ListView) view.findViewById(R.id.emergency_video_view);
        emptyView = (LinearLayout) view.findViewById(R.id.view_empty);
        footerView = (ExpandFooterView) view.findViewById(R.id.emergency_video_footer);

        footerView.setOnLoadListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null){
            selectedList = new ArrayList<>();
            initListView();
            registerBroadCast();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(browseFileFragment != null && emergencyVideoTask == null){
            emergencyVideoTask = browseFileFragment.getMediaTask();
            if(emergencyVideoTask != null && browseFileFragment.currentFragment() instanceof EmergencyVideoFragment){
                emergencyVideoTask.setUIHandler(mHandler);
            }
        }
        updateTextUI();
    }

    @Override
    public void onStop() {
        super.onStop();
        isLoading = false;
        isOpenTask = false;
        if(mAdapter != null){
            mAdapter.cancelTasks();
        }
        if(emergencyVideoTask != null){
            emergencyVideoTask.setUIHandler(null);
            emergencyVideoTask = null;
        }
    }

    @Override
    public void onDetach() {
        browseFileFragment = null;
        super.onDetach();
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if(notifyDialog != null){
            if(notifyDialog.isShowing()){
                notifyDialog.dismiss();
            }
            notifyDialog = null;
        }
        unRegisterBroadCast();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE_SHARE_FILES){
            sendStateChange(TYPE_EDIT, false);
        }
    }

    private void onStopLoad(){
        if(footerView != null){
            footerView.stopLoad();
        }
        isLoading = false;
    }

    @Override
    public void onLoad(BaseFooterView baseFooterView) {
        if(mHandler != null && !isLoading){
            isLoading = true;
            mHandler.sendEmptyMessageDelayed(MSG_LOAD_DATA, 1500);
        }
    }

    private void initListView(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String srcPath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), null, null, null);
                allDataList = AppUtils.queryAllLocalFileList(srcPath, DIR_RECORD);
                if(allDataList != null){
                    allDataList = AppUtils.selectTypeList(allDataList, FILE_TYPE_VIDEO);
                    Dbug.i(TAG, "allDataList size = " +allDataList.size());
                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(mAdapter != null){
                                    mAdapter.clear();
                                }
                                loadMoreData(0);
                            }
                        });
                    }
                }else{
                    Dbug.e(TAG, "allDataList is null");
                }
            }
        }).start();
    }

    private void registerBroadCast(){
        if(getActivity() != null){
            if(mReceiver == null){
                mReceiver = new EmergencyVideoBroadCastReceiver();
            }
            IntentFilter filter = new IntentFilter(ACTION_BROWSE_FILE_OPERATION);
            filter.addAction(ACTION_LANGUAAGE_CHANGE);
            getActivity().getApplicationContext().registerReceiver(mReceiver, filter);
        }
    }

    private void unRegisterBroadCast(){
        if(getActivity() != null && mReceiver != null){
            getActivity().getApplicationContext().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void loadMoreData(int offset){
        if(allDataList != null){
            int totalSize = allDataList.size();
            int difference = totalSize - offset;
            emptyView.setVisibility(View.GONE);
            if(difference > 0){
                if(difference > PAGE_LIMIT_COUNT){
                    dataList = allDataList.subList(0, offset + PAGE_LIMIT_COUNT);
                }else{
                    dataList = allDataList;
                }
                List<ItemBean> tmp = AppUtils.convertDataList(dataList);
                if(tmp != null) {
                    if (mAdapter == null) {
                        mAdapter = new TimeLineAdapter(getActivity().getApplicationContext());
                        mAdapter.setOnSubViewItemClickListener(this);
                    }
                    mAdapter.clear();
                    mListView.setAdapter(mAdapter);
                    mAdapter.setDataList(tmp);
                }
            }else if(difference == 0){
                if(totalSize > 0){
                    mApplication.showToastShort(R.string.no_more_data);
                }else{
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void sendMsg(int num){
        if(getActivity() != null){
            Intent intent = new Intent(ACTION_SELECT_FILES);
            intent.putExtra(KEY_SELECT_FILES_NUM, num);
            getActivity().sendBroadcast(intent);
        }
    }

    private void sendStateChange(int type, boolean state){
        if(getActivity() != null){
            Intent intent = new Intent(ACTION_SELECT_STATE_CHANGE);
            intent.putExtra(KEY_STATE_TYPE, type);
            intent.putExtra(KEY_SELECT_STATE, state);
            getActivity().sendBroadcast(intent);
        }
    }

    @Override
    public void onSubItemClick(int parentPos, int childPos, FileInfo info) {
        if(info != null && mAdapter != null && allDataList != null){
            if(mAdapter.isEditMode()){
                info.setSelected(!info.isSelected());
                if(info.isSelected()){
                    if(!selectedList.contains(info)){
                        selectedList.add(info);
                    }
                    if(!isSelectAll){
                        if(selectedList.size() == allDataList.size()){
                            isSelectAll = true;
                            sendStateChange(TYPE_SELECT_ALL, true);
                        }
                    }
                }else{
                    selectedList.remove(info);
                    if(isSelectAll){
                        isSelectAll = false;
                        sendStateChange(TYPE_SELECT_ALL, false);
                    }
                }
                sendMsg(selectedList.size());
                mAdapter.notifyDataSetChanged();
            }else{
                String saveUrl = info.getPath();
                if(AppUtils.checkFileExist(saveUrl)){
                    Dbug.i(TAG, "play video url : " + saveUrl);
//                    AppUtils.browseFileWithOther((BaseActivity)getActivity(), info.getName(), saveUrl);
                    Intent intent = new Intent(getActivity(), GenericActivity.class);
                    intent.putExtra(KEY_FRAGMENT_TAG, VIDEO_PLAYER_FRAGMENT);
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_PATH_LIST, saveUrl);
                    intent.putExtra(KEY_DATA, bundle);
                    startActivity(intent);
                }
            }
        }
    }

    private void handlerTaskList(int fileOp, boolean result){
        Dbug.i(TAG, "- handlerTaskList - isOpenTask : " + isOpenTask);
        if(selectedList != null && isOpenTask){
            this.mOp = fileOp;
            int taskSize = selectedList.size();
            if(result){
                if(taskSize > 0){
                    FileInfo info = selectedList.remove(0);
                    if(info != null && mOp == OP_DELETE_FILES){
                        updateDeleteUI(info);
                    }
                }
                retryNum = 0;
            }else{
                retryNum++;
                if(retryNum > 2){
                    retryNum = 0;
                    if(taskSize > 0){
                        selectedList.remove(0);
                    }
                }
            }
            taskSize = selectedList.size();
            if(taskSize > 0){
                Dbug.w(TAG, "handler task size = " +taskSize);
                sendMsg(selectedList.size());
                FileInfo info = selectedList.get(0);
                if(info != null){
                    if(emergencyVideoTask != null){
                        MediaTaskInfo taskInfo = new MediaTaskInfo();
                        taskInfo.setInfo(info);
                        taskInfo.setOp(fileOp);
                        emergencyVideoTask.tryToStartTask(taskInfo);
                    }
                }
            }else{
                sendMsg(selectedList.size());
                sendStateChange(TYPE_EDIT, false);
            }
        }
    }

    private void showNotifyDialog(){
        if(notifyDialog == null){
            notifyDialog = NotifyDialog.newInstance(R.string.dialog_warning, R.string.delete_emergency_video_tip,
                    R.string.dialog_cancel, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            if(notifyDialog != null){
                                notifyDialog.dismiss();
                                notifyDialog = null;
                            }
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            isOpenTask = true;
                            handlerTaskList(OP_DELETE_FILES, false);
                            if(notifyDialog != null){
                                notifyDialog.dismiss();
                                notifyDialog = null;
                            }
                            if(browseFileFragment != null){
                                browseFileFragment.showWaitingDialog();
                            }
                        }
                    });
        }
        if(!notifyDialog.isShowing()){
            notifyDialog.show(getFragmentManager(), "notify_dialog");
        }
    }

    private void updateDeleteUI(FileInfo fileInfo){
        if(fileInfo != null){
            ThumbLoader.getInstance().removeBitmap(fileInfo.getPath());
            if(allDataList != null){
                allDataList.remove(fileInfo);
            }
        }
    }
}
