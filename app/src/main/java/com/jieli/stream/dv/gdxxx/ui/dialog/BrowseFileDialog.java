package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.SDFileInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.FileUtil;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zqjasonZhong
 *         date : 2017/3/1
 */
public class BrowseFileDialog extends BaseDialogFragment implements IConstant, View.OnClickListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {
    private String tag = getClass().getSimpleName();
    private TextView currentPathTv;
    private ListView mListView;
    private FileListAdapter mAdapter;

    private List<SDFileInfo> fileInfoList;
    private String currentPath;

    private NotifyDialog movingDialog, deleteDirDialog;
    private MovingDirThread movingDirThread;

    private OnSelectResultListener onSelectResultListener;


    public interface OnSelectResultListener {
        void onResult(String path);
    }

    public void setOnSelectResultListener(OnSelectResultListener onSelectResultListener) {
        this.onSelectResultListener = onSelectResultListener;
    }

    private static final int MOVE_DIR_MSG = 0xa0a0;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MOVE_DIR_MSG:
                    int op = msg.arg1;
                    controlDialog(op);
                    if (op == ARGS_DISMISS_DIALOG) {
                        int result = msg.arg2;
                        if (result == 1) {
                            if (onSelectResultListener != null) {
                                onSelectResultListener.onResult(currentPath);
                            }
                            Dbug.w("BrowseFileDialog", "select document path :" + currentPath);
                            dismiss();
                        } else {
                            showToastShort(R.string.modify_storage_url_failed);
                        }
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light_NoTitleBar);
        setCancelable(false);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_browse_file, container, false);
        currentPathTv = (TextView) view.findViewById(R.id.dialog_file_path);
        ImageView createDirBtn = (ImageView) view.findViewById(R.id.create_dir_btn);
        ImageView previousPathBtn = (ImageView) view.findViewById(R.id.dialog_return_path);
        mListView = (ListView) view.findViewById(R.id.dialog_file_list);
        Button cancelBtn = (Button) view.findViewById(R.id.dialog_file_cancel_btn);
        Button confirmBtn = (Button) view.findViewById(R.id.dialog_file_confirm_btn);

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        createDirBtn.setOnClickListener(this);
        previousPathBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null || getActivity().getWindow() == null || getDialog().getWindow() == null)
            return;
        final WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
     /*   DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            params.width = displayMetrics.heightPixels * 4 / 5;
            params.height = displayMetrics.heightPixels * 5 / 6;
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            params.width = displayMetrics.widthPixels * 4 / 5;
            params.height = displayMetrics.widthPixels * 5 / 6;
        }
        params.gravity = Gravity.CENTER;*/
        getDialog().getWindow().setAttributes(params);

        if (fileInfoList == null) {
            fileInfoList = new ArrayList<>();
        }
        currentPath = ROOT_PATH;
        if (mAdapter == null) {
            mAdapter = new FileListAdapter(getActivity(), fileInfoList);
        }
        mListView.setAdapter(mAdapter);

        viewFiles(currentPath);

    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    onKeyBack();
                }
                return false;
            }
        });
    }

    @Override
    public void onDetach() {
        if (movingDialog != null) {
            if (movingDialog.isShowing()) {
                movingDialog.dismiss();
            }
            movingDialog = null;
        }
        if (deleteDirDialog != null) {
            if (deleteDirDialog.isShowing()) {
                deleteDirDialog.dismiss();
            }
            deleteDirDialog = null;
        }
        super.onDetach();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 获取该目录下所有文件
     **/
    private void viewFiles(String filePath) {
        ArrayList<SDFileInfo> tmp = AppUtils.getFiles(filePath);
        if (tmp != null) {
            // 清空数据
            fileInfoList.clear();
            fileInfoList.addAll(tmp);
            tmp.clear();

            // 设置当前目录
            currentPath = filePath;
            currentPathTv.setText(filePath);

            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (getActivity() == null || getActivity().getWindow() == null) return;
        switch (v.getId()) {
            case R.id.create_dir_btn:
                InputContentDialog inputContentDialog = new InputContentDialog();
                inputContentDialog.setOnContentListener(new InputContentDialog.OnContentListener() {
                    @Override
                    public void onInput(String content) {
                        if (!TextUtils.isEmpty(content)) {
                            String newPath = currentPath + File.separator + content;
                            File file = new File(newPath);
                            if (file.mkdir()) {
                                showToastShort(R.string.create_dir_success);
                                currentPath = newPath;
                                viewFiles(currentPath);
                            }
                        }
                    }
                });
                inputContentDialog.show(getActivity().getSupportFragmentManager(), "input_content_dialog");
                break;
            case R.id.dialog_return_path:
                onKeyBack();
                break;
            case R.id.dialog_file_cancel_btn:
                dismiss();
                break;
            case R.id.dialog_file_confirm_btn:
                if (ROOT_PATH.equals(currentPath)) {
                    showToastShort(R.string.select_dir_error);
                } else {
                    if (AppUtils.checkIsEmptyFolder(currentPath)) { // ok
                        String oldPath = a.getApplication().getAppFilePath();
                        if (movingDirThread == null) {
                            movingDirThread = new MovingDirThread(oldPath, currentPath, mHandler);
                            movingDirThread.start();
                        }
                    } else {
                        showToastShort(R.string.select_dir_tip);
                    }
                }
                break;
        }
    }

    public void onKeyBack() {
        if (ROOT_PATH.equals(currentPath)) {
            // showToastShort(R.string.current_path_is_root);
            dismiss();
        } else {
            currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
            viewFiles(currentPath);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter != null && position < mAdapter.getCount()) {
            SDFileInfo fileInfo = (SDFileInfo) mAdapter.getItem(position);
            if (fileInfo != null) {
                currentPath = fileInfo.Path;
                viewFiles(currentPath);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter != null && position < mAdapter.getCount()) {
            SDFileInfo fileInfo = (SDFileInfo) mAdapter.getItem(position);
            if (fileInfo != null) {
                controlDeleteDirDialog(fileInfo.Path, ARGS_SHOW_DIALOG);
            }
        }
        return true;
    }

    private void controlDialog(int op) {
        if (getActivity() == null) return;
        if (movingDialog == null) {
            movingDialog = NotifyDialog.newInstance(R.string.moving_dir_tip, true);
            // new NotifyDialog(true, R.string.moving_dir_tip);
        }
        switch (op) {
            case ARGS_SHOW_DIALOG:
                if (!movingDialog.isShowing() && !getActivity().isFinishing()) {
                    movingDialog.show(getActivity().getSupportFragmentManager(), "moving_dialog");
                }
                break;
            case ARGS_DISMISS_DIALOG:
                if (movingDialog.isShowing()) {
                    movingDialog.dismiss();
                    movingDialog = null;
                }
                break;
        }
    }

    private void controlDeleteDirDialog(String path, int op) {
        if (getActivity() == null) return;
        final String deleteMsg = String.format(getString(R.string.delete_dir_tip), path);
        if (deleteDirDialog == null) {
            deleteDirDialog = NotifyDialog.newInstance(getString(R.string.dialog_tips), deleteMsg, R.string.dialog_cancel, R.string.dialog_confirm,
                    new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            deleteDirDialog.dismiss();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            Bundle bundle = deleteDirDialog.getBundle();
                            if (bundle != null) {
                                String path = bundle.getString(KEY_DIR_PATH, null);
                                if (!TextUtils.isEmpty(path)) {
                                    File deleteFile = new File(path);
                                    if (FileUtil.deleteFile(deleteFile)) {
                                        showToastShort(R.string.delete_dir_success);
                                        viewFiles(currentPath);
                                    } else {
                                        showToastShort(R.string.delete_dir_failed);
                                    }
                                }
                            }
                            deleteDirDialog.dismiss();
                        }
                    });
        }
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DIR_PATH, path);
        deleteDirDialog.setBundle(bundle);
        deleteDirDialog.setContent(deleteMsg);
        switch (op) {
            case ARGS_SHOW_DIALOG:
                if (!deleteDirDialog.isShowing() && !getActivity().isFinishing()) {
                    deleteDirDialog.show(getActivity().getSupportFragmentManager(), "delete_dir_dialog");
                }
                break;
            case ARGS_DISMISS_DIALOG:
                if (deleteDirDialog.isShowing()) {
                    deleteDirDialog.dismiss();
                    deleteDirDialog = null;
                }
                break;
        }
    }

    private class MovingDirThread extends Thread {
        private String srcPath;
        private String destPath;
        private SoftReference<Handler> softReference;

        MovingDirThread(String srcPath, String destPath, Handler handler) {
            this.srcPath = srcPath;
            this.destPath = destPath;
            softReference = new SoftReference<>(handler);
        }

        @Override
        public void run() {
            super.run();
            Handler handler = softReference.get();
            if (handler != null) {
                handler.sendEmptyMessageDelayed(MOVE_DIR_MSG, ARGS_SHOW_DIALOG);
                if (FileUtil.moveDirectory(srcPath, destPath)) {
                    handler.sendMessage(handler.obtainMessage(MOVE_DIR_MSG, ARGS_DISMISS_DIALOG, 1, destPath));
                } else {
                    handler.sendMessage(handler.obtainMessage(MOVE_DIR_MSG, ARGS_DISMISS_DIALOG, 0, srcPath));
                }
            }
        }
    }

    private class FileListAdapter extends BaseAdapter {
        private List<SDFileInfo> fileInfoList;
        private LayoutInflater inflater;

        FileListAdapter(Context context, List<SDFileInfo> fileInfos) {
            this.fileInfoList = fileInfos;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return fileInfoList == null ? 0 : fileInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            SDFileInfo info = null;
            if (fileInfoList != null && fileInfoList.size() >= 0 && position < fileInfoList.size()) {
                info = fileInfoList.get(position);
            }
            return info;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_file_path, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.icon = (ImageView) convertView.findViewById(R.id.file_icon);
                viewHolder.name = (TextView) convertView.findViewById(R.id.file_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SDFileInfo info = (SDFileInfo) getItem(position);
            if (info != null) {
                viewHolder.name.setText(info.Name);
                if (info.IsDirectory) {
                    viewHolder.icon.setImageResource(R.mipmap.ic_folder);
                } else {
                    viewHolder.icon.setImageResource(R.mipmap.ic_file_icon);
                }
            }
            return convertView;
        }

        private class ViewHolder {
            ImageView icon;
            TextView name;
        }
    }


}
