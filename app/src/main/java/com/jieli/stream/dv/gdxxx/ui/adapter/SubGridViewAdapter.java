package com.jieli.stream.dv.gdxxx.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.widget.NoScrollGridView;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.ThumbLoader;
import com.jieli.stream.dv.gdxxx.util.TimeFormate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 子项加载适配器
 * @author zqjasonZhong
 *         date : 2017/6/20
 */
public class SubGridViewAdapter extends BaseAdapter implements IConstant{
    private Context mContext;
    private NoScrollGridView gridView;
    private a mApplication;
    private Map<String, LoadCover> taskCollection;
    private List<FileInfo> mDataList;
    private String mIP;
    private int parentPost = -1;
    private int viewWidth;
    private int viewHeight;
    private boolean isEditMode;
    private boolean isCancelTask;

    SubGridViewAdapter(Context context, NoScrollGridView gridView){
        this.mContext = context;
        this.gridView = gridView;

        taskCollection = new HashMap<>();
        mIP = ClientManager.getClient().getConnectedIP();
        mApplication = a.getApplication();

        int screenWidth = AppUtils.getScreenWidth(mContext);
        //设置列数
        viewWidth = (screenWidth - (6 * AppUtils.dp2px(mContext, 3))) / 5;
        viewHeight = viewWidth * 9 / 16;
        //viewWidth = (screenWidth - (4 * AppUtils.dp2px(mContext, 3))) / 3;
        //viewHeight = viewWidth * 9 / 16;
    }

    public void setCheckFirstFile(){

    }

    public void setDataList(int pos, List<FileInfo> dataList){
        this.parentPost = pos;
        mDataList = dataList;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean isEditMode){
        this.isEditMode = isEditMode;
    }
    public void setItemSel(FileInfo fileInfo,int i,View view){
        if(fileInfo!=null){
            if(fileInfo.isVideo()) {
                mApplication.lastVideoSel = fileInfo.getName();
            }else{
                mApplication.lastPictureSel = fileInfo.getName();
            }
        }
//        if(view==null){
//            return;
//        }
//        ViewHolder viewHolder = (ViewHolder) view.getTag();
//        viewHolder.ivVideoState.setBackgroundResource(R.mipmap.ic_player_sel);
    }

