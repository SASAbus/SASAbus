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
    public static final String PARKING_ID = "parking/{language}/id/{id}";
    public static final String PATHS = "paths/{id}";
    public static final String ROUTE = "route/{language}/from/{from}/to/{to}/on/{date}/at/{time}/walk/{walk}/results/{results}";
    public static final String TOKEN = "gcm/tokens/{token}";
    public static final String BEACONS = "beacons";
    public static final String TRAFFIC_LIGHT = "traffic_light/{language}/{city}";

    public static final String USER_LOGIN = "auth/login";
    public static final String USER_REGISTER = "auth/register";
    public static final String USER_LOGOUT = "auth/logout";
    public static final String USER_LOGOUT_ALL = "auth/logout/all";
    public static final String USER_DELETE = "auth/delete";
    public static final String USER_VERIFY = "auth/verify/{email}/{token}";
    public static final String CHANGE_PASSWORD = "auth/password/change";

    public static final String ECO_POINTS_BADGES = "eco/badges";
    public static final String ECO_POINTS_BADGES_NEXT = "eco/badges/next";
    public static final String ECO_POINTS_BADGES_EARNED = "eco/badges/earned";
    public static final String ECO_POINTS_BADGES_SEND = "eco/badges/earned/{id}";

    public static final String ECO_POINTS_LEADERBOARD = "eco/leaderboard/page/{page}";
    public static final String ECO_POINTS_PROFILE = "eco/profile";
    public static final String ECO_POINTS_PROFILE_ID = "eco/profile/{id}";

    public static final String ECO_POINTS_PROFILE_PICTURE_DEFAULT = "eco/profile/default";
    public static final String ECO_POINTS_PROFILE_PICTURE_CUSTOM = "eco/profile/custom";
    public static final String ECO_POINTS_PROFILE_PICTURE_USER = "assets/images/profile_pictures/";

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

    private Endpoint() {
    }
}
