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

import it.sasabz.android.sasabus.provider.ApiUtils;

/**
 * Represents a departure from a bus stop, not considering the real-time delays. This only uses
 * the planned offline open data (in JSON format) of SASA SpA-AG.
 *
 * @author David Dejori
 */
public class PlannedDeparture {

    private final int line;
    private final int time;
    private final int trip;

    public PlannedDeparture(int line, int time, int trip) {
        this.line = line;
        this.time = time;
        this.trip = trip;
    }

    public int getLine() {
        return line;
    }

    public int getTime() {
        return time;
    }

    public int getTrip() {
        return trip;
    }

    @Override
    public String toString() {
        return "PlannedDeparture{" +
                "line=" + line +
                ", time=" + ApiUtils.getTime(time) +
                ", trip=" + trip +
                '}';
    }
}
