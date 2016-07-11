package it.sasabz.android.sasabus;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import it.sasabz.android.sasabus.fcm.FcmUtils;
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

        // We want to be alerted when the app does heavy work on the UI thread like access
        // disk or network by using StrictMode.
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new StrictMode.ThreadPolicy.Builder().detectAll().build();

            new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectCleartextNetwork()
                    .detectFileUriExposure()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectLeakedSqlLiteObjects()
                    .penaltyDeathOnCleartextNetwork()
                    .penaltyLog()
                    .build();
        }


        // Change the language to the one the user selected in the app settings.
        // If the user didn't select anything, use the default system language.
        Utils.changeLanguage(this);

        // Set up the day/night theme if the user has chosen to enable it in the settings.
        // Defaults to {@link AppCompatDelegate#MODE_NIGHT_FOLLOW_SYSTEM}
        //noinspection WrongConstant,ResourceType
        AppCompatDelegate.setDefaultNightMode(SettingsUtils.getNightMode(this));

        // Setup google analytics. Tracking is only done after the user accepted the terms
        // of use and privacy policy and has tracking enabled in the settings,
        // which defaults to true.
        AnalyticsHelper.prepareAnalytics(this);

        // Initialize the rest adapter which is used throughout the app.
        RestClient.init(this);

        // Initialize realms.
        BusStopRealmHelper.init(this);
        UserRealmHelper.init(this);

        // Start the beacon handler if it hasn't been started already and start listening
        // for nearby beacons. Also start the beacon service.
        startBeacon();

        // Sets up Google Cloud Messaging. If the token has not yet been generated/sent,
        // we have to generate and send it to the app engine and store it there. If the sending
        // of the token failed try to resend it.
        FcmUtils.checkFcm(this);

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