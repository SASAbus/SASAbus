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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.sasabz.android.sasabus.sync.SyncHelper;
import it.sasabz.android.sasabus.util.AlarmUtils;

/**
 * Receiver which starts on device boot and schedules the planned trip notifications and
 * the daily sync.
 *
 * @author Alex Lardschneider
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            // Reschedule all alarms as they get cleared on reboot.
            AlarmUtils.scheduleTrips(context);

            // Schedule sync at night.
            SyncHelper.scheduleSync(context);
        }
    }
}