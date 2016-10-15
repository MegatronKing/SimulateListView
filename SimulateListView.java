package com.github.megatronking.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 正常的ListView在ScrollView中会出现性能问题(getView多次调用)。 此类使用LinearLayout模拟ListView,具备基本的ListView特性<br>
 * <p>
 * 3.0及以上版本支持分割线
 * </p>
 * <p>
 * 使用Orientation属性设置ListView方向，默认Horizonal
 * </p>
 * 
 * @author Megatron King
 * @since 2014-12-19 下午2:35:49
 */
public class SimulateListView extends LinearLayout {

    /**
     * Represents an invalid position. All valid positions are in the range 0 to 1 less than the
     * number of items in the current adapter.
     */
    public static final int INVALID_POSITION = -1;

    /**
     * Represents an empty or invalid row id
     */
    public static final long INVALID_ROW_ID = Long.MIN_VALUE;

    /**
     * Should be used by subclasses to listen to changes in the dataset
     */
    private AdapterDataSetObserver mDataSetObserver;

    /**
     * The listener that receives notifications when an item is clicked.
     */
    private OnItemClickListener mOnItemClickListener;

    /**
     * The listener that receives notifications when an item is long clicked.
     */
    private OnItemLongClickListener mOnItemLongClickListener;

    /**
     * The stored views for recycle
     */
    private View[] mStoredViews;

    private ListAdapter mAdapter;

    private GestureDetector mGestureDetector;

    /**
     * Rectangle used for hit testing children
     */
    private Rect mTouchFrame;

    private boolean mInLayout;

    private ArrayList<View> mHeaderViews = new ArrayList<View>();
    private ArrayList<View> mFooterViews = new ArrayList<View>();

    public SimulateListView(Context context) {
        this(context, null);
    }

    public SimulateListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimulateListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context, new OnClickGestureListener());
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;
        super.onLayout(changed, l, t, r, b);
        mInLayout = false;
    }

    /**
     * Sets the data behind this ListView.
     * 
     * @param adapter The ListAdapter which is responsible for maintaining the data backing this
     *            list and for producing a view to represent an item in that data set.
     * @see #getAdapter()
     */
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            throw new UnsupportedOperationException("The adpater of SimulateListView has been attached!");
        }

        mAdapter = adapter;

        if (mAdapter != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);

            mStoredViews = new View[mAdapter.getCount()];

            layoutViews();
        }
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     * 
     * @param child Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(View child) {
        throw new UnsupportedOperationException("addView(View) is not supported in SimulateListView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     * 
     * @param child Ignored.
     * @param index Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(View child, int index) {
        throw new UnsupportedOperationException("addView(View, int) is not supported in SimulateListView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     * 
     * @param child Ignored.
     * @param params Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, LayoutParams) " + "is not supported in SimulateListView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     * 
     * @param child Ignored.
     * @param index Ignored.
     * @param params Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, int, LayoutParams) "
                + "is not supported in SimulateListView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     * 
     * @param child Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeView(View child) {
        throw new UnsupportedOperationException("removeView(View) is not supported in SimulateListView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     * 
     * @param index Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported in SimulateListView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     * 
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported in SimulateListView");
    }

    /**
     * Register a callback to be invoked when an item in this View has been clicked.
     * 
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an item in this SimulateListView has been clicked and
     * held
     * 
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnItemLongClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this View has been clicked, or null id no
     *         callback has been set.
     */
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /**
     * @return The callback to be invoked with an item in this SimulateListView has been clicked and
     *         held, or null id no callback as been set.
     */
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    /**
     * Maps a point to a position in the list.
     * 
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an item.
     */
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return i;
                }
            }
        }
        return INVALID_POSITION;
    }

    /**
     * Sets the currently selected item. If the specified selection position is less than 0, then
     * the item at position 0 will be selected.
     * <p>
     * Only supported when the view is the only child of {@link android.widget.ScrollView} or
     * {@link android.widget.HorizontalScrollView}
     * </p>
     *
     * @param position Index (starting at 0) of the data item to be selected.
     */
    public void setSelection(int position) {
        if (((ViewGroup) getParent()).getChildCount() > 1) {
            return;
        }

        if (position >= getChildCount()) {
            return;
        }

        if (position < 0) {
            position = 0;
        }

        if (getParent() instanceof ScrollView && getOrientation() == VERTICAL) {
            scrollToPositionVertical(position);
        } else if (getParent() instanceof HorizontalScrollView && getOrientation() == HORIZONTAL) {
            scrollToPositionHorizonal(position);
        }
    }

    /**
     * Sets the end selected item. If the specified selection position is less than 0, then the item
     * at position 0 will be selected.
     * <p>
     * Only supported when the view is the only child of {@link android.widget.ScrollView} or
     * {@link android.widget.HorizontalScrollView}
     * </p>
     *
     * @param position Index (starting at 0) of the data item to be selected.
     */
    public void setSelectionEnd(int position) {
        if (((ViewGroup) getParent()).getChildCount() > 1) {
            return;
        }

        if (position >= getChildCount()) {
            return;
        }

        if (position < 0) {
            position = 0;
        }

        if (getParent() instanceof ScrollView && getOrientation() == VERTICAL) {
            scrollToEndPositionVertical(position);
        } else if (getParent() instanceof HorizontalScrollView && getOrientation() == HORIZONTAL) {
            scrollToEndPositionHorizonal(position);
        }

    }

    /**
     * Sets the appointed item visible. <br>
     * If the item is at left, will call {@linkplain #setSelection(int)} , else will call
     * {@linkplain #setSelectionEnd(int)}. <br>
     * If the specified selection position is less than 0, then the item at position 0 will be
     * selected.
     * <p>
     * Only supported when the view is the only child of {@link android.widget.ScrollView} or
     * {@link android.widget.HorizontalScrollView}
     * </p>
     *
     * @param position Index (starting at 0) of the data item to be selected.
     */
    public void setSelectionAutoVisible(int position) {
        if (((ViewGroup) getParent()).getChildCount() > 1) {
            return;
        }

        if (position >= getChildCount()) {
            return;
        }

        if (position < 0) {
            position = 0;
        }

        View itemView = getChildAt(position);
        int scrollPoint = 0;
        if (getParent() instanceof ScrollView && getOrientation() == VERTICAL) {
            // ignore at both side
            if (itemView.getHeight() > ((ViewGroup) getParent()).getHeight()) {
                return;
            }
            scrollPoint = ((ViewGroup) getParent()).getScrollY();
            // check the item is at top or bottom
            if (itemView.getTop() < scrollPoint) {
                scrollToPositionVertical(position);
            } else if (itemView.getBottom() > scrollPoint + ((ViewGroup) getParent()).getHeight()) {
                scrollToEndPositionVertical(position);
            }
        } else if (getParent() instanceof HorizontalScrollView && getOrientation() == HORIZONTAL) {
            // ignore at both side
            if (itemView.getWidth() > ((ViewGroup) getParent()).getWidth()) {
                return;
            }
            scrollPoint = ((ViewGroup) getParent()).getScrollX();
            // check the item is at left or right
            if (itemView.getLeft() < scrollPoint) {
                scrollToPositionHorizonal(position);
            } else if (itemView.getRight() > scrollPoint + ((ViewGroup) getParent()).getWidth()) {
                scrollToEndPositionHorizonal(position);
            }
        }
    }

    /**
     * Gets the data associated with the specified position in the list.
     *
     * @param position Which data to get
     * @return The data associated with the specified position in the list
     */
    public Object getItemAtPosition(int position) {
        return mAdapter == null || position < 0 ? null : mAdapter.getItem(position);
    }

    public long getItemIdAtPosition(int position) {
        return mAdapter == null || position < 0 ? INVALID_ROW_ID : mAdapter.getItemId(position);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new RuntimeException("Don't call setOnClickListener for an SimulateListView. "
                + "You probably want setOnItemClickListener instead");
    }

    /**
     * Returns the adapter currently associated with this widget.
     *
     * @return The adapter used to provide this view's content.
     */
    public ListAdapter getAdapter() {
        return mAdapter;
    };

    /**
     * Add a fixed view to appear at the top of the list. If addHeaderView is called more than once,
     * the views will appear in the order they were added. Views added using this call can take
     * focus if they want.
     * <p>
     * Note: When first introduced, this method could only be called before setting the adapter with
     * {@link #setAdapter(android.widget.ListAdapter)}.
     *
     * @param v The view to add.
     */
    public void addHeaderView(View v) {
        if (v == null) {
            return;
        }
        mHeaderViews.add(v);
    }

    public int getHeaderViewsCount() {
        return mHeaderViews.size();
    }

    /**
     * Removes a previously-added header view.
     *
     * @param v The view to remove
     * @return true if the view was removed, false if the view was not a header view
     */
    public boolean removeHeaderView(View v) {
        if (mHeaderViews.size() > 0 && mHeaderViews.contains(v)) {
            if (mAdapter != null) {
                removeViewInLayout(v);
            }
            return mHeaderViews.remove(v);
        }
        return false;
    }

    /**
     * Add a fixed view to appear at the bottom of the list. If addFooterView is called more than
     * once, the views will appear in the order they were added. Views added using this call can
     * take focus if they want.
     * <p>
     * Note: When first introduced, this method could only be called before setting the adapter with
     * {@link #setAdapter(android.widget.ListAdapter)}.
     *
     * @param v The view to add.
     */
    public void addFooterView(View v) {
        if (v == null) {
            return;
        }
        mFooterViews.add(v);
    }

    public int getFooterViewsCount() {
        return mFooterViews.size();
    }

    /**
     * Removes a previously-added footer view.
     *
     * @param v The view to remove
     * @return true if the view was removed, false if the view was not a footer view
     */
    public boolean removeFooterView(View v) {
        if (mFooterViews.size() > 0 && mFooterViews.contains(v)) {
            if (mAdapter != null) {
                removeViewInLayout(v);
            }
            return mFooterViews.remove(v);
        }
        return false;
    }

    /**
     * Set dividers between items include headers and footers. Only support the API version higher
     * than 3.0.
     *
     * @param shapeResId
     */
    public void setDivider(int shapeResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.setDividerDrawable(getResources().getDrawable(shapeResId));
            super.setShowDividers(SHOW_DIVIDER_MIDDLE);
        }
    }

    @Override
    public void setDividerPadding(int padding) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.setDividerPadding(padding);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mOnItemClickListener == null && mOnItemLongClickListener == null ? super.onTouchEvent(event)
                                                                               : mGestureDetector.onTouchEvent(event);
    }

    private void layoutViews() {
        requestLayout();
        invalidate();

        // add header views
        for (int i = 0; i < mHeaderViews.size(); i++) {
            View header = mHeaderViews.get(i);
            LayoutParams lp = (LayoutParams) header.getLayoutParams();
            if (lp == null) {
                lp = generateDefaultLayoutParams();
            }
            addViewInLayout(header, i, lp, false);
        }

        // add item views
        for (int position = 0; position < mStoredViews.length; position++) {
            makeAndAddView(position);
        }

        // add footer views
        for (int i = 0; i < mFooterViews.size(); i++) {
            View footer = mFooterViews.get(i);
            LayoutParams lp = (LayoutParams) footer.getLayoutParams();
            if (lp == null) {
                lp = generateDefaultLayoutParams();
            }
            addViewInLayout(footer, i + mHeaderViews.size() + mStoredViews.length, lp, false);
        }

    }

    private void makeAndAddView(int position) {

        View child = mAdapter.getView(position, mStoredViews[position], this);
        if (child == null) {
            throw new IllegalArgumentException("The adapter of SimulateListView must be not return a view not null!");
        }

        if (mStoredViews[position] != child) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp == null) {
                lp = generateDefaultLayoutParams();
            }

            if (mStoredViews[position] != null) {
                removeViewInLayout(mStoredViews[position]);
            }
            addViewInLayout(child, mHeaderViews.size() + position, lp, false);

            // save item view for recycle
            mStoredViews[position] = child;
        }

    }

    private void scrollToPositionHorizonal(int position) {
        View itemView = getChildAt(position);
        ((ViewGroup) getParent()).scrollTo(itemView.getLeft(), 0);
    }

    private void scrollToPositionVertical(int position) {
        View itemView = getChildAt(position);
        ((ViewGroup) getParent()).scrollTo(0, itemView.getTop());
    }

    private void scrollToEndPositionHorizonal(int position) {
        View itemView = getChildAt(position);
        ((ViewGroup) getParent()).scrollBy(itemView.getRight() - ((ViewGroup) getParent()).getWidth()
                - ((ViewGroup) getParent()).getScrollX(), 0);
    }

    private void scrollToEndPositionVertical(int position) {
        View itemView = getChildAt(position);
        ((ViewGroup) getParent()).scrollBy(0, itemView.getBottom() - ((ViewGroup) getParent()).getHeight()
                - ((ViewGroup) getParent()).getScrollY());
    }

    private class OnClickGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mOnItemClickListener == null) {
                return super.onSingleTapUp(e);
            }

            int position = pointToPosition((int) e.getX(), (int) e.getY());
            if (position != INVALID_POSITION) {
                mOnItemClickListener.onItemClick(
                        SimulateListView.this,
                        getChildAt(position),
                        position,
                        mAdapter.getItemId(position));
                return true;
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mOnItemLongClickListener == null) {
                return;
            }

            int position = pointToPosition((int) e.getX(), (int) e.getY());
            if (position != INVALID_POSITION) {
                mOnItemLongClickListener.onItemLongClick(
                        SimulateListView.this,
                        getChildAt(position),
                        position,
                        mAdapter.getItemId(position));
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    private class AdapterDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            // remove header and footer first
            for (View header : mHeaderViews) {
                removeViewInLayout(header);
            }
            for (View footer : mFooterViews) {
                removeViewInLayout(footer);
            }

            int newCount = mAdapter.getCount();
            int oldCount = mStoredViews.length;
            if (newCount < oldCount) {
                removeViewsInLayout(newCount, oldCount - newCount);
                mStoredViews = Arrays.copyOfRange(mStoredViews, 0, newCount);
            } else if (newCount > oldCount) {
                View[] temp = new View[newCount];
                System.arraycopy(mStoredViews, 0, temp, 0, oldCount);
                mStoredViews = temp;
            }
            layoutViews();
        }
    }

    /**
     * Interface definition for a callback to be invoked when an item in this SimulateListView has
     * been clicked.
     */
    public interface OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this SimulateListView has been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need to access the data
         * associated with the selected item.
         * 
         * @param parent The SimulateListView where the click happened.
         * @param view The view within the SimulateListView that was clicked (this will be a view
         *            provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id The row id of the item that was clicked.
         */
        void onItemClick(SimulateListView parent, View view, int position, long id);
    }

    /**
     * Interface definition for a callback to be invoked when an item in this view has been clicked
     * and held.
     */
    public interface OnItemLongClickListener {

        /**
         * Callback method to be invoked when an item in this view has been clicked and held.
         * Implementers can call getItemAtPosition(position) if they need to access the data
         * associated with the selected item.
         * 
         * @param parent The SimulateListView where the click happened
         * @param view The view within the SimulateListView that was clicked
         * @param position The position of the view in the list
         * @param id The row id of the item that was clicked
         */
        void onItemLongClick(SimulateListView parent, View view, int position, long id);
    }
}
