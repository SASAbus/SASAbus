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

package it.sasabz.android.sasabus.beacon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import it.sasabz.android.sasabus.data.vdv.Api;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Service which runs in the background and keeps the beacon handlers alive.
 *
 * @author Alex Lardschneider
 */
public class BeaconService extends Service {

    private BeaconHandler beaconHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.e("onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.e("onStartCommand()");

        beaconHandler = BeaconHandler.get(getApplication());

        Api.todayExistsRx(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(exists -> {
                    if (exists) {
                        beaconHandler.startListening();
                    } else {
                        Timber.e("No plan data for this day");
                        stopSelf();
                    }
                });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Timber.e("onDestroy()");

        if (beaconHandler != null) {
            beaconHandler.stopListening();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}