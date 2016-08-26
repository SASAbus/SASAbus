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

package it.sasabz.android.sasabus.beacon.busstop;

import java.util.Date;

import it.sasabz.android.sasabus.beacon.Beacon;

/**
 * Model which represents a bus stop beacon and holds information about it.
 *
 * @author Alex Lardschneider
 */
public class BusStopBeacon implements Beacon {

    public final int id;
    private final Date startDate;

    long seenSeconds;
    long lastSeen;

    boolean isNotificationShown;

    public double distance;

    BusStopBeacon(int id) {
        this.id = id;
        startDate = new Date();

        seen();
    }

    /**
     * Sets the current distance from the device to the beacon.
     *
     * @param distance the distance.
     */
    void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Updates {@link #seenSeconds} and {@link #lastSeen} to the current time stamp.
     */
    @Override
    public void seen() {
        long millis = System.currentTimeMillis();

        seenSeconds = (millis - startDate.getTime()) / 1000;
        lastSeen = millis;
    }

    /**
     * Sets {@link #isNotificationShown} to {@code true}.
     */
    void setNotificationShown() {
        isNotificationShown = true;
    }
}