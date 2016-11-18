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

package it.sasabz.android.sasabus.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.sasabz.android.sasabus.util.LogUtils;

/**
 * Called when a planned trip calculation was scheduled. This receiver will calculate the next
 * departure after the user specified time at the specified bus stop, and then schedule all the
 * notifications.
 *
 * Called when a notification for a planned trip needs to be displayed. If the bus is currently
 * in service (which it most probably is 10 min before the bus is at the selected bus stop),
 * the notification will include the bus delay and launch the map if clicked.
 *
 * @author Alex Lardschneider
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    public static final String ACTION_HIDE_NOTIFICATION =
            "it.sasabz.android.sasabus.HIDE_NOTIFICATION";

    public static final String EXTRA_NOTIFICATION_ID = "" +
            "it.sasabz.android.sasabus.EXTRA_NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e(TAG, "onReceive() " + intent.getAction());

        if (ACTION_HIDE_NOTIFICATION.equals(intent.getAction())) {
            hideNotification(context, intent);
        }
    }

    private void hideNotification(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);

        if (notificationId == 0) {
            LogUtils.e(TAG, "Notification id == 0");
            return;
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(notificationId);

        LogUtils.e(TAG, "Cancelled notification with id " + notificationId);
    }
}