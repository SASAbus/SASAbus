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

package it.sasabz.android.sasabus.data.vdv.data;

import android.content.Context;
import android.os.SystemClock;

import net.danlew.android.joda.JodaTimeAndroid;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import it.sasabz.android.sasabus.beacon.BeaconHandler;
import it.sasabz.android.sasabus.data.vdv.PlannedData;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import it.sasabz.android.sasabus.util.Settings;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observable;
import timber.log.Timber;

/**
 * Loads the JSON open data of SASA SpA-AG that is downloaded and stored on the device.
 *
 * @author David Dejori
 * @author Alex Lardschneider
 */
public final class VdvHandler {

    private static final AtomicBoolean isLoaded = new AtomicBoolean();
    private static final AtomicBoolean isLoading = new AtomicBoolean();
    private static final AtomicBoolean isValid = new AtomicBoolean();

    private VdvHandler() {
    }

    public static Observable<Void> load(Context context) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Preconditions.checkNotUiThread();

                JodaTimeAndroid.init(context);

                if (!PlannedData.planDataExists(context)) {
                    Timber.e("Planned data does not exist, skipping loading");
                    return null;
                }

                if (isLoaded.get()) {
                    Timber.i("Planned data already loaded");
                    return null;
                }

                if (isLoading.get()) {
                    Timber.i("Already loading data...");
                    blockTillLoaded();
                    return null;
                }

                isLoading.set(true);

                try {
                    long time = -System.currentTimeMillis();

                    JSONObject data = new JSONObject(IOUtils.readFileAsString(new File(IOUtils
                            .getDataDir(context).getAbsolutePath(), "/planned-data.json")));

                    VdvCalendar.loadCalendar(data.getJSONArray("calendar"));
                    VdvPaths.loadPaths(data.getJSONArray("paths"));
                    VdvTrips.loadTrips(data.getJSONArray("trips"), VdvCalendar.today(context).getId());
                    VdvIntervals.loadIntervals(data.getJSONArray("travel_times"));
                    VdvBusStopBreaks.loadBreaks(data.getJSONArray("bus_stop_stop_times"));
                    VdvTripBreaks.loadBreaks(data.getJSONArray("trip_stop_times"));

                    isValid.set(true);

                    Timber.i("Loaded planned data in %d ms", time + System.currentTimeMillis());
                } catch (JSONException e) {
                    // If this happens, the json plan data most likely is in an invalid format
                    // because it got corrupted somehow, or someone modified it on purpose.
                    // We should reschedule a new plan data download if this happens.
                    Utils.logException(new RuntimeException("JSON format is invalid", e));
                    isValid.set(false);

                    Settings.markDataUpdateAvailable(context, true);
                } catch (Exception e) {
                    Utils.logException(new RuntimeException("Failed to load planned data", e));
                    isValid.set(false);
                }

                if (!isValid()) {
                    BeaconHandler.get(context).stop();
                }

                isLoaded.set(true);
                isLoading.set(false);

                return null;
            }
        });
    }

    public static boolean isValid() {
        return isValid.get();
    }

    public static void blockTillLoaded() {
        blockTillLoaded(true);
    }

    public static void blockTillLoaded(boolean verifyUiThread) {
        if (verifyUiThread) {
            Preconditions.checkNotUiThread();
        }

        if (isLoading.get()) {
            Timber.i("Data not isLoaded yet, waiting...");

            while (isLoading.get()) {
                SystemClock.sleep(50);
            }
        }
    }

    public static void reset() {
        Timber.e("Reset plan data");

        isLoaded.set(false);
        isLoading.set(false);
        isValid.set(false);
    }
}
