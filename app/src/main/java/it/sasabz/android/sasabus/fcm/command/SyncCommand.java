package it.sasabz.android.sasabus.fcm.command;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import it.sasabz.android.sasabus.sync.SyncService;
import it.sasabz.android.sasabus.util.LogUtils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
