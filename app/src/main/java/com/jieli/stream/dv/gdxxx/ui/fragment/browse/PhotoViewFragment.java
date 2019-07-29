package com.jieli.stream.dv.gdxxx.ui.fragment.browse;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.activity.GenericActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.util.ImageLoader;

import java.io.File;
import java.util.List;

/**
 * 图片浏览器
 */
public class PhotoViewFragment extends BaseFragment implements ViewPager.OnPageChangeListener{
    private ViewPager mViewPager;
    private TextView tvCounter;
    private TextView tvTitle;

    private PhotoViewAdapter mAdapter;
    private Button btnBack=null;

    public PhotoViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_view, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.photo_view_pager);
        tvCounter = (TextView) view.findViewById(R.id.photo_view_counter);
        tvTitle= (TextView) view.findViewById(R.id.photo_view_title);
        btnBack = (Button) view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!=null)  getActivity().onBackPressed();
                //getFragmentManager().popBackStack();
            }
        });
        mViewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mViewPager.setOnPageChangeListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null){
            Bundle bundle = getBundle();
            if(bundle != null){
                List<String> dataList = bundle.getStringArrayList(KEY_PATH_LIST);
                int pos = bundle.getInt(KEY_POSITION);
                mAdapter = new PhotoViewAdapter(getActivity().getApplicationContext(), dataList);
                mViewPager.setAdapter(mAdapter);
                if(pos >= 0 && pos < mAdapter.getCount()){
                    mViewPager.setCurrentItem(pos);
                    setCounter(pos, mAdapter.getCount());
                    String path = mAdapter.getItem(pos);
                    if(!TextUtils.isEmpty(path)){
                        setTitle(path);
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImageLoader.getInstance().release();
    }

    private void setCounter(int position, int total){
        position++;
        String text = getString(R.string.counter_format, position, total);
        if(tvCounter != null){
            tvCounter.setText(text);
        }
    }

    private void setTitle(String title){
        if(!TextUtils.isEmpty(title) && tvTitle != null){
            String text = formatTitle(title);
            tvTitle.setText(text);
        }
    }

    private String formatTitle(String src){
        if(!TextUtils.isEmpty(src)){
            String des;
            if(src.contains(File.separator)){
                String[] strs = src.split(File.separator);
                des = strs[strs.length -1];
            }else{
                des = src;
            }
            return des;
        }
        return null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if(mAdapter != null){
            setCounter(position, mAdapter.getCount());
            String path = mAdapter.getItem(position);
            setTitle(path);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class PhotoViewAdapter extends PagerAdapter{
        private List<String> dataList;
        private Context mContext;

        PhotoViewAdapter(Context context, List<String> list){
            mContext = context;
            dataList = list;
        }

        public String getItem(int position){
            String item = null;
            if(dataList != null && position < dataList.size()){
                item = dataList.get(position);
            }
            return item;
        }

        @Override
        public int getCount() {
            return dataList == null ? 0 : dataList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(mContext);
            photoView.enable();
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            String savePath = getItem(position);
            if(!TextUtils.isEmpty(savePath)){
                loadThumbs(photoView, savePath);
            }else{
                photoView.setImageResource(R.mipmap.ic_default_picture);
            }
            container.addView(photoView);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private void loadThumbs(PhotoView view, String path){
            Bitmap bitmap = ImageLoader.getInstance().loadImage(mContext, path);
            view.setImageBitmap(bitmap);
        }
    }
}
