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

/**
 * Represents a time interval between two bus stops.
 *
 * @author David Dejori
 */
public class VdvInterval {

    private final int timeGroup;
    private final int origin;
    private final int destination;

    public VdvInterval(int timeGroup, int origin, int destination) {
        this.timeGroup = timeGroup;
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VdvInterval interval = (VdvInterval) o;

        if (timeGroup != interval.timeGroup) return false;
        if (origin != interval.origin) return false;
        return destination == interval.destination;
    }

    @Override
    public int hashCode() {
        int result = timeGroup;
        result = 31 * result + origin;
        result = 31 * result + destination;
        return result;
    }
}
