package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;
import com.jieli.stream.dv.gdxxx.util.QRCode;
import com.jieli.stream.dv.gdxxx.util.json.listener.OnCompletedListener;

/**
 * Description:
 * Author:created by bob on 18-1-17.
 */
public class QRCodeDialog extends BaseDialogFragment implements View.OnClickListener {
    private String tag = getClass().getSimpleName();
    private static final String WIFI_NAME = "wifi_name";
    private static final String HOT_SPOT = "hot_spot";
    private static final String SAVE_INFO = "save_to_dev";

    private Button mStartSearchButton;
    private OnCompletedListener<Boolean> mOnCompletedListener;

    public static QRCodeDialog newInstance(String wifi, String password) {
        return newInstance(wifi, password, false);
    }
    public static QRCodeDialog newInstance(String wifi, String password, boolean isSave) {
        Bundle args = new Bundle();
        args.putString(WIFI_NAME, wifi);
        args.putString(HOT_SPOT, password);
        args.putBoolean(SAVE_INFO, isSave);
        QRCodeDialog fragment = new QRCodeDialog();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null)
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_qr_code, container, false);
        mStartSearchButton = (Button) view.findViewById(R.id.start_search_btn);
        mStartSearchButton.setOnClickListener(this);
        ImageView imageView = (ImageView) view.findViewById(R.id.qr_code_view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String wifi = bundle.getString(WIFI_NAME);
            String hotSpot = bundle.getString(HOT_SPOT);
            boolean isSave = bundle.getBoolean(SAVE_INFO, false);
            //String info = "{\"SSID\":\"hello\",\"PWD\":\"Hello\",\"AUTH\":\"JL_ONLY\"}";
            String codeString = "{"
                    + "\"SSID\":\"" + wifi + "\","
                    + "\"PWD\":\"" + hotSpot + "\","
                    + "\"SAVE\":" + (isSave?1:0) + ","
                    + "\"AUTH\":\"JL_ONLY\""
                    + "}";
            //Dbug.e(tag, "codeString=" + codeString);
            imageView.setImageBitmap(QRCode.createQRCode(codeString));
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() == null || getDialog().getWindow() == null) return;
        final WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();

        params.width = 100;
        params.height = 50;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.width = displayMetrics.heightPixels * 9 / 10;
            params.height = displayMetrics.heightPixels;// * 3 / 4;
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.width = displayMetrics.widthPixels * 9 / 10;
            params.height = displayMetrics.widthPixels;// * 3 / 4;
        }
        params.gravity = Gravity.CENTER;
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    public void onClick(View v) {
        if (v == mStartSearchButton) {
            dismiss();
            if (mOnCompletedListener != null) {
                mOnCompletedListener.onCompleted(true);
            }
        }
    }

    public void setOnCompletedListener(OnCompletedListener<Boolean> listener) {
        mOnCompletedListener = listener;
    }
}
