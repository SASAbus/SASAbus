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