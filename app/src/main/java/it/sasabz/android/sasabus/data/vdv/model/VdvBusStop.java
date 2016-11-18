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

/**
 * Represents a bus stop.
 *
 * @author David Dejori
 */
public class VdvBusStop {

    private final int id;
    private int departure;

    public VdvBusStop(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getDeparture() {
        return departure;
    }

    public void setDeparture(int departure) {
        this.departure = departure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VdvBusStop busStop = (VdvBusStop) o;

        return id == busStop.id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public String getTime() {
        return Api.Time.toTime(departure);
    }
}
