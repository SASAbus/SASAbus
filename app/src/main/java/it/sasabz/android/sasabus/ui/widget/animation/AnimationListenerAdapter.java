package it.sasabz.android.sasabus.ui.widget.animation;

import android.view.animation.Animation;

/**
 * This adapter class provides empty implementations of the methods from
 * {@link Animation.AnimationListener}.
 * Any custom listener that cares only about a subset of the methods of this listener can
 * simply subclass this adapter class instead of implementing the interface directly.
 */
public class AnimationListenerAdapter implements Animation.AnimationListener {

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
