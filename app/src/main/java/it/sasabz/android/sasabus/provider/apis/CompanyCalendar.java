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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Utils;

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
