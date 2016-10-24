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

package it.sasabz.android.sasabus.data.vdv.model;

import it.sasabz.android.sasabus.data.vdv.Api;
import it.sasabz.android.sasabus.model.Departure;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.busstop.BusStop;

/**
 * Represents a tripId identifiable by an unique ID, in JSON format described with the parameter
 * 'FRT_FID'. This parameter is only unique for one day. It might repeat on another day.
 *
 * @author David Dejori
 */
public class VdvDeparture {

    private final int lineId;
    private final int time;
    private final VdvBusStop destination;
    private final int tripId;

    public VdvDeparture(int lineId, int time, VdvBusStop destination, int tripId) {
        this.lineId = lineId;
        this.time = time;
        this.destination = destination;
        this.tripId = tripId;
    }

    private String getLine() {
        return Lines.lidToName(lineId);
    }

    public int getLineId() {
        return lineId;
    }

    public VdvBusStop getDestination() {
        return destination;
    }

    public int getTripId() {
        return tripId;
    }

    private String getTime() {
        return Api.Time.toTime(time);
    }

    @SuppressWarnings("CovariantCompareTo")
    public int compareTo(VdvDeparture departure) {
        return time - departure.time;
    }

    public Departure asDeparture(int busStopId) {
        BusStop busStop = BusStopRealmHelper.getBusStop(busStopId);

        return new Departure(
                lineId,
                tripId,
                busStop.getFamily(),
                getLine(),
                getTime(),
                BusStopRealmHelper.getName(destination.getId())
        );
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VdvDeparture trip = (VdvDeparture) o;

        if (lineId != trip.lineId) return false;
        if (time != trip.time) return false;
        if (destination != trip.destination) return false;

        return tripId == trip.tripId;
    }

    @Override
    public int hashCode() {
        int result = lineId;
        result = 31 * result + time;
        result = 31 * result + destination.hashCode();
        result = 31 * result + tripId;
        return result;
    }
}
