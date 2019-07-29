package com.jieli.stream.dv.gdxxx.ui.activity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;

public class UserAgreementActivity extends BaseActivity {
    private final String USER_PROTOCOL = "http://cam.jieli.net:28111/app/app.user.service.protocol.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        WebView mWebView = new WebView(this);
        mWebView.loadUrl(USER_PROTOCOL);
        setContentView(mWebView);
    }
}
