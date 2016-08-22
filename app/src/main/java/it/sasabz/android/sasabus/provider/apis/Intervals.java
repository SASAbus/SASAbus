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

import it.sasabz.android.sasabus.provider.model.Interval;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Utils;

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