    public int getParentPost(){
        return this.parentPost;
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public Object getItem(int i) {
        FileInfo info = null;
        if(mDataList != null && i >= 0 && i < mDataList.size()){
            info = mDataList.get(i);
        }
        return info;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_media, viewGroup, false);
            viewHolder = new ViewHolder(view);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        if(!gridView.isMeasure()){
            FileInfo item = (FileInfo) getItem(i);
            if(null != item){
                String filename = item.getName();
                String saveFilename = AppUtils.getDownloadFilename(item);
                if(!TextUtils.isEmpty(filename)) {
                    int fileType = AppUtils.judgeFileType(filename);
                    int source = item.getSource();
                    String saveUrl;
                    if(source == COME_FORM_LOCAL){
                        saveUrl = item.getPath();
                    }else{
                        saveUrl = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(item), DIR_DOWNLOAD)
                                + File.separator + saveFilename;
                    }
                    switch (fileType){
                        case FILE_TYPE_PIC: {
                            viewHolder.layoutVideo.setVisibility(View.GONE);
                            viewHolder.ivVideoState.setVisibility(View.GONE);

//                            //记忆第一个对象
//                            if(i==0){
//                                if(mApplication.lastPictureIsGetFirst==false) {
//                                    mApplication.lastPictureIsGetFirst = true;
//                                    mApplication.lastPicture_First_rlMain =viewHolder.rlMain;
//                                }
//                            }
//
//                            //:::显示最后播放的文件
//                            if(mApplication.lastPictureSel !=null){//需要记忆文件
//                                if(mApplication.lastPictureSel.equals(filename)){
//                                    viewHolder.rlMain.setBackgroundResource(R.color.bb_darkBackgroundColor);
//                                }else {
//                                    viewHolder.rlMain.setBackgroundResource(0);
//                                }
//                            }else{//需要选中第一个文件
//                                if(i==0){
//                                    if(mApplication.lastPictureIsSelOne==false){
//                                        mApplication.lastPictureIsSelOne=true;
//                                        viewHolder.rlMain.setBackgroundResource(R.color.bb_darkBackgroundColor);
//
//                                        mApplication.lastPicture_Check_rlMain=viewHolder.rlMain;
//                                    }else{
//                                        viewHolder.rlMain.setBackgroundResource(0);
//                                    }
//                                }else{
//                                    viewHolder.rlMain.setBackgroundResource(0);
//                                }
//                            }

                            if(AppUtils.checkFileExist(saveUrl)){
                                viewHolder.ivPicState.setVisibility(View.GONE);
                                Bitmap bitmap = ThumbLoader.getInstance().loadLocalThumbnail(mContext, saveUrl, viewWidth, viewHeight);
                                viewHolder.ivThumb.setImageBitmap(bitmap);
                            }else{
                                viewHolder.ivPicState.setVisibility(View.VISIBLE);
                                viewHolder.ivThumb.setTag(saveUrl);
                                getPictureThumb(item, i);
                            }
                            break;
                        }
                        case FILE_TYPE_VIDEO: {
                            viewHolder.ivPicState.setVisibility(View.GONE);
                            viewHolder.layoutVideo.setVisibility(View.VISIBLE);
                            viewHolder.tvDuration.setTag(i);

//                            //记忆第一个对象
//                            if(i==0){
//                                if(mApplication.lastVideoIsGetFirst==false) {
//                                    mApplication.lastVideoIsGetFirst = true;
//                                    mApplication.lastVideo_First_rlMain =viewHolder.rlMain;
//                                }
//                            }
//
//                            //:::显示最后播放的文件
//                            if(mApplication.lastVideoSel !=null){//需要记忆文件
//                                if(mApplication.lastVideoSel.equals(filename)) {
//                                    viewHolder.rlMain.setBackgroundResource(R.color.bb_darkBackgroundColor);
//                                }else{
//                                    viewHolder.rlMain.setBackgroundResource(0);
//                                }
//                            }else{//需要选中第一个文件
//                                if(i==0){
//                                    if(mApplication.lastVideoIsSelOne==false){
//                                        mApplication.lastVideoIsSelOne=true;
//                                        viewHolder.rlMain.setBackgroundResource(R.color.bb_darkBackgroundColor);
//
//                                        mApplication.lastVideo_Check_rlMain=viewHolder.rlMain;
//                                    }else{
//                                        viewHolder.rlMain.setBackgroundResource(0);
//                                    }
//                                }else{
//                                    viewHolder.rlMain.setBackgroundResource(0);
//                                }
//                            }

                            if(AppUtils.checkFileExist(saveUrl)){
                                viewHolder.ivVideoState.setVisibility(View.VISIBLE);
                                viewHolder.ivThumb.setTag(saveUrl);
                                getLoadVideoThumb(saveUrl, i);
                            }else{
                                viewHolder.ivVideoState.setVisibility(View.GONE);
                                getVideoThumb(viewHolder.ivThumb, item);
                            }
                            viewHolder.tvDuration.setText(TimeFormate.getTimeFormatValue(item.getDuration()));
                            break;
                        }
                        default:
                            viewHolder.ivThumb.setImageResource(R.mipmap.ic_default_picture);
                            break;
                    }
                    if(isEditMode){
                        viewHolder.ivSelectState.setVisibility(View.VISIBLE);
                        if(item.isSelected()){
                            viewHolder.ivSelectState.setImageResource(R.mipmap.ic_check_round_blue);
                        }else{
                            viewHolder.ivSelectState.setImageResource(R.mipmap.ic_uncheck_round);
                        }
                    }else{
                        item.setSelected(false);
                        viewHolder.ivSelectState.setVisibility(View.GONE);
                    }
                }
            }
        }
        return view;
    }

    private class ViewHolder{
        private RelativeLayout rlMain;
        private ImageView ivThumb;
        private ImageView ivSelectState;
        private ImageView ivPicState;
        private ImageView ivVideoState;
        private RelativeLayout layoutVideo;
        private TextView tvDuration;

        ViewHolder(View view){
            rlMain = (RelativeLayout) view.findViewById(R.id.rlMain);
            ivThumb = (ImageView) view.findViewById(R.id.item_media_thumb);
            layoutVideo = (RelativeLayout) view.findViewById(R.id.item_media_video_layout);
            tvDuration = (TextView) view.findViewById(R.id.item_media_duration);

            //添加修改实现5个显示同页
            RelativeLayout.LayoutParams linearParams =  (RelativeLayout.LayoutParams)layoutVideo.getLayoutParams();
            linearParams.width = viewWidth;
            layoutVideo.setLayoutParams(linearParams);


            ivThumb.setLayoutParams(new RelativeLayout.LayoutParams(viewWidth, viewHeight));
            ivSelectState = (ImageView) view.findViewById(R.id.item_media_select_state);
            ivPicState = (ImageView) view.findViewById(R.id.item_media_picture_state);
            ivVideoState = (ImageView) view.findViewById(R.id.item_media_video_state);

            view.setTag(this);
        }
    }

    private void getPictureThumb(FileInfo info, int position){
        String savePath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(info), DIR_DOWNLOAD)
                + File.separator + AppUtils.getDownloadFilename(info);
        if(!taskCollection.containsKey(savePath)) {
            LoadCover loadPhotoCover = new LoadCover();
            taskCollection.put(savePath, loadPhotoCover);
            loadPhotoCover.execute(position);
        }
    }

    private void getVideoThumb(final ImageView ivCover, final FileInfo fileInfo){
        String saveUrl = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(fileInfo), DIR_THUMB)
                + File.separator + AppUtils.getVideoThumbName(fileInfo);
        if(AppUtils.checkFileExist(saveUrl)){
            ThumbLoader.getInstance().loadLocalThumbnail(mContext, saveUrl, viewWidth, viewHeight);
            Bitmap bitmap = ThumbLoader.getInstance().getBitmap(saveUrl);
            if(bitmap != null){
                ivCover.setImageBitmap(bitmap);
            }else{
                ivCover.setImageResource(R.mipmap.ic_default_picture);
            }
        }else{
            ivCover.setImageResource(R.mipmap.ic_default_picture);
        }
    }

    private void getLoadVideoThumb(String savePath, int position){
        if(!taskCollection.containsKey(savePath)) {
            LoadCover loadVideoCover = new LoadCover();
            taskCollection.put(savePath, loadVideoCover);
            loadVideoCover.execute(position);
        }
    }

    /**
     * 取消所有正在下载或等待下载的任务。
     */
    public void cancelAllTasks() {
        isCancelTask = true;
        if (taskCollection != null) {
            Set<String> keySet = taskCollection.keySet();
            if(keySet.size() > 0) {
                for (String url : keySet) {
                    LoadCover task = taskCollection.get(url);
                    if(task != null){
                        task.cancel(true);
                    }
                }
            }
            taskCollection.clear();
        }
        isCancelTask = false;
    }

    private class LoadCover extends AsyncTask<Integer, Void,  Bitmap>{
        private int position;
        private String imageUrl;
        private Bitmap bmp;
        private FileInfo info;

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            position = integers[0];
            info = (FileInfo) getItem(position);
            if(info != null){
                int source = info.getSource();
                if(source == COME_FORM_LOCAL){
                    imageUrl = info.getPath();
                }else{
                    imageUrl = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(info), DIR_DOWNLOAD)
                            + File.separator + AppUtils.getDownloadFilename(info);
                }
                if(info.isVideo()) {
                    ThumbLoader.getInstance().loadLocalVideoThumb(mContext, imageUrl, viewWidth, viewHeight, new ThumbLoader.OnLoadVideoThumbListener() {
                        @Override
                        public void onComplete(Bitmap bitmap, int duration) {
                            bmp = bitmap;
                            info.setDuration(duration);
                        }
                    });
                }else{
                    String saveThumbPath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), AppUtils.checkCameraDir(info),
                            DIR_THUMB) + File.separator + AppUtils.getVideoThumbName(info);
                    File file = new File(saveThumbPath);
                    Bitmap bitmap = null;
                    if (file.exists()) {
                        ThumbLoader.getInstance().loadLocalThumbnail(mContext, saveThumbPath, viewWidth, viewHeight);
                        bitmap = ThumbLoader.getInstance().getBitmap(saveThumbPath);
                    }
                    if(bitmap == null){
                        String url = AppUtils.formatUrl(mIP, DEFAULT_HTTP_PORT, info.getPath());
                        ThumbLoader.getInstance().loadWebThumbnail(mContext, url, saveThumbPath, viewWidth, viewHeight, new ThumbLoader.OnLoadThumbListener() {
                            @Override
                            public void onComplete(Bitmap bitmap) {
                                if(bitmap != null){
                                    bmp = bitmap;
                                }
                            }
                        });
                    }else{
                        bmp = bitmap;
                    }

                }
                int totalTime = 0;
                while (bmp == null && !isCancelTask){
                    try {
                        Thread.sleep(5);
                        totalTime += 5;
                        if(totalTime > 2000){
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                bmp = null;
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView ivCover = (ImageView) gridView.findViewWithTag(imageUrl);
            if(ivCover != null){
                if(bitmap != null){
                    ivCover.setImageBitmap(bitmap);
                }else {
                    ivCover.setImageResource(R.mipmap.ic_default_picture);
                }
            }
            if(info != null && info.isVideo()){
                TextView tvDuration = (TextView) gridView.findViewWithTag(position);
                if(tvDuration != null){
                    tvDuration.setText(TimeFormate.getTimeFormatValue(info.getDuration()));
                }
            }
            taskCollection.remove(imageUrl);
        }
    }
}
