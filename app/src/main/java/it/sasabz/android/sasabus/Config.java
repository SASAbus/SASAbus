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

package it.sasabz.android.sasabus;

/**
 * Holds common constants for passing data between two {@link android.app.Activity} or saving data
 * to {@link android.os.Bundle}.
 *
 * @author Alex Lardschneider
 */
public final class Config {

    /**
     * Empty constructor to prevent initialization.
     */
    private Config() {
    }

    public static final int NOTIFICATION_SURVEY = 1 << 20;

    public static final int NOTIFICATION_TRIP_SUCCESS = 1 << 19;

    public static final int NOTIFICATION_BUS = 1 << 18;

    public static final int NOTIFICATION_TRIP_DEPARTURE = 1 << 17;

    public static final int NOTIFICATION_PLANNED_TRIP_DEPARTURE_AT = 1 << 16;

    public static final int NOTIFICATION_PLANNED_TRIP_DEPARTURE_IN = 1 << 15;

    public static final int SYNC_ALARM_ID = 1 << 14;

    public static final int NOTIFICATION_BADGE = 1 << 13;

    /**
     * The delay between the line fragments finishing loading and starting the internet data fetch.
     */
    public static final int LINE_FRAGMENTS_POST_DELAY = 500;

    public static final int BUS_STOP_FRAGMENTS_POST_DELAY = 500;

    /**
     * Static constant for intent extras which indicate a station id
     */
    public static final String EXTRA_STATION_ID = "EXTRA_STATION_ID";

    /**
     * Static constant for route arrival id
     * used to pass the station id to route results
     */
    public static final String EXTRA_LINE_ID = "EXTRA_LINE_ID";

    public static final String EXTRA_LINE = "EXTRA_LINE";

    public static final String EXTRA_BADGE = "com.davale.sasabus.EXTRA_BADGE";

    /**
     * Static constant for intent extras which indicate a vehicle id
     */
    public static final String EXTRA_VEHICLE = "EXTRA_VEHICLE";

    /**
     * Static constant for intent extras
     */
    public static final String EXTRA_DISPLAY_BUS = "EXTRA_DISPLAY_BUS";

    /**
     * Static constant for route departure id used to pass
     * the station id to route results
     */
    public static final String EXTRA_DEPARTURE_ID = "EXTRA_DEPARTURE_ID";

    /**
     * Static constant for route arrival id
     * used to pass the station id to route results
     */
    public static final String EXTRA_ARRIVAL_ID = "EXTRA_ARRIVAL_ID";

    /**
     * Static constant for a station intent extra
     */
    public static final String EXTRA_STATION = "EXTRA_STATION";

    /**
     * Static constant for a trip hash intent extra
     */
    public static final String EXTRA_TRIP_HASH = "EXTRA_TRIP_HASH";

    /**
     * Static constant for a planned trip hash intent extra
     */
    public static final String EXTRA_PLANNED_TRIP_HASH = "EXTRA_PLANNED_TRIP_HASH";

    /**
     * Static constant for a news notification intent extra
     */
    public static final String EXTRA_SHOW_NEWS = "EXTRA_SHOW_NEWS";

    /**
     * Static constant for a news notification zone intent extra
     */
    public static final String EXTRA_NEWS_ZONE = "EXTRA_NEWS_ZONE";

    /**
     * Static constant for a news notification intent extra
     */
    public static final String EXTRA_NEWS_ID = "EXTRA_NEWS_ID";

    /**
     * Static constant for a {@link java.util.ArrayList} which needs to be saved
     * in {@link android.os.Bundle saved instance} to restore later
     */
    public static final String BUNDLE_LIST = "BUNDLE_LIST";

    /**
     * Static constant for error wifi visibility which need to be saved
     * in {@link android.os.Bundle saved instance} to restore later
     */
    public static final String BUNDLE_ERROR_WIFI = "BUNDLE_ERROR_WIFI";

    /**
     * Static constant for error general visibility which need to be saved
     * in {@link android.os.Bundle saved instance} to restore later
     */
    public static final String BUNDLE_ERROR_GENERAL = "BUNDLE_ERROR_GENERAL";

    /**
     * Integer constant to define a bus stop detail item which has no delay, so the adapter
     * can hide the delay {@link android.widget.TextView}.
     */
    public static final int BUS_STOP_DETAILS_NO_DELAY = 1 << 10;

    /**
     * Integer constant to define a bus stop detail which is currently fetching delay data
     * from the internet.
     */
    public static final int BUS_STOP_DETAILS_OPERATION_RUNNING = 1 << 9;
}
