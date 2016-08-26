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

package it.sasabz.android.sasabus.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Utility class to manage common animations like fade in/out.
 *
 * @author Alex Lardschneider
 */
public final class AnimUtils {

    private static final int DURATION_SHORT = 100;
    public static final int DURATION_MEDIUM = 250;
    private static final int DURATION_LONG = 500;

    private static final int DURATION_EMPTY_STATE = DURATION_LONG;

    private AnimUtils() {
    }

    @SuppressLint("UniqueConstants")
    @IntDef({DURATION_SHORT, DURATION_MEDIUM, DURATION_LONG, DURATION_EMPTY_STATE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Duration {}

    /**
     * Fades out a view and sets its visibility to {@link View#GONE} at the end of
     * the animation.
     *
     * @param view     the {@link View} to fade out.
     * @param duration the duration of the animation.
     */
    public static void fadeOut(View view, @Duration int duration) {
        Preconditions.checkNotNull(view, "view == null");

        view.animate()
                .alpha(0)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    /**
     * Fades in a view and sets its visibility to {@link View#GONE} at the end of
     * the animation.
     *
     * @param view     the {@link View} to fade out.
     * @param duration the duration of the animation.
     */
    public static void fadeIn(View view, @Duration int duration) {
        Preconditions.checkNotNull(view, "view == null");

        view.setVisibility(View.VISIBLE);
        ViewCompat.setAlpha(view, 0);

        view.animate()
                .alpha(1)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewCompat.setAlpha(view, 1);
                    }
                })
                .start();
    }
}
