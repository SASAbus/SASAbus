package it.sasabz.android.sasabus.beacon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import it.sasabz.android.sasabus.receiver.BluetoothReceiver;
import it.sasabz.android.sasabus.util.LogUtils;

/**
 * Service which runs in the background and keeps the beacon handlers alive.
 *
 * @author Alex Lardschneider
 */
public class BeaconService extends Service {

    private static final String TAG = "BeaconService";

    private BeaconHandler beaconHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.e(TAG, "onCreate()");

        beaconHandler = BeaconHandler.get(getApplication());
        beaconHandler.startListening();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.e(TAG, "onStartCommand()");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.e(TAG, "onDestroy()");

        beaconHandler.stopListening();
        sendBroadcast(new Intent(this, BluetoothReceiver.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}