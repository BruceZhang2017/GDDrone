package com.jieli.stream.dv.gdxxx.ui.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.view.WindowManager;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.fragment.browse.BrowseFileFragment;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;

public class BrowseFileActivity extends BaseActivity  {
    String tag = getClass().getSimpleName();
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //修改APP语言
        String lan = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_APP_LANGUAGE_CODE, "-1");
        if (!"-1".equals(lan))
            AppUtils.changeAppLanguage(getApplicationContext(), lan);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_browsefile);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (!(fragment instanceof BrowseFileFragment)) {
            fragment = new BrowseFileFragment();
        }
        changeFragment(R.id.container, fragment, BrowseFileFragment.class.getSimpleName());
    }
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }












}
