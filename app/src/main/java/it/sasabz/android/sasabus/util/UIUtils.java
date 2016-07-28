package it.sasabz.android.sasabus.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import it.sasabz.android.sasabus.R;

public final class UIUtils {

    private UIUtils() {
    }

    public static int getColorForDelay(Context context, int delay) {
        if (delay > 0) {
            return ContextCompat.getColor(context, R.color.primary_red);
        } else {
            return ContextCompat.getColor(context, R.color.primary_green);
        }
    }
}
