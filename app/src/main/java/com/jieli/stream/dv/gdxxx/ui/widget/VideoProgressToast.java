package com.jieli.stream.dv.gdxxx.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jieli.stream.dv.gdxxx.R;


/**
 * @author zqjasonZhong
 *         date : 2017/5/11
 */
public class VideoProgressToast {
    private Toast toast;
    private Context mContext;

    private ImageView ivFastForward;
    private TextView tvText;
    private ImageView ivFastBackward;

    public static final int FAST_FORWARD = 0x102a;
    public static final int FAST_BACKWARD = 0x102b;

    public VideoProgressToast(@NonNull Context context){
        this.mContext = context;
    }

    public void show(int type, String text){
        if(toast == null){
            toast = new Toast(mContext);
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_video_progress, null);
            ivFastForward = (ImageView) view.findViewById(R.id.view_video_fast_forward);
            tvText = (TextView) view.findViewById(R.id.view_video_tv);
            ivFastBackward = (ImageView) view.findViewById(R.id.view_video_fast_backward);
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        switch (type){
            case FAST_FORWARD:
                ivFastBackward.setVisibility(View.GONE);
                ivFastForward.setVisibility(View.VISIBLE);
                tvText.setText(mContext.getString(R.string.fast_forward));
                break;
            case FAST_BACKWARD:
                ivFastForward.setVisibility(View.GONE);
                ivFastBackward.setVisibility(View.VISIBLE);
                tvText.setText(mContext.getString(R.string.fast_backward));
                break;
        }
        if(!TextUtils.isEmpty(text)){
            tvText.setText(text);
        }
        toast.show();
    }
}
