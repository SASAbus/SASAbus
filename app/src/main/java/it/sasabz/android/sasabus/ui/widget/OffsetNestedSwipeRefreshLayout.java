package it.sasabz.android.sasabus.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import it.sasabz.android.sasabus.R;

public class OffsetNestedSwipeRefreshLayout extends NestedSwipeRefreshLayout {

    public OffsetNestedSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public OffsetNestedSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));

        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }

        mTarget.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));

        mCircleView.measure(MeasureSpec.makeMeasureSpec(mCircleWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mCircleHeight, MeasureSpec.EXACTLY));

        if (!mOriginalOffsetCalculated) {
            mOriginalOffsetCalculated = true;
            mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight() +
                    (int) getResources().getDimension(R.dimen.statusBarHeight);
        }

        mCircleViewIndex = -1;

        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index).equals(mCircleView)) {
                mCircleViewIndex = index;
                break;
            }
        }
    }
}