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

package it.sasabz.android.sasabus.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.davale.sasabus.core.util.DeviceUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

import javax.net.ssl.SSLException;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.beacon.bus.BusBeacon;
import it.sasabz.android.sasabus.data.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.data.realm.UserRealmHelper;
import timber.log.Timber;

/**
 * Utility class which holds various methods to help with things like logging exceptions.
 *
 * @author Alex Lardschneider
 */
public final class Utils {

    private Utils() {
    }

    public static boolean isFDroid() {
        return BuildConfig.FLAVOR.equals("fdroid");
    }

    /**
     * Changes the language of the current activity or fragment
     *
     * @param context AppApplication context
     */
    public static void changeLanguage(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        Locale locale = Locale.getDefault();

        String language = Settings.getLanguage(context).toLowerCase();
        if (!language.equals("system")) {
            locale = new Locale(language);
        }

        //noinspection deprecation
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public static String locale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0).getLanguage();
        }

        //noinspection deprecation
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    /**
     * Checks if all prerequisites are met for the beacon handler to be started.
     *
     * @param context Context to access device info and preferences.
     * @return a boolean value indicating whether the beacon handler can be started.
     */
    public static boolean areBeaconsEnabled(Context context) {
        if (!Settings.isBeaconEnabled(context)) {
            Timber.e("Beacons are disabled via preferences");
            return false;
        }

        if (!DeviceUtils.isBluetoothEnabled()) {
            Timber.e("Bluetooth is disabled");
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Timber.e("Device does not support beacons, SDK_INT < JELLY_BEAN_MR2");
            return false;
        }

        if (!DeviceUtils.hasBle(context)) {
            Timber.e("Device does not support BLE");
            return false;
        }

        return true;
    }

    /**
     * Logs a {@link Throwable}. If the current build is a debug version print the stack trace, else
     * log the exception using {@link Crashlytics}.
     *
     * @param t the {@link Throwable} to log.
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void logException(Throwable t) {
        if (BuildConfig.DEBUG) {
            t.printStackTrace();
        } else {
            if (Log.isLoggable("LOG", Log.ERROR)) {
                t.printStackTrace();
            }

            if (t instanceof SocketTimeoutException) return;
            if (t instanceof SocketException) return;
            if (t instanceof UnknownHostException) return;
            if (t instanceof SSLException) return;

            if (t.getMessage() != null) {
                if (t instanceof IOException && t.getMessage().equals("PROTOCOL_ERROR")) return;
                if (t instanceof IOException && t.getMessage().equals("Canceled")) return;
                if (t instanceof IOException && t.getMessage().equals("CANCEL")) return;
                if (t instanceof JSONException && t.getMessage().contains("<!DOCTYPE")) return;
                if (t instanceof JSONException && t.getMessage().contains("End of input")) return;
                if (t instanceof JSONException && t.getMessage().contains("shutdown")) return;
                if (t instanceof JSONException && t.getMessage().contains("Socket closed")) return;
                if (t instanceof JSONException && t.getMessage().contains("<html><head>")) return;
            }

            Crashlytics.getInstance().core.logException(t);
        }
    }

    /**
     * Logs a {@link Throwable}. If the current build is a debug version print the stack trace, else
     * log the exception using {@link Crashlytics}.
     *
     * @param t the {@link Throwable} to log.
     */
    public static void logException(Throwable t, String format, Object... params) {
        logException(new Throwable(String.format(format, params), t));
    }

    /**
     * Returns the play services connection status.
     *
     * @param context Context to access Google Play apis.
     * @return an {@link Integer} representing the connection status.
     * @see ConnectionResult#SUCCESS
     * @see ConnectionResult#API_UNAVAILABLE
     */
    public static int getPlayServicesStatus(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    /**
     * Method used to log when there was an error when saving the trip, due to
     * i.e a invalid start or stop station. Try to show a alert dialog, when not possible
     * try to show an error notification.
     *
     * @param text the trip error
     */
    public static void throwTripError(Context context, String text) {
        if (BuildConfig.DEBUG) {
            Notifications.error(context, new IllegalTripException(text));
            Timber.e("Trip error: " + text);
        }
    }

    public static CloudTrip insertTripIfValid(Context context, BusBeacon beacon) {
        if (beacon.destination == 0) {
            throwTripError(context, "Trip " + beacon.id + " invalid -> getStopStation == 0");
            return null;
        }

        if (beacon.origin == beacon.destination &&
                beacon.lastSeen - beacon.getStartDate().getTime() < 600000) {
            throwTripError(context, "Trip " + beacon.id + " invalid -> getOrigin == getStopStation: " +
                    beacon.origin + ", " + beacon.destination);
            return null;
        }

        return UserRealmHelper.insertTrip(context, beacon);
    }

    public static boolean isBrokenSamsungDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && isBetweenAndroidVersions(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }
}
