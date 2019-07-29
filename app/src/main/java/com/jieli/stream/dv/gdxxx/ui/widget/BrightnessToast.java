package com.jieli.stream.dv.gdxxx.ui.widget;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.widget.verticalseekbar.VerticalSeekBar;
import com.jieli.stream.dv.gdxxx.util.AppUtils;


/**
 * @author zqjasonZhong
 *         date : 2017/5/11
 */
public class BrightnessToast {
    private Toast toast;
    private Activity context;

    private VerticalSeekBar sbBrightness;

    private static final int maxValue = 255;

    public BrightnessToast(@NonNull Activity context){
        this.context = context;
    }

    public void show(int progress){
        int currentValue = AppUtils.getScreenBrightness(context);
        if(toast == null) {
            toast = new Toast(context);
            View view = LayoutInflater.from(context).inflate(R.layout.view_brightness, null);
            sbBrightness = (VerticalSeekBar) view.findViewById(R.id.view_brightness_seek_progress);
            sbBrightness.setMax(maxValue);
            sbBrightness.setProgress(currentValue);
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        progress = currentValue + progress;
        progress = Math.min(progress, maxValue);
        if(progress >= 0){
            sbBrightness.setProgress(progress);
            AppUtils.setBrightness(context, progress);
        }else{
            sbBrightness.setProgress(currentValue);
        }
        toast.show();
    }

    public static int getMaxValue() {
        return maxValue;
    }
}
