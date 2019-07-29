package com.jieli.stream.dv.gdxxx.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.jieli.lib.dv.control.connect.listener.OnConnectStateListener;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.AboutFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.AddDeviceFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.HelpFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.UpgradeFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.browse.DevPhotoFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.browse.PhotoViewFragment;
import com.jieli.stream.dv.gdxxx.ui.fragment.browse.VideoPlayerFragment;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IActions;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;

/**
 * 通用界面
 * date : 2017/3/6
 */
public class GenericActivity extends BaseActivity {
    String tag = getClass().getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_generic);

        ClientManager.getClient().registerConnectStateListener(connectStateListener);
        Intent intent = getIntent();
        if(intent != null){
            int fragmentTag = intent.getIntExtra(KEY_FRAGMENT_TAG, 0);
            Bundle bundle = intent.getBundleExtra(KEY_DATA);
            switchFragmentByTag(fragmentTag, bundle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClientManager.getClient().unregisterConnectStateListener(connectStateListener);
    }

    @Override
    public void onBackPressed() {
        if (a.isFactoryMode) {
            PreferencesHelper.remove(mApplication, CURRENT_WIFI_SSID);
            sendBroadcast(new Intent(IActions.ACTION_ACCOUT_CHANGE));
        } else {
            setResult(Activity.RESULT_OK);
        }
        finish();
    }

    private OnConnectStateListener connectStateListener = new OnConnectStateListener() {
        @Override
        public void onStateChanged(Integer state) {
            switch (state) {
                case Constants.DEVICE_STATE_CONNECTION_TIMEOUT:
                case Constants.DEVICE_STATE_EXCEPTION:
                case Constants.DEVICE_STATE_DISCONNECTED:
                case Constants.DEVICE_STATE_UNREADY:
                    Dbug.e(tag, "state=" + state);
//                    BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.generic_fragment_layout);
//                    if (fragment instanceof DevPhotoFragment) {
//                        ((DevPhotoFragment)fragment).dismissWaitingDialog();
//                    }
//                    if (fragment instanceof AboutFragment) {
//                        ((AboutFragment)fragment).dismissWaitingDialog();
//                    }
//                    finish();
                    break;
            }
        }
    };
    private void switchFragmentByTag(int tag, Bundle bundle){
        BaseFragment fragment = null;
        String fragmentTag = null;
        switch (tag){
            case ADD_DEVICE_FRAGMENT:
                fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(AddDeviceFragment.class.getSimpleName());
                if(fragment == null){
                    fragment = new AddDeviceFragment();
                }
                fragmentTag = AddDeviceFragment.class.getSimpleName();
                break;
            case UPGRADE_FRAGMENT:
                fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(UpgradeFragment.class.getSimpleName());
                if(fragment == null){
                    fragment = new UpgradeFragment();
                }
                fragmentTag = UpgradeFragment.class.getSimpleName();
                break;
            case DEV_PHOTO_FRAGMENT:
                fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(DevPhotoFragment.class.getSimpleName());
                if(fragment == null){
                    fragment = new DevPhotoFragment();
                }
                fragmentTag = DevPhotoFragment.class.getSimpleName();
                break;
            case PHOTO_VIEW_FRAGMENT:
                fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(PhotoViewFragment.class.getSimpleName());
                if(fragment == null){
                    fragment = new PhotoViewFragment();
                }
                fragmentTag = PhotoViewFragment.class.getSimpleName();
                break;
            case VIDEO_PLAYER_FRAGMENT:
                fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(VideoPlayerFragment.class.getSimpleName());
                if(fragment == null){
                    fragment = new VideoPlayerFragment();
                }
                fragmentTag = VideoPlayerFragment.class.getSimpleName();
                break;
            case HELP_FRAGMENT:
                fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(HelpFragment.class.getSimpleName());
                if(fragment == null){
                    fragment = HelpFragment.newInstance();
                }
                fragmentTag = HelpFragment.class.getSimpleName();
                break;
            case ABOUT_FRAGMENT:
                fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(AboutFragment.class.getSimpleName());
                if(fragment == null){
                    fragment = AboutFragment.newInstance();
                }
                fragmentTag = AboutFragment.class.getSimpleName();
                break;
        }
        if(fragment != null){
            if(bundle != null){
                fragment.setBundle(bundle);
            }
            if(!TextUtils.isEmpty(fragmentTag)) {
                changeFragment(R.id.generic_fragment_layout, fragment, fragmentTag);
            }else{
                changeFragment(R.id.generic_fragment_layout, fragment);
            }
        }
    }
}
