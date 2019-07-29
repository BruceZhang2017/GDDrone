package com.jieli.stream.dv.gdxxx.ui.fragment.browse;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.lib.dv.control.utils.TopicParam;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.bean.ItemBean;
import com.jieli.stream.dv.gdxxx.bean.MediaTaskInfo;
import com.jieli.stream.dv.gdxxx.task.MediaTask;
import com.jieli.stream.dv.gdxxx.ui.activity.GenericActivity;
import com.jieli.stream.dv.gdxxx.ui.adapter.TimeLineAdapter;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.layout.BaseFooterView;
import com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.view.ExpandFooterView;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.ThumbLoader;
import com.jieli.stream.dv.gdxxx.util.json.JSonManager;
import com.jieli.stream.dv.gdxxx.util.json.listener.OnCompletedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 设备图库
 */
public class DevPhotoFragment extends BaseFragment implements View.OnClickListener,
        TimeLineAdapter.OnSubViewItemClickListener,BaseFooterView.OnLoadListener,
        OnNotifyListener{
    private RelativeLayout normalLayout;
    private RelativeLayout editLayout;
    private LinearLayout bottomBar;
    private TextView tvSelectAll;
    private TextView tvSelected;
    private TextView tvExitMode;
    private LinearLayout emptyView;
    private ListView mListView;
    private ExpandFooterView footerView;

    private TimeLineAdapter mAdapter;
    private MediaTask photoTask;

    private WaitingDialog waitingDialog;

    private List<FileInfo> photoInfoList;
    private List<FileInfo> dataList;
    private List<FileInfo> selectedList = new ArrayList<>();
    private String msgContent;
    private String downloadDir;
    private boolean isEditMode;
    private boolean isSelectAll;
    private boolean isLoading;
    private int retryNum;
    private int fileOp;

    private static final int MSG_LOAD_DATE = 0x0001;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(getActivity() != null && message != null){
                int result;
                switch (message.what){
                    case RESULT_DOWNLOAD_FILE:
                        result = message.arg1;
                        Dbug.i(TAG, "download file result : " + result);
                        switch (result){
                            case RESULT_SUCCESS:
                            case RESULT_CANCEL:
                            case RESULT_FILE_EXIST:
                                handlerTaskList(true);
                                break;
                            case RESULT_FALSE:
                                handlerTaskList(false);
                                break;
                        }
                        break;
                    case MSG_LOAD_DATE:
                        if(dataList != null){
                            loadMoreData(dataList.size(), PAGE_LIMIT_COUNT);
                        }
                        stopLoad();
                        break;
                }
            }
            return false;
        }
    });


    public DevPhotoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dev_photo, container, false);
        normalLayout = (RelativeLayout) view.findViewById(R.id.dev_photo_normal_layout);
        editLayout = (RelativeLayout) view.findViewById(R.id.dev_photo_edit_layout);
        bottomBar = (LinearLayout) view.findViewById(R.id.dev_photo_bottom_bar);
        tvSelectAll = (TextView) view.findViewById(R.id.dev_photo_select_all);
        tvSelected = (TextView) view.findViewById(R.id.dev_photo_select_num);
        tvExitMode = (TextView) view.findViewById(R.id.dev_photo_exit_mode);
        ImageView ivBack = (ImageView) view.findViewById(R.id.dev_photo_return);
        ImageView ivEdit = (ImageView) view.findViewById(R.id.dev_photo_edit);
        emptyView = (LinearLayout) view.findViewById(R.id.view_empty);
        mListView = (ListView) view.findViewById(R.id.dev_photo_view);
        footerView = (ExpandFooterView) view.findViewById(R.id.dev_photo_footer);
        LinearLayout shareLayout = (LinearLayout) view.findViewById(R.id.pop_bottom_bar_share);
        shareLayout.setVisibility(View.GONE);
        LinearLayout downloadLayout = (LinearLayout) view.findViewById(R.id.pop_bottom_bar_download);
        downloadLayout.setVisibility(View.VISIBLE);
        LinearLayout deleteLayout = (LinearLayout) view.findViewById(R.id.pop_bottom_bar_delete);

        ivBack.setOnClickListener(this);
        ivEdit.setOnClickListener(this);
        tvSelectAll.setOnClickListener(this);
        tvExitMode.setOnClickListener(this);
        footerView.setOnLoadListener(this);
        downloadLayout.setOnClickListener(this);
        deleteLayout.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null){
            Bundle bundle = getBundle();
            if(bundle != null){
                msgContent = bundle.getString(KEY_VIDEO_LIST);
            }

            if(mApplication.getUUID() != null && TextUtils.isEmpty(downloadDir)){
                downloadDir = AppUtils.splicingFilePath(mApplication.getAppFilePath(), mApplication.getCameraDir(), DIR_DOWNLOAD);
            }

            mAdapter = new TimeLineAdapter(getActivity().getApplicationContext());
            mListView.setAdapter(mAdapter);
            mAdapter.setOnSubViewItemClickListener(this);

            tryToParseData(msgContent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ClientManager.getClient().registerNotifyListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(photoTask == null || photoTask.isInterrupted()){
            photoTask = new MediaTask(getContext(), "photo_task");
            photoTask.setUIHandler(mHandler);
            photoTask.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isLoading = false;
        ClientManager.getClient().unregisterNotifyListener(this);
    }

    @Override
    public void onDetach() {
        dismissWaitingDialog();
        if(mAdapter != null){
            mAdapter.cancelTasks();
            mAdapter = null;
        }
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if(photoTask != null){
            photoTask.tryToStopTask();
            if(selectedList != null){
                selectedList.clear();
            }
            photoTask.interrupt();
            photoTask.quit();
        }
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        if(getActivity() != null && view != null){
            switch (view.getId()){
                case R.id.dev_photo_return:
                    getActivity().onBackPressed();
                    break;
                case R.id.dev_photo_edit:
                    if(photoInfoList != null && photoInfoList.size() > 0) {
                        isEditMode = true;
                        if (selectedList != null) {
                            selectedList.clear();
                        }
                        handlerEditUI();
                        if (mAdapter != null) {
                            mAdapter.setEditMode(true);
                            mAdapter.notifyDataSetChanged();
                        }
                    }else{
                        mApplication.showToastShort(R.string.no_data_tip);
                    }
                    break;
                case R.id.dev_photo_exit_mode:
                    isEditMode = false;
                    isSelectAll = false;
                    if(selectedList != null){
                        selectedList.clear();
                    }
                    handlerEditUI();
                    handlerSelectAllUI();
                    dismissWaitingDialog();
                    if(mAdapter != null){
                        mAdapter.setEditMode(false);
                        mAdapter.notifyDataSetChanged();
                    }
                    if(photoInfoList != null && photoInfoList.size() == 0){
                        emptyView.setVisibility(View.VISIBLE);
                    }else{
                        emptyView.setVisibility(View.GONE);
                    }
                    fileOp = 0;
                    break;
                case R.id.dev_photo_select_all:
                    isSelectAll = !isSelectAll;
                    if(isSelectAll){
                        selectedList.clear();
                        for (FileInfo fileInfo : photoInfoList){
                            if(fileInfo != null){
                                fileInfo.setSelected(true);
                                selectedList.add(fileInfo);
                            }
                        }
                        for (FileInfo info : dataList){
                            if(info != null){
                                info.setSelected(true);
                            }
                        }
                    }else{
                        selectedList.clear();
                        for (FileInfo fileInfo : photoInfoList){
                            if(fileInfo != null){
                                fileInfo.setSelected(false);
                            }
                        }
                        for (FileInfo info : dataList){
                            if(info != null){
                                info.setSelected(false);
                            }
                        }
                    }
                    handlerSelectAllUI();
                    if(mAdapter != null){
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case R.id.pop_bottom_bar_download:
                    if(selectedList != null && selectedList.size() > 0){
                        fileOp = OP_DOWNLOAD_FILES;
                        showWaitingDialog(getString(R.string.downloading_tip));
                        handlerTaskList(false);
                    }else{
                        mApplication.showToastShort(R.string.selected_file_empty_tip);
                    }
                    break;
                case R.id.pop_bottom_bar_delete:
                    if(selectedList != null && selectedList.size() > 0) {
                        showWaitingDialog(getString(R.string.deleting_tip));
                        handlerDeleteFiles();
                    }else{
                        mApplication.showToastShort(R.string.selected_file_empty_tip);
                    }
                    break;
            }
        }
    }

    @Override
    public void onSubItemClick(int parentPos, int childPos, FileInfo info) {
        if(info != null && mAdapter != null && photoInfoList != null){
            if(mAdapter.isEditMode()){
                info.setSelected(!info.isSelected());
                if(info.isSelected()){
                    if(!selectedList.contains(info)){
                        selectedList.add(info);
                    }
                    if(!isSelectAll){
                        if(selectedList.size() == photoInfoList.size()){
                            isSelectAll = true;
                            handlerSelectAllUI();
                        }
                    }
                }else{
                    selectedList.remove(info);
                    if(isSelectAll){
                        isSelectAll = false;
                        handlerSelectAllUI();
                    }
                }
                setSelectNum(selectedList.size());
                mAdapter.notifyDataSetChanged();
            }else{
                if(mApplication.getUUID() != null && TextUtils.isEmpty(downloadDir)){
                    downloadDir = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(info), DIR_DOWNLOAD);
                }
                String savePath = downloadDir + File.separator + AppUtils.getDownloadFilename(info);
                if(!AppUtils.checkFileExist(savePath)){
                    showWaitingDialog(getString(R.string.downloading_tip));
                    selectedList.add(info);
                    fileOp = OP_DOWNLOAD_FILES;
                    handlerTaskList(false);
                }else{
//                    AppUtils.browseFileWithOther((BaseActivity)getActivity(), info.getName(), savePath);
                    ArrayList<String> pathList = new ArrayList<>();
                    for (FileInfo fileInfo : photoInfoList){
                        if(fileInfo != null){
                            String path = downloadDir + File.separator + AppUtils.getDownloadFilename(fileInfo);
                            if(AppUtils.checkFileExist(path)){
                                pathList.add(path);
                            }
                        }
                    }
                    int currentPos = pathList.indexOf(savePath);
                    if(pathList.size() > 0){
                        Intent intent = new Intent(getActivity(), GenericActivity.class);
                        intent.putExtra(KEY_FRAGMENT_TAG, PHOTO_VIEW_FRAGMENT);
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList(KEY_PATH_LIST, pathList);
                        bundle.putInt(KEY_POSITION, currentPos);
                        intent.putExtra(KEY_DATA, bundle);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    private void setSelectNum(int num){
        if(tvSelected != null){
            tvSelected.setText(getString(R.string.selected_num, num));
        }
    }

    private void tryToParseData(String content) {
        if (TextUtils.isEmpty(content)) return;
        JSonManager.getInstance().parseJSonData(content, new OnCompletedListener<Boolean>() {
            @Override
            public void onCompleted(Boolean state) {
                if (state) {
                    photoInfoList = JSonManager.getInstance().getPictureInfoList();
                    if (photoInfoList != null && photoInfoList.size() > 0) {
                        //Collections.reverse(photoInfoList);
                        Collections.sort(photoInfoList, new FileComparator());
                        if(dataList != null){
                            dataList.clear();
                        }
                        if(mAdapter != null){
                            mAdapter.clear();
                        }
                        loadMoreData(0, PAGE_LIMIT_COUNT);
                    }else{
                        Dbug.e(TAG, "-tryToParseData- parseJSonData photoInfoList is null!!!");
                    }
                } else {
                    Dbug.e(TAG, "-tryToParseData- parseJSonData failed!!!");
                }
            }
        });
    }

    class FileComparator implements Comparator<FileInfo>{

        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            if (o1.getStartTime().getTimeInMillis() > o2.getStartTime().getTimeInMillis()) {
                return -1;
            } else if (o1.getStartTime().getTimeInMillis() == o2.getStartTime().getTimeInMillis()) {
                return 0;
            }
            return 1;
        }
    }

    private void loadMoreData(int startOffset, int limit){
        if (photoInfoList != null && photoInfoList.size() > 0){
            int totalSize = photoInfoList.size();
            Dbug.w(TAG, "-loadMoreData- total size : " + totalSize +", startOffset = " + startOffset);
            int difference = totalSize - startOffset;
            emptyView.setVisibility(View.GONE);
            if(difference > 0){
                if(difference > limit){
                    dataList = photoInfoList.subList(0, startOffset + limit);
                }else{
                    dataList = photoInfoList;
                }
                Dbug.w(TAG, "-loadMoreData- dataList size : " + dataList.size());
                List<ItemBean> tmp = AppUtils.convertDataList(dataList);
                if(tmp != null) {
                    if (mAdapter == null) {
                        mAdapter = new TimeLineAdapter(getActivity().getApplicationContext());
                    }
                    mListView.setAdapter(mAdapter);
                    mAdapter.clear();
                    mAdapter.setDataList(tmp);
                }
            }else if(difference == 0){
                if(totalSize == 0){
                    if(mAdapter != null){
                        mAdapter.clear();
                    }
                    emptyView.setVisibility(View.VISIBLE);
                }else{
                    mApplication.showToastShort(R.string.no_more_data);
                }
            }
        }
    }

    private void handlerEditUI(){
        if(getActivity() == null) return;
        if(isEditMode){
            if(normalLayout != null && normalLayout.getVisibility() != View.GONE){
                normalLayout.setVisibility(View.GONE);
            }
            if(editLayout != null && editLayout.getVisibility() != View.VISIBLE){
                editLayout.setVisibility(View.VISIBLE);
            }
            setSelectNum(0);
            if(bottomBar != null && bottomBar.getVisibility() != View.VISIBLE){
                bottomBar.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_in_bottom));
                bottomBar.setVisibility(View.VISIBLE);
            }
        }else{
            if(editLayout.getVisibility() != View.GONE){
                editLayout.setVisibility(View.GONE);
            }
            if(bottomBar != null && bottomBar.getVisibility() != View.GONE){
                bottomBar.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_out_bottom));
                bottomBar.setVisibility(View.GONE);
            }
            if(normalLayout.getVisibility() != View.VISIBLE){
                normalLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void handlerSelectAllUI(){
        if(isSelectAll){
            tvSelectAll.setText(getString(R.string.cancel_all_select));
            if(selectedList != null){
                setSelectNum(selectedList.size());
            }
        }else{
            tvSelectAll.setText(getString(R.string.all_select));
        }
    }

    private void handlerTaskList(boolean result){
        if(selectedList != null){
            int taskSize = selectedList.size();
            if(result){
                if(taskSize > 0){
                    FileInfo fileInfo = selectedList.remove(0);
                    if(fileOp == OP_DELETE_FILES){
                        updateDeleteUI(fileInfo);
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
                setSelectNum(selectedList.size());
                FileInfo info = selectedList.get(0);
                if(info != null){
                    if(photoTask != null){
                        MediaTaskInfo taskInfo = new MediaTaskInfo();
                        taskInfo.setInfo(info);
                        taskInfo.setOp(fileOp);
                        photoTask.tryToStartTask(taskInfo);
                    }
                }
            }else{
                setSelectNum(selectedList.size());
                tvExitMode.performClick();
            }
        }
    }

    private void handlerDeleteFiles(){
        if(selectedList != null && selectedList.size() > 0){
            List<String> params = new ArrayList<>();
            for (FileInfo fileInfo : selectedList){
                params.add(fileInfo.getPath());
            }
            ClientManager.getClient().tryToDeleteFile(params, new SendResponse() {
                @Override
                public void onResponse(Integer code) {
                    if(code != Constants.SEND_SUCCESS){
                        dismissWaitingDialog();
                    }
                }
            });
        }else{
            dismissWaitingDialog();
        }
    }

    private void showWaitingDialog(String text){
        if(waitingDialog == null){
            waitingDialog = new WaitingDialog();
            waitingDialog.setCancelable(false);
            waitingDialog.setNotifyContent(text);
            waitingDialog.setOnWaitingDialog(new WaitingDialog.OnWaitingDialog() {
                @Override
                public void onCancelDialog() {
                    if(selectedList != null){
                        cancelLoading();
                        selectedList.clear();
                    }
                    if(photoTask != null){
                        photoTask.tryToStopTask();
                    }
                    handlerTaskList(true);
                }
            });
        }
        if(!TextUtils.isEmpty(text)){
            waitingDialog.updateNotifyContent(text);
        }
        if(!waitingDialog.isShowing()){
            waitingDialog.show(getFragmentManager(), "wait_dialog");
        }
    }

    public void dismissWaitingDialog(){
        if(waitingDialog != null){
            if(getActivity() != null && waitingDialog.isShowing()){
                waitingDialog.dismiss();
            }
            waitingDialog.setOnWaitingDialog(null);
            waitingDialog = null;
        }
    }

    private void stopLoad(){
        if(footerView != null){
            footerView.stopLoad();
        }
        isLoading = false;
    }

    @Override
    public void onLoad(BaseFooterView baseFooterView) {
        if(mHandler != null && !isLoading){
            isLoading = true;
            mHandler.sendEmptyMessageDelayed(MSG_LOAD_DATE, 1500);
        }
    }

    @Override
    public void onNotify(NotifyInfo data) {
        if(null != data){
            int errCode = data.getErrorType();
            String topic = data.getTopic();
            if(errCode == Code.ERROR_NONE){
                if(!TextUtils.isEmpty(topic)){
                    switch (topic){
                        case Topic.FORMAT_TF_CARD:
                            if(getActivity() != null){
                                getActivity().onBackPressed();
                            }
                            break;
                        case Topic.TF_STATUS:
                            if(null == data.getParams()){
                                return;
                            }
                            if (TopicParam.TF_OFFLINE.equals(data.getParams().get(TopicKey.ONLINE))) {
                               if(getActivity() != null){
                                   dismissWaitingDialog();
                                   getActivity().onBackPressed();
                               }
                            }
                            break;
                        case Topic.PHOTO_CTRL:
                            if (null == data.getParams()) {
                                return;
                            }
                            String photoDesc = data.getParams().get(TopicKey.DESC);
                            if (!TextUtils.isEmpty(photoDesc)) {
                                photoDesc = photoDesc.replaceAll("\\\\", "");
                                Dbug.w(TAG, "-PHOTO_CTRL- photoDesc = " + photoDesc);
                                FileInfo fileInfo = JSonManager.parseFileInfo(photoDesc);
                                if (fileInfo != null) {
                                    int cameraType = DeviceClient.CAMERA_FRONT_VIEW;
                                    if(CAMERA_TYPE_REAR.equals(fileInfo.getCameraType())){
                                        cameraType = DeviceClient.CAMERA_REAR_VIEW;
                                    }
                                    if(cameraType == mApplication.getDeviceSettingInfo().getCameraType()) {
                                        if (photoInfoList == null) {
                                            photoInfoList = new ArrayList<>();
                                        }
                                        photoInfoList.add(0, fileInfo);
                                        msgContent = JSonManager.convertJson(photoInfoList);
                                        int size = PAGE_LIMIT_COUNT;
                                        if (mAdapter != null) {
                                            int tmp = mAdapter.getCount();
                                            if (tmp > size) {
                                                size = tmp;
                                            }
                                        }
                                        loadMoreData(0, size);
                                    }
                                }
                            }
                            break;
                        case Topic.FILES_DELETE:
                            if (null == data.getParams()) {
                                return;
                            }
                            String delPath = data.getParams().get(TopicKey.PATH);
                            if (!TextUtils.isEmpty(delPath)) {
                                Dbug.w(TAG, "-FILES_DELETE- delPath = " + delPath);
                                FileInfo fileInfo = findFileInfo(delPath);
                                if (fileInfo != null) {
                                    if(photoInfoList != null){
                                        if(photoInfoList.remove(fileInfo)){
                                            if(selectedList != null){
                                                selectedList.remove(fileInfo);
                                                setSelectNum(selectedList.size());
                                            }
                                            int size = PAGE_LIMIT_COUNT;
                                            if(mAdapter != null){
                                                int tmp = mAdapter.getCount();
                                                if(tmp > size){
                                                    size = tmp;
                                                }
                                            }
                                            loadMoreData(0, size);
                                            if(selectedList != null && selectedList.size() == 0){
                                                dismissWaitingDialog();
                                                msgContent = JSonManager.convertJson(photoInfoList);
                                                tvExitMode.performClick();
                                            }else{
                                                msgContent = JSonManager.convertJson(photoInfoList);
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                    }
                }
            }else{
                if(Topic.PHOTO_CTRL.equals(topic) || Topic.FILES_DELETE.equals(topic)){
                    dismissWaitingDialog();
                    if(selectedList != null && selectedList.size() > 0){
                        selectedList.clear();
                        tvExitMode.performClick();
                    }
                }
            }
        }
    }

    private void updateDeleteUI(FileInfo fileInfo){
        if(fileInfo != null && mAdapter != null && photoInfoList != null){
            int adapterSize = mAdapter.getCount();
            ThumbLoader.getInstance().removeBitmap(fileInfo.getPath());
            adapterSize--;
            photoInfoList.remove(fileInfo);
            if(adapterSize >= photoInfoList.size()){
                dataList = photoInfoList.subList(0, adapterSize);
            }else{
                dataList = photoInfoList;
            }
            List<ItemBean> tmp = AppUtils.convertDataList(dataList);
            mAdapter.clear();
            mListView.setAdapter(mAdapter);
            mAdapter.setDataList(tmp);
        }
    }

    private FileInfo findFileInfo(String path){
        FileInfo fileInfo = null;
        if(!TextUtils.isEmpty(path) && photoInfoList != null){
            for (FileInfo info : photoInfoList){
                if(path.equals(info.getPath())){
                    fileInfo = info;
                    break;
                }
            }
        }
        return fileInfo;
    }

    private void cancelLoading() {
        if (selectedList != null && selectedList.size() > 0) {
            FileInfo info = selectedList.get(0);
            if(mApplication.getUUID() != null && TextUtils.isEmpty(downloadDir)){
                downloadDir = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(info), DIR_DOWNLOAD);
            }
            String savePath = downloadDir + File.separator + AppUtils.getDownloadFilename(info);
            if(AppUtils.checkFileExist(savePath)) {
                File file= new File(savePath);
                if (file.exists() && file.delete()) {
                    //handlerTaskList(true);
                }
            }
        }
    }
}
