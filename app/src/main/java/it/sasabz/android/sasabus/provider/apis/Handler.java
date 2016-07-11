package it.sasabz.android.sasabus.provider.apis;

import android.content.Context;

import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Preconditions;

import java.io.File;

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
