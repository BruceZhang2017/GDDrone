package com.jieli.stream.dv.gdxxx.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;

/**
 * Fragment 基类
 * date : 2017/2/27
 */
public class BaseFragment extends Fragment implements IConstant, IActions {
    public String TAG = getClass().getSimpleName();
    public WifiHelper mWifiHelper;
    public a mApplication;

    private Bundle bundle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity() == null) return;
        mApplication = a.getApplication();
        mWifiHelper = WifiHelper.getInstance(mApplication);
//        if (a.isOpenLeakCanary) mApplication.getRefWatcher().watch(this);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
