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

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import it.sasabz.android.sasabus.AppApplication;
import it.sasabz.android.sasabus.beacon.BeaconHandler;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import timber.log.Timber;

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

        if (!Utils.areBeaconsEnabled(context)) {
            Timber.w("Beacon scanning not available or enabled");

            BeaconHandler.get(context).stop();

            completeWakefulIntent(intent);
        } else {
            ((AppApplication) context.getApplicationContext()).initBeacons();
        }
    }
}