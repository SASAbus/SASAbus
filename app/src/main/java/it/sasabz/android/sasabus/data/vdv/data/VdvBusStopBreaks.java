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

import it.sasabz.android.sasabus.data.vdv.model.VdvStopTime;

/**
 * Describes stop times for some specific trips (a few only) at a bus stop. A common example is line
 * 7B waiting in Perathoner Street for about 5 minutes until continue its journey.
 *
 * @author David Dejori
 */
public final class VdvBusStopBreaks {

    private static Map<VdvStopTime, Integer> STOP_TIMES;

    private VdvBusStopBreaks() {
    }

    /**
     * Loads the breaks done in some specific trips at a defined stop.
     *
     * @param jStopTimes the JSON data with the corresponding information
     */
    static void loadBreaks(JSONArray jStopTimes) throws Exception {
        Map<VdvStopTime, Integer> map = new HashMap<>();

        // iterates through all the stop times
        for (int i = 0; i < jStopTimes.length(); i++) {
            JSONObject jStopTime = jStopTimes.getJSONObject(i);
            map.put(new VdvStopTime(jStopTime.getInt("time_group"), jStopTime.getInt("bus_stop_id")),
                    jStopTime.getInt("stop_time"));
        }

        STOP_TIMES = Collections.unmodifiableMap(map);
    }

    /**
     * Retrieves how long a bus is stopping at a specific bus stop in its path. If there is
     * pre-planned break (most likely of some minutes) the method will return the seconds a bus
     * stops at that bus stop.
     *
     * @param timeGroup the time group (peak times or not and so on)
     * @param busStop   the bus stop in the trip
     * @return the amount of seconds the bus of the given trip waits at the given stop
     */
    public static int getStopTime(int timeGroup, int busStop) {
        Integer stopTime = STOP_TIMES.get(new VdvStopTime(timeGroup, busStop));
        return stopTime == null ? 0 : stopTime;
    }
}
