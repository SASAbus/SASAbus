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

package it.sasabz.android.sasabus.provider.model;

/**
 * Represents a time interval between two bus stops.
 *
 * @author David Dejori
 */
public class Interval {

    private final int fgr;
    private final int busStop1;
    private final int busStop2;

    public Interval(int fgr, int busStop1, int busStop2) {
        this.fgr = fgr;
        this.busStop1 = busStop1;
        this.busStop2 = busStop2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interval interval = (Interval) o;
        return fgr == interval.fgr && busStop1 == interval.busStop1 && busStop2 == interval.busStop2;
    }

    @Override
    public int hashCode() {
        int result = fgr;
        result = 31 * result + busStop1;
        result = 31 * result + busStop2;
        return result;
    }
}
