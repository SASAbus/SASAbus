package it.sasabz.android.sasabus.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;

import it.sasabz.android.sasabus.beacon.BeaconHandler;

/**
 * Called as soon as the user changes settings regarding location provider. This is only used
 * on api > M as Google changed the way how beacons work, which now require an active location
 * provider. If the user disables the location provider, the beacon handler has to stop.
 *
 * @author Alex Lardschneider
 */
public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "";

        if (action.equals("android.location.PROVIDERS_CHANGED")) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            if (Build.VERSION.SDK_INT >= 23 && adapter != null && adapter.isEnabled()) {
                LocationManager locationManager = (LocationManager)
                        context.getSystemService(Context.LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                        !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    BeaconHandler.get(context).stopListening();
                } else {
                    context.sendBroadcast(new Intent(context, BluetoothReceiver.class));
                }
            }
        }
    }
}