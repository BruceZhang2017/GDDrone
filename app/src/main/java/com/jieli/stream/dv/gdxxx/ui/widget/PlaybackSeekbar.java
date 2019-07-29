package com.jieli.stream.dv.gdxxx.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;

/**
 * Created by 陈森华 on 2017/8/11.
 * 功能：用一句话描述
 */

public class PlaybackSeekbar extends View {
    //缩略浏览模式
    public static final int BROWSE_COVER_MODE = 0;
    //视频内容浏览模式
    public static final int BROWSE_CONTENT_MODE = 1;
    //实时流查看模式
    public static final int REAL_TIME_STREAM_MODE = 2;
    private Paint backgroundPaint;
    private Paint textPaint;
    private Paint grayPaint;
    private Paint cursorPaint;
    private Paint cursorThumbPaint;
    private Paint textTipPaint;
    private Paint tipBackgroundPaint;
    private float endPos;

    private FileInfo selectFileInfo;
    private String tag = getClass().getSimpleName();

    private int mWidth;
    private int mHeight;
    private int halfHeight;
    private int mode;
    private int currentSelected;
    private int thumbRadio = 3;
    private int paddingRight = 20;
    private int paddingLeft = 20;
    private int paddingTop = 20;
    private float density = 1;

    //浏览模式
    private int browseinterval = -1;
    private int browseMaxPostion;
    private float browseCursorPos;
    private String leftTip;
    private String rightTip;
    private float tipWidth;
    private float tipHeight;
    private float baseline;
    private String tipText = "";

    private Paint browseRightPaint;
    //时间模式
    private long left;
    private long right;
    private float timeCursorPos = paddingLeft;
    private Paint timeLeftPaint;
    private Paint timeSosPaint;
    private long time;

    private int offset = 0;


    private OnStatechangeListener mOnStatechangeListener;

    public void setOnStatechangeListener(OnStatechangeListener mOnStatechangeListener) {
        this.mOnStatechangeListener = mOnStatechangeListener;
    }

    public void setMode(int mode) {
        setModeNotCallback(mode);
        if (browseMaxPostion > -1 && mOnStatechangeListener != null)
            mOnStatechangeListener.onModeChange(mode);
    }

    public int getMode() {
        return mode;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.selectFileInfo = fileInfo;
        left = fileInfo.getStartTime().getTimeInMillis();
        right = fileInfo.getEndTime().getTimeInMillis();
        time = right - left;
        // mode = SELECTED_MODE;
        timeCursorPos = paddingLeft;
        offset = 0;
        invalidate();
    }

    public void setModeNotCallback(int mode) {
        this.mode = mode;
        if (mode == BROWSE_COVER_MODE) {
            tipText = rightTip;
        } else if (mode == REAL_TIME_STREAM_MODE) {
            tipText = leftTip;
            browseCursorPos = endPos;
   /*         Dbug.i(tag, "-----------------------setModeNotCallback---------------------------------");
            Dbug.i(tag, "endPos" + endPos);
            Dbug.i(tag, "browseCursorPos" + browseCursorPos);*/
        }
        Dbug.i(tag, modeToString(mode));
        invalidate();
    }

    public void setFileInfoCount(int count) {
        if (count < 0) {
            return;
        }
        this.browseMaxPostion = count - 1;
        setBrowseinterval(count);
        if (browseMaxPostion > 0) {
            browseCursorPos = endPos - currentSelected * endPos / browseMaxPostion;
        } else {
            currentSelected = 0;
        }
        if (mode == REAL_TIME_STREAM_MODE && browseCursorPos < endPos) {
            browseCursorPos = endPos + thumbRadio;
        }
/*        Dbug.i(tag, "-----------------------setFileInfoCount---------------------------------");
        Dbug.i(tag, "endPos" + endPos);
        Dbug.i(tag, "browseCursorPos" + browseCursorPos);*/
        invalidate();
    }

    public void setBrowseinterval(int count) {
        if (count < 60 && count > 0) {
            browseinterval = (int) ((endPos - paddingLeft) / (count));
        } else if (count >= 60) {
            browseinterval = (int) ((endPos - paddingLeft) / 60);
        } else {
            browseinterval = 0;
        }
    }

