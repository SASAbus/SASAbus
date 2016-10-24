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

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.Callable;

import it.sasabz.android.sasabus.data.vdv.PlannedData;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import rx.Observable;
import timber.log.Timber;

/**
 * Loads the JSON open data of SASA SpA-AG that is downloaded and stored on the device.
 *
 * @author David Dejori
 */
public final class VdvHandler {

    private static boolean loaded;
    private static boolean isLoading;

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

                if (!loaded) {
                    isLoading = true;

                    try {
                        long time = -System.currentTimeMillis();

                        JSONObject data = new JSONObject(IOUtils.readFileAsString(new File(IOUtils
                                .getDataDir(context).getAbsolutePath(), "/planned-data.json")));

                        VdvCalendar.loadCalendar(data.getJSONArray("calendar"));
                        VdvPaths.loadPaths(data.getJSONArray("paths"));
                        VdvTrips.loadTrips(data.getJSONArray("trips"), VdvCalendar.today().getId());
                        VdvIntervals.loadIntervals(data.getJSONArray("travel_times"));
                        VdvBusStopBreaks.loadBreaks(data.getJSONArray("bus_stop_stop_times"));
                        VdvTripBreaks.loadBreaks(data.getJSONArray("trip_stop_times"));

                        Timber.w("Loaded planned data in %d ms", time + System.currentTimeMillis());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load planned data", e);
                    }

                    loaded = true;
                    isLoading = false;
                }

                return null;
            }
        });
    }

    public static void blockTillLoaded() {
        Preconditions.checkNotUiThread();

        if (isLoading) {
            Timber.i("Data not loaded yet, waiting...");

            while (isLoading) {
                SystemClock.sleep(50);
            }
        }
    }
}
