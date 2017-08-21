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

    public static final int[] REFRESH_COLORS = {
            R.color.material_amber_500, R.color.material_red_500,
            R.color.material_green_500, R.color.material_indigo_500
    };

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
    public static final String EXTRA_TRIP = "EXTRA_TRIP";

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

    public static final String EXTRA_TRIP_ID = "com.davale.sasabus.EXTRA_TRIP_ID";


    public static final String BUNDLE_EMPTY_STATE_VISIBILITY =
            "com.davale.sasabus.BUNDLE_EMPTY_STATE_VISIBILITY";

    public static final String EXTRA_BUS_STOP_GROUP = "com.davale.sasabus.EXTRA_BUS_STOP_GROUP";
}
