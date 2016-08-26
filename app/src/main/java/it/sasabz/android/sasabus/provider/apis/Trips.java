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

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import it.sasabz.android.sasabus.provider.API;
import it.sasabz.android.sasabus.provider.model.BusStop;
import it.sasabz.android.sasabus.provider.model.Trip;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Stores all trips executed by SASA SpA-AG. There are more than 20000.
 */
public final class Trips {
    private static final Collection<Trip> TRIPS = new ArrayList<>();

    private Trips() {
    }

    static void loadTrips(Context context, File dir) {
        if (!API.todayExists(context)) return;

        try {
            Calendar calendar = Calendar.getInstance();
            TimeZone timeZone = TimeZone.getTimeZone("Europe/Rome");

            if (timeZone.inDaylightTime(calendar.getTime())) {
                calendar.add(Calendar.MILLISECOND, timeZone.getDSTSavings());
            }

            if (calendar.getTimeInMillis() / 1000 % 86400 < 5400) {
                calendar.add(Calendar.DATE, -1);
            }

            // TODO: fix the following line to use the correct date even though the time is ok
            int today = CompanyCalendar.getDayType(API.DATE.format(calendar.getTime()));
            JSONArray jDepartures = new JSONArray(IOUtils.readFileAsString(new File(dir.getAbsolutePath(), "/REC_FRT.json")));

            for (int i = 0; i < jDepartures.length(); i++) {
                JSONObject jLine = jDepartures.getJSONObject(i);

                for (int j = 0; j < jLine.getJSONArray("tagesartlist").length(); j++) {
                    JSONObject jDay = jLine.getJSONArray("tagesartlist").getJSONObject(j);

                    if (jDay.getInt("TAGESART_NR") == today) {
                        for (int k = 0; k < jDay.getJSONArray("varlist").length(); k++) {
                            JSONObject jVariant = jDay.getJSONArray("varlist").getJSONObject(k);

                            for (int l = 0; l < jVariant.getJSONArray("triplist").length(); l++) {
                                JSONObject jDeparture = jVariant.getJSONArray("triplist").getJSONObject(l);

                                TRIPS.add(new Trip(Integer.parseInt(jLine.getString("LI_NR")), jVariant.getInt("STR_LI_VAR"),
                                        today, jDeparture.getInt("FRT_START"), jDeparture.getInt("FGR_NR"), jDeparture.getInt("FRT_FID")));
                            }
                        }
                    }
                }
            }
        } catch (JSONException | IOException e) {
            Utils.logException(e);
        }

        //noinspection CallToSystemGC
        System.gc();
    }

    @Nullable
    public static Trip getTrip(int id) {
        for (Trip trip : TRIPS) {
            if (trip.getTrip() == id) {
                return trip;
            }
        }

        return null;
    }

    @Nullable
    public static List<BusStop> getPath(Context context, int tripId) {
        Preconditions.checkNotNull(context, "getPath() context == null");

        Handler.load(context);

        for (Trip trip : TRIPS) {
            if (trip.getTrip() == tripId) {
                return trip.getPath();
            }
        }

        return null;
    }

    public static List<Trip> getTrips(int day, int line) {
        List<Trip> trips = new ArrayList<>();
        for (Trip trip : TRIPS) {
            if (trip.getLine() == line && trip.getDay() == day) {
                trips.add(trip);
            }
        }
        return trips;
    }

    static Iterable<Trip> getTrips(int day, int line, int variant) {
        Collection<Trip> trips = new ArrayList<>();
        for (Trip trip : TRIPS) {
            if (trip.getLine() == line && trip.getVariant() == variant && trip.getDay() == day) {
                trips.add(trip);
            }
        }
        return trips;
    }

    static Iterable<int[]> getCoursesPassingAt(BusStop station) {
        Collection<int[]> coursesPassingAtStation = new ArrayList<>();

        for (Map.Entry<Integer, HashMap<Integer, List<BusStop>>> line : Paths.getPaths().entrySet()) {
            for (Map.Entry<Integer, List<BusStop>> busStops : line.getValue().entrySet()) {
                if (busStops.getValue().contains(station) && busStops.getValue().indexOf(station) != busStops.getValue().size() - 1) {
                    coursesPassingAtStation.add(new int[]{
                            line.getKey(), busStops.getKey()
                    });
                }
            }
        }

        return coursesPassingAtStation;
    }
}