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

package it.sasabz.android.sasabus.model;

public class ExecutedTrip {

    private final long date;
    private final int tripId;
    private final int lineId;
    private final String line;
    private final int departure;
    private final String firstStop;
    private final String lastStop;

    public ExecutedTrip(long date, int tripId, int lineId, String line, int departure, String firstStop, String lastStop) {
        this.date = date;
        this.tripId = tripId;
        this.lineId = lineId;
        this.line = line;
        this.departure = departure;
        this.firstStop = firstStop;
        this.lastStop = lastStop;
    }

    public long getDate() {
        return date;
    }

    public int getTripId() {
        return tripId;
    }

    public int getLineId() {
        return lineId;
    }

    public CharSequence getLine() {
        return line;
    }

    public int getDeparture() {
        return departure;
    }

    public CharSequence getFirstStop() {
        return firstStop;
    }

    public CharSequence getLastStop() {
        return lastStop;
    }
}
