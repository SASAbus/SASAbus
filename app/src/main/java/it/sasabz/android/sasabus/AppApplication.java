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

package it.sasabz.android.sasabus;

import android.app.Application;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.receiver.BluetoothReceiver;
import it.sasabz.android.sasabus.sync.SyncHelper;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Main application which handles common app functionality like exception logging and
 * setting user preferences.
 *
 * @author Alex Lardschneider
 */
public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up Crashlytics so in case the app crashes we get a detailed stacktrace
        // and device info.
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        // Change the language to the one the user selected in the app settings.
        // If the user didn't select anything, use the default system language.
        Utils.changeLanguage(this);

        // Setup google analytics. Tracking is only done after the user accepted the terms
        // of use and privacy policy and has tracking enabled in the settings,
        // which defaults to true.
        AnalyticsHelper.prepareAnalytics(this);

        // Initialize the rest adapter which is used throughout the app.
        RestClient.init(this);

        // Initialize realms.
        BusStopRealmHelper.init(this);
        UserRealmHelper.init(this);

        // Initialize authentication helper
        AuthHelper.init(this);

        // Start the beacon handler if it hasn't been started already and start listening
        // for nearby beacons. Also start the beacon service.
        startBeacon();

        // Schedules the daily trip/plan data sync.
        SyncHelper.scheduleSync(this);

        // Check if the user upgraded the app and perform various operation if necessary.
        SettingsUtils.checkUpgrade(this);
    }

    /**
     * Start the beacon handler if it hasn't been started already and start listening
     * for nearby beacons. Also start the beacon service.
     */
    public void startBeacon() {
        if (Utils.isBeaconEnabled(this)) {
            sendBroadcast(new Intent(this, BluetoothReceiver.class));
        }
    }
}