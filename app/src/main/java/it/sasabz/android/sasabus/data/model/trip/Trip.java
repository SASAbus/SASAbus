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

package it.sasabz.android.sasabus.data.model.trip;

import com.davale.sasabus.core.util.Strings;

import it.sasabz.android.sasabus.data.network.rest.model.CloudTrip;

public class Trip {

    private final int line;
    private final int variant;
    private final int trip;
    private final int vehicle;
    private final int startStation;
    private final int stopStation;
    private final long startTime;
    private final long stopTime;
    private final String tripList;
    private final String hash;

    private String origin;
    private String destination;

    public Trip(String hash, int lineId, int variant, int tripId, int vehicle,
                int startStation, int stopStation, long startTime, long stopTime, String tripList) {

        line = lineId;
        this.variant = variant;
        trip = tripId;
        this.vehicle = vehicle;
        this.startStation = startStation;
        this.stopStation = stopStation;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.tripList = tripList;
        this.hash = hash;
    }

    public Trip(CloudTrip trip) {
        line = trip.getLine();
        variant = trip.getVariant();
        this.trip = trip.getTrip();
        vehicle = trip.getVehicle();
        startStation = trip.getOrigin();
        stopStation = trip.getDestination();
        startTime = trip.getDeparture();
        stopTime = trip.getArrival();
        tripList = Strings.listToString(trip.getPath(), Strings.DEFAULT_DELIMITER);
        hash = trip.getHash();
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

    public int getStartStation() {
        return startStation;
    }

    public int getStopStation() {
        return stopStation;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public String getTripList() {
        return tripList;
    }

    public String getHash() {
        return hash;
    }

    public CharSequence getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public CharSequence getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trip trip = (Trip) o;

        return hash != null ? hash.equals(trip.hash) : trip.hash == null;

    }

    @Override
    public int hashCode() {
        return hash != null ? hash.hashCode() : 0;
    }

    @Override
    public String toString() {
        return hash;
    }
}
