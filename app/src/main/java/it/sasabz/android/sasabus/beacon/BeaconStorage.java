package it.sasabz.android.sasabus.beacon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.util.NotificationUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

final class BeaconStorage {

    /**
     * Preferences which contain the saved bus beacons to keep the trip progress
     * in case the user quits the app. The saved beacons get restored as soon as
     * the beacon handler starts.
     */
    private static final String STORAGE_NAME = BuildConfig.APPLICATION_ID + "_beacons";

    private static final String PREF_BEACON_CURRENT_TRIP = "pref_beacon_current_trip";
    private static final String PREF_BUS_BEACON_MAP = "pref_bus_beacon_map";
    private static final String PREF_BUS_BEACON_MAP_LAST = "pref_bus_beacon_map_last";

    private final SharedPreferences mPrefs;
    private final Context mContext;

    @SuppressLint("StaticFieldLeak")
    private static BeaconStorage sInstance;

    private CurrentTrip mCurrentTrip;

    private static final Gson GSON = new Gson();

    private BeaconStorage(Context context) {
        mContext = context;

        mPrefs = context
                .getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
    }

    public static BeaconStorage getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BeaconStorage(context);
        }
        return sInstance;
    }

    void setCurrentTrip(CurrentTrip trip) {
        mCurrentTrip = trip;

        if (trip == null) {
            NotificationUtils.cancelBus(mContext);
        } else if (trip.checkUpdate() && trip.isNotificationShown() &&
                SettingsUtils.isBusNotificationEnabled(mContext)) {

            NotificationUtils.bus(mContext, trip.getId(), trip.getTitle());
        }

        try {
            mPrefs.edit().putString(PREF_BEACON_CURRENT_TRIP, GSON.toJson(trip)).apply();
        } catch (Exception e) {
            Utils.handleException(e);
        }
    }

    CurrentTrip getCurrentTrip() {
        if (mCurrentTrip != null) {
            return mCurrentTrip;
        }

        mCurrentTrip = readCurrentTrip();
        return mCurrentTrip;
    }

    private CurrentTrip readCurrentTrip() {
        String json = mPrefs.getString(PREF_BEACON_CURRENT_TRIP, null);
        if (json == null) {
            return null;
        }

        try {
            return GSON.fromJson(json, CurrentTrip.class);
        } catch (Exception e) {
            Utils.handleException(e);
        }

        return null;
    }

    void writeBeaconMap(Map<Integer, BusBeacon> mBusBeaconMap) {
        try {
            String json = GSON.toJson(mBusBeaconMap);
            mPrefs.edit().putString(PREF_BUS_BEACON_MAP, json).apply();
        } catch (Exception e) {
            Utils.handleException(e);
        }

        if (mBusBeaconMap == null) {
            mPrefs.edit().remove(PREF_BUS_BEACON_MAP_LAST).apply();
        } else {
            mPrefs.edit().putLong(PREF_BUS_BEACON_MAP_LAST, new Date().getTime()).apply();
        }
    }

    Map<Integer, BusBeacon> getBeaconMap() {
        long currentTripTimeStamp = 0;

        if (mPrefs.getLong(PREF_BUS_BEACON_MAP_LAST, -999) != -999) {
            currentTripTimeStamp = mPrefs.getLong(PREF_BUS_BEACON_MAP_LAST, -999);
        }

        if (currentTripTimeStamp != 0) {
            long nowTimeStamp = new Date().getTime();
            long difference = nowTimeStamp - currentTripTimeStamp;
            int configuredMilliseconds = 240000;

            if (difference < configuredMilliseconds) {
                try {
                    String json = mPrefs.getString(PREF_BUS_BEACON_MAP, null);
                    if (json == null) {
                        return Collections.emptyMap();
                    }

                    Type type = new TypeToken<Map<Integer, BusBeacon>>() {}.getType();
                    return GSON.fromJson(json, type);
                } catch (Exception e) {
                    Utils.handleException(e);
                }
            } else {
                setCurrentTrip(null);
                return Collections.emptyMap();
            }
        }

        return Collections.emptyMap();
    }

    boolean hasCurrentTrip() {
        return getCurrentTrip() != null;
    }
}
