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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.ui.intro.Intro;

/**
 * Utility class to help with saving and getting shared preferences.
 *
 * @author Alex Lardschneider
 */
public final class SettingsUtils {

    private static final String TAG = "SettingsUtils";

    /**
     * Boolean indicating whether ToS has been accepted.
     */
    private static final String PREF_SHOW_INTRO = "pref_show_intro";

    /**
     * Integer indicating the last version the app was started with
     */
    private static final String PREF_VERSION_KEY = "pref_version_code";

    /**
     * Integer indicating how often the app has been opened.
     */
    private static final String PREF_STARTUP_COUNT = "pref_startup_count";

    /**
     * App language
     */
    private static final String PREF_LANGUAGE = "pref_language";

    /**
     * ClusterMarker auto update
     */
    public static final String PREF_AUTO_UPDATE = "pref_auto_update";

    /**
     * ClusterMarker auto update interval in millis
     */
    public static final String PREF_AUTO_UPDATE_INTERVAL = "pref_auto_update_interval";

    /**
     * Beacon scanner enabled or disabled
     */
    public static final String PREF_BEACONS_ENABLED = "pref_beacons_enabled";

    /**
     * BusMarker beacon notifications enabled or disabled.
     */
    private static final String PREF_BUS_BEACONS_ENABLED = "pref_bus_beacons_enabled";

    /**
     * BusMarker stop beacon notifications enabled or disabled.
     */
    private static final String PREF_BUS_STOP_BEACONS_ENABLED = "pref_bus_stop_beacons_enabled";

    /**
     * Trip notifications enabled or disabled.
     */
    private static final String PREF_TRIP_NOTIFICATION_ENABLED = "pref_trips_enabled";

    /**
     * BusMarker stop beacon notification vibrations enabled or disabled.
     */
    private static final String PREF_BUS_STOP_BEACON_VIBRATION = "pref_bus_stop_beacon_vibration";

    /**
     * App is allowed to show a rating popup
     */
    private static final String PREF_RATING_ENABLED = "pref_rating_enabled";

    /**
     * Preferences holding all the favorite line ids separated by a comma.
     */
    private static final String PREF_FAVORITE_LINES = "pref_favorite_lines";

    /**
     * Preferences holding all the favorite bus stop ids separated by a comma.
     */
    private static final String PREF_FAVORITE_BUS_STOPS = "pref_favorite_busstops";

    /**
     * User already rated or not.
     */
    private static final String PREF_HAS_RATED = "pref_has_rated";

    /**
     * Determines when to load bus stop images.
     */
    private static final String PREF_FETCH_IMAGES = "pref_fetch_images";

    /**
     * Show station ids next to the name.
     */
    private static final String PREF_SHOW_BUS_STOP_IDS = "pref_show_station_ids";

    private static final String PREF_SHOULD_SHOW_CHANGELOG = "pref_should_show_changelog";

    private static final String PREF_DATA_UPDATE_AVAILABLE = "pref_data_update_available";

    private static final String PREF_DATA_DATE = "pref_data_date";

    private static final String PREF_TIMETABLE_DATE = "pref_timetable_date";

    private static final String PREF_NEWS_PUSH_ENABLED = "pref_news_push_enabled";

    private static final String PREF_SURVEY_ENABLED = "pref_survey_enabled";

    private static final String PREF_SURVEY_INTERVAL = "pref_survey_interval";

    private static final String PREF_SURVEY_LAST_MILLIS = "pref_survey_last_millis";

    private static final String PREF_TRAFFIC_LIGHT_CITY = "pref_traffic_light_city_is_bz";

    private static final String PREF_SHOW_MAP_DOWNLOAD_DIALOG = "pref_show_map_download_dialog";

    /**
     * Save the parking id of the widget
     */
    private static final String PREF_WIDGET_PARKING_ID = "pref_widget_parking_id";

    /**
     * Default value to return when the user has installed the app and opened it
     * for the first time.
     */
    private static final int NO_VERSION = -1;

    private static boolean wasUpgrade;

    private SettingsUtils() {
    }


    // ========================================== WIDGETS ==========================================

    public static int getWidgetParking(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_WIDGET_PARKING_ID, 0);
    }

    public static void setWidgetParking(Context context, int parkingId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREF_WIDGET_PARKING_ID, parkingId).apply();
    }


    // ======================================== PREFERENCES ========================================

    /**
     * Determines if the app can ask for a rating if the user opened the app a certain amount
     * of times.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return a boolean value.
     */
    public static boolean canAskForRating(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_RATING_ENABLED, true);
    }

    /**
     * Determines if the beacon scanner is enabled ans should scan for beacons. This value
     * does not force the beacon scanner to be enabled, it also depends on values found in
     * {@link Utils#isBeaconEnabled(Context)}.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return a boolean value.
     */
    static boolean isBeaconEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_BEACONS_ENABLED, true);
    }

    /**
     * Determines if the app should show a notification if a bus is nearby.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return a boolean value.
     */
    public static boolean isBusNotificationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_BUS_BEACONS_ENABLED, true);
    }

    /**
     * Determines if the app should show a notification if a bus stop is nearby.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return a boolean value.
     */
    public static boolean isBusStopNotificationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_BUS_STOP_BEACONS_ENABLED, true);
    }

    /**
     * Determines if the app should show a notification if a trip has been completed.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return a boolean value.
     */
    public static boolean isTripNotificationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_TRIP_NOTIFICATION_ENABLED, true);
    }

    /**
     * Determines if the app should vibrate when a bus stop notification appears.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return a boolean value.
     */
    static boolean isBusStopVibrationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_BUS_STOP_BEACON_VIBRATION, true);
    }

    /**
     * Determines if the app should auto reload the content. The interval is specified by
     * {@link #getMapAutoInterval(Context)}.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return a boolean value.
     */
    public static boolean isMapAutoEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_AUTO_UPDATE, false);
    }

    /**
     * Returns the interval in millis which the app should reload.
     * Depends on {@link #isMapAutoEnabled(Context)}.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return the interval.
     */
    public static int getMapAutoInterval(Context context) {
        int interval = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(context).getString(PREF_AUTO_UPDATE_INTERVAL, "5000"));

        if (interval == 3000) {
            LogUtils.e(TAG, "Auto refresh interval 3000 was changed to 5000");

            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(PREF_AUTO_UPDATE_INTERVAL, "5000").apply();

            interval = 5000;
        }

        return interval;
    }

    /**
     * Returns the language the app should use. This language will be set using
     * {@link Utils#changeLanguage(Context)}.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return the language code.
     */
    static String getLanguage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LANGUAGE, "system");
    }

    /**
     * Determines if the app should show the station ids next to the names.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return {@code true} if it should display the ids, {@code false} otherwise.
     */
    public static boolean showBusStopIds(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SHOW_BUS_STOP_IDS, BuildConfig.DEBUG);
    }

    public static boolean isNewsPushEnabled(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_NEWS_PUSH_ENABLED, true);
    }

    /**
     * Returns when to downloadTrips bus stop background images.
     * {@code 1}: Always downloadTrips images.
     * {@code 2}: Download on wifi only
     * {@code 3}: Never downloadTrips them.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return an int which determines when to downloadTrips images.
     */
    public static int getFetchImages(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_FETCH_IMAGES, "2"));
    }

    public static boolean isSurveyEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SURVEY_ENABLED, true);
    }

    public static int getSurveyInterval(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SURVEY_INTERVAL, "2"));
    }

    public static long getLastSurveyMillis(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREF_SURVEY_LAST_MILLIS, 0);
    }

    /**
     * Disables the rating popup if the user already rated or chose to not rate again.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static void setLastSurveyMillis(Context context, long millis) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(PREF_SURVEY_LAST_MILLIS, millis).apply();
    }

    public static String getTrafficLightCity(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_TRAFFIC_LIGHT_CITY, "bz");
    }

    public static void setTrafficLightCity(Context context, String city) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREF_TRAFFIC_LIGHT_CITY, city).apply();
    }


    // ========================================= OTHER =============================================

    /**
     * Returns how often the app was started.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return app startup count
     */
    public static int getStartupCount(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_STARTUP_COUNT, 0);
    }

    /**
     * Increments the app startup counter each time the app starts
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static void incrementStartupCount(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(PREF_STARTUP_COUNT, getStartupCount(context) + 1).apply();
    }

    /**
     * Disables the rating popup if the user already rated or chose to not rate again.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static void setRatingDisabled(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_RATING_ENABLED, false).apply();
    }


    // ===================================== DATE & TIMETABLES =====================================

    /**
     * Returns the date when the plan data has been downloaded. This is done to check if there are
     * new plan data updates available depending on when the user downloaded it last.
     *
     * @param context Context to be used to access the {@link SharedPreferences}.
     * @return a String with the date in form {@code YYYYMMDD}.
     */
    public static String getDataDate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_DATA_DATE, "19700101");
    }

    /**
     * Returns the date when the timetables have been downloaded. This is done to check if there are
     * new timetable updates available depending on when the user downloaded them last.
     *
     * @param context Context to be used to access the {@link SharedPreferences}.
     * @return a String with the date in form {@code YYYYMMDD}.
     */
    public static String getTimetableDate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_TIMETABLE_DATE, "19700101");
    }

    /**
     * Sets the date when the plan data has been downloaded.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     */
    public static void setDataDate(Context context) {
        SimpleDateFormat format = new SimpleDateFormat("yyyMMdd", Locale.ITALY);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_DATA_DATE, format.format(new Date())).apply();
    }

    /**
     * Sets the date when the timetables have been downloaded.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     */
    public static void setTimetableDate(Context context) {
        SimpleDateFormat format = new SimpleDateFormat("yyyMMdd", Locale.ITALY);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_TIMETABLE_DATE, format.format(new Date())).apply();
    }


    // ========================================= GENERAL ===========================================

    /**
     * Return true if user has already seen the {@link Intro} app intro,
     * false if they haven't (yet).
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static boolean shouldShowIntro(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_INTRO, true);
    }

    /**
     * Mark {@code newValue whether} the user has accepted the TOS so the app doesn't ask again.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     */
    public static void markIntroAsShown(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_SHOW_INTRO, false).apply();
    }

    public static void checkUpgrade(Context context) {
        int mLastVersionCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_VERSION_KEY, NO_VERSION);

        if (mLastVersionCode == NO_VERSION) {
            setCurrentAppVersion(context);
            return;
        }

        if (mLastVersionCode < BuildConfig.VERSION_CODE) {
            new SettingsMigration().migrate(context, mLastVersionCode, BuildConfig.VERSION_CODE);

            wasUpgrade = true;

            setCurrentAppVersion(context);
        }
    }

    /**
     * Mark {@code newValue value} if a data update is available.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     */
    public static void markDataUpdateAvailable(Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_UPDATE_AVAILABLE, newValue).apply();
    }

    /**
     * Check if a data update is available.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     * @return {@code true} if an upgrade has happened, {@code false} otherwise.
     */
    public static boolean isDataUpdateAvailable(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_DATA_UPDATE_AVAILABLE, false);
    }

    /**
     * Mark whether the user has clicked next on the changelog fragment
     * so the app doesn't show it again.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     */
    private static void setCurrentAppVersion(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(PREF_VERSION_KEY, BuildConfig.VERSION_CODE).apply();
    }

    /**
     * Mark {@code newValue whether} the user has accepted the TOS so the app doesn't ask again.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     */
    public static void markAsRated(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_HAS_RATED, true).apply();
    }

    /**
     * Check if the app should show a changelog dialog after the user has updated the app.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     */
    public static boolean shouldShowChangelog(Context context) {
        if (wasUpgrade) {
            wasUpgrade = false;

            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(PREF_SHOULD_SHOW_CHANGELOG, true);
        }

        return false;
    }

    /**
     * Disable the changelog so it doesn't show again after the next update.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     */
    static void disableChangelog(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_SHOULD_SHOW_CHANGELOG, false).apply();
    }

    public static boolean shouldShowMapDialog(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_MAP_DOWNLOAD_DIALOG, true);
    }

    @SuppressLint("CommitPrefEdits")
    public static void disableMapDialog(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_SHOW_MAP_DOWNLOAD_DIALOG, false).commit();
    }
}

