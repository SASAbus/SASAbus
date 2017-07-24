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

package it.sasabz.android.sasabus.data.network.rest.model;

import com.davale.sasabus.core.util.Strings;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.sasabz.android.sasabus.data.realm.user.PlannedTrip;

public class CloudPlannedTrip {

    private final String hash;
    private final String title;

    @SerializedName("timestamp")
    private final long timeStamp;

    private final List<Integer> lines;
    private final List<Integer> notifications;

    @SerializedName("bus_stop")
    private final int busStop;

    @SerializedName("repeat_days")
    private final int repeatDays;

    @SerializedName("repeat_weeks")
    private final int repeatWeeks;

    public CloudPlannedTrip(PlannedTrip trip) {
        hash = trip.getHash();
        title = trip.getTitle();
        timeStamp = trip.getTimestamp();
        busStop = trip.getBusStop();
        lines = Strings.stringToList(trip.getLines(), Strings.DEFAULT_DELIMITER);
        notifications = Strings.stringToList(trip.getNotifications(), Strings.DEFAULT_DELIMITER);
        repeatDays = trip.getRepeatDays();
        repeatWeeks = trip.getRepeatWeeks();
    }

    public String getHash() {
        return hash;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getBusStop() {
        return busStop;
    }

    public List<Integer> getLines() {
        return lines;
    }

    public List<Integer> getNotifications() {
        return notifications;
    }

    public int getRepeatDays() {
        return repeatDays;
    }

    public int getRepeatWeeks() {
        return repeatWeeks;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "CloudPlannedTrip{" +
                "hash='" + hash + '\'' +
                ", title='" + title + '\'' +
                ", timeStamp=" + timeStamp +
                ", lines=" + lines +
                ", notifications=" + notifications +
                ", busStop=" + busStop +
                ", repeatDays=" + repeatDays +
                ", repeatWeeks=" + repeatWeeks +
                '}';
    }
}
