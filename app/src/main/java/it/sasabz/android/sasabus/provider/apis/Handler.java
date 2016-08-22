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

package it.sasabz.android.sasabus.provider.apis;

import android.content.Context;

import java.io.File;

import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Preconditions;

/**
 * Loads the JSON open data of SASA SpA-AG that is downloaded and stored on the device.
 *
 * @author David Dejori
 */
public final class Handler {
    private static final String TAG = "Handler";

    private static boolean loaded;

    private Handler() {
    }

    public static void load(Context context) {
        Preconditions.checkNotNull(context, "context == null");

        if (loaded) return;

        LogUtils.e(TAG, "Loading JSON data from files");

        File filesDir = IOUtils.getDataDir(context);

        loaded = true;

        CompanyCalendar.loadCalendar(filesDir);
        Paths.loadPaths(filesDir);
        Trips.loadTrips(context, filesDir);
        Intervals.loadIntervals(filesDir);
        StopTimes.loadStopTimes(filesDir);
        HaltTimes.loadHaltTimes(filesDir);

        LogUtils.e(TAG, "Loaded JSON data from files");
    }
}
