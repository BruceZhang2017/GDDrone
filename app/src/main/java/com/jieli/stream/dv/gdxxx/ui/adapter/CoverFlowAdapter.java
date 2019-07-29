package com.jieli.stream.dv.gdxxx.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jieli.stream.dv.gdxxx.bean.ThumbnailInfo;
import com.jieli.stream.dv.gdxxx.ui.a;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author:created by bob on 17-6-13.
 */
public class CoverFlowAdapter extends BaseAdapter {
    private List<ThumbnailInfo> mDataList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    public void addData(ThumbnailInfo thumbnailInfo) {
        if (mDataList.contains(thumbnailInfo)) {
            return;
        }
        mDataList.add(thumbnailInfo);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public ThumbnailInfo getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageFrame imageFrame;
        if (convertView == null) {
            imageFrame = new ImageFrame(a.getApplication());
        } else {
            imageFrame = (ImageFrame) convertView;
        }
        ThumbnailInfo thumbnailInfo = mDataList.get(position);
        imageFrame.setImageBitmap(thumbnailInfo.getBitmap());
        if (mOnItemClickListener != null) {
            imageFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });
        }

        return imageFrame;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private static class ImageFrame extends FrameLayout {
        private ImageView mImageView;

        public void setImageResource(int resId){
            mImageView.setImageResource(resId);
        }

        public void setImageBitmap(Bitmap bitmap){
            mImageView.setImageBitmap(bitmap);
        }

        public ImageFrame(Context context) {
            super(context);

            mImageView = new ImageView(context);
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            addView(mImageView);

            setBackgroundColor(Color.WHITE);
            setSelected(false);
        }

        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);

            if(selected) {
                mImageView.setAlpha(1.0f);
            } else {
                mImageView.setAlpha(0.5f);
            }
        }
    }
}
