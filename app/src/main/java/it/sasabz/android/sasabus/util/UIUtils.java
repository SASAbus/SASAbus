package it.sasabz.android.sasabus.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

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

    public static void okDialog(Context context, @StringRes int title, @StringRes int message) {
        okDialog(context, title, message, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    public static void okDialog(Context context, @StringRes int title, @StringRes int message,
                                DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context, R.style.DialogStyle)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, listener)
                .create()
                .show();
    }
}
