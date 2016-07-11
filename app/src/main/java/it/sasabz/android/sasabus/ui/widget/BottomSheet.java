package it.sasabz.android.sasabus.ui.widget;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class BottomSheet<V extends View> extends BottomSheetBehavior<V> {

    private boolean initialized;
    private static int defaultState;

    public BottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <V extends View> BottomSheetBehavior<V> from(V view, @State int state) {
        defaultState = state;

        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }

        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();

        if (!(behavior instanceof BottomSheetBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with BottomSheetBehavior");
        }

        return (BottomSheetBehavior<V>) behavior;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        if (!initialized) {
            Parcelable dummySavedState = new SavedState(onSaveInstanceState(parent, child), defaultState);

            onRestoreInstanceState(parent, child, dummySavedState);
        }

        initialized = true;

        return super.onLayoutChild(parent, child, layoutDirection);
    }
}