/*
 * Copyright (C) 2016 David Dejori, Alex Lardschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.sasabz.android.sasabus.ui.intro;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;

/**
 * Extended {@link ViewPager} which handles advanced swiping of fragments and allows to disable
 * swiping.
 *
 * @author Alex Lardschneider
 */
public class AppIntroViewPager extends ViewPager {

    private boolean pagingEnabled;
    private boolean nextPagingEnabled;
    private float initialXValue;
    private int lockPage;
    private OnPageChangeListener listener;

    public AppIntroViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        pagingEnabled = true;
        nextPagingEnabled = true;
        lockPage = 0;
        initViewPagerScroller();
    }

    @Override
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        super.addOnPageChangeListener(listener);
        this.listener = listener;
    }

    /**
     * Override is required to trigger {@link OnPageChangeListener#onPageSelected} for the first page.
     * This is needed to correctly handle progress button display after rotation on a locked first page.
     */
    @Override
    public void setCurrentItem(int item) {
        // when you pass set current item to 0,
        // the listener won't be called so we call it on our own
        boolean invokeMeLater = false;

        if (getCurrentItem() == 0 && item == 0) {
            invokeMeLater = true;
        }

        super.setCurrentItem(item);

        if (invokeMeLater && listener != null) {
            listener.onPageSelected(0);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !checkPagingState(event) && super.onInterceptTouchEvent(event);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !checkPagingState(event) && super.onTouchEvent(event);

    }

    private boolean checkPagingState(MotionEvent event) {
        if (!pagingEnabled) {
            return true;
        }

        if (!nextPagingEnabled) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                initialXValue = event.getX();
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (detectSwipeToRight(event)) {
                    return true;
                }
            }
        }
        return false;
    }

    // To enable/disable swipe
    public void setNextPagingEnabled(boolean nextPagingEnabled) {
        this.nextPagingEnabled = nextPagingEnabled;
        if (!nextPagingEnabled) {
            lockPage = getCurrentItem();
        }
    }

    private ScrollerCustomDuration mScroller;

    /**
     * Override the Scroller instance with our own class so we can change the
     * duration
     */
    private void initViewPagerScroller() {
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            mScroller = new ScrollerCustomDuration(getContext(),
                    (Interpolator) interpolator.get(null));
            scroller.set(this, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    void setScrollDurationFactor() {
        mScroller.setScrollDurationFactor(AppIntro.DEFAULT_SCROLL_DURATION_FACTOR);
    }

    boolean isNextPagingEnabled() {
        return nextPagingEnabled;
    }

    boolean isPagingEnabled() {
        return pagingEnabled;
    }

    void setPagingEnabled(boolean pagingEnabled) {
        this.pagingEnabled = pagingEnabled;
    }

    int getLockPage() {
        return lockPage;
    }

    void setLockPage(int lockPage) {
        this.lockPage = lockPage;
    }

    // Detects the direction of swipe. Right or left.
    // Returns true if swipe is in right direction
    private boolean detectSwipeToRight(MotionEvent event) {
        int SWIPE_THRESHOLD = 0; // detect swipe
        boolean result = false;

        try {
            float diffX = event.getX() - initialXValue;
            if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                if (diffX < 0) {
                    // swipe from right to left detected ie.SwipeLeft
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}