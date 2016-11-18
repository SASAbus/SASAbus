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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.sasabz.android.sasabus.data.vdv.model.VdvInterval;

/**
 * Retrieves travel times between two bus stops (depending from driving speed severity as described
 * by 'FGR_NR'). Travel times are stored in seenSeconds but are always full minutes, e. g. 0, 60, 120...
 *
 * @author David Dejori
 */
public final class VdvIntervals {

    private static Map<VdvInterval, Integer> INTERVALS;

    private VdvIntervals() {
    }

    /**
     * Loads the travel times between two bus stops. The times are given in seconds. They may defer
     * for the same two bus stops at a different time as there are on-peak and off-peak schedules.
     *
     * @param jIntervals the JSON data with the corresponding information
     */
    public static void loadIntervals(JSONArray jIntervals) throws Exception {
        Map<VdvInterval, Integer> map = new HashMap<>();

        for (int i = 0; i < jIntervals.length(); i++) {
            JSONObject jInterval = jIntervals.getJSONObject(i);

            map.put(new VdvInterval(
                    jInterval.getInt("time_group"),
                    jInterval.getInt("origin_id"),
                    jInterval.getInt("destination_id")
            ), jInterval.getInt("travel_time"));
        }

        INTERVALS = Collections.unmodifiableMap(map);
    }

    public static int getInterval(int timeGroup, int origin, int destination) {
        return INTERVALS.get(new VdvInterval(timeGroup, origin, destination));
    }
}
