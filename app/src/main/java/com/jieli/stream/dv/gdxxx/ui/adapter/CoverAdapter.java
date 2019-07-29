package com.jieli.stream.dv.gdxxx.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.bean.ThumbnailInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.Dbug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.jieli.stream.dv.gdxxx.util.IConstant.FILE_TYPE_SOS;

/**
 * Description:
 * Author:created by bob on 17-6-30.
 */
public class CoverAdapter extends RecyclerView.Adapter<CoverAdapter.ViewHolder> {
    private Context mContext;
    private List<ThumbnailInfo> mDataList = new ArrayList<>();
    private String tag = getClass().getSimpleName();
    private int contentFlag = -1;


    public void setContentThumbnailFlag(int contentFlag) {
        if (contentFlag < mDataList.size()) {
            this.contentFlag = contentFlag;
        }
    }

    public void clearContentThumbnail() {
        if (contentFlag > -1 && contentFlag < mDataList.size()) {
            mDataList.get(contentFlag).setBitmap(null);
            contentFlag = -1;
        }
    }

    public void clear() {
        this.mDataList.clear();
        notifyDataSetChanged();
    }

    public CoverAdapter(Context c) {
        mContext = c;
    }

    public void addData(ThumbnailInfo thumbnailInfo) {
        if (thumbnailInfo == null || mDataList.contains(thumbnailInfo)) {
            Dbug.i(tag, "add data failed. info : "+ thumbnailInfo);
            return;
        }
        mDataList.add(thumbnailInfo);
        //判断是否需要排序，根据文件名排序
        if (mDataList.size() > 1 && thumbnailInfo.getStartTime() != null && mDataList.get(mDataList.size() - 2).getStartTime().compareTo(thumbnailInfo.getStartTime()) < 0) {
            Collections.sort(mDataList, new Comparator<ThumbnailInfo>() {
                @Override
                public int compare(ThumbnailInfo o1, ThumbnailInfo o2) {
                    return o2.getStartTime().compareTo(o1.getStartTime());
                }
            });
        }
    }

    public void remove(int position) {
        if (mDataList != null && position >= 0 && position < mDataList.size()) {
            FileInfo fileInfo = mDataList.remove(position);
            if(fileInfo != null){
                Dbug.i(tag, "mDataList del file -> name= " + fileInfo.getName() + "   time=" + fileInfo.getCreateTime());
            }
        }
    }


    public ThumbnailInfo getItem(int position) {
        ThumbnailInfo info = null;
        if(mDataList != null && position >= 0 && position < mDataList.size()){
            info = mDataList.get(position);
        }
        return info;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cover_flow_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Bitmap bitmap = mDataList.get(position).getBitmap();
        //边框标识文件类型
        if (mDataList.get(position).getType() == FILE_TYPE_SOS) {
            holder.img.setBackgroundColor(Color.RED);
        } else {
            holder.img.setBackgroundColor(Color.WHITE);
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            holder.img.setImageBitmap(bitmap);
        } else {
            Glide.with(a.getApplication())
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.mipmap.bg_thumbnail_default))
                    .load(mDataList.get(position).getSaveUrl())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(holder.img);
        }
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(v, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(v, pos);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (mDataList == null) ? 0 : mDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.cover_image);
        }
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * AdapterView has been clicked.
     */
    public interface OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param view     The view within the AdapterView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         */
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
