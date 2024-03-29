package com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.layout.BaseFooterView;
import com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.layout.PullRefreshLayout;
import com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.support.type.LayoutType;
import com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.utils.AnimUtil;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class ExpandFooterView extends BaseFooterView {
    private View progress;
    private View stateImg;
    private View loadBox;

    private int state = NONE;

    private int layoutType = LayoutType.LAYOUT_DRAWER;

    public ExpandFooterView(Context context) {
        this(context, null);
    }

    public ExpandFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_footer_expand, this, true);
        progress = findViewById(R.id.progress);
        stateImg = findViewById(R.id.state);
        loadBox = findViewById(R.id.load_box);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 350));
    }

    @Override
    public void setPullRefreshLayout(PullRefreshLayout refreshLayout) {
        super.setPullRefreshLayout(refreshLayout);
        refreshLayout.setMaxDistance(350);
    }

    @Override
    protected void onStateChange(int state) {
        this.state = state;
        ObjectAnimator.clearAllAnimations();
        stateImg.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(progress, 1f);
        switch (state) {
            case NONE:
                break;
            case PULLING:
                break;
            case LOOSENT_O_LOAD:
                break;
            case LOADING:
                AnimUtil.startRotation(progress, ViewHelper.getRotation(progress) + 359.99f, 500, 0, -1);
                break;
            case LOAD_CLONE:
                AnimUtil.startShow(stateImg, 0.1f, 400, 200);
                AnimUtil.startHide(progress);
                break;

        }

    }

    @Override
    public float getSpanHeight() {
        return loadBox.getHeight();
    }

    @Override
    public int getLayoutType() {
        return layoutType;
    }

    @Override
    public boolean onScroll(float y) {
        boolean intercept = super.onScroll(y);
        if (!isLockState()) {
            ViewHelper.setRotation(progress, y * y * 48 / 31250);
        }
        return intercept;
    }
}
