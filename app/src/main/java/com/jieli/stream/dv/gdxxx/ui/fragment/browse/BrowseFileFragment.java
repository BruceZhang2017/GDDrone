package com.jieli.stream.dv.gdxxx.ui.fragment.browse;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.task.MediaTask;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.shizhefei.view.indicator.FixedIndicatorView;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

/**
 * 文件浏览
 * date : 2017/03/01
 */
public class BrowseFileFragment extends BaseFragment implements View.OnClickListener, IndicatorViewPager.OnIndicatorPageChangeListener{
    private IndicatorViewPager indicatorViewPager;
    private FileBrowseAdapter mAdapter;
    private RelativeLayout normalModeLayout;
    private RelativeLayout editModeLayout;
    private TextView tvAllSelect;
    private TextView tvCenter;
    private TextView tvExit;
    private LinearLayout editLayout;

    private MediaTask mediaTask;
    private WaitingDialog waitingDialog;

    private String[] tabNames;
    private boolean isEditMode;
    private boolean isSelectAll;

    private BrowseBroadcast mReceiver;
    private NotifyDialog mShareTipsDialog;

    private static final int MSG_DISCONNECT_DEVICE = 0X100;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISCONNECT_DEVICE:
                    sendOperation(OP_SHARE_FILES);
                    break;
            }
            return false;
        }
    });

    public BrowseFileFragment() {
        // Required empty public constructor
    }

    private class BrowseBroadcast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getActivity() != null && context != null && intent != null){
                String action = intent.getAction();
                if(!TextUtils.isEmpty(action)){
                    switch (action){
                        case ACTION_SELECT_FILES:
                            int num = intent.getIntExtra(KEY_SELECT_FILES_NUM, 0);
                            tvCenter.setText(getString(R.string.selected_num, num));
                            break;
                        case ACTION_SELECT_STATE_CHANGE:
                            int type = intent.getIntExtra(KEY_STATE_TYPE, 0);
                            switch (type){
                                case TYPE_EDIT:
                                    isEditMode = intent.getBooleanExtra(KEY_SELECT_STATE, false);
                                    updateTopBarUI(isEditMode);
                                    if(!isEditMode){
                                        tvExit.performClick();
                                    }
                                    break;
                                case TYPE_SELECT_ALL:
                                    isSelectAll = intent.getBooleanExtra(KEY_SELECT_STATE, false);
                                    updateAllSelectUI(false);
                                    break;
                            }
                            break;
                        case ACTION_LANGUAAGE_CHANGE:
                            initViewPager();
                            break;
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_file, container, false);
        ImageView editModeBtn = (ImageView) view.findViewById(R.id.operation_edit_btn);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.file_view_pager);
        normalModeLayout = (RelativeLayout) view.findViewById(R.id.top_normal_mode_layout);
        editModeLayout = (RelativeLayout) view.findViewById(R.id.top_edit_mode_layout);
        tvAllSelect = (TextView) view.findViewById(R.id.edit_all_select);
        tvCenter = (TextView) view.findViewById(R.id.edit_center_tv);
        tvExit = (TextView) view.findViewById(R.id.edit_exit);
        FixedIndicatorView fixedIndicatorView = (FixedIndicatorView) view.findViewById(R.id.file_indicator);

        fixedIndicatorView.setOnTransitionListener(new OnTransitionTextListener()
                .setColorId(getActivity(),R.color.text_orange, R.color.text_white)
                .setSizeId(getActivity(), R.dimen.text_normal, R.dimen.text_smaller_x));
        fixedIndicatorView.setDividerPadding(5);
        fixedIndicatorView.setScrollBar(new ColorBar(getActivity(), 0xFFff7f27, 5));
        viewPager.setOffscreenPageLimit(2);

        indicatorViewPager = new IndicatorViewPager(fixedIndicatorView, viewPager);
        indicatorViewPager.setOnIndicatorPageChangeListener(this);
        editModeBtn.setOnClickListener(this);
        tvAllSelect.setOnClickListener(this);
        tvExit.setOnClickListener(this);

        ImageButton btnBack=findImageButton_AutoBack(view,R.id.btnBack, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }


    //自动生成按下按钮背影效果代码(如果收不到MotionEvent.ACTION_UP事件可以将onTouch返回值变成true即可)
    public View.OnTouchListener BtnAutoBackgroundEvent=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //方式一:设置整个按钮透明度
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.1f);
                //aView.setImageAlpha();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1);
            }
            return false;
        }
    };
    public View.OnFocusChangeListener BtnFocusAutoBackgroundEvent=new View.OnFocusChangeListener(){
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            //方式一:设置整个按钮透明度
            if (hasFocus) {
                v.setAlpha(0.1f);
            } else {
                v.setAlpha(1);
            }
        }
    };
    //View自动加上按下按钮效果
    public View findViewById_AutoBack(View view,int iResID, final View.OnClickListener btnEvent){
        View aView=view.findViewById(iResID);
        aView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnEvent!=null) {
                    btnEvent.onClick(v);
                }
                v.setAlpha(1f);
            }
        });
        aView.setOnTouchListener(BtnAutoBackgroundEvent);
        aView.setOnFocusChangeListener(BtnFocusAutoBackgroundEvent);
        return aView;
    }
    public ImageButton findImageButton_AutoBack(View view, int iResID, View.OnClickListener btnEvent){
        return (ImageButton)this.findViewById_AutoBack(view,iResID, btnEvent);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null){
            editLayout = (LinearLayout) getActivity().findViewById(R.id.edit_mode_bar);
            LinearLayout layoutShare = (LinearLayout) getActivity().findViewById(R.id.pop_bottom_bar_share);
            LinearLayout layoutDelete = (LinearLayout) getActivity().findViewById(R.id.pop_bottom_bar_delete);
            layoutShare.setOnClickListener(this);
            layoutDelete.setOnClickListener(this);
            initViewPager();

            if(mReceiver == null){
                mReceiver = new BrowseBroadcast();
            }
            IntentFilter filter = new IntentFilter(ACTION_SELECT_FILES);
            filter.addAction(ACTION_SELECT_STATE_CHANGE);
            filter.addAction(ACTION_LANGUAAGE_CHANGE);
            getActivity().getApplicationContext().registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mediaTask == null || mediaTask.isInterrupted()){
            mediaTask = new MediaTask(getContext(), "media_thread");
            mediaTask.start();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(getActivity() != null && mReceiver != null){
            getActivity().getApplication().unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        dismissWaitingDialog();

        if(mediaTask != null){
            mediaTask.tryToStopTask();
            mediaTask.interrupt();
            mediaTask.release();
            mediaTask = null;
        }
    }

    public MediaTask getMediaTask(){
        return mediaTask;
    }

    private void initViewPager(){
        if(getActivity() == null) return;
        //if(tabNames == null)
        {
            tabNames = getActivity().getResources().getStringArray(R.array.browse_file_list);
        }
        if (mAdapter == null) {
            mAdapter = new FileBrowseAdapter(getActivity().getApplicationContext(), getChildFragmentManager());
            indicatorViewPager.setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if(getActivity() != null && view != null){
            switch (view.getId()){
                case R.id.operation_edit_btn:
                    sendOperation(OP_ENTER_EDIT_MODE);
                    break;
                case R.id.edit_exit:
                    updateTopBarUI(false);
                    sendOperation(OP_EXIT_EDIT_MODE);
                    break;
                case R.id.edit_all_select:
                    isSelectAll = !isSelectAll;
                    updateAllSelectUI(true);
                    break;
                case R.id.pop_bottom_bar_share:
                    if (ClientManager.getClient().isConnected()) {
                        showShareTipsDialog();
                    } else {
                        sendOperation(OP_SHARE_FILES);
                    }
                    break;
                case R.id.pop_bottom_bar_delete:
                    sendOperation(OP_DELETE_FILES);
                    break;
            }
        }
    }

    private void updateTopBarUI(boolean isEditMode){
        this.isEditMode = isEditMode;
        if(isEditMode){
            if(normalModeLayout != null && normalModeLayout.getVisibility() != View.GONE){
                normalModeLayout.setVisibility(View.GONE);
            }
            tvCenter.setText(getString(R.string.selected_num, 0));
            if(editModeLayout != null && editModeLayout.getVisibility() != View.VISIBLE){
                editModeLayout.setVisibility(View.VISIBLE);
            }
        }else{
            if(editModeLayout != null && editModeLayout.getVisibility() != View.GONE){
                editModeLayout.setVisibility(View.GONE);
            }
            if(normalModeLayout != null && normalModeLayout.getVisibility() != View.VISIBLE){
                normalModeLayout.setVisibility(View.VISIBLE);
            }
        }
        updateBottomBar();
    }

    private void updateBottomBar(){
        if(getActivity() == null) return;
        if(isEditMode){
            if(editLayout != null && editLayout.getVisibility() != View.VISIBLE){
                editLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom));
                editLayout.setVisibility(View.VISIBLE);
            }
        }else{
            if(editLayout != null && editLayout.getVisibility() != View.GONE){
                editLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom));
                editLayout.setVisibility(View.GONE);
            }
        }
    }

    private void updateAllSelectUI(boolean isSendOp){
        if(isSelectAll){
            tvAllSelect.setText(getString(R.string.cancel_all_select));
            if(isSendOp){
                sendOperation(OP_SELECT_ALL);
            }
        }else{
            tvAllSelect.setText(getString(R.string.all_select));
            if(isSendOp){
                sendOperation(OP_CANCEL_SELECT_ALL);
            }
        }
    }

    private void sendOperation(int operation){
        if(getActivity() != null){
            Intent intent = new Intent(ACTION_BROWSE_FILE_OPERATION);
            intent.putExtra(KEY_BROWSE_OPERATION, operation);
            getActivity().sendBroadcast(intent);
        }
    }

    public Fragment currentFragment(){
        if(mAdapter != null){
            //Dbug.e(TAG, "Fragment : " +mAdapter.getCurrentFragment());
            return mAdapter.getCurrentFragment();
        }
        return null;
    }

    @Override
    public void onIndicatorPageChange(int preItem, int currentItem) {
        //默认选中第一个
        mApplication.lastPictureSel=null;
        mApplication.lastPictureIsSelOne=false;
        //默认选中第一个
        mApplication.lastVideoSel=null;
        mApplication.lastVideoIsSelOne=false;

        Fragment fragment = mAdapter.getFragmentForPage(currentItem);

        //分享功能
        LinearLayout layoutShare = (LinearLayout) getActivity().findViewById(R.id.pop_bottom_bar_share);
        if (fragment != null && fragment instanceof LocalPhotoFragment) {
            layoutShare.setVisibility(View.VISIBLE);

            LocalPhotoFragment photo=(LocalPhotoFragment)fragment;
            photo.setCheckFirstFile();

            mApplication.setCheckPicture_FirstFile();
        } else {
            layoutShare.setVisibility(View.VISIBLE);

            LocalVideoFragment video=(LocalVideoFragment)fragment;
            video.setCheckFirstFile();


            mApplication.setCheckVideo_FirstFile();
        }

        if(isEditMode){
            tvExit.performClick();
        }
    }

    private class FileBrowseAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {
        private Context mContext;

        FileBrowseAdapter(Context context, FragmentManager fragmentManager) {
            super(fragmentManager);
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return tabNames == null ? 0 : tabNames.length;
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.main_tab_view, container, false);
            }
            TextView textView = (TextView) convertView;
            if(tabNames != null && position < tabNames.length){
                textView.setText(tabNames[position]);
            }

            int width = getTextWidth(textView);
            int padding = AppUtils.dp2px(mContext, 8);
            textView.setWidth((int)(width * 1.3f) + padding);
            textView.setPadding(0, 5, 0, 0);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            Fragment fragment = new BaseFragment();
            if(tabNames != null && position < tabNames.length){
                String tab = tabNames[position];
                if(tab.equals(getString(R.string.tab_image))){

                    fragment = new LocalPhotoFragment();
                    ((LocalPhotoFragment)fragment).setParentFragment(BrowseFileFragment.this);
                }else if(tab.equals(getString(R.string.tab_video))){

                    fragment = new LocalVideoFragment();
                    ((LocalVideoFragment)fragment).setParentFragment(BrowseFileFragment.this);
                }
            }
            return fragment;
        }

        private int getTextWidth(TextView textView) {
            if (textView == null) {
                return 0;
            }
            Rect bounds = new Rect();
            String text = textView.getText().toString();
            Paint paint = textView.getPaint();
            paint.getTextBounds(text, 0, text.length(), bounds);
            return bounds.left + bounds.width();
        }
    }

    public void showWaitingDialog(){
        if(waitingDialog == null){
            waitingDialog = new WaitingDialog();
            waitingDialog.setCancelable(false);
            waitingDialog.setNotifyContent(getString(R.string.deleting_tip));
            waitingDialog.setOnWaitingDialog(new WaitingDialog.OnWaitingDialog() {
                @Override
                public void onCancelDialog() {
                    sendOperation(OP_CANCEL_TASK);
                }
            });
        }
        if(!waitingDialog.isShowing()){
            waitingDialog.show(getFragmentManager(), "wait_dialog");
        }
    }

    public void dismissWaitingDialog(){
        if(waitingDialog != null){
            if(waitingDialog.isShowing()){
                waitingDialog.dismiss();
            }
            waitingDialog = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (ClientManager.getClient().isConnected())
            updateTopBarUI(false);
    }

    private void showShareTipsDialog() {
//        mShareTipsDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.sharing_needs_disconnect,R.string.dialog_confirm,
//                new NotifyDialog.OnConfirmClickListener() {
//                    @Override
//                    public void onClick() {
//                        mShareTipsDialog.dismiss();
//                    }
//        });
        mShareTipsDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.sharing_needs_disconnect,
                R.string.dialog_cancel, R.string.dialog_confirm,
                new NotifyDialog.OnNegativeClickListener() {
                    @Override
                    public void onClick() {
                        mShareTipsDialog.dismiss();
                    }
                }, new NotifyDialog.OnPositiveClickListener() {
                    @Override
                    public void onClick() {
                        mShareTipsDialog.dismiss();
                        ClientManager.getClient().close();
                        mApplication.switchWifi();//断开WIFI
                        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT_DEVICE, 500);
                    }
                });
        if (!mShareTipsDialog.isShowing())
            mShareTipsDialog.show(getActivity().getSupportFragmentManager(), "Share_Tips");
    }
}
