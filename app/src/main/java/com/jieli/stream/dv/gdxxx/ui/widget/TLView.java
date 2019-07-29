package com.jieli.stream.dv.gdxxx.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Description:
 * Author:created by bob on 17-6-29.
 */
public class TLView extends ViewGroup implements IConstant {
    private final String tag = getClass().getSimpleName();
    public interface OnValueChangeListener {
        /**
         * value不再变化，终点
         *
         * @param calendar 刻度盘上当前时间
         */
        void onValueChangeEnd(Calendar calendar);
    }

    private static final long SEC_MILLIS = 1000;//millisecond
    private static final long MIN_MILLIS = 60 * SEC_MILLIS;
    private static final long HOUR_MILLIS = 60 * MIN_MILLIS;
    private static final long DAY_MILLIS = HOUR_MILLIS * 24;

    private TextPaint textPaint, blacktext30;
    private TextPaint dateAndTimePaint;

    private Paint blackline, middlePaint, mGrayLine, eventPaint, mSelectedPaint, mBackgroundPaint, mSosPaint,
            mCapturePaint, mDelayPaint;
    private final List<FileInfo> mCalendarStuff = new ArrayList<>();
    public void setData(List<FileInfo> c) {
        if (mCalendarStuff != null)
            mCalendarStuff.clear();
        else
            throw new NullPointerException("mCalendarStuff is null");
        mCalendarStuff.addAll(c);
    }
    public void setData(FileInfo c) {
        if (mCalendarStuff != null)
            mCalendarStuff.clear();
        else
            throw new NullPointerException("mCalendarStuff is null");
        if (c==null)
            throw new NullPointerException("Param is null");
        //Calendar calendar = Calendar.getInstance();
        left = c.getStartTime().getTimeInMillis()- MIN_MILLIS;
        right = c.getEndTime().getTimeInMillis() + MIN_MILLIS;
        span = right - left;
        mCalendarStuff.add(c);
        invalidate();
    }

    /**
     * width of view in pixels
     */
    private int width;
    /**
     * Height of view in pixels
     */
    private int mHeight;
    /**
     * left and right limit of the ruler in view, in milliseconds
     */
    private long left, right;
    /**
     * how many fingers are being used? 0, 1, 2
     */
    int fingers;
    /**
     * holds pointer id of #1/#2 fingers
     */
    int finger1id, finger2id = -1;
    /**
     * holds x/y in pixels of #1/#2 fingers from last frame
     */
    volatile float finger1x, finger1y, finger2x, finger2y;

    volatile float mFinger1x, mFinger1y, mFinger2x, mFinger2y;

    /**
     * width of the view in milliseconds, cached value of (right-left)
     */
    float span;//跨度
    /**
     * how many pixels does each millisecond correspond to?
     */
    float pixels_per_milli;
    /**
     * length in pixels of time units, at current zoom scale
     */
    float sec_pixels, min_pixels, hour_pixels, day_pixels;

    /**
     * reusable calendar class object for rounding time to nearest applicable unit in onDraw
     */
    private Calendar acalendar;

    private OnValueChangeListener mOnValueChangeListener;

    /**
     * 是否正在移动
     */
    private volatile boolean isMove = false;

    /**
     * The logical density of the display.
     */
    private float mDensity;

    /**
     * 视频选择模式
     */
    private volatile boolean isSelectionMode = false;

    /**
     * 当前哪个手指
     */
    private volatile int mWhichFinger = 0;

    private volatile long mFinger1Time = 0;
    private volatile long mFinger2Time = 0;
    /**
     * 请求下一张缩略图
     */
    private volatile boolean isRequestNext = false;

    public TLView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TLView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TLView(Context context) {
        this(context, null);
    }

    private int mDeviceWidth;
    private static final String TAG_LEFT_THUMBNAIL = "tag_left_thumbnail";
    private static final String TAG_LEFT_IMAGEVIEW = "tag_left_image_view";
    private static final String TAG_LEFT_PROG = "tag_left_progress";
    private static final String TAG_LEFT_TIME = "tag_left_time";

