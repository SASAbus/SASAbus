package it.sasabz.android.sasabus.provider.apis;

import it.sasabz.android.sasabus.provider.model.Interval;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Retrieves travel times between two bus stops (depending from driving speed severity as described
 * by 'FGR_NR'). Travel times are stored in seconds but are always full minutes, e. g. 0, 60, 120...
 *
 * @author David Dejori
 */
public final class Intervals {

    private static final HashMap<Interval, Integer> INTERVALS = new HashMap<>();

    private Intervals() {
    }

    static void loadIntervals(File dir) {
        try {
            JSONArray jIntervals = new JSONArray(IOUtils.readFileAsString(new File(dir.getAbsolutePath(), "/SEL_FZT_FELD.json")));

            // iterates through all the intervals
            for (int i = 0; i < jIntervals.length(); i++) {
                JSONObject jInterval = jIntervals.getJSONObject(i);
                INTERVALS.put(new Interval(
                        Integer.parseInt(jInterval.getString("FGR_NR")),
                        Integer.parseInt(jInterval.getString("ORT_NR")),
                        Integer.parseInt(jInterval.getString("SEL_ZIEL"))
                ), Integer.parseInt(jInterval.getString("SEL_FZT")));
            }
        } catch (JSONException | IOException e) {
            Utils.handleException(e);
        }

        //noinspection CallToSystemGC
        System.gc();
    }

    public static Integer getInterval(int fgr, int busStop1, int busStop2) {
        return INTERVALS.get(new Interval(fgr, busStop1, busStop2));
    }
}
