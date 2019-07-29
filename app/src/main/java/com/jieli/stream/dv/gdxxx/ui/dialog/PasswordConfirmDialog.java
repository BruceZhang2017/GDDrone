package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;
import com.jieli.stream.dv.gdxxx.util.IConstant;


public class PasswordConfirmDialog extends BaseDialogFragment implements IConstant {
    private EditText mContent;
    private TextView mConfirm;
    private TextView mCancel;
    private TextView mTitle;
    private String mTextTitle;

    public static PasswordConfirmDialog newInstance(String title) {
        PasswordConfirmDialog dialog = new PasswordConfirmDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        mTextTitle = getArguments().getString("title", null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.input_password_dialog, container, false);
        mContent = (EditText) v.findViewById(R.id.et_password);
        mConfirm = (TextView) v.findViewById(R.id.tv_confirm);
        mCancel = (TextView) v.findViewById(R.id.tv_cancel);
        mTitle = (TextView) v.findViewById(R.id.tv_title);
        mConfirm.setOnClickListener(mOnClickListener);
        mCancel.setOnClickListener(mOnClickListener);
        mTitle.setText(mTextTitle);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getDialog() == null || getDialog().getWindow() == null) return;
        final WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();

        params.width = 100;
        params.height = 50;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.width = displayMetrics.heightPixels * 4 / 5;
            params.height = displayMetrics.heightPixels * 3 / 5;
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.width = displayMetrics.widthPixels * 4 / 5;
            params.height = displayMetrics.widthPixels * 3 / 5;
        }
        params.gravity = Gravity.CENTER;
        getDialog().getWindow().setAttributes(params);
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mConfirm == v) {
                commitPassword();
            } else if(mCancel == v) {
                dismiss();
            }
        }
    };

    private void commitPassword(){
        String textPWD = mContent.getText().toString().trim();

        dismiss();
        if (mOnInputCompletionListener != null){
            mOnInputCompletionListener.onCompletion(mTextTitle, textPWD);
        }
    }

    private OnInputCompletionListener mOnInputCompletionListener;
  public  void setOnInputCompletionListener(OnInputCompletionListener listener){
        mOnInputCompletionListener = listener;
    }
   public interface OnInputCompletionListener {
        void onCompletion(String ssid, String password);
    }
}
