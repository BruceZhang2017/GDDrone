package com.jieli.stream.dv.gdxxx.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.bean.ItemBean;
import com.jieli.stream.dv.gdxxx.ui.widget.NoScrollGridView;
import com.jieli.stream.dv.gdxxx.util.AppUtils;

import java.util.List;


/**
 * 时间轴适配器
 * @author zqjasonZhong
 *         date : 2017/6/20
 */
public class TimeLineAdapter extends BaseAdapter{
    private Context mContext;
    private List<ItemBean> mDataList;
    private OnSubViewItemClickListener listener;

    private boolean isEditMode;
    private boolean isCancelTask;

    ViewHolder viewHolder=null;

    public TimeLineAdapter(Context context){
        this.mContext = context;
    }

    public void setDataList(List<ItemBean> dataList){
        this.mDataList = AppUtils.mergeList(mDataList, dataList);
        notifyDataSetChanged();
    }

    public void setCheckFirstFile(){
        //if(viewHolder!=null)
        //    viewHolder.mAdapter.setCheckFirstFile();
    }

    public void setOnSubViewItemClickListener(OnSubViewItemClickListener listener){
        this.listener = listener;
    }

    public boolean isEditMode(){
        return isEditMode;
    }

    public void setEditMode(boolean bl){
        isEditMode = bl;
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public Object getItem(int i) {
        ItemBean itemBean = null;
        if(mDataList != null && i >= 0 && i < mDataList.size()){
            itemBean = mDataList.get(i);
        }
        return itemBean;
    }

    public void clear(){
        if(mDataList != null){
            mDataList.clear();
        }
        cancelTasks();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ItemBean item = (ItemBean) getItem(i);

        if(view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_time_line, viewGroup, false);
            viewHolder = new ViewHolder(view);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        if(item != null){
            String date = item.getData();
            if(!TextUtils.isEmpty(date)){
                date = AppUtils.formatDate(date);
                viewHolder.tvDate.setText(date);
            }
            List<FileInfo> temp = item.getInfoList();
            if(null != temp){
                viewHolder.mAdapter.setDataList(i, temp);
            }

            viewHolder.mAdapter.setEditMode(isEditMode);
        }
        if(isCancelTask){
            viewHolder.mAdapter.cancelAllTasks();
            isCancelTask = false;
        }
        return view;
    }

    public void cancelTasks(){
        isCancelTask = true;
        notifyDataSetChanged();
    }

    private class ViewHolder  implements AdapterView.OnItemClickListener{
        private TextView tvDate;
        private NoScrollGridView subGridView;
        private SubGridViewAdapter mAdapter;

        ViewHolder(View view){
            tvDate = (TextView) view.findViewById(R.id.item_time_line_tv);
            subGridView = (NoScrollGridView) view.findViewById(R.id.item_time_line_grid_view);
            mAdapter = new SubGridViewAdapter(mContext, subGridView);
            subGridView.setAdapter(mAdapter);
            subGridView.setOnItemClickListener(this);

            view.setTag(this);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(view != null && mAdapter != null && listener != null){
                FileInfo fileInfo = (FileInfo) mAdapter.getItem(i);
                if(null != fileInfo){
                    //设置播放状态
                    mAdapter.setItemSel(fileInfo,i,view);


                    listener.onSubItemClick(mAdapter.getParentPost(), i, fileInfo);
                }
            }
        }
    }

    public interface OnSubViewItemClickListener{
        void onSubItemClick(int parentPos, int childPos, FileInfo info);
    }
}
