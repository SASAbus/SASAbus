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

package it.sasabz.android.sasabus.fcm.command;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.sync.SyncService;
import it.sasabz.android.sasabus.util.LogUtils;

/**
 * Starts a remote sync. The sync will be spread across a time period of 15 minutes to reduce
 * server load. The jitter period can be specified by sending a {@code jitter} parameter.
 *
 * @author Alex Lardschneider
 */
public class SyncCommand implements FcmCommand {

    private static final String TAG = "SyncCommand";

    private static final int DEFAULT_TRIGGER_SYNC_MAX_JITTER_MILLIS = (int) TimeUnit.MINUTES.toMillis(15);
    private static final Random RANDOM = new Random();

    @Override
    public void execute(Context context, @NonNull Map<String, String> data) {
        LogUtils.e(TAG, "Received GCM sync message");

        int jitter = DEFAULT_TRIGGER_SYNC_MAX_JITTER_MILLIS;
        if (data.containsKey("jitter")) {
            jitter = Integer.valueOf(data.get("jitter"));
        }

        scheduleSync(context, jitter);
    }

    private void scheduleSync(Context context, int jitter) {
        int jitterMillis = (int) (RANDOM.nextFloat() * jitter);

        LogUtils.e(TAG, "Scheduling next sync for " + jitterMillis + "ms");

        PendingIntent intent = PendingIntent.getService(context, 0,
                new Intent(context, SyncService.class),
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + jitterMillis, intent);
    }
}
