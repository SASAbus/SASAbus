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

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.davale.sasabus.core.realm.BusStopRealmHelper;
import com.davale.sasabus.core.vdv.data.VdvHandler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import bz.davide.catchsolve.catcher.android.CatchSolve;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import it.sasabz.android.sasabus.beacon.BeaconHandler;
import it.sasabz.android.sasabus.data.network.auth.AuthHelper;
import it.sasabz.android.sasabus.data.network.RestClient;
import it.sasabz.android.sasabus.data.realm.UserRealmHelper;
import it.sasabz.android.sasabus.sync.SyncHelper;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Settings;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.rx.NextObserver;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Main application which handles common app functionality like exception logging and
 * setting user preferences.
 *
 * @author Alex Lardschneider
 */
public class AppApplication extends Application {

    private GoogleApiClient mApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up Crashlytics so in case the app crashes we get a detailed stacktrace
        // and device info.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(this, new Crashlytics());
            Timber.plant(new CrashReportingTree());

            CatchSolve.init(this, BuildConfig.CATCH_AND_SOLVE_API_KEY);
        }

        // Change the language to the one the user selected in the app settings.
        // If the user didn't select anything, use the default system language.
        Utils.changeLanguage(this);

        // Setup google analytics. Tracking is only done after the user accepted the terms
        // of use and privacy policy and has tracking enabled in the settings,
        // which defaults to true.
        AnalyticsHelper.prepareAnalytics(this);

        // Initialize the rest adapter which is used throughout the app.
        RestClient.INSTANCE.init(this);

        // Initialize realms.
        Realm.init(this);
        BusStopRealmHelper.init(this);
        UserRealmHelper.init(this);

        // Initialize authentication helper
        AuthHelper.init(this);

        // Schedules the daily trip/plan data sync.
        SyncHelper.Companion.scheduleSync(this);

        // Check if the user upgraded the app and perform various operation if necessary.
        Settings.checkUpgrade(this);

        // Load plan data
        VdvHandler.load(this)
                .subscribeOn(Schedulers.io())
                .subscribe(new NextObserver<>());

        //noinspection CodeBlock2Expr
        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Timber.i("Connected to Google API");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Timber.e("Connection to Google API suspended");

                    }
                })
                .addOnConnectionFailedListener(connectionResult -> {
                    Timber.e("Connection to Google API failed: %s", connectionResult);
                })
                .addApi(LocationServices.API)
                .build();

        mApiClient.connect();

        // Start the beacon handler if it hasn't been started already and start listening
        // for nearby beacons. Also start the beacon service.
        initBeacons();
    }


    public GoogleApiClient getGoogleApiClient() {
        return mApiClient;
    }

    public void initBeacons() {
        if (Utils.areBeaconsEnabled(this)) {
            BeaconHandler beaconHandler = BeaconHandler.get(this);
            beaconHandler.start();
        } else {
            Timber.e("Beacons are disabled");
        }
    }


    private static class CrashReportingTree extends Timber.Tree {

        @Override
        @SuppressLint("LogTagMismatch")
        protected void log(int priority, String tag, String message, Throwable throwable) {
            Crashlytics.log(message);

            if (Log.isLoggable("LOG", priority)) {
                Log.println(priority, "APP", message);
            }

            if (throwable != null) {
                Utils.logException(throwable);
            }
        }
    }
}