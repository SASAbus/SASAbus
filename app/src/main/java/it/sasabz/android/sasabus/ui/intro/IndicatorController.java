package it.sasabz.android.sasabus.ui.intro;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import it.sasabz.android.sasabus.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the indicator dots at the bottom of the intro screen.
 *
 * @author Alex Lardschneider
 */
class IndicatorController {

    private static final int DEFAULT_COLOR = 1;

    private Context mContext;
    private LinearLayout mDotLayout;
    private List<ImageView> mDots;
    private int mSlideCount;
    private int selectedDotColor = DEFAULT_COLOR;
    private int unselectedDotColor = DEFAULT_COLOR;

    private static final int FIRST_PAGE_NUM = 0;

    public View newInstance(@NonNull Context context) {
        mContext = context;
        mDotLayout = (LinearLayout) View.inflate(context, R.layout.default_indicator, null);
        return mDotLayout;
    }

    public void initialize(int slideCount) {
        mDots = new ArrayList<>();
        mSlideCount = slideCount;
        selectedDotColor = -1;
        unselectedDotColor = -1;

        for (int i = 0; i < slideCount; i++) {
            ImageView dot = new ImageView(mContext);
            dot.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_dot_grey));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            mDotLayout.addView(dot, params);
            mDots.add(dot);
        }

        selectPosition(FIRST_PAGE_NUM);
    }

    public void selectPosition(int index) {
        for (int i = 0; i < mSlideCount; i++) {
            int drawableId = i == index ? R.drawable.bg_dot_white :
                    R.drawable.bg_dot_grey;

            Drawable drawable = ContextCompat.getDrawable(mContext, drawableId);
            if (selectedDotColor != DEFAULT_COLOR && i == index) {
                drawable.mutate().setColorFilter(selectedDotColor, PorterDuff.Mode.SRC_IN);
            }
            if (unselectedDotColor != DEFAULT_COLOR && i != index) {
                drawable.mutate().setColorFilter(unselectedDotColor, PorterDuff.Mode.SRC_IN);
            }
            mDots.get(i).setImageDrawable(drawable);
        }
    }
}