    public PlaybackSeekbar(Context context) {
        this(context, null);
    }

    public PlaybackSeekbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public PlaybackSeekbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        Dbug.i(tag, "density->" + density);
        thumbRadio = (int) (thumbRadio * density);
        paddingRight = paddingLeft = paddingTop = (int) (6 * density);
        backgroundPaint = new Paint();
        backgroundPaint.setColor(getResources().getColor(R.color.half_transparent));
        backgroundPaint.setStrokeWidth(1f);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(15 * getResources().getDisplayMetrics().density);

        grayPaint = new Paint();
        grayPaint.setColor(Color.GRAY);
        grayPaint.setStrokeWidth(1f);
        grayPaint.setAntiAlias(true);
        grayPaint.setStyle(Paint.Style.STROKE);

        cursorPaint = new Paint();
        cursorPaint.setColor(getResources().getColor(R.color.bg_playback_seek_thumb));
        cursorPaint.setStrokeWidth(thumbRadio / 2);
        cursorPaint.setAntiAlias(true);
        cursorPaint.setStyle(Paint.Style.FILL);

        cursorThumbPaint = new Paint();
        cursorThumbPaint.setColor(Color.WHITE);
        cursorThumbPaint.setStrokeWidth(2f);
        cursorThumbPaint.setAntiAlias(true);
        cursorThumbPaint.setStyle(Paint.Style.FILL);

        browseRightPaint = new Paint();
        browseRightPaint.setColor(getResources().getColor(R.color.bg_playback_seek_bar));
        browseRightPaint.setStrokeWidth(2f);
        browseRightPaint.setAntiAlias(true);
        browseRightPaint.setStyle(Paint.Style.FILL);

        timeLeftPaint = new Paint();
        timeLeftPaint.setColor(getResources().getColor(R.color.bg_playback_seek_bar));
        timeLeftPaint.setStrokeWidth(2f);
        timeLeftPaint.setAntiAlias(true);
        timeLeftPaint.setStyle(Paint.Style.FILL);

        timeSosPaint = new Paint();
        timeSosPaint.setColor(getResources().getColor(R.color.bg_playback_seek_bar));
        timeSosPaint.setStrokeWidth(2f);
        timeSosPaint.setAntiAlias(true);
        timeSosPaint.setStyle(Paint.Style.FILL);

        textTipPaint = new Paint();
        textTipPaint.setColor(getResources().getColor(R.color.text_white));
        textTipPaint.setAntiAlias(true);
        textTipPaint.setTextSize(14 * density);
        textTipPaint.setTextAlign(Paint.Align.CENTER);

        leftTip = getResources().getString(R.string.playback_seekbar_tip_left);
        rightTip = getResources().getString(R.string.playback_seekbar_tip_right);
        tipText = leftTip;
        float leftWidth = textTipPaint.measureText(leftTip);
        float rightWidth = textTipPaint.measureText(rightTip);
        tipWidth = leftWidth > rightWidth ? leftWidth : rightWidth;
        tipWidth = tipWidth + 20;
        tipHeight = (textTipPaint.descent() - textTipPaint.ascent()) * 1.5f;
        Paint.FontMetricsInt fontMetrics = textTipPaint.getFontMetricsInt();
        baseline = (tipHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top + paddingTop;
        tipBackgroundPaint = new Paint();
        tipBackgroundPaint.setColor(getResources().getColor(R.color.bg_playback_tip));
        tipBackgroundPaint.setAntiAlias(true);
        tipBackgroundPaint.setStrokeWidth(2);
        tipBackgroundPaint.setStyle(Paint.Style.FILL);

        paddingLeft = paddingRight = AppUtils.dp2px(getContext(), 10);
    }

