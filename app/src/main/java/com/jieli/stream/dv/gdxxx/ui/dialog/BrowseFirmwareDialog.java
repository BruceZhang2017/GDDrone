package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.jieli.stream.dv.gdxxx.interfaces.OnSelectedListener;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob
 *         date : 2018/01/26
 */
public class BrowseFirmwareDialog extends BaseDialogFragment implements IConstant, View.OnClickListener,
        AdapterView.OnItemClickListener {
    private String tag = getClass().getSimpleName();
    private TextView currentPathTv;
    private ListView mListView;
    private FileListAdapter mAdapter;

    private List<SDFileInfo> fileInfoList;
    private SDFileInfo mSelectedFile = null;
    private String currentPath;

    private NotifyDialog movingDialog, deleteDirDialog;

    private OnSelectedListener<String> onSelectResultListener;

    public void setOnSelectedListener(OnSelectedListener<String> onSelectResultListener) {
        this.onSelectResultListener = onSelectResultListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light_NoTitleBar);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_browse_firmware, container, false);
        currentPathTv = (TextView) view.findViewById(R.id.dialog_file_path);
        ImageView previousPathBtn = (ImageView) view.findViewById(R.id.dialog_return_path);
        mListView = (ListView) view.findViewById(R.id.dialog_file_list);
        Button cancelBtn = (Button) view.findViewById(R.id.dialog_file_cancel_btn);
        Button confirmBtn = (Button) view.findViewById(R.id.dialog_file_confirm_btn);

        mListView.setOnItemClickListener(this);
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
        mSelectedFile = null;
        super.onDetach();
    }

    /**
     * 获取该目录下所有文件
     **/
    private void viewFiles(String filePath) {
        ArrayList<SDFileInfo> tmp = AppUtils.getFirmwareFile(filePath);
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
            case R.id.dialog_return_path:
                onKeyBack();
                break;
            case R.id.dialog_file_cancel_btn:
                dismiss();
                break;
            case R.id.dialog_file_confirm_btn:
                if(mSelectedFile != null){ // ok
                    showToastShort(mSelectedFile.Name);
                    dismiss();
                    if (onSelectResultListener != null)
                        onSelectResultListener.onSelected(mSelectedFile.Path);
                } else{
                    showToastShort(R.string.selected_file_empty_tip);
                }
                break;
        }
    }

    private void onKeyBack() {
        if (ROOT_PATH.equals(currentPath)) {
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
            if(fileInfo != null){
                File file = new File(fileInfo.Path);
                if (file.isFile()) {
                    Dbug.i(tag, "File name=" + file.getAbsolutePath());
                    mSelectedFile = fileInfo;
                    mAdapter.notifyDataSetChanged();
                } else{
                    currentPath = fileInfo.Path;
                    viewFiles(fileInfo.Path);
                }
            } else {
                Dbug.w(tag, "file is null");
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
                viewHolder.flag = (ImageView) convertView.findViewById(R.id.file_selected);
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
                if (info.equals(mSelectedFile))
                    viewHolder.flag.setVisibility(View.VISIBLE);
                else
                    viewHolder.flag.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        private class ViewHolder {
            ImageView icon;
            TextView name;
            ImageView flag;
        }
    }
}
