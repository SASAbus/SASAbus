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

import java.util.Collections;
import java.util.List;

import it.sasabz.android.sasabus.data.vdv.data.VdvBusStopBreaks;
import it.sasabz.android.sasabus.data.vdv.data.VdvHandler;
import it.sasabz.android.sasabus.data.vdv.data.VdvIntervals;
import it.sasabz.android.sasabus.data.vdv.data.VdvPaths;
import it.sasabz.android.sasabus.data.vdv.data.VdvTripBreaks;

/**
 * Represents a tripId identifiable by an unique ID, in JSON format described with the parameter
 * 'FRT_FID'. This parameter is only unique for one day. It might repeat on another day.
 *
 * @author David Dejori
 */
public class VdvTrip {

    public static final VdvTrip empty = new VdvTrip(0, 0, 0, 0, 0);

    private final int lineId;
    private final int variant;
    private final int departure;
    private final int timeGroup;
    private final int tripId;

    private List<VdvBusStop> path;

    public VdvTrip(int lineId, int variant, int departure, int timeGroup, int tripId) {
        this.lineId = lineId;
        this.variant = variant;
        this.departure = departure;
        this.timeGroup = timeGroup;
        this.tripId = tripId;
    }

    public int getLineId() {
        return lineId;
    }

    public int getVariant() {
        return variant;
    }

    public int getDeparture() {
        return departure;
    }

    public int getTripId() {
        return tripId;
    }

    public List<VdvBusStop> calcPath() {
        VdvHandler.blockTillLoaded();

        if (path == null) {
            path = Collections.unmodifiableList(VdvPaths.getPath(lineId, variant));
        }

        return path;
    }

    public List<VdvBusStop> calcTimedPath() {
        List<VdvBusStop> path = calcPath();

        if (!path.isEmpty()) {
            path.get(0).setDeparture(departure);

            for (int i = 1; i < path.size(); i++) {
                VdvBusStop last = path.get(i - 1);
                VdvBusStop current = path.get(i);

                current.setDeparture(
                        last.getDeparture() +
                                VdvIntervals.getInterval(timeGroup, last.getId(), current.getId()) +
                                VdvBusStopBreaks.getStopTime(timeGroup, current.getId()) +
                                VdvTripBreaks.getStopTime(tripId, current.getId())
                );
            }

            return Collections.unmodifiableList(path);
        }

        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VdvTrip trip = (VdvTrip) o;

        if (lineId != trip.lineId) return false;
        if (variant != trip.variant) return false;
        if (departure != trip.departure) return false;
        if (timeGroup != trip.timeGroup) return false;
        return tripId == trip.tripId;
    }

    @Override
    public int hashCode() {
        int result = lineId;
        result = 31 * result + variant;
        result = 31 * result + departure;
        result = 31 * result + timeGroup;
        result = 31 * result + tripId;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(tripId);
    }
}