    private void drawTimeRule(Canvas canvas) {
        RectF background = new RectF(0, halfHeight, mWidth, mHeight);
        canvas.drawRect(background, backgroundPaint);
        background.set(0, halfHeight, timeCursorPos, mHeight);
        canvas.drawRect(background, timeLeftPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("0", paddingLeft, mHeight * 0.85f, textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(formatTime((int) time), mWidth - paddingRight, mHeight * 0.85f, textPaint);
        canvas.drawLine(timeCursorPos, halfHeight, timeCursorPos, mHeight, cursorPaint);
        canvas.drawRect(timeCursorPos - thumbRadio, ((mHeight - halfHeight) / 2 + halfHeight - thumbRadio), timeCursorPos + thumbRadio, ((mHeight - halfHeight) / 2 + halfHeight + thumbRadio), cursorThumbPaint);
    }

    private String formatTime(int time) {
        String min = String.format("%02d", time / 1000 / 60);
        String second = String.format("%02d", time / 1000 % 60);
        return min + ":" + second;

    }

    private void drawBrowseRule(Canvas canvas) {
        RectF rectF = new RectF(browseCursorPos, halfHeight, mWidth, mHeight);
        canvas.drawRect(rectF, browseRightPaint);
        if (browseinterval > 0) {
            int index = paddingLeft;
            int count = 0;
            int interalPad = 20;
            while (index < mWidth) {
                if (count == 5) {
                    canvas.drawLine(index, halfHeight, index, mHeight, grayPaint);
                    count = 0;
                } else {
                    canvas.drawLine(index, halfHeight + interalPad, index, mHeight - interalPad, grayPaint);
                    count++;
                }
                index += browseinterval;
            }
        }
    /*    Dbug.i(tag, "endPos" + endPos);
        Dbug.i(tag, "browseCursorPos" + browseCursorPos);*/
        canvas.drawLine(browseCursorPos, halfHeight, browseCursorPos, mHeight, cursorPaint);
        canvas.drawRect(browseCursorPos - thumbRadio, ((mHeight - halfHeight) / 2 + halfHeight - thumbRadio), browseCursorPos + thumbRadio, ((mHeight - halfHeight) / 2 + halfHeight + thumbRadio), cursorThumbPaint);
    }


    private void drawTip(Canvas canvas) {
        RectF rectF = null;
        float centerX = 0;
        if (browseCursorPos + tipWidth / 2 >= mWidth) {
            centerX = mWidth - tipWidth / 2;
        } else if (browseCursorPos - tipWidth / 2 <= 0) {
            centerX = tipWidth / 2;
        } else {
            centerX = browseCursorPos;
        }
        rectF = new RectF(centerX - tipWidth / 2, paddingTop, centerX + tipWidth / 2, tipHeight + paddingTop);
        canvas.drawRoundRect(rectF, 10, 10, cursorPaint);
        canvas.drawText(tipText, centerX, baseline, textTipPaint);
        canvas.drawLine(browseCursorPos, rectF.bottom, browseCursorPos, halfHeight - thumbRadio * 2, tipBackgroundPaint);
        canvas.drawCircle(browseCursorPos, halfHeight - thumbRadio * 2, thumbRadio, tipBackgroundPaint);
    }

    private void handleBrowseCoverTouchEvent(MotionEvent motionevent) {
        float x = motionevent.getX();
        browseCursorPos = x < thumbRadio ? thumbRadio : x < mWidth - thumbRadio ? x : mWidth - thumbRadio;
        if (x > endPos) {
            mode = REAL_TIME_STREAM_MODE;
            tipText = leftTip;
            if (mOnStatechangeListener != null)
                mOnStatechangeListener.onModeChange(getMode());
            return;
        }
        int postion = Math.round((endPos - browseCursorPos) / (endPos - paddingLeft) * browseMaxPostion);
//        Dbug.i(tag, "postion  = " + postion);
//        Dbug.i(tag, "currentSelected = " + currentSelected);
        if (postion < 0 || browseMaxPostion < 0) {
            return;
        }
        if (postion != currentSelected || motionevent.getAction() == MotionEvent.ACTION_UP) {
            currentSelected = postion > browseMaxPostion ? browseMaxPostion : postion;
            if (mOnStatechangeListener != null) {
                mOnStatechangeListener.onBrowseCoverChange(currentSelected);
            } else {
                Dbug.d(tag, "mOnStatechangeListener is null");
            }
        }
    }

    private void handleBrowseContentTouchEvent(MotionEvent motionevent) {
        float x = motionevent.getX();
        timeCursorPos = x < thumbRadio ? thumbRadio : x < mWidth - thumbRadio ? x : mWidth - thumbRadio;
        if (motionevent.getAction() == MotionEvent.ACTION_UP || motionevent.getAction() == MotionEvent.ACTION_CANCEL) {
            float per = (timeCursorPos - paddingLeft) / (endPos - paddingLeft);
            //Dbug.e(tag, "handleActionUpEvent offset-> "+offset);
            this.offset = (int) (per * time);
            long time = this.offset + left;
            if (mOnStatechangeListener != null)
                mOnStatechangeListener.onBrowseContentChange(time, offset);
        }
    }

    private void handleRealTimeModeTouchEvent(MotionEvent motionevent) {
        if (motionevent.getX() > endPos) {
            if (motionevent.getX() < mWidth - thumbRadio) {
                browseCursorPos = motionevent.getX();
            } else {
                browseCursorPos = mWidth - thumbRadio;
            }
            return;
        }
        mode = BROWSE_COVER_MODE;
        tipText = rightTip;
        if (mOnStatechangeListener != null) {
            mOnStatechangeListener.onModeChange(getMode());
        }
        handleBrowseCoverTouchEvent(motionevent);
    }

    private void dispatchEvent(MotionEvent motionevent) {
        switch (getMode()) {
            case REAL_TIME_STREAM_MODE:
                handleRealTimeModeTouchEvent(motionevent);
                break;
            case BROWSE_COVER_MODE:
                handleBrowseCoverTouchEvent(motionevent);
                break;
            case BROWSE_CONTENT_MODE:
                handleBrowseContentTouchEvent(motionevent);
                break;
        }
    }

    public void setBrowsePostion(int postion) {
        if (postion != currentSelected && mode != REAL_TIME_STREAM_MODE) {
            currentSelected = postion;
            if (browseMaxPostion > 0) {
                browseCursorPos = (browseMaxPostion - currentSelected) * endPos / browseMaxPostion;
            }
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
   /*     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Dbug.i(tag,MotionEvent.actionToString(motionevent.getAction()));
        }*/
        switch (motionevent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (motionevent.getY() >= halfHeight) {
                    return true;
                } else {
                    return false;
                }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dispatchEvent(motionevent);
                invalidate();
                break;
        }
        return super.onTouchEvent(motionevent);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF background = new RectF(0, halfHeight, mWidth, mHeight);
        canvas.drawRect(background, backgroundPaint);
        if (getMode() == BROWSE_CONTENT_MODE) {
            drawTimeRule(canvas);
        } else if (getMode() == BROWSE_COVER_MODE || getMode() == REAL_TIME_STREAM_MODE) {
            drawBrowseRule(canvas);
            drawTip(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = measureWidth(widthMeasureSpec);
        mHeight = measureHeight(heightMeasureSpec);
        halfHeight = (int) (mHeight / 1.8f);
        endPos = mWidth - cursorPaint.getStrokeWidth() - paddingRight;
        if (mode == REAL_TIME_STREAM_MODE && browseCursorPos <= endPos) {
            browseCursorPos = endPos + thumbRadio;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        //设置一个默认值，就是这个View的默认宽度为500，这个看我们自定义View的要求
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST) {//相当于我们设置为wrap_content
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {//相当于我们设置为match_parent或者为一个具体的值
            result = specSize;
        }
        return result;
    }


    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 100;
        if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }


    public static String modeToString(int mode) {
        String str = "PlaybackSeekBar mode: ";
        switch (mode) {
            case BROWSE_CONTENT_MODE:
                str += "内容浏览";
                break;
            case BROWSE_COVER_MODE:
                str += "封面浏览";
                break;
            case REAL_TIME_STREAM_MODE:
                str += "实时流浏览";
                break;
        }
        return str;
    }

    public interface OnStatechangeListener {
        void onBrowseCoverChange(int position);

        void onBrowseContentChange(long time, int offset);

        /**
         * @param mode:{@link #BROWSE_CONTENT_MODE,#BROWSE_COVER_MODE,#REAL_TIME_STREAM_MODE}.
         */
        void onModeChange(int mode);
    }

    public int getOffset() {
        return offset;
    }
}
