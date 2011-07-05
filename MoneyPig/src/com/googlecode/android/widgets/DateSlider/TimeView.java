/*
 * Copyright (C) 2011 Daniel Berndt - Codeus Ltd  -  DateSlider
 * 
 * This interface represents Views that are put onto the ScrollLayout
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

import java.util.Calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.android.widgets.DateSlider.DateSlider.TimeObject;

/**
 * This interface represents the views that will settle the ScrollLayout. Each element has to deal with its label via
 * the setText method and needs to contain the start and end time of the element. Moreover this interface contains three
 * implementations one simple TextView, A two-row LinearLayout and a LinearLayout which colors Sundays red.
 * 
 */
public interface TimeView {

    public long getEndTime();

    public long getStartTime();

    public String getTimeText();

    public void setVals(TimeObject to);

    public void setVals(TimeView other);

    /**
     * More complex implementation of the TimeView which is based on the TimeLayoutView. Sundays are colored red in
     * here.
     * 
     */
    public static class DayTimeLayoutView extends TimeLayoutView {

        protected boolean isSunday = false;

        /**
         * Constructor
         * 
         * @param context
         * @param isCenterView
         *            true if the element is the centered view in the ScrollLayout
         * @param topTextSize
         *            text size of the top TextView in dps
         * @param bottomTextSize
         *            text size of the bottom TextView in dps
         * @param lineHeight
         *            LineHeight of the top TextView
         */
        public DayTimeLayoutView(final Context context, final boolean isCenterView, final int topTextSize, final int bottomTextSize,
                final float lineHeight) {
            super(context, isCenterView, topTextSize, bottomTextSize, lineHeight);
        }

        @Override
        public void setVals(final TimeObject to) {
            super.setVals(to);
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(to.endTime);
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && !this.isSunday) {
                this.isSunday = true;
                colorMeSunday();
            } else if (this.isSunday && c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                this.isSunday = false;
                colorMeWorkday();
            }
        }

        @Override
        public void setVals(final TimeView other) {
            super.setVals(other);
            final DayTimeLayoutView otherDay = (DayTimeLayoutView) other;
            if (otherDay.isSunday && !this.isSunday) {
                this.isSunday = true;
                colorMeSunday();
            } else if (this.isSunday && !otherDay.isSunday) {
                this.isSunday = false;
                colorMeWorkday();
            }
        }

        /**
         * this method is called when the current View takes a Sunday as time unit
         */
        protected void colorMeSunday() {
            if (this.isCenter) {
                this.bottomView.setTextColor(0xFF773333);
                this.topView.setTextColor(0xFF553333);
            } else {
                this.bottomView.setTextColor(0xFF442222);
                this.topView.setTextColor(0xFF553333);
            }
        }

