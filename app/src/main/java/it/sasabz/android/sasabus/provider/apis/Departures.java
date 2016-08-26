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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.sasabz.android.sasabus.provider.ApiUtils;
import it.sasabz.android.sasabus.provider.model.BusStop;
import it.sasabz.android.sasabus.provider.model.Trip;

/**
 * Retrieves the departing buses at a specific bus stop. The data is taken from SASA SpA-AG.
 *
 * @author David Dejori
 */
public final class Departures {

    private Departures() {
    }

    public static List<Trip> getDepartures(String date, String time, int stop) {
        int seconds = ApiUtils.getSeconds(time);
        List<Trip> trips = new ArrayList<>();

        // finds all the lines/variants passing at the stop
        for (int[] course : Trips.getCoursesPassingAt(new BusStop(stop))) {
            for (Trip trip : Trips.getTrips(CompanyCalendar.getDayType(date), course[0], course[1])) {
                if (trip.getSecondsAtStation(stop) >= seconds) {
                    trips.add(trip);
                }
            }
        }

        // sorts trips by time at stop in path
        Collections.sort(trips, (t1, t2) -> t1.getSecondsAtStation(stop) - t2.getSecondsAtStation(stop));

        return trips;
    }

    public static List<Trip> getDepartures(String date, String time, Iterable<BusStop> stops) {
        int seconds = ApiUtils.getSeconds(time);
        List<Trip> trips = new ArrayList<>();

        // finds all the lines/variants passing at the stop
        for (BusStop stop : stops) {
            int id = stop.getId();
            for (int[] course : Trips.getCoursesPassingAt(stop)) {
                for (Trip trip : Trips.getTrips(CompanyCalendar.getDayType(date), course[0], course[1])) {
                    if (trip.getSecondsAtStation(id) >= seconds) {
                        trips.add(trip);
                    }
                }
            }
        }

        // sorts trips by time at stop in path
        Collections.sort(trips, (t1, t2) -> t1.getSecondsAtUserStop() - t2.getSecondsAtUserStop());

        return trips;
    }
}
