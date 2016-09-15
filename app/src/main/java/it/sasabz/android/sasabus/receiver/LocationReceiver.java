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

                    if (BeaconHandler.isListening) {
                        BeaconHandler.get(context).stopListening();
                    }
                } else {
                    context.sendBroadcast(new Intent(context, BluetoothReceiver.class));
                }
            }
        }
    }
}