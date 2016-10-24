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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import it.sasabz.android.sasabus.R;
import timber.log.Timber;

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

    public static void hideKeyboard(Activity activity) {
        View focus = activity.getCurrentFocus();

        if (focus == null) {
            Timber.e("Tried to hide keyboard but there is no focused window");
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }
}
