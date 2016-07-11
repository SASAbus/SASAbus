package it.sasabz.android.sasabus.provider.apis;

import it.sasabz.android.sasabus.provider.model.StopTime;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Describes stop times for some specific trips (a few only) at a bus stop. A common example is line
 * 7B waiting in Perathoner Street for about 5 minutes.
 *
 * @author David Dejori
 */
public final class StopTimes {

    private static final HashMap<StopTime, Integer> STOP_TIMES = new HashMap<>();

    private StopTimes() {
    }

    static void loadStopTimes(File dir) {
        try {
            JSONArray jStopTimes = new JSONArray(IOUtils.readFileAsString(new File(dir.getAbsolutePath(), "/ORT_HZT.json")));

            // iterates through all the stop times
            for (int i = 0; i < jStopTimes.length(); i++) {
                JSONObject jStopTime = jStopTimes.getJSONObject(i);
                STOP_TIMES.put(new StopTime(Integer.parseInt(jStopTime.getString("FGR_NR")),
                                Integer.parseInt(jStopTime.getString("ORT_NR"))),
                        Integer.parseInt(jStopTime.getString("HP_HZT")));
            }
        } catch (JSONException | IOException e) {
            Utils.handleException(e);
        }

        //noinspection CallToSystemGC
        System.gc();
    }

    public static int getStopSeconds(int fgr, int stop) {
        Integer stopTime = STOP_TIMES.get(new StopTime(fgr, stop));
        return stopTime == null ? 0 : stopTime;
    }
}
