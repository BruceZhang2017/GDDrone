package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;


/**
 * 等待框
 * @author zqjasonZhong
 *         date : 2017/4/17
 */
public class WaitingDialog extends BaseDialogFragment implements DialogInterface.OnKeyListener{

    private TextView tvNotifyContent;

    private String notifyContent;

    private OnWaitingDialog onWaitingDialog;

    public interface OnWaitingDialog{
        void onCancelDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if(window == null) return;
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.5f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowParams.windowAnimations = 0;
        window.setAttributes(windowParams);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_waiting, container, false);
        if(getDialog() != null){
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        tvNotifyContent = (TextView) view.findViewById(R.id.dialog_waiting_tv);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null && getDialog() != null && getDialog().getWindow() != null){

            if(tvNotifyContent != null && tvNotifyContent.getVisibility() == View.VISIBLE){
                tvNotifyContent.setText(notifyContent);
            }

            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            getDialog().getWindow().setAttributes(params);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
            getDialog().setOnKeyListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(onWaitingDialog != null){
                onWaitingDialog.onCancelDialog();
            }
            dismiss();
            return true;
        }
        return false;
    }

    public void setNotifyContent(String notifyContent) {
        this.notifyContent = notifyContent;
    }

    public void updateNotifyContent(String content){
        if(!TextUtils.isEmpty(content)){
            this.notifyContent = content;
            if(tvNotifyContent != null && tvNotifyContent.getVisibility() == View.VISIBLE){
                tvNotifyContent.setText(notifyContent);
            }
        }
    }

    public void setOnWaitingDialog(OnWaitingDialog onWaitingDialog) {
        this.onWaitingDialog = onWaitingDialog;
    }
}
