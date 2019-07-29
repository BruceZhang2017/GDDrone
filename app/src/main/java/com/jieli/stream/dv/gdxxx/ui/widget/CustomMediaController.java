package com.jieli.stream.dv.gdxxx.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.MediaController;

import com.jieli.stream.dv.gdxxx.ui.widget.media.IMediaController;

import java.util.ArrayList;

/**
 * @author zqjasonZhong
 *         date : 2017/7/10
 */
public class CustomMediaController extends MediaController implements IMediaController{
    private ArrayList<View> mShowOnceArray = new ArrayList<>();
    private ActionBar mActionBar;

    public CustomMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMediaController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public CustomMediaController(Context context) {
        super(context);
    }

    public void setSupportActionBar(@NonNull ActionBar actionBar) {
        mActionBar = actionBar;
        if (isShowing()) {
            actionBar.show();
        } else {
            actionBar.hide();
        }
    }

    @Override
    public void showOnce(View view) {
        mShowOnceArray.add(view);
        view.setVisibility(View.VISIBLE);
        show();
    }

    @Override
    public void show() {
        super.show();
        if (mActionBar != null)
            mActionBar.show();
    }

    @Override
    public void hide() {
        super.hide();
        if (mActionBar != null)
            mActionBar.hide();
        for (View view : mShowOnceArray)
            view.setVisibility(View.GONE);
        mShowOnceArray.clear();
    }
}
