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

package it.sasabz.android.sasabus.ui.widget;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

class BottomSheet<V extends View> extends BottomSheetBehavior<V> {

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