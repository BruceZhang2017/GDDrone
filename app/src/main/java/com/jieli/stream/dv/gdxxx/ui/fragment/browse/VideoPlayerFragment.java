package com.jieli.stream.dv.gdxxx.ui.fragment.browse;


import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.widget.BrightnessToast;
import com.jieli.stream.dv.gdxxx.ui.widget.VideoProgressToast;
import com.jieli.stream.dv.gdxxx.ui.widget.VolumeToast;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IRenderView;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IjkVideoView;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.TimeFormate;

import java.io.File;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 视频播放器
 */
public class VideoPlayerFragment extends BaseFragment implements View.OnTouchListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener{
    private IjkVideoView videoView;
    private RelativeLayout topBar;
    private RelativeLayout bottomBar;
    private TextView tvTitle;
    private ImageView ivPlayOrPause;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private SeekBar sbProgress;
    private ImageView ivFullScreen;

    private VolumeToast volumeToast;
    private BrightnessToast brightnessToast;
    private VideoProgressToast videoProgressToast;

    private boolean playerSupport;
    private boolean isPausing;
    private boolean isPreparing;
    private int screenWidth;
    private int screenHeight;
    private int pauseTime;
    private int saveBrightness;
    private String videoPath;

    private float mLastMotionX;
    private float mLastMotionY;
    private int startX;
    private int startY;
    private int threshold;
    private boolean isClick = true;
    private boolean isFastPlay;

    public static final int OP_UP = 0xedf1;
    public static final int OP_DOWN = 0xedf2;

    private static final long TIME_UPDATE = 200L;
    private final static int MSG_HIDE_BAR_UI = 0x00b1;
    private final static int MSG_UPDATE_PROGRESS = 0x00b2;
    private final static int MSG_VIDEO_PAUSE = 0x00b3;
    private final static int MSG_VIDEO_RESUME = 0x00b4;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(getActivity() != null && message != null){
                switch (message.what){
                    case MSG_HIDE_BAR_UI:
                        showOrHideBar();
                        break;
                    case MSG_UPDATE_PROGRESS:
                        int currentProgress = message.arg1;
                        //Dbug.w(TAG, "currentProgress : " + currentProgress);
                        if(currentProgress == videoView.getDuration()){
                            ivPlayOrPause.setImageResource(R.drawable.drawable_btn_play);
                        }
                        currentProgress = Math.round(currentProgress/1000.0f);
                        sbProgress.setProgress(currentProgress);
                        tvCurrentTime.setText(TimeFormate.getTimeFormatValue(currentProgress));
                        if(isPlaying()){
                            if(mHandler != null){
                                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_PROGRESS,
                                        videoView.getCurrentPosition(), 0), TIME_UPDATE);
                            }
                        }
                        break;
                    case MSG_VIDEO_PAUSE:
                        ivPlayOrPause.setImageResource(R.drawable.drawable_btn_play);
                        videoView.requestFocus();
                        break;
                    case MSG_VIDEO_RESUME:
                        ivPlayOrPause.setImageResource(R.drawable.drawable_btn_pause);
                        videoView.requestFocus();
                        tvCurrentTime.setText(getString(R.string.default_time_format));
                        break;
                }
            }
            return false;
        }
    });

    public VideoPlayerFragment() {
        // Required empty public constructor
    }


    private Button btnBack=null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);
        videoView = (IjkVideoView) view.findViewById(R.id.video_player_media);
        topBar = (RelativeLayout) view.findViewById(R.id.video_player_top_layout);
        bottomBar = (RelativeLayout) view.findViewById(R.id.video_player_bottom_bar);
        tvTitle = (TextView) view.findViewById(R.id.video_player_top_tv);
        tvCurrentTime = (TextView) view.findViewById(R.id.video_player_current_time);
        tvTotalTime = (TextView) view.findViewById(R.id.video_player_total_time);
        sbProgress = (SeekBar) view.findViewById(R.id.video_player_progress);
        ivPlayOrPause = (ImageView) view.findViewById(R.id.video_player_play_btn);
        ivFullScreen = (ImageView) view.findViewById(R.id.video_player_full_screen);
        btnBack = (Button) view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!=null) getActivity().onBackPressed();
                //getFragmentManager().popBackStack();
            }
        });

        ivPlayOrPause.setOnClickListener(this);
        ivFullScreen.setOnClickListener(this);
        sbProgress.setOnSeekBarChangeListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null){
            screenWidth = AppUtils.getScreenWidth(getContext());
            screenHeight = AppUtils.getScreenHeight(getContext());
            saveBrightness = AppUtils.getScreenBrightness(getActivity());
            threshold = AppUtils.dp2px(getContext(), 20);
            initPlayer();

            if(playerSupport){
                Bundle bundle = getBundle();
                videoPath = bundle.getString(KEY_PATH_LIST);
                if(!TextUtils.isEmpty(videoPath)){
                    playVideo(videoPath);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == 1){
            screenWidth = AppUtils.getScreenWidth(getContext());
            screenHeight = AppUtils.getScreenHeight(getContext());
        }else if(newConfig.orientation == 2){
            screenWidth = AppUtils.getScreenWidth(getContext());
            screenHeight = AppUtils.getScreenHeight(getContext());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isPausing()){
            onResumeVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        boolean isPlaying=playerSupport && videoView.isPlaying();
        pauseVideo();
        if(isPlaying){
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AppUtils.setBrightness(getActivity(), saveBrightness);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void initPlayer(){
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            playerSupport = true;
        } catch (Throwable e) {
            Dbug.e(TAG, "loadLibraries error : "+ e.getMessage());
        }
        if (!playerSupport) {
            mApplication.showToastShort(R.string.player_not_support_dev);
            if(getActivity() != null){
                getActivity().finish();
            }
        }else{
            videoView.setOnTouchListener(this);
            videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
                    if(mHandler != null){
                        mHandler.removeMessages(MSG_UPDATE_PROGRESS);
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_PROGRESS, videoView.getDuration(), 0), 200);
                    }
                    isPausing = true;
                }
            });
            videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                    if (i1 == -10000)//TODO 暂时不处理
                        return true;
                    mApplication.showToastShort(R.string.player_not_support_media);
                    if(getActivity() != null){
                        getActivity().finish();
                    }
                    return true;
                }
            });
        }
    }

    private boolean isPlaying(){
        return videoView != null && videoView.isPlaying();
    }

    private boolean isPausing(){
        return videoView != null && isPausing;
    }

    private boolean isPreparing(){
        return videoView != null && isPreparing;
    }

    private void playVideo(String path){
        if(playerSupport && !TextUtils.isEmpty(path)){
            videoView.setVideoPath(path);
            isPreparing = true;
            videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    Dbug.w(TAG, "onPrepared is ok! start playing.");
                    videoView.start();
                    updatePlayingUI();
                    if(mHandler != null){
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS, videoView.getCurrentPosition(), 0));
                    }
                    isPreparing = false;
                    isPausing = false;

                }
            });
        }
    }

    private void pauseVideo(){
        if(playerSupport && videoView.isPlaying()){
            if(videoView.canPause()){
                videoView.pause();
                isPausing = true;
                pauseTime = videoView.getCurrentPosition();
                if(mHandler != null){
                    mHandler.removeMessages(MSG_UPDATE_PROGRESS);
                    mHandler.sendEmptyMessage(MSG_VIDEO_PAUSE);
                }
            }
        }
    }

    private void onResumeVideo(){
        if(playerSupport && isPausing()){
            videoView.seekTo(pauseTime);
            videoView.start();
            isPausing = false;
            if(mHandler != null){
                mHandler.sendEmptyMessage(MSG_VIDEO_RESUME);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS, videoView.getCurrentPosition(), 0));
            }
        }
    }

    private void playOrPause(){
        if(isPreparing()) return;
        if(isPlaying()){
            pauseVideo();
        }else if(isPausing()){
            onResumeVideo();
        }else{
            if(!TextUtils.isEmpty(videoPath)){
                playVideo(videoPath);
            }
        }
    }

    private void updatePlayingUI(){
        if(!TextUtils.isEmpty(videoPath)){
            ivPlayOrPause.setImageResource(R.drawable.drawable_btn_pause);
            videoView.requestFocus();
            sbProgress.setMax(videoView.getDuration()/1000);
            sbProgress.setProgress(0);
            tvTitle.setText(formatTitle(videoPath));
            tvCurrentTime.setText(getString(R.string.default_time_format));
            tvTotalTime.setText(TimeFormate.getTimeFormatValue(Math.round(videoView.getDuration()/1000.0f)));
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

    private void changeOrientation(){
        int orientation = getActivity().getRequestedOrientation();
        if(orientation == 1) // 竖屏
        {
            videoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            ivFullScreen.setImageResource(R.mipmap.ic_no_fullscreen);
        }else // 横屏
        {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ivFullScreen.setImageResource(R.mipmap.ic_fullscreen);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                startX = (int) x;
                startY = (int) y;
                isFastPlay = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mLastMotionX;
                float deltaY = y - mLastMotionY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                // 声音调节标识
                boolean isAdjustAudio;
                if (absDeltaX > threshold && absDeltaY > threshold) {
                    isAdjustAudio = absDeltaX < absDeltaY;
                } else if (absDeltaX < threshold && absDeltaY > threshold) {
                    isAdjustAudio = true;
                } else if (absDeltaX > threshold && absDeltaY < threshold) {
                    isAdjustAudio = false;
                } else {
                    return true;
                }
                if (isAdjustAudio) {
                    if(x < screenWidth /2){ //亮度调节
                        if (deltaY > 0) {
                            showBrightnessToast(OP_DOWN, absDeltaY);
                        } else if (deltaY < 0) {
                            showBrightnessToast(OP_UP, absDeltaY);
                        }
                    }else{                  //声音调节
                        if (deltaY > 0) {
                            showVolumeToastUI(OP_DOWN, absDeltaY);
                        } else if (deltaY < 0) {
                            showVolumeToastUI(OP_UP, absDeltaY);
                        }
                    }
                }else {
                    if (deltaX > 0 && x > screenWidth /2) {       //快进
                        if(!isFastPlay){
                            showFastForwardToast(absDeltaX);
                            isFastPlay = true;
                        }
                    } else if (deltaX < 0 && x < screenWidth /2) { //快退
                        if(!isFastPlay){
                            showFastBackward(absDeltaX);
                            isFastPlay = true;
                        }
                    }
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(x - startX) > threshold || Math.abs(y - startY) > threshold) {
                    isClick = false;
                }
                mLastMotionX = 0;
                mLastMotionY = 0;
                startX = 0;
                if (isClick) {
                    showOrHideBar();
                }
                isClick = true;
                isFastPlay = false;
                break;
            default:
                break;
        }
        return true;
    }

    private void showVolumeToastUI(int op, float y){
        if(getActivity() != null){
            if(volumeToast == null){
                volumeToast = new VolumeToast(getActivity().getApplicationContext());
            }
            int progress = -1;
            switch (op){
                case OP_UP:
                    progress = (int) ((y / screenHeight) * volumeToast.getMaxVol() * 3);
                    break;
                case OP_DOWN:
                    progress = -(int)((y / screenHeight) * volumeToast.getMaxVol() * 3);
                    break;
            }
            volumeToast.show(progress);
        }
    }

    private void showBrightnessToast(int op, float y){
        if(getActivity() != null){
            if(brightnessToast == null){
                brightnessToast = new BrightnessToast(getActivity());
            }
            int progress = -1;
            switch (op){
                case OP_UP:
                    progress = (int) ((y / screenHeight) * BrightnessToast.getMaxValue() * 3);
                    break;
                case OP_DOWN:
                    progress = -(int)((y / screenHeight) *  BrightnessToast.getMaxValue() * 3);
                    break;
            }
            brightnessToast.show(progress);
        }
    }

    private void showOrHideBar(){
        if(topBar.getVisibility() == View.VISIBLE){
            topBar.setVisibility(View.GONE);
        }else{
            topBar.setVisibility(View.VISIBLE);
        }
        if(bottomBar.getVisibility() == View.VISIBLE){
            bottomBar.setVisibility(View.GONE);
        }else{
            bottomBar.setVisibility(View.VISIBLE);
        }
        if(topBar.getVisibility() == View.VISIBLE && bottomBar.getVisibility() == View.VISIBLE){
            if(mHandler != null){
                mHandler.removeMessages(MSG_HIDE_BAR_UI);
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_BAR_UI, 5000);
            }
        }else {
            mHandler.removeMessages(MSG_HIDE_BAR_UI);
        }
    }

    private void showFastForwardToast(float x){
        if(getActivity() != null){
            int progress = (int)(x/(screenWidth/2) * (videoView.getDuration() - videoView.getCurrentPosition()));
            progress = videoView.getCurrentPosition() + progress;
            if(progress >= videoView.getDuration()){
                progress = videoView.getDuration() - 1000;
            }
            if(videoView.canSeekForward()){
                videoView.seekTo(progress);
                if(isPausing()){
                    pauseTime = progress;
                }
                progress = progress /1000;
                sbProgress.setProgress(progress);
                tvCurrentTime.setText(TimeFormate.getTimeFormatValue(progress));
            }
            showVideoProgressToast(VideoProgressToast.FAST_FORWARD, TimeFormate.getTimeFormatValue(progress));
        }
    }

    private void showFastBackward(float x){
        if(getActivity() != null){
            int progress = (int)(x/(screenWidth/2) * (videoView.getDuration() - videoView.getCurrentPosition()));
            progress = videoView.getCurrentPosition() - progress;
            if(progress <= 0){
                progress = 1000;
            }
            if(videoView.canSeekBackward()){
                videoView.seekTo(progress);
                if(isPausing()){
                    pauseTime = progress;
                }
                progress = progress /1000;
                sbProgress.setProgress(progress);
                tvCurrentTime.setText(TimeFormate.getTimeFormatValue(progress));
            }
            showVideoProgressToast(VideoProgressToast.FAST_BACKWARD, TimeFormate.getTimeFormatValue(progress));
        }
    }

    /**
     * 显示快进\快退UI
     * @param type {@link VideoProgressToast#FAST_FORWARD,VideoProgressToast#FAST_BACKWARD}
     */
    private void showVideoProgressToast(int type, String timeText){
        if(getActivity() != null){
            if(videoProgressToast == null){
                videoProgressToast = new VideoProgressToast(getActivity().getApplicationContext());
            }
            videoProgressToast.show(type, timeText);
        }
    }

    @Override
    public void onClick(View view) {
        if(getActivity() != null && view != null){
            switch (view.getId()){
                case R.id.video_player_play_btn:
                    playOrPause();
                    break;
                case R.id.video_player_full_screen:
                    changeOrientation();
                    break;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(isPlaying() || isPausing()){
            int progress = seekBar.getProgress();
            videoView.seekTo(progress * 1000);
            if(isPausing()){
                pauseTime = progress * 1000;
            }
            tvCurrentTime.setText(TimeFormate.getTimeFormatValue(progress));
        } else {
            sbProgress.setProgress(0);
            tvCurrentTime.setText(getString(R.string.default_time_format));
        }
    }
}