    private static final String TAG_RIGHT_THUMBNAIL = "tag_right_thumbnail";
    private static final String TAG_RIGHT_IMAGEVIEW = "tag_right_image_view";
    private static final String TAG_RIGHT_PROG = "tag_right_progress";
    private static final String TAG_RIGHT_TIME = "tag_right_time";
    private void init() {
        mDensity = getResources().getDisplayMetrics().density;

        final Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point deviceDisplay = new Point();
        display.getSize(deviceDisplay);
        mDeviceWidth = deviceDisplay.x;
/*
        //Dbug.i(tag, "=====init======mDensity=" + mDensity + ", width=" + getWidth() + ", mDeviceWidth=" + mDeviceWidth + ",deviceDisplay.y="+ deviceDisplay.y);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View leftView = layoutInflater.inflate(R.layout.left_area_thumbnail, null, false);
        leftView.setTag(TAG_LEFT_THUMBNAIL);
        ProgressBar leftProgressBar = (ProgressBar) leftView.findViewById(R.id.left_progress);
        leftProgressBar.setTag(TAG_LEFT_PROG);
        ImageView leftImageView = (ImageView) leftView.findViewById(R.id.left_image);
        leftImageView.setTag(TAG_LEFT_IMAGEVIEW);
        TextView leftTimeView = (TextView) leftView.findViewById(R.id.left_time);
        leftTimeView.setTag(TAG_LEFT_TIME);

        leftView.setY(deviceDisplay.y - 240 * mDensity);

        View rightView = layoutInflater.inflate(R.layout.right_area_thumbnail, null, false);
        rightView.setTag(TAG_RIGHT_THUMBNAIL);
        ProgressBar rightProgressBar = (ProgressBar) rightView.findViewById(R.id.right_progress);
        rightProgressBar.setTag(TAG_RIGHT_PROG);
        ImageView rightImageView = (ImageView) rightView.findViewById(R.id.right_image);
        rightImageView.setTag(TAG_RIGHT_IMAGEVIEW);
        TextView rightTimeView = (TextView) rightView.findViewById(R.id.right_time);
        rightTimeView.setTag(TAG_RIGHT_TIME);

        rightView.setY(deviceDisplay.y - 240 * mDensity);

        addView(leftView);
        addView(rightView);
*/
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStrokeWidth(1f);
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(getResources().getColor(R.color.half_transparent));

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(15 * mDensity);

        dateAndTimePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        dateAndTimePaint.setColor(Color.WHITE);
        dateAndTimePaint.setTextSize(15 * mDensity);

        mSelectedPaint = new Paint();
        mSelectedPaint.setStrokeWidth(1f);
        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setColor(getResources().getColor(R.color.half_transparent));

        eventPaint = new Paint();
        eventPaint.setStrokeWidth(1f);
        eventPaint.setAntiAlias(true);
        eventPaint.setColor(Color.GREEN);

        middlePaint = new Paint();
        middlePaint.setStrokeWidth(1f);
        middlePaint.setAntiAlias(true);
        middlePaint.setColor(Color.WHITE);

        blackline = new Paint();
        blackline.setColor(Color.WHITE);
        blackline.setStrokeWidth(1f);
        blackline.setAntiAlias(true);
        blackline.setStyle(Style.STROKE);

        blacktext30 = new TextPaint();
        blacktext30.setColor(Color.WHITE);
        blacktext30.setStrokeWidth(1f);
        blacktext30.setAntiAlias(true);
        blacktext30.setTextSize(10 * mDensity);

        mGrayLine = new Paint();
        mGrayLine.setColor(Color.GRAY);
        mGrayLine.setStrokeWidth(1f);
        mGrayLine.setAntiAlias(true);
        mGrayLine.setStyle(Style.STROKE);

        mSosPaint = new Paint();
        mSosPaint.setStrokeWidth(1f);
        mSosPaint.setAntiAlias(true);
        mSosPaint.setColor(Color.RED);

        mCapturePaint = new Paint();
        mCapturePaint.setStrokeWidth(1f);
        mCapturePaint.setAntiAlias(true);
        mCapturePaint.setColor(Color.BLUE);

        mDelayPaint = new Paint();
        mDelayPaint.setStrokeWidth(1f);
        mDelayPaint.setAntiAlias(true);
        mDelayPaint.setColor(Color.YELLOW);

        acalendar = new GregorianCalendar();

        // start the view off somewhere, +/- some time around now, and set default display unit
        left = System.currentTimeMillis() - HOUR_MILLIS;
        right = System.currentTimeMillis();// + HOUR_MILLIS;
        span = right - left;
        mFinger1x = mDeviceWidth/2 - 200.0f;
        mFinger2x = mDeviceWidth/2 + 200.0f;
        setSelectionMode(isSelectionMode);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        width = getWidth();
        mHeight = getHeight();
        //Dbug.i(tag, "onlayout====" + mHeight + ", mDensity=" + mDensity);

        final int count = getChildCount();
        int curWidth, curHeight, curLeft, curTop, maxHeight;

        //get the available size of child view
        final int childLeft = this.getPaddingLeft();
        final int childTop = this.getPaddingTop();
        final int childRight = this.getMeasuredWidth() - this.getPaddingRight();
        final int childBottom = this.getMeasuredHeight() - this.getPaddingBottom();
        final int childWidth = childRight - childLeft;
        final int childHeight = childBottom - childTop;

        maxHeight = 0;
        curLeft = childLeft;
        curTop = childTop;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                return;

            //Get the maximum size of the child
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));
            curWidth = child.getMeasuredWidth();
            curHeight = child.getMeasuredHeight();
            //wrap is reach to the end
            if (curLeft + curWidth >= childRight) {
                curLeft = childLeft;
                curTop += maxHeight;
                maxHeight = 0;
            }
            //do the layout
            child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight);
            //store the max height
            if (maxHeight < curHeight)
                maxHeight = curHeight;
            curLeft += curWidth;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Dbug.i(tag, "========onMeasure==========width="+getWidth() + ", mDeviceWidth="+ mDeviceWidth);
        int count = getChildCount();
        // Measurement will ultimately be computing these values.
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        int mLeftWidth = 0;
        int rowCount = 0;

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            // Measure the child.
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            maxWidth += Math.max(maxWidth, child.getMeasuredWidth());
            mLeftWidth += child.getMeasuredWidth();

            if ((mLeftWidth / mDeviceWidth) > rowCount) {
                maxHeight += child.getMeasuredHeight();
                rowCount++;
            } else {
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            }
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Report our final dimensions.
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    private static final int MSG_GET_THUMBNAIL = 0x100;
    private static final int MSG_SET_TIME = MSG_GET_THUMBNAIL + 2;
    static final SimpleDateFormat yyyy_MMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_THUMBNAIL:
                    if (mOnValueChangeListener != null) {
//                        long time = left + (right - left) / 2;
                        long time = (long) msg.obj;
                        acalendar.setTimeInMillis(time);
                        Dbug.i(tag, "MSG_GET_THUMBNAIL Format time=" + yyyy_MMddHHmmss.format(time));
                        mOnValueChangeListener.onValueChangeEnd(acalendar);
                    }
                    break;
                case MSG_SET_TIME:

                    break;
            }
            return false;
        }
    });

    public long getCurrentMiddleTime() {
        long middle = left + (right-left)/2;
        if (middle > 0) {
            return middle;
        }
        return -1;
    }

    public void setTimeOffset (long offsetMilliseconds) {
        if (isMove) {
            Dbug.e(tag, "Moving..............");
            return;
        }

        if (offsetMilliseconds == 0) {
            Dbug.e(tag, "offsetMilliseconds = 0");
            return;
        }
        long last = left + (right-left)/2;
        long move = offsetMilliseconds - last;

//        Dbug.w(tag, "move =" + move + ", offsetMilliseconds=" + offsetMilliseconds + ", format move= "
//                + timeFormat.format(move) + ", format offsetMilliseconds=" + timeFormat.format(offsetMilliseconds));
        if (move >= 800 || move <= -1200) {
            left += move;
            right += move;
            postInvalidate();
        }
    }

    /**
     * 设置用于接收结果的监听器
     *
     * @param listener 回调的实现
     */
    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    /**
     * 画中间的红色指示线、阴影等。指示线两端简单的用了两个矩形代替
     *
     * @param canvas canvas
     */
    private void drawMiddleLine(Canvas canvas) {
        canvas.save();
        //canvas.drawLine(width / 2, 0, width / 2, mHeight, middlePaint);
        canvas.drawLine(width / 2, mHeight - 100 * mDensity, width / 2, mHeight, middlePaint);
        canvas.restore();
    }

    /**
     * 画日期时间的文字
     *
     * @param canvas 画布
     */
    private void drawMiddleTimeText(Canvas canvas) {
//        Log.d(tag, "drawMiddleTimeText width=" + width + ", mHeight=" + mHeight);
        // round calendar down to leftmost hour
        acalendar.setTimeInMillis(left + (right-left)/2);

//        String timeStr = date2timeStr(acalendar.getTime());
        String timeStr = timeFormat.format(left + (right - left) / 2);
        //canvas.drawText(timeStr, width / 2 + 5 * mDensity, mHeight / 2 - 35 * mDensity, dateAndTimePaint);
        canvas.drawText(timeStr, width / 2 + 5 * mDensity, (mHeight - 85 * mDensity), dateAndTimePaint);
        drawDateText(canvas);
    }

    private float textWidth = 0;
    /**画日期文本
     * @param canvas 画布
     */
    private void drawDateText(Canvas canvas) {
        String dateStr = date2DateStr(acalendar.getTime());
        textWidth = Layout.getDesiredWidth(dateStr, textPaint);
        //canvas.drawText(dateStr, width / 2 - (textWidth + 5 * mDensity), mHeight / 2 - 35 * mDensity, dateAndTimePaint);
        canvas.drawText(dateStr, width / 2 - (textWidth + 5 * mDensity), (mHeight - 85 * mDensity), dateAndTimePaint);
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public String date2DateStr(Date date) {
        return dateFormat.format(date);
    }

    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        if (isSelectionMode) {
            setLeftThumbnailVisibility(VISIBLE);
            setRightThumbnailVisibility(VISIBLE);
            mFinger1x = mDeviceWidth/2 - 200.0f;
            mFinger2x = mDeviceWidth/2 + 200.0f;

            ///Left side area
            long delta1xinmillis = (long) (mFinger1x * span / width);
            long time = left + delta1xinmillis;
            mFinger1Time = time;
            mWhichFinger = 1;
            acalendar.setTimeInMillis(time);
            isRequestNext = true;
            //EventBus.getDefault().post(acalendar);

            TextView timeText = (TextView) findViewWithTag(TAG_LEFT_TIME);
            if (timeText != null) {
                long millis = left + (long) (mFinger1x * span / width);
                String timeStr = timeFormat.format(millis);
                timeText.setText(timeStr);
            }

            timeText = (TextView) findViewWithTag(TAG_RIGHT_TIME);
            if (timeText != null) {
                long millis = left + (long) (mFinger2x * span / width);

                String timeStr = timeFormat.format(millis);
                timeText.setText(timeStr);
            }
        } else {
            setLeftThumbnailVisibility(GONE);
            setRightThumbnailVisibility(GONE);
            setLeftThumbnail(null);
            setRightThumbnail(null);
            isRequestNext = false;
        }
        postInvalidate();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setThumbnail(byte[] data) {
        Dbug.w(tag, "setThumbnail: mWhichFinger=" + mWhichFinger + ", isRequestNext=" + isRequestNext);

        switch (mWhichFinger) {
            case 1://left finger
                setLeftThumbnail(data);
                if (isRequestNext) {
                    isRequestNext = false;
                    ///Right side area
                    long delta1xinmillis = (long) (mFinger2x * span / width);
                    long time = left + delta1xinmillis;
                    mFinger2Time = time;
                    mWhichFinger = 2;
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time);
                    //EventBus.getDefault().post(calendar);
                }
                break;
            case 2://right finger
                //mFinger2Time = millisecond;
                setRightThumbnail(data);
                break;
            default:
                break;
        }
    }

    public long getInterceptionStartTime() {
        return mFinger1Time;
    }

    public long getInterceptionEndTime() {
        return mFinger2Time;
    }

    private void setLeftThumbnail(final byte[] data) {

        View view = findViewWithTag(TAG_LEFT_THUMBNAIL);
        if (view != null) {
            final ImageView v = (ImageView) view.findViewWithTag(TAG_LEFT_IMAGEVIEW);
            if (v != null) {
                if (data != null){
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inSampleSize = 2;
                    bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    //noinspection deprecation
                    bitmapOptions.inPurgeable = true;
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapOptions);
                    if (bitmap != null) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //Dbug.i(tag, "===setLeftThumbnail=========INVISIBLE=");
                                setLeftProgressVisibility(GONE);
                                v.setImageBitmap(bitmap);
                            }
                        });
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Dbug.i(tag, "=====setLeftThumbnail=======data=" + data);
                            v.setImageBitmap(null);
                            setLeftProgressVisibility(VISIBLE);
                        }
                    });
                }
            }
        }
    }

    private void setRightThumbnail(final byte[] data) {

        View view = findViewWithTag(TAG_RIGHT_THUMBNAIL);
        if (view != null) {
            final ImageView v = (ImageView) view.findViewWithTag(TAG_RIGHT_IMAGEVIEW);
            if (v != null) {
                if (data != null){
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inSampleSize = 2;
                    bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    //noinspection deprecation
                    bitmapOptions.inPurgeable = true;
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapOptions);
                    if (bitmap != null) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //Dbug.i(tag, "==setRightThumbnail==========INVISIBLE=");
                                setRightProgressVisibility(GONE);
                                v.setImageBitmap(bitmap);
                            }
                        });
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Dbug.i(tag, "====setRightThumbnail========data=" +data);
                            v.setImageBitmap(null);
                            setRightProgressVisibility(VISIBLE);
                        }
                    });
                }
            }
        }
    }

    private void setLeftProgressVisibility(int visibility) {
        View view = findViewWithTag(TAG_LEFT_THUMBNAIL);
        if (view != null) {
            //Dbug.i(tag, "======00======visibility=" + visibility);
            View v = view.findViewWithTag(TAG_LEFT_PROG);
            if (v != null) {
                //Dbug.i(tag, "====11========visibility=" + visibility);
                v.setVisibility(visibility);
            }
        }
    }

    private void setRightProgressVisibility(int visibility) {
        View view = findViewWithTag(TAG_RIGHT_THUMBNAIL);
        if (view != null) {
            View v = view.findViewWithTag(TAG_RIGHT_PROG);
            if (v != null) {
                v.setVisibility(visibility);
            }
        }
    }

    private void setLeftThumbnailVisibility(int visibility) {
        View view = findViewWithTag(TAG_LEFT_THUMBNAIL);
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private void setRightThumbnailVisibility(int visibility) {
        View view = findViewWithTag(TAG_RIGHT_THUMBNAIL);
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long next;

        // calculate span/width
        pixels_per_milli = (float) width / span;
        sec_pixels = (float) SEC_MILLIS * pixels_per_milli;
        min_pixels = (float) MIN_MILLIS * pixels_per_milli;
        hour_pixels = (float) HOUR_MILLIS * pixels_per_milli;
        day_pixels = (float) DAY_MILLIS * pixels_per_milli;

        // draw background
        canvas.drawRect(0, mHeight - 100 * mDensity, width, mHeight, mBackgroundPaint);

        // draw some events maybe
        if (mCalendarStuff != null) {
            drawEvents(canvas);
        }

        // draw finger selected area
        //if (fingers > 0)
        if (isSelectionMode())
        {
            //canvas.drawCircle(finger1x, finger1y, 60, redline);
            /*int y = (int) (mHeight - 80 * mDensity);
            float h = 28 * mDensity;
            canvas.drawBitmap(sThumbnail, finger1x, getHeight() - 150 * mDensity, null);*/
            canvas.drawLine(mFinger1x, getHeight() - 200 * mDensity, mFinger1x, getHeight(), mGrayLine);
            View view = findViewWithTag(TAG_LEFT_THUMBNAIL);
            if (view != null) {
                view.setX(mFinger1x - view.getWidth()/2);
            }

            //if (fingers > 1)
            {
                //canvas.drawCircle(finger2x, finger2y, 60, magentaline);
                canvas.drawLine(mFinger2x, getHeight() - 200 * mDensity, mFinger2x, getHeight(), mGrayLine);
                //canvas.drawBitmap(sThumbnail, finger2x, getHeight() - 150* mDensity, null);
                View rightView = findViewWithTag(TAG_RIGHT_THUMBNAIL);
                if (rightView != null) {
                    rightView.setX(mFinger2x - rightView.getWidth()/2);
                }
                int y = (int) (mHeight - 80 * mDensity);
                float h = 28 * mDensity;
                if (mFinger1x < mFinger2x){
                    canvas.drawRect(mFinger1x, y, mFinger2x, y+h, mSelectedPaint);
                } else {
                    canvas.drawRect(mFinger2x, y, mFinger1x, y+h, mSelectedPaint);
                }
            }
        }

        // draw ruler
        canvas.drawLine(0, mHeight, width, mHeight, blackline);

        //canvas.drawText("Minutes in view: " + Integer.toString((int) (width / min_pixels)), 0, getHeight() - 80, redtext20);
        //canvas.drawText("Seconds width: " + Float.toString(sec_pixels), 0, getHeight() - 120* mDensity, eventPaint);
        //canvas.drawText("Minutes width: " + Float.toString(min_pixels), 0, getHeight() - 100* mDensity, eventPaint);
        //canvas.drawText("Hour width: " + Float.toString(hour_pixels), 0, getHeight() - 80* mDensity, eventPaint);
        //canvas.drawText("Day width: " + Float.toString(day_pixels), 0, getHeight() - 60* mDensity, eventPaint);
        //canvas.drawText("day > width: " + Boolean.toString(width < day_pixels), 0, getHeight() - 40* mDensity, eventPaint);

        //画刻度尺中间的日期和时间文本
        //drawMiddleTimeText(canvas);

        // round calendar down to leftmost hour
        acalendar.setTimeInMillis(left);
        // floor the calendar to various time units to find where (in ms) they start
        acalendar.set(Calendar.MILLISECOND, 0); // second start
        acalendar.set(Calendar.SECOND, 0); // minute start
        acalendar.set(Calendar.MINUTE, 0); // hour start

//        Log.w(tag, "date=" + date2DateStr(acalendar.getTime()) + ", time=" + date2timeStr(acalendar.getTime()));
        // draw seconds
        if (sec_pixels > 1f) {
            int theSecondOfMinute = acalendar.get(Calendar.SECOND);
            next = acalendar.getTimeInMillis(); // set to start of leftmost hour
            for (long i = next; i < right; i += SEC_MILLIS) {
                float x = ((float) (i - left) / span * width);
                int second = theSecondOfMinute % 60;
                if (sec_pixels < 5f) {
                    if (second == 0 || second == 30) {
                        canvas.drawLine(x, mHeight - 7.5f * mDensity, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(second), x, mHeight - 5 * mDensity, blacktext30);
                    }
                } else if (sec_pixels < 10f) {
                    if (second % 15 == 0) {
                        canvas.drawLine(x, mHeight - 5 * mDensity, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(second), x, mHeight - 5 * mDensity, blacktext30);
                    }
                } else if (sec_pixels < 20f) {
                    if (second == 10 || second == 20 || second == 40 || second == 50) {
                        canvas.drawLine(x, mHeight - 5 * mDensity, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(second), x, mHeight - 5 * mDensity, blacktext30);
                    } else if (second == 0 || second == 30) {
                        canvas.drawLine(x, mHeight - 7.5f * mDensity, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(second), x, mHeight - 5 * mDensity, blacktext30);
                    }
                } else if (sec_pixels < 25f) {
                    if (second % 5 == 0) {
                        canvas.drawLine(x, mHeight - 5 * mDensity, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(second), x, mHeight - 5 * mDensity, blacktext30);
                    }
                } else {
                    if (second % 5 == 0) {
                        canvas.drawLine(x, mHeight - 5 * mDensity, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(second), x, mHeight - 5 * mDensity, blacktext30);
                    } else {
                        canvas.drawLine(x, mHeight - 5 * mDensity, x, mHeight, blackline);
                    }
                }
                theSecondOfMinute ++;
            }
        }
        // draw minutes
        if (min_pixels > 6f) {
            int theMinuteOfHour = acalendar.get(Calendar.MINUTE);
            next = acalendar.getTimeInMillis(); // set to start of leftmost hour
            for (long i = next; i < right; i += MIN_MILLIS) {
                float x = ((float) (i - left) / span * width);
                float y = mHeight - 5 * mDensity;
                int minute = theMinuteOfHour % 60;
                if (min_pixels < 10f) {
                    if (minute % 15==0) {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(minute), x, y, blacktext30);
                    }
                } else if (min_pixels < 25f) {
                    if (minute % 10 == 0) {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(minute), x, y, blacktext30);
                    }
                } else if (min_pixels < 60) {
                    if (minute % 5 == 0) {
                        canvas.drawLine(x,y, x, mHeight, blackline);
                        canvas.drawText(Integer.toString(minute), x, y, blacktext30);
                    } else {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                    }
                }
                theMinuteOfHour ++;
            }
        }
        // draw hours
        if (hour_pixels > 2) {
            int thehourofday = acalendar.get(Calendar.HOUR_OF_DAY);
            next = acalendar.getTimeInMillis(); // set to start of leftmost hour
//            Log.i(tag, "hour_pixels=" + hour_pixels + ",thehourofday=" +thehourofday + ",next=" +next);
            for (long i = next; i < right; i += HOUR_MILLIS) {
                float x = ((float) (i - left) / span * (float) width);
                float y = mHeight - 5 * mDensity;
                int h24 = thehourofday % 24;
                int h12 = thehourofday % 12;
                if (h12 == 0) h12 = 12;

                if (hour_pixels < 4) {
                    if (h24 == 12)
                        canvas.drawLine(x, y, x, mHeight, blackline);
                    if (h12 == 6)
                        canvas.drawLine(x, y, x, mHeight, blackline);

                } else if (hour_pixels < 20) {
                    if (h24 == 12)
                        canvas.drawLine(x, y, x, mHeight, blackline);
                    else if (h12 == 6)
                        canvas.drawLine(x, y, x, mHeight, blackline);
                    else if ((h12 == 3) || (h12 == 9))
                        canvas.drawLine(x, mHeight - 2.5f * mDensity, x, mHeight, blackline);

                } else if (hour_pixels < 60) {
                    if (h24 == 12) {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                        drawHourText(canvas, x, y, h24, h12);
                    } else if (h12 == 6) {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                        drawHourText(canvas, x, y, h24, h12);
                    } else if ((h12 == 3) || (h12 == 9)) {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                    } else
                        canvas.drawLine(x, mHeight - 2.5f * mDensity, x, mHeight, blackline);

                } else if (hour_pixels < 360) {
                    if (h24 == 12) {
                        canvas.drawLine(x, mHeight - 7.5f * mDensity, x, mHeight, blackline);
                    } else if (h12 == 6) {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                    } else if ((h12 == 3) || (h12 == 9)) {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                    } else {
                        canvas.drawLine(x, y, x, mHeight, blackline);
                    }
                    drawHourText(canvas, x, y, h24, h12);
                }

                thehourofday++;
            }
        } // done drawing hours

        // draw months
        // round calendar down to leftmost month
        acalendar.set(Calendar.HOUR_OF_DAY, 0); // day start
        acalendar.set(Calendar.DAY_OF_MONTH, 1); // month start
        next = acalendar.getTimeInMillis(); // set to start of leftmost month
        do {
            // draw each month
            int monthnumber = acalendar.get(Calendar.MONTH);
            int daysthismonth = acalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            String monthnamelong = acalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String monthnameshort = acalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
            int daynumber = acalendar.get(Calendar.DAY_OF_WEEK);

            long daymsx = next; // first day starts at start of month
            int x = (int) ((daymsx - left) / span * width); // convert to pixels
            /**each month scale*/
            canvas.drawLine(x, mHeight - 20 * mDensity, x, mHeight, blackline);

            ///draw each year
            if (monthnumber == 0) {
//                canvas.drawLine(x, mHeight - 130, x, mHeight - 110, blackline);
                canvas.drawLine(x, mHeight - 40 * mDensity, x, mHeight - 30 * mDensity, blackline);
                String year = Integer.toString(acalendar.get(Calendar.YEAR));
                textWidth = Layout.getDesiredWidth(year, blacktext30);
                int textX = (int) (x - textWidth / 2);
//                canvas.drawText(year, x + 8, mHeight - 167, blacktext30);
                canvas.drawText(year, textX, mHeight - 40 * mDensity, blacktext30);
            }

            // draw month names
            if (day_pixels < 1) {
                if ((monthnumber+1) % 6 == 0) {
                    textWidth = Layout.getDesiredWidth(monthnameshort, blacktext30);
                    // short month name
                    canvas.drawText(monthnameshort, x - textWidth/2, mHeight - 20 * mDensity, blacktext30);
                }
            } else if (day_pixels < 2) {
                if ((monthnumber+1) % 3 == 0) {
                    textWidth = Layout.getDesiredWidth(monthnameshort, blacktext30);
                    // short month name
                    canvas.drawText(monthnameshort, x - textWidth/2, mHeight - 20 * mDensity, blacktext30);
                }
            } else if (day_pixels < 5) {
                textWidth = Layout.getDesiredWidth(monthnameshort, blacktext30);
                // short month name
                canvas.drawText(monthnameshort, x - textWidth/2, mHeight - 20 * mDensity, blacktext30);
            } else {
                textWidth = Layout.getDesiredWidth(monthnamelong, blacktext30);
                // long month name
                canvas.drawText(monthnamelong, x - textWidth/2, mHeight - 20 * mDensity, blacktext30);
            }

            // draw days, weeks
            for (int date = 1; date <= daysthismonth; date++, daynumber++, daymsx += DAY_MILLIS) {
                x = (int) ((daymsx - left) / span * width);

                if (daynumber == 7) {
                    daynumber = 0;
                }

                if (day_pixels < 20 * mDensity) {
                    // tiny days
                    canvas.drawLine(x, mHeight - 15 * mDensity, x, mHeight, blackline);

                } else if (day_pixels > 170) {
                    // big days
                    canvas.drawLine(x, mHeight - 15 * mDensity, x, mHeight, blackline);
                    canvas.drawText(Integer.toString(date), x + 5 * mDensity, mHeight - 15 * mDensity, blacktext30);

                } else {
                    // sideways days
                    canvas.drawLine(x, mHeight - 15 * mDensity, x, mHeight, blackline);
                    canvas.save();
                    canvas.rotate(-90, x, mHeight);
                    canvas.drawText(Integer.toString(date), x + 5 * mDensity, mHeight + 15 * mDensity, blacktext30);
                    canvas.restore();
                }
            }

            acalendar.add(Calendar.MONTH, 1);
            next = acalendar.getTimeInMillis();
        } while (next < right);
        // done drawing months

        //drawMiddleLine(canvas);
        canvas.drawLine(finger1x, 0, finger1x, mHeight, middlePaint);
    }

    /**draw some events maybe
     * @param canvas canvas
     */
    private void drawEvents(Canvas canvas) {
//        Dbug.e(tag, "drawEvents==" + calstuff.size());
        for (FileInfo videoInfo : mCalendarStuff) {
            if(videoInfo == null) continue;
            long startTime = videoInfo.getStartTime().getTimeInMillis();
            long endTime = videoInfo.getEndTime().getTimeInMillis();
//                Dbug.w(tag, "(startTime > right)==" + (startTime > right)+", endTime < left =" + (endTime < left)
//                        + ", start time=" + videoInfo.getStartTime().getTime().toString()
//                        + ", end time=" + videoInfo.getEndTime().getTime().toString()
//                        + ", start date=" + date2DateStr(videoInfo.getStartTime().getTime())
//                        + ", end date=" + date2DateStr(videoInfo.getEndTime().getTime()));
            if ((endTime < left) || (startTime > right)) {
                continue;
            }
//            Dbug.e(tag, "drawEvents date=" + date2DateStr(e.getStartTime().getTime()));
            float x = (startTime - left) / span * width;
            float x2 = (endTime - left) / span * width;

            //int y = mHeight / 2 - (int)(30f * mDensity);
            int y = (int) (mHeight - 80 * mDensity);
            float h = 28 * mDensity;
            int videoType = videoInfo.getType();
            if ((x2 - x) > (mDensity *2)) {  //Video painting spacing greater than 2 densities (1 unit)
                //Dbug.i(tag, "x=" + x + ", x2=" + x2 + ", x2-x=" + (x2-x));
                if (videoType == FILE_TYPE_SOS) { //Protected file
                    //Dbug.i(tag, "x=" + x + ", x2=" + x2 + ", x2-x=" + (x2-x) + ", start time=" + timeFormat.format(startTime)
                    //        + ", end time=" + timeFormat.format(endTime));
                    canvas.drawRect(x, y, x2, y + h, mSosPaint );
                } else if(videoType == FILE_TYPE_NORMAL){   // Normal file
                    canvas.drawRect(x, y, x2, y + h, eventPaint);
                    /*if(videoInfo.getIsCapture()){
                        List<Calendar> leftTime = videoInfo.getLeftCaptureTime();
                        List<Calendar> rightTime = videoInfo.getRightCaptureTime();
                        if(leftTime != null && rightTime != null){
                            for (int i = 0; i < leftTime.size(); i++){
                                Calendar leftCalender = leftTime.get(i);
                                Calendar rightCalender = rightTime.get(i);
                                if(leftCalender == null || rightCalender == null){
                                    continue;
                                }
                                long leftCaptureTime = leftCalender.getTimeInMillis();
                                long rightCaptureTime = rightCalender.getTimeInMillis();
                                float cx = (leftCaptureTime - left) / span * width;
                                float cx2 = (rightCaptureTime - left) / span * width;
                                canvas.drawRect(cx, y, cx2, y + h, mCapturePaint);
                            }
                        }
                    }*/
                }else if(videoType == FILE_TYPE_LATENCY){ //Delayed shooting file
                    canvas.drawRect(x, y, x2, y + h, mDelayPaint);
                }
            } else {
                if (videoType == FILE_TYPE_SOS) {  // Protected file
                    canvas.drawRect(x, y, x + mDensity*2, y + h, mSosPaint);
                } else if(videoType == FILE_TYPE_NORMAL){  // Normal file
                    canvas.drawRect(x, y, x + mDensity, y + h, eventPaint);
                    /*if(videoInfo.getIsCapture()){
                        List<Calendar> leftTime = videoInfo.getLeftCaptureTime();
                        if(leftTime != null){
                            for (int i = 0; i < leftTime.size(); i++){
                                Calendar leftCalender = leftTime.get(i);
                                if(leftCalender == null){
                                    continue;
                                }
                                long leftCaptureTime = leftCalender.getTimeInMillis();
                                float cx = (leftCaptureTime - left) / span * width;
                                canvas.drawRect(cx, y, cx + mDensity, y + h, mCapturePaint);
                            }
                        }
                    }*/
                }else if(videoType == FILE_TYPE_LATENCY){ //Delayed shooting file
                    canvas.drawRect(x, y, x + mDensity, y + h, mDelayPaint);
                }
            }
        }
    }

    private void drawHourText(Canvas canvas, float x, float y, int h24, int h12) {
        if (h24 < 12) {
            canvas.drawText(Integer.toString(h12), x, y, blacktext30);
        } else {
            canvas.drawText(Integer.toString(h24), x, y, blacktext30);
        }
    }

    private boolean isBuffering = false;
    public void setMovingLock(boolean isLock) {
        isBuffering = isLock;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
        if (isBuffering) {
            return super.onTouchEvent(motionevent);
        }
        switch (motionevent.getActionMasked()) {
            // First finger down, start panning
            case MotionEvent.ACTION_DOWN:

                fingers = 1; // panning mode

                // save id and coords
                finger1id = motionevent.getPointerId(motionevent.getActionIndex());
                finger1x = motionevent.getX();
                finger1y = motionevent.getY();

//                Log.d(tag, "down " + finger1x);
                invalidate(); // redraw
                return true;

            // Second finger down, start scaling
            case MotionEvent.ACTION_POINTER_DOWN:
                Dbug.d(tag, "ACTION_POINTER_DOWN=");
                if (fingers == 2) // if already tracking 2 fingers
                    break; // ignore 3rd finger
                // else fingers == 1
                fingers = 2; // scaling mode

                // save id and coords
                finger2id = motionevent.getPointerId(motionevent.getActionIndex());
                finger2x = motionevent.getX(finger2id);
                finger2y = motionevent.getY(finger2id);

                invalidate(); // redraw
                return true;

            case MotionEvent.ACTION_MOVE:
                isMove = true;

                if (fingers == 0) // if not tracking fingers as down
                    return false; // ignore move events

                float new1x,
                        new1y,
                        new2x,
                        new2y; // Hold new positions of two fingers

                // get finger 1 position
                int pointerindex = motionevent.findPointerIndex(finger1id);
                if (pointerindex == -1) // no change
                {
                    new1x = finger1x; // use values from previous frame
                    new1y = finger1y;
                } else
                // changed
                {
                    // get new values
                    new1x = motionevent.getX(pointerindex);
                    new1y = motionevent.getY(pointerindex);
                }

                // get finger 2 position
                pointerindex = motionevent.findPointerIndex(finger2id);
                if (pointerindex == -1) {
                    new2x = finger2x;
                    new2y = finger2y;
                } else {
                    new2x = motionevent.getX(pointerindex);
                    new2y = motionevent.getY(pointerindex);
//                    Log.w(tag, "new2x - finger2x=" + (new2x - finger2x));
                }
/*
                // panning
                if (fingers == 1) {
                    // how far to scroll in milliseconds to match the scroll input in pixels
                    long delta1xinmillis = (long) ((finger1x - new1x) * span / width); // (deltax)*span/width
                    // = delta-x
                    // in
                    // milliseconds
                    // pan the view
                    left += delta1xinmillis;
                    right += delta1xinmillis;
                }
                // scaling
                else if (fingers == 2) {
                    //Stop stretch
                    if (sec_pixels >= 30f) {
                        float a = Math.abs(finger2x - finger1x);
                        float b = Math.abs(new2x - new1x);
                        int c = (int) (b- a);
                        //Log.w(tag,"left=" + left +", right=" + right + ", (b-a)=" +(c));
                        if (c > 0) {
                            return false;
                        }
                    } else if (day_pixels < 0.5f) {
                        float a = Math.abs(finger2x - finger1x);
                        float b = Math.abs(new2x - new1x);
                        int c = (int) (b- a);
                        if (c < 0) {
                            return false;
                        }
                    }
                    // don't scale if fingers too close, or past each other
                    if (Math.abs(new1x - new2x) < 10) return true;
                    if (finger1x > finger2x) if (new1x < new2x) return true;
                    if (finger1x < finger2x) if (new1x > new2x) return true;

                    // find ruler time in ms under each finger at start of move
                    // y = mx+b, b = left, span = right - left [ms]
                    double m = (double) span / (double) width; // m = span/width
                    double y1 = m * finger1x + left; // ms at finger1
                    double y2 = m * finger2x + left; // ms at finger2
                    // y values are set to the millisecond time shown at the old finger1x and
                    // finger2x, using old left and right span
                    // construct a new line equation through points (new1x,y1),(new2x,y2)
                    // f(x) = y1 + (x - new1x) * (y2 - y1) / (new2x - new1x)
                    left = (long) (y1 + (0 - new1x) * (y2 - y1) / (new2x - new1x));
                    right = (long) (y1 + (width - new1x) * (y2 - y1) / (new2x - new1x));
                    span = right - left; // span of milliseconds in view
                }
*/
                // save
                finger1x = new1x;
                finger1y = new1y;
                finger2x = new2x;
                finger2y = new2y;

                invalidate(); // redraw with new left,right
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                int id = motionevent.getPointerId(motionevent.getActionIndex());

                if (id == finger1id) {
                    // 1st finger went up, make 2nd finger new firstfinger and go back to panning
                    finger1id = finger2id;
                    finger1x = finger2x; // copy coords so view won't jump to other finger
                    finger1y = finger2y;
                    fingers = 1; // panning
                } else if (id == finger2id) {
                    // 2nd finger went up, just go back to panning
                    fingers = 1; // panning
                } else {
                    return false; // ignore 3rd finger ups
                }
                invalidate(); // redraw
                return true;

            case MotionEvent.ACTION_UP:// last pointer up, no more motionevents
                Dbug.i(tag, "ACTION_UP=");
                // last pointer up, no more motionevents
                fingers = 0;
                invalidate(); // redraw

                long delta1xinmillis = (long) (finger1x * span / width);

                long time = left + delta1xinmillis;
                acalendar.setTimeInMillis(time);

                if (mOnValueChangeListener != null) {
                    mOnValueChangeListener.onValueChangeEnd(acalendar);
                }
                return true;
        }
        return super.onTouchEvent(motionevent);
    }
}