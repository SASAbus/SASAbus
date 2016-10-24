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
 * Describes the stop times at a bus stop for some specific trips. Those trips are identifiable by
 * their unique ID being 'FRT_FID' in the SASA SpA-AG open data.
 *
 * @author David Dejori
 */
public final class VdvTripBreaks {

    private static Map<VdvStopTime, Integer> STOP_TIMES;

    private VdvTripBreaks() {
    }

    /**
     * Loads the pre-planned stop times at some bus stops in some pre-defined trips.
     *
     * @param jHaltTimes the JSON data with the corresponding information
     */
    static void loadBreaks(JSONArray jHaltTimes) throws Exception {
        Map<VdvStopTime, Integer> map = new HashMap<>();

        for (int i = 0; i < jHaltTimes.length(); i++) {
            JSONObject jStopTime = jHaltTimes.getJSONObject(i);
            map.put(new VdvStopTime(jStopTime.getInt("trip_id"),
                    jStopTime.getInt("bus_stop_id")), jStopTime.getInt("stop_time"));
        }

        STOP_TIMES = Collections.unmodifiableMap(map);
    }

    /**
     * Retrieves how long a bus is stopping at a specific bus stop in its path. When a bus just
     * passes a bus stop (which applies to the most cases), stops and departs again (in order to let
     * the passengers get onto and off the bus), that is not referred as a break and thus the method
     * will return 0 for nearly all stops. Only if there is a pre-planned break (most likely of some
     * minutes) the method will return the seconds a bus stops at that bus stop.
     *
     * @param tripId  the ID of a trip
     * @param busStop the bus stop in the trip
     * @return the amount of seconds the bus of the given trip waits at the given stop
     */
    public static int getStopTime(int tripId, int busStop) {
        Integer stopTime = STOP_TIMES.get(new VdvStopTime(tripId, busStop));
        return stopTime == null ? 0 : stopTime;
    }
}
