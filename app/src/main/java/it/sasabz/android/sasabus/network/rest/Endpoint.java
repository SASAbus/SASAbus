package it.sasabz.android.sasabus.network.rest;

import it.sasabz.android.sasabus.network.NetUtils;

/**
 * Holds all the rest api endpoint URLs.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public final class Endpoint {

    public static final String API = NetUtils.HOST + "/v1/";

    public static final String REALTIME = "realtime/{language}";
    public static final String REALTIME_VEHICLE = "realtime/vehicle/{id}";
    public static final String REALTIME_DELAYS = "realtime/delays";
    public static final String REALTIME_LINE = "realtime/line/{id}";
    public static final String REALTIME_TRIP = "realtime/trip/{id}";

    public static final String NEWS = "news/{language}";
    public static final String PARKING = "parking/{language}";
    public static final String PATHS = "paths/{id}";
    public static final String ROUTE = "route/{language}/from/{from}/to/{to}/on/{date}/at/{time}/walk/{walk}/results/{results}";
    public static final String TOKEN = "gcm/tokens/{token}";
    public static final String BEACONS = "beacons";

    public static final String REPORT = "report/{type}";
    public static final String SURVEY = "survey";

    public static final String TRIPS_VEHICLE = "vehicles/id/{id}/trips";

    public static final String LINES_ALL = "lines/{language}";
    public static final String LINES_HYDROGEN = "realtime/h2";
    public static final String LINES_FILTER = "lines/{language}/line/{lines}";
    public static final String LINES = "lines/{language}/line/{id}";

    public static final String VALIDITY_DATA = "validity/data/{date}";
    public static final String VALIDITY_TIMETABLES = "validity/timetables/{date}";

    public static final String CLOUD_TRIPS = "sync/trips";
    public static final String CLOUD_TRIPS_DELETE = "sync/trips/{hash}";
    public static final String CLOUD_PLANNED_TRIPS = "sync/planned_trips";
    public static final String CLOUD_PLANNED_TRIPS_DELETE = "sync/planned_trips/{hash}";

    private Endpoint() {
    }
}
