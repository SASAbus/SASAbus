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
 * Describes the stop times at a bus stop for some specific trips. Those trips are identifiable by
 * their unique ID being 'FRT_FID' in the SASA SpA-AG open data.
 *
 * @author David Dejori
 */
public final class HaltTimes {

    private static final HashMap<StopTime, Integer> HALT_TIMES = new HashMap<>();

    private HaltTimes() {
    }

    static void loadHaltTimes(File dir) {
        try {
            JSONArray jHaltTimes = new JSONArray(IOUtils.readFileAsString(new File(dir.getAbsolutePath(), "/REC_FRT_HZT.json")));

            // iterates through all the stop times
            for (int i = 0; i < jHaltTimes.length(); i++) {
                JSONObject jStopTime = jHaltTimes.getJSONObject(i);
                HALT_TIMES.put(new StopTime(Integer.parseInt(jStopTime.getString("FRT_FID")),
                                Integer.parseInt(jStopTime.getString("ORT_NR"))),
                        Integer.parseInt(jStopTime.getString("FRT_HZT_ZEIT")));
            }
        } catch (JSONException | IOException e) {
            Utils.handleException(e);
        }

        //noinspection CallToSystemGC
        System.gc();
    }

    public static int getStopSeconds(int trip, int stop) {
        Integer stopTime = HALT_TIMES.get(new StopTime(trip, stop));
        return stopTime == null ? 0 : stopTime;
    }
}