        /**
         * this method is called when the current View takes no Sunday as time unit
         */
        protected void colorMeWorkday() {
            if (this.isCenter) {
                this.topView.setTextColor(0xFF333333);
                this.bottomView.setTextColor(0xFF444444);
            } else {
                this.topView.setTextColor(0xFF666666);
                this.bottomView.setTextColor(0xFF666666);
            }
        }

    }

    /**
     * This is a more complex implementation of the TimeView consisting of a LinearLayout with two TimeViews.
     * 
     */
    public static class TimeLayoutView extends LinearLayout implements TimeView {
        protected long    endTime, startTime;
        protected String  text;
        protected boolean isCenter = false;
        protected TextView topView, bottomView;

        /**
         * constructor
         * 
         * @param context
         * @param isCenterView
         *            true if the element is the centered view in the ScrollLayout
         * @param topTextSize
         *            text size of the top TextView in dps
         * @param bottomTextSize
         *            text size of the bottom TextView in dps
         * @param lineHeight
         *            LineHeight of the top TextView
         */
        public TimeLayoutView(final Context context, final boolean isCenterView, final int topTextSize, final int bottomTextSize,
                final float lineHeight) {
            super(context);
            setupView(context, isCenterView, topTextSize, bottomTextSize, lineHeight);
        }

        @Override
        public long getEndTime() {
            return this.endTime;
        }

        @Override
        public long getStartTime() {
            return this.startTime;
        }

        @Override
        public String getTimeText() {
            return this.text;
        }

        @Override
        public void setVals(final TimeObject to) {
            this.text = to.text.toString();
            setText();
            this.startTime = to.startTime;
            this.endTime = to.endTime;
        }

        @Override
        public void setVals(final TimeView other) {
            this.text = other.getTimeText().toString();
            setText();
            this.startTime = other.getStartTime();
            this.endTime = other.getEndTime();
        }

        /**
         * sets the TextView texts by splitting the text into two
         */
        protected void setText() {
            final String[] splitTime = this.text.split(" ");
            this.topView.setText(splitTime[0]);
            this.bottomView.setText(splitTime[1]);
        }

        /**
         * Setting up the top TextView and bottom TextVew
         * 
         * @param context
         * @param isCenterView
         *            true if the element is the centered view in the ScrollLayout
         * @param topTextSize
         *            text size of the top TextView in dps
         * @param bottomTextSize
         *            text size of the bottom TextView in dps
         * @param lineHeight
         *            LineHeight of the top TextView
         */
        protected void setupView(final Context context, final boolean isCenterView, final int topTextSize, final int bottomTextSize,
                final float lineHeight) {
            setOrientation(LinearLayout.VERTICAL);
            this.topView = new TextView(context);
            this.topView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            this.topView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, topTextSize);
            this.bottomView = new TextView(context);
            this.bottomView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            this.bottomView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, bottomTextSize);
            this.topView.setLineSpacing(0, lineHeight);
            if (isCenterView) {
                this.isCenter = true;
                this.topView.setTypeface(Typeface.DEFAULT_BOLD);
                this.topView.setTextColor(0xFF333333);
                this.bottomView.setTypeface(Typeface.DEFAULT_BOLD);
                this.bottomView.setTextColor(0xFF444444);
                this.topView.setPadding(0, 5 - (int) (topTextSize / 15.0), 0, 0);
            } else {
                this.topView.setPadding(0, 5, 0, 0);
                this.topView.setTextColor(0xFF666666);
                this.bottomView.setTextColor(0xFF666666);
            }
            addView(this.topView);
            addView(this.bottomView);

        }

    }

    /**
     * This is a simple implementation of a TimeView which realised through a TextView.
     * 
     */
    public static class TimeTextView extends TextView implements TimeView {
        private long endTime, startTime;

        /**
         * constructor
         * 
         * @param context
         * @param isCenterView
         *            true if the element is the centered view in the ScrollLayout
         * @param textSize
         *            text size in dps
         */
        public TimeTextView(final Context context, final boolean isCenterView, final int textSize) {
            super(context);
            setupView(isCenterView, textSize);
        }

        @Override
        public long getEndTime() {
            return this.endTime;
        }

        @Override
        public long getStartTime() {
            return this.startTime;
        }

        @Override
        public String getTimeText() {
            return getText().toString();
        }

        @Override
        public void setVals(final DateSlider.TimeObject to) {
            setText(to.text);
            this.startTime = to.startTime;
            this.endTime = to.endTime;
        }

        @Override
        public void setVals(final TimeView other) {
            setText(other.getTimeText());
            this.startTime = other.getStartTime();
            this.endTime = other.getEndTime();
        }

        /**
         * this method should be overwritten by inheriting classes to define its own look and feel
         * 
         * @param isCenterView
         *            true if the element is in the center of the scrollLayout
         * @param textSize
         *            textSize in dps
         */
        protected void setupView(final boolean isCenterView, final int textSize) {
            setGravity(Gravity.CENTER);
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
            if (isCenterView) {
                setTypeface(Typeface.DEFAULT_BOLD);
                setTextColor(0xFF333333);
            } else {
                setTextColor(0xFF666666);
            }
        }
    }
}
