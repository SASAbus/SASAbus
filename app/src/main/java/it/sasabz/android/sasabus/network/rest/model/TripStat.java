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

package it.sasabz.android.sasabus.network.rest.model;

import com.google.gson.annotations.SerializedName;

public class TripStat {

    @SerializedName("time")
    private int time;

    @SerializedName("trip")
    private int trip;

    @SerializedName("line_id")
    private int lineId;

    @SerializedName("line_name")
    private String lineName;

    @SerializedName("variant")
    private int variant;

    @SerializedName("inserted")
    private int inserted;

    @SerializedName("departure")
    private int departure;

    @SerializedName("origin")
    private int origin;

    @SerializedName("destination")
    private int destination;

    @SerializedName("zone")
    private String zone;

    public int getTime() {
        return time;
    }

    public int getTrip() {
        return trip;
    }

    public int getLineId() {
        return lineId;
    }

    public String getLineName() {
        return lineName;
    }

    public int getVariant() {
        return variant;
    }

    public int getInserted() {
        return inserted;
    }

    public int getDeparture() {
        return departure;
    }

    public int getOrigin() {
        return origin;
    }

    public int getDestination() {
        return destination;
    }

    public String getZone() {
        return zone;
    }
}