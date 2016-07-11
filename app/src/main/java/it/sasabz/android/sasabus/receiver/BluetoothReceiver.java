package it.sasabz.android.sasabus.receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import it.sasabz.android.sasabus.beacon.BeaconService;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;
import it.sasabz.android.sasabus.util.Utils;

import org.altbeacon.beacon.BeaconManager;

import it.sasabz.android.sasabus.beacon.BeaconHandler;

/**
 * Receiver to listen for changes in the bluetooth state.
 * Starts the {@link BeaconHandler} with a wakelock to prevent the phone
 * going to sleep (or doze, if on M) before the service could start.
 *
 * @author Alex Lardschneider
 */
public class BluetoothReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "BluetoothReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e(TAG, "onReceive()");

        if (!Utils.isBeaconEnabled(context)) {
            LogUtils.e(TAG, "Beacon scanning not available or enabled");

            NotificationUtils.cancelBus(context);
            context.stopService(new Intent(context, BeaconService.class));

            return;
        }

        try {
            if (BeaconManager.getInstanceForApplication(context).checkAvailability()) {
                ComponentName component = new ComponentName(context.getPackageName(),
                        BeaconService.class.getName());

                startWakefulService(context, intent.setComponent(component));

                LogUtils.e(TAG, "Started beacon service");
            } else {
                context.stopService(new Intent(context, BeaconService.class));

                NotificationUtils.cancelBus(context);
            }
        } catch (Exception e) {
            Utils.handleException(e);
        }
    }
}