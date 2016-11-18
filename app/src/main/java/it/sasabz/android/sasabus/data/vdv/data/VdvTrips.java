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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import it.sasabz.android.sasabus.data.vdv.model.VdvTrip;
import timber.log.Timber;

/**
 * Stores all trips executed by SASA SpA-AG.
 *
 * @author David Dejori
 */
public final class VdvTrips {

    private static JSONArray jDepartures;
    private static Collection<VdvTrip> TRIPS = new ArrayList<>();

    private VdvTrips() {
    }

    public static void loadTrips(JSONArray jDepartures, int dayId) throws Exception {
        Timber.w("Loading trips of day %d", dayId);

        if (jDepartures != null) {
            VdvTrips.jDepartures = jDepartures;
        }

        Collection<VdvTrip> trips = new ArrayList<>();

        for (int i = 0; i < VdvTrips.jDepartures.length(); i++) {
            JSONObject jLine = VdvTrips.jDepartures.getJSONObject(i);

            for (int j = 0; j < jLine.getJSONArray("days").length(); j++) {
                JSONObject jDay = jLine.getJSONArray("days").getJSONObject(j);

                if (jDay.getInt("day_id") == dayId) {
                    for (int k = 0; k < jDay.getJSONArray("variants").length(); k++) {
                        JSONObject jVariant = jDay.getJSONArray("variants").getJSONObject(k);

                        for (int l = 0; l < jVariant.getJSONArray("trips").length(); l++) {
                            JSONObject jDeparture = jVariant.getJSONArray("trips")
                                    .getJSONObject(l);

                            trips.add(new VdvTrip(jLine.getInt("line_id"),
                                    jVariant.getInt("variant_id"),
                                    jDeparture.getInt("departure"),
                                    jDeparture.getInt("time_group"),
                                    jDeparture.getInt("trip_id"))
                            );
                        }
                    }
                }
            }
        }

        TRIPS = Collections.unmodifiableCollection(trips);
    }

    public static Collection<VdvTrip> ofSelectedDay() {
        return TRIPS;
    }
}