package it.sasabz.android.sasabus.sync;

import android.app.IntentService;
import android.content.Intent;

import it.sasabz.android.sasabus.fcm.FcmService;

/**
 * {@link IntentService} which starts the sync helper. Called when a sync was requested via
 * {@link FcmService FCM}.
 *
 * @author Alex Lardschneider
 */
public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        new SyncHelper(this).performSync();
    }
}
