/*
 * Copyright (C) 2011 Daniel Berndt - Codeus Ltd  -  DateSlider
 * 
 * This class contains all the scrolling logic of the slidable elements
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.android.widgets.DateSlider;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class ScrollLayout extends LinearLayout {

    // private static String TAG = "SCROLLLAYOUT";

    private final Scroller     mScroller;
    private boolean            mDragMode;
    private int                mLastX, mLastScroll, mFirstElemOffset, childrenWidth, mScrollX;
    private VelocityTracker    mVelocityTracker;
    private final int          mMinimumVelocity, mMaximumVelocity;

    private int                mInitialOffset;
    private long               currentTime;
    private int                objWidth, objHeight;

    private DateSlider.Labeler labeler;
    private OnScrollListener   listener;
    private TimeView           mCenterView;

    public ScrollLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.mScroller = new Scroller(getContext());
        setGravity(Gravity.CENTER_VERTICAL);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        // as mMaximumVelocity does not exist in API<4
        final float density = getContext().getResources().getDisplayMetrics().density;
        this.mMaximumVelocity = (int) (4000 * 0.5f * density);
    }

    /**
     * scroll the element when the mScroller is still scrolling
     */
    @Override
    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            this.mScrollX = this.mScroller.getCurrX();
            reScrollTo(this.mScrollX, 0, true);
            // Keep on drawing until the animation has finished.
            postInvalidate();
        }
    }

    @Override
    public void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mInitialOffset = (this.childrenWidth - w) / 2;
        super.scrollTo(this.mInitialOffset, 0);
        this.mScrollX = this.mInitialOffset;
        setTime(this.currentTime, 0);
    }

    /**
     * finding whether to scroll or not
     */
    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        final int action = ev.getAction();
        final int x = (int) ev.getX();
        if (action == MotionEvent.ACTION_DOWN) {
            this.mDragMode = true;
            if (!this.mScroller.isFinished()) {
                this.mScroller.abortAnimation();
            }
        }

        if (!this.mDragMode) {
            return super.onTouchEvent(ev);
        }

        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                this.mScrollX += this.mLastX - x;
                reScrollTo(this.mScrollX, 0, true);
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                final int initialVelocity = (int) Math.min(velocityTracker.getXVelocity(), this.mMaximumVelocity);

                if (getChildCount() > 0 && Math.abs(initialVelocity) > this.mMinimumVelocity) {
                    fling(-initialVelocity);
                }
            case MotionEvent.ACTION_CANCEL:
            default:
                this.mDragMode = false;

        }
        this.mLastX = x;

        return true;
    }

    @Override
    public void scrollTo(final int x, final int y) {
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
        reScrollTo(x, y, true);
    }

    /**
     * This method is called usually after a ScrollLayout is instanciated, it provides the scroller with all necessary
     * information
     * 
     * @param labeler
     *            the labeler instance which will provide the ScrollLayout with time unit information
     * @param time
     *            the start time as timestamp representation
     * @param objwidth
     *            the width of an TimeTextView in dps
     * @param objheight
     *            the height of an TimeTextView in dps
     */
    public void setLabeler(final DateSlider.Labeler labeler, final long time, final int objwidth, final int objheight) {
        this.labeler = labeler;
        this.currentTime = time;
        this.objWidth = (int) (objwidth * getContext().getResources().getDisplayMetrics().density);
        this.objHeight = (int) (objheight * getContext().getResources().getDisplayMetrics().density);

        // TODO: make it not dependent on the display width but rather on the layout width
        final Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final int displayWidth = display.getWidth();
        while (displayWidth > this.childrenWidth - 0 * this.objWidth && labeler != null) {
            final LayoutParams lp = new LayoutParams(this.objWidth, this.objHeight);
            if (this.childrenWidth == 0) {
                final TimeView ttv = labeler.createView(getContext(), true);
                ttv.setVals(labeler.getElem(this.currentTime));
                addView((View) ttv, lp);
                this.mCenterView = ttv;
                this.childrenWidth += this.objWidth;
            }
            TimeView ttv = labeler.createView(getContext(), false);
            ttv.setVals(labeler.add(((TimeView) getChildAt(getChildCount() - 1)).getEndTime(), 1));
            addView((View) ttv, lp);
            ttv = labeler.createView(getContext(), false);
            ttv.setVals(labeler.add(((TimeView) getChildAt(0)).getEndTime(), -1));
            addView((View) ttv, 0, lp);
            this.childrenWidth += this.objWidth + this.objWidth;
        }
    }

    public void setOnScrollListener(final OnScrollListener l) {
        this.listener = l;
    }

    /**
     * this element will position the TimeTextViews such that they correspond to the given time
     * 
     * @param time
     * @param loops
     *            prevents setTime getting called too often, if loop is > 2 the procedure will be stopped
     */
    public void setTime(final long time, final int loops) {
        this.currentTime = time;
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
        final int pos = getChildCount() / 2;
        final TimeView currelem = (TimeView) getChildAt(pos);
        if (loops > 2 || currelem.getStartTime() <= time && currelem.getEndTime() >= time) {
            if (loops > 2) {
                // Log.d(ScrollLayout.TAG,
                // String.format("time: %d, start: %d, end: %d", time, currelem.getStartTime(),
                // currelem.getStartTime()));
                return;
            }
            final double center = getWidth() / 2.0;
            final int left = getChildCount() / 2 * this.objWidth - getScrollX();
            final double currper = (center - left) / this.objWidth;
            final double goalper = (time - currelem.getStartTime()) / (double) (currelem.getEndTime() - currelem.getStartTime());
            final int shift = (int) Math.round((currper - goalper) * this.objWidth);
            this.mScrollX -= shift;
            reScrollTo(this.mScrollX, 0, false);
        } else {
            final double diff = currelem.getEndTime() - currelem.getStartTime();
            final int steps = (int) Math.round(((time - (currelem.getStartTime() + diff / 2)) / diff));
            moveElements(-steps);
            setTime(time, loops + 1);
        }
    }

    /**
     * when the scrolling procedure causes "steps" elements to fall out of the visible layout, all TimeTextViews swap
     * their contents so that it appears that there happens an endless scrolling with a very limited amount of views
     * 
     * @param steps
     */
    protected void moveElements(final int steps) {
        if (steps < 0) {
            for (int i = 0; i < getChildCount() + steps; i++) {
                ((TimeView) getChildAt(i)).setVals((TimeView) getChildAt(i - steps));
            }
            for (int i = getChildCount() + steps; i > 0 && i < getChildCount(); i++) {
                final DateSlider.TimeObject newTo = this.labeler.add(((TimeView) getChildAt(i - 1)).getEndTime(), 1);
                ((TimeView) getChildAt(i)).setVals(newTo);
            }
            if (getChildCount() + steps <= 0) {
                for (int i = 0; i < getChildCount(); i++) {
                    final DateSlider.TimeObject newTo = this.labeler.add(((TimeView) getChildAt(i)).getEndTime(), -steps);
                    ((TimeView) getChildAt(i)).setVals(newTo);
                }
            }
        } else if (steps > 0) {
            for (int i = getChildCount() - 1; i >= steps; i--) {
                ((TimeView) getChildAt(i)).setVals((TimeView) getChildAt(i - steps));
            }
            for (int i = steps - 1; i >= 0 && i < getChildCount() - 1; i--) {
                final DateSlider.TimeObject newTo = this.labeler.add(((TimeView) getChildAt(i + 1)).getEndTime(), -1);
                ((TimeView) getChildAt(i)).setVals(newTo);
            }
            if (steps >= getChildCount()) {
                for (int i = 0; i < getChildCount(); i++) {
                    final DateSlider.TimeObject newTo = this.labeler.add(((TimeView) getChildAt(i)).getEndTime(), -steps);
                    ((TimeView) getChildAt(i)).setVals(newTo);
                }
            }
        }
    }

    /**
     * core scroll function which will replace and move TimeTextViews so that they don't get scrolled out of the layout
     * 
     * @param x
     * @param y
     * @param notify
     *            if false, the listeners won't be called
     */
    protected void reScrollTo(final int x, final int y, final boolean notify) {
        if (getChildCount() > 0) {
            this.mFirstElemOffset += x - this.mLastScroll;
            if (this.mFirstElemOffset - this.mInitialOffset > this.objWidth / 2) {
                final int stepsRight = (this.mFirstElemOffset - this.mInitialOffset + this.objWidth / 2) / this.objWidth;
                moveElements(-stepsRight);
                this.mFirstElemOffset = (this.mFirstElemOffset - this.mInitialOffset - this.objWidth / 2) % this.objWidth
                        + this.mInitialOffset - this.objWidth / 2;
            } else if (this.mInitialOffset - this.mFirstElemOffset > this.objWidth / 2) {
                final int stepsLeft = (this.mInitialOffset + this.objWidth / 2 - this.mFirstElemOffset) / this.objWidth;
                moveElements(stepsLeft);
                this.mFirstElemOffset = this.mInitialOffset + this.objWidth / 2
                        - (this.mInitialOffset + this.objWidth / 2 - this.mFirstElemOffset) % this.objWidth;
            }
        }
        super.scrollTo(this.mFirstElemOffset, y);
        if (this.listener != null && notify) {

            final double center = getWidth() / 2.0;
            final int left = getChildCount() / 2 * this.objWidth - this.mFirstElemOffset;
            final double f = (center - left) / this.objWidth;
            final long newTime = (long) (this.mCenterView.getStartTime() + (this.mCenterView.getEndTime() - this.mCenterView.getStartTime())
                    * f);
            this.listener.onScroll(newTime);
        }
        ;
        this.mLastScroll = x;
    }

    /**
     * causes the underlying mScroller to do a fling action which will be recovered in the computeScroll method
     * 
     * @param velocityX
     */
    private void fling(final int velocityX) {
        if (getChildCount() > 0) {
            this.mScroller.fling(this.mScrollX, 0, velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
            invalidate();
        }
    }

    public interface OnScrollListener {
        public void onScroll(long x);
    }
}
