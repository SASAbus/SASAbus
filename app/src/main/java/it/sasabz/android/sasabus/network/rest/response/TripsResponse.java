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

package it.sasabz.android.sasabus.network.rest.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TripsResponse {

    @SerializedName("trip")
    public Trip trip;

    public int getLineId() {
        return trip.lineId;
    }

    public int getVariant() {
        return trip.variant;
    }

    public int getTrip() {
        return trip.trip;
    }

    public int getBusStop() {
        return trip.busStop;
    }

    public List<Integer> getPath() {
        return trip.path;
    }

    @Override
    public String toString() {
        return trip != null ? trip.toString() : "trip: null";
    }

    public static class Trip {

        @SerializedName("line_id")
        private int lineId;

        @SerializedName("variant")
        private int variant;

        @SerializedName("id")
        private int trip;

        @SerializedName("bus_stop")
        private int busStop;

        @SerializedName("path")
        private List<Integer> path;

        @Override
        public String toString() {
            return "Trip{" +
                    "lineId=" + lineId +
                    ", variant=" + variant +
                    ", trip=" + trip +
                    ", busStop=" + busStop +
                    ", path=" + path +
                    '}';
        }
    }
}
