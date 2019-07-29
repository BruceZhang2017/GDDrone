package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;


/**
 * @author zqjasonZhong
 *  date : 2017/3/2
 */
public class InputContentDialog extends BaseDialogFragment implements View.OnClickListener{

    private EditText mEditText;

    private OnContentListener onContentListener;

    public interface OnContentListener{
        void onInput(String content);
    }

    public void setOnContentListener(OnContentListener onContentListener) {
        this.onContentListener = onContentListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_input_content, container, false);
        mEditText = (EditText) view.findViewById(R.id.dialog_edit_text);
        Button cancelBtn = (Button) view.findViewById(R.id.dialog_input_cancel_btn);
        Button confirmBtn = (Button) view.findViewById(R.id.dialog_input_confirm_btn);

        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() == null || getActivity().getWindow() == null || getDialog().getWindow() == null) return;
        final WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();

        params.width = 150;
        params.height = 50;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            params.width = displayMetrics.heightPixels * 4 / 6;
            params.height = displayMetrics.heightPixels * 2 / 5;
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            params.width = displayMetrics.widthPixels * 4 / 6;
            params.height = displayMetrics.widthPixels * 2 / 5;
        }
        params.gravity = Gravity.CENTER;
        getDialog().getWindow().setAttributes(params);
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
    public void onClick(View v) {
        if(getActivity() == null || getActivity().getWindow() == null || getDialog().getWindow() == null) return;
        switch (v.getId()){
            case R.id.dialog_input_cancel_btn:
                dismiss();
                break;
            case R.id.dialog_input_confirm_btn:
                if(mEditText != null){
                    String content = mEditText.getText().toString().trim();
                    if(!TextUtils.isEmpty(content)){
                        if(onContentListener != null){
                            onContentListener.onInput(content);
                        }
                        dismiss();
                    }else{
                        showToastShort(R.string.input_content_empty);
                    }
                }
                break;
        }
    }
}
