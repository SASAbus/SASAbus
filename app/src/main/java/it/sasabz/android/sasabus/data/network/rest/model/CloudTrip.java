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

package it.sasabz.android.sasabus.data.network.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class  CloudTrip {

    private final String hash;
    private final int line;
    private final int variant;
    private final int trip;
    private final int vehicle;
    private final int origin;
    private final int destination;
    private final int departure;
    private final int arrival;
    private final List<Integer> path;

    public CloudTrip(String hash, int line, int variant, int trip, int vehicle, int origin,
                     int destination, int departure, int arrival, List<Integer> path) {

        this.hash = hash;
        this.line = line;
        this.variant = variant;
        this.trip = trip;
        this.vehicle = vehicle;
        this.origin = origin;
        this.destination = destination;
        this.departure = departure;
        this.arrival = arrival;
        this.path = path;
    }

    // TODO: 08/05/16 Use the diesel price when starting a trip
    @SerializedName("diesel_price")
    private float dieselPrice;

    public String getHash() {
        return hash;
    }

    public int getLine() {
        return line;
    }

    public int getVariant() {
        return variant;
    }

    public int getTrip() {
        return trip;
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getOrigin() {
        return origin;
    }

    public int getDestination() {
        return destination;
    }

    public int getDeparture() {
        return departure;
    }

    public int getArrival() {
        return arrival;
    }

    public List<Integer> getPath() {
        return path;
    }

    public float getDieselPrice() {
        return dieselPrice;
    }

    @Override
    public String toString() {
        return "CloudTrip{" +
                "hash='" + hash + '\'' +
                ", title=" + line +
                ", variant=" + variant +
                ", trip=" + trip +
                ", vehicle=" + vehicle +
                ", origin=" + origin +
                ", destination=" + destination +
                ", departure=" + departure +
                ", arrival=" + arrival +
                ", path=" + path +
                ", dieselPrice=" + dieselPrice +
                '}';
    }
}
