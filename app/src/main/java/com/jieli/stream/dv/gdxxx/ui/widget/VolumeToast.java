package com.jieli.stream.dv.gdxxx.ui.widget;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;


import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.widget.verticalseekbar.VerticalSeekBar;

import static android.content.Context.AUDIO_SERVICE;

/**
 * @author zqjasonZhong
 *         date : 2017/5/11
 */
public class VolumeToast {
    private Toast toast;
    private Context mContext;

    private VerticalSeekBar sbVolume;

    private AudioManager mAudioManager;

    private int maxVol;

    public VolumeToast(@NonNull Context context){
        this.mContext = context;
        mAudioManager = (AudioManager)context.getSystemService(AUDIO_SERVICE);
        maxVol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void show(int progress){
        int currentVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(toast == null){
            toast = new Toast(mContext);
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_volume, null);
            sbVolume = (VerticalSeekBar) view.findViewById(R.id.view_volume_seek_progress);
            sbVolume.setMax(maxVol);
            sbVolume.setProgress(currentVol);
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        progress = currentVol + progress;
        progress = Math.min(progress, maxVol);
        if(progress >= 0){
            sbVolume.setProgress(progress);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        }else{
            sbVolume.setProgress(currentVol);
        }
        toast.show();
    }

    public int getMaxVol() {
        return maxVol;
    }
}
