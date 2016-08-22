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
 * Represents the stop time of a bus at a specific stop.
 *
 * @author David Dejori
 */
public class StopTime {

    private final int id;
    private final int stop;

    public StopTime(int id, int stop) {
        this.id = id;
        this.stop = stop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StopTime stopTime = (StopTime) o;
        return id == stopTime.id && stop == stopTime.stop;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + stop;
        return result;
    }
}
