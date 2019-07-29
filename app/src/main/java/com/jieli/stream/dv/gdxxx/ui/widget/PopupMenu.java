package com.jieli.stream.dv.gdxxx.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;

import java.util.HashMap;
import java.util.Map;

public class PopupMenu {

    private String tag = getClass().getSimpleName();
    private Context mContext;

    private PopupWindow popupWindow;
    private View parentView;

    private OnPopItemClickListener listener;
    private int mGravity;
    private Map<Integer, Integer> resIds = new HashMap<>();
    private View mContentView;

    public interface OnPopItemClickListener {
        void onItemClick(int level, Integer resId, int index);
    }

    public PopupMenu(Context context, Map<Integer, Integer> ids){
        if(context == null){
            Dbug.e(tag, "PopupMenu context is null!");
            return;
        }
        this.mContext = context;
        this.resIds = ids;

        mContentView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_layout, null);

        //init listView
        ListView mListView = (ListView) mContentView.findViewById(R.id.pop_list_view);
        PopupAdapter mAdapter = new PopupAdapter(mContext);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(parent != null && parent.getAdapter() != null) {
                    Integer item = (Integer) parent.getAdapter().getItem(position);
                    if(item != null && resIds != null){
                        Integer res = resIds.get(item);
                        if (listener != null) {
                            listener.onItemClick(item, res, position);
                        }
                    }
                }
                dismiss();
            }
        });
        popupWindow = new PopupWindow(mContentView, 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(mLocationLayoutListener);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void setOnPopItemClickListener(OnPopItemClickListener listener) {
        this.listener = listener;
    }

	public void showAtLocation(View parent, int gravity, int x, int y){
		if(popupWindow == null){
			Dbug.e(tag, "PopupMenu popupWindow is null!");
			return;
		}

		parentView = parent;

		popupWindow.showAtLocation(parent, gravity, x, y);
		popupWindow.setFocusable(true);

		popupWindow.setOutsideTouchable(true);

		popupWindow.update();
	}

	private static Rect locateView(View v)
	{
		int[] loc_int = new int[2];
		if (v == null) return null;
		try
		{
			v.getLocationOnScreen(loc_int);
		} catch (NullPointerException npe)
		{
			//Happens when the view doesn't exist on screen anymore.
			return null;
		}
		Rect location = new Rect();
		location.left = loc_int[0] + v.getWidth();
		location.top = 0;//loc_int[1] - v.getHeight()/2;
		location.right = location.left + v.getWidth();
		location.bottom = location.top + v.getHeight();
		return location;
	}

    public void showAsDropDown(View parent){
        if(popupWindow == null){
            Dbug.e(tag, "PopupMenu popupWindow is null!");
            return;
        }

        parentView = parent;

        popupWindow.showAsDropDown(parent);

        popupWindow.setOutsideTouchable(true);

        popupWindow.update();
    }

    public void showAsUp(View parent){
        if(popupWindow == null){
            Dbug.e(tag, "PopupMenu popupWindow is null!");
            return;
        }
        mGravity = Gravity.TOP;
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
//        Dbug.w("popuaWindow", "viewWidth : " + viewWidth + " ,viewHeight : "+viewHeight
//            +'\n' + " parent width :" + parent.getWidth() + " ,parent height : " +parent.getHeight()
//            +'\n' + " parent on screen width : " +location[0] + " , parent on screen height : " + location[1]);

        parentView = parent;

        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, location[0], location[1]);

        popupWindow.setOutsideTouchable(true);
    }

    private final ViewTreeObserver.OnGlobalLayoutListener mLocationLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            final PopupWindow popup = popupWindow;
            if (popup == null) return;
            mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            PointF location = calculatePopupLocation(mGravity, popupWindow, parentView);
            popup.setClippingEnabled(true);
            popup.update((int) location.x, (int) location.y, popup.getWidth(), popup.getHeight());
            popup.getContentView().requestLayout();
            Dbug.e(tag, "x " + location.x + ", y " + location.y + ", w " + popup.getWidth() + ", h " + popup.getHeight());
        }
    };

    private PointF calculatePopupLocation(int mGravity, PopupWindow mPopupWindow, View mAnchorView) {
        PointF location = new PointF();
        int mMargin = 0;
        final RectF anchorRect = calculateRectInWindow(mAnchorView);
        final PointF anchorCenter = new PointF(anchorRect.centerX(), anchorRect.centerY());

        switch (mGravity) {
            case Gravity.START:
                location.x = anchorRect.left - mPopupWindow.getContentView().getWidth() - mMargin;
                location.y = anchorCenter.y - mPopupWindow.getContentView().getHeight() / 2f;
                break;
            case Gravity.END:
                location.x = anchorRect.right + mMargin;
                location.y = anchorCenter.y - mPopupWindow.getContentView().getHeight() / 2f;
                break;
            case Gravity.TOP:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth() / 2f;
                location.y = anchorRect.top - mPopupWindow.getContentView().getHeight() - mMargin;
                break;
            case Gravity.BOTTOM:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth() / 2f;
                location.y = anchorRect.bottom + mMargin;
                break;
            case Gravity.CENTER:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth() / 2f;
                location.y = anchorCenter.y - mPopupWindow.getContentView().getHeight() / 2f;
                break;
            default:
                throw new IllegalArgumentException("Gravity must have be CENTER, START, END, TOP or BOTTOM.");
        }

        return location;
    }

    private static RectF calculateRectInWindow(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return new RectF(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }
    public void showAsRight(View parent){
        if(popupWindow == null){
            Dbug.e(tag, "PopupMenu popupWindow is null!");
            return;
        }
        mGravity = Gravity.END;
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
//        Dbug.w("popuaWindow", "viewWidth : " + viewWidth + " ,viewHeight : "+viewHeight
//            +'\n' + " parent width :" + parent.getWidth() + " ,parent height : " +parent.getHeight()
//            +'\n' + " parent on screen width : " +location[0] + " , parent on screen height : " + location[1]);

        parentView = parent;

        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, location[0]+ AppUtils.dp2px(mContext, 8), location[1]);

        popupWindow.setOutsideTouchable(true);
    }

    public void dismiss() {
        if(popupWindow != null){
            popupWindow.dismiss();
        }
    }


    public boolean isShowing(){
        return popupWindow != null && popupWindow.isShowing();
    }

    public View getParentView(){
        return parentView;
    }

    private class PopupAdapter extends BaseAdapter {
        private Context mContext;

        public PopupAdapter(Context context){
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return resIds == null ? 0 : resIds.size();
        }

        @Override
        public Object getItem(int position) {
            Integer item = null;
            if(resIds != null){
                Integer[] keys = resIds.keySet().toArray(new Integer[resIds.size()]);
                if(position < keys.length){
                    item = keys[position];
                }
            }
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.item_image);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Integer item = (Integer) getItem(position);
            if(item != null){
                int resId = 0;
                if(resIds != null){
                    Integer id = resIds.get(item);
                    if(null != id){
                        resId = id;
                    }
                }
                if(resId != 0){
                    viewHolder.imageView.setImageResource(resId);
                }
            }

            return convertView;
        }

        private final class ViewHolder {
            ImageView imageView;
        }
    }

}
