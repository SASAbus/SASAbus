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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.beacon.bus.BusBeacon;
import it.sasabz.android.sasabus.beacon.bus.BusBeaconHandler;
import it.sasabz.android.sasabus.beacon.bus.CurrentTrip;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;

public final class BeaconStorage {

    private static final String TAG = "BeaconStorage";

    /**
     * Preferences which contain the saved bus beacons to keep the trip progress
     * in case the user quits the app. The saved beacons getPublicKey restored as soon as
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

    public void setCurrentTrip(CurrentTrip trip) {
        mCurrentTrip = trip;

        if (trip == null) {
            LogUtils.e(TAG, "trip == null, cancelling notification");
            NotificationUtils.cancelBus(mContext);
        } else if (trip.checkUpdate() && trip.isNotificationShown &&
                trip.beacon.isSuitableForTrip && SettingsUtils.isBusNotificationEnabled(mContext)) {

            BusBeaconHandler.notificationAction.showNotification(trip);
        }

        try {
            String json = GSON.toJson(trip);
            mPrefs.edit().putString(PREF_BEACON_CURRENT_TRIP, json).apply();
        } catch (Exception e) {
            Utils.handleException(e);
        }
    }

    public CurrentTrip getCurrentTrip() {
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
            CurrentTrip trip = GSON.fromJson(json, CurrentTrip.class);

            if (trip != null) {
                trip.setContext(mContext);
                trip.update();

                if (trip.beacon.trip == 0) {
                    return null;
                }
            }

            return trip;
        } catch (Exception e) {
            Utils.handleException(e);
        }

        return null;
    }

    public void writeBeaconMap(Map<Integer, BusBeacon> mBusBeaconMap) {
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

    public Map<Integer, BusBeacon> getBeaconMap() {
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

                    Type type = new TypeToken<Map<Integer, BusBeacon>>() {
                    }.getType();
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

    public boolean hasCurrentTrip() {
        return getCurrentTrip() != null;
    }

    public void clear() {
        mPrefs.edit().remove(PREF_BEACON_CURRENT_TRIP).apply();
        mPrefs.edit().remove(PREF_BUS_BEACON_MAP).apply();
        mPrefs.edit().remove(PREF_BUS_BEACON_MAP_LAST).apply();
    }
}
