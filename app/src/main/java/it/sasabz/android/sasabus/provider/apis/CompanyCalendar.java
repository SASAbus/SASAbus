package it.sasabz.android.sasabus.provider.apis;

import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Represents the calendar of day types of SASA SpA-AG. For example Saturdays, Sundays and holidays
 * have different timetables than the normal days (from Monday to Friday) and so on.
 *
 * @author David Dejori
 */
public final class CompanyCalendar {

    private static final HashMap<String, Integer> CALENDAR = new HashMap<>();

    private CompanyCalendar() {
    }

    static void loadCalendar(File dir) {
        try {
            JSONArray jCalendar = new JSONArray(IOUtils.readFileAsString(
                    new File(dir.getAbsolutePath(), "/FIRMENKALENDER.json")));

            for (int i = 0; i < jCalendar.length(); i++) {
                JSONObject jDay = jCalendar.getJSONObject(i);
                CALENDAR.put(jDay.getString("BETRIEBSTAG"), Integer.parseInt(jDay.getString("TAGESART_NR")));
            }
        } catch (JSONException | IOException e) {
            Utils.handleException(e);
        }

        //noinspection CallToSystemGC
        System.gc();
    }

    public static int getDayType(String date) {
        Integer day = CALENDAR.get(date);
        return day == null ? -1 : day;
    }
}
