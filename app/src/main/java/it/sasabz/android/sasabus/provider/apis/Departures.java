package it.sasabz.android.sasabus.provider.apis;

import it.sasabz.android.sasabus.provider.ApiUtils;
import it.sasabz.android.sasabus.provider.model.BusStop;
import it.sasabz.android.sasabus.provider.model.Trip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
