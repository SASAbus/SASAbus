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

package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds the planned trips.
 *
 * @author Alex Lardschneider
 */
public class PlannedTrip extends RealmObject {

    private int busStop;
    private long timeStamp;

    private String title;
    private String hash;
    private String lines;
    private String notifications;

    private int repeatDays;
    private int repeatWeeks;

    public int getBusStop() {
        return busStop;
    }

    public void setBusStop(int busStop) {
        this.busStop = busStop;
    }

    public long getTimestamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLines() {
        return lines;
    }

    public void setLines(String lines) {
        this.lines = lines;
    }

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    public int getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(int repeatDays) {
        this.repeatDays = repeatDays;
    }

    public int getRepeatWeeks() {
        return repeatWeeks;
    }

    public void setRepeatWeeks(int repeatWeeks) {
        this.repeatWeeks = repeatWeeks;
    }

    @Override
    public String toString() {
        return "PlannedTrip{" +
                "busStop=" + busStop +
                ", timeStamp=" + timeStamp +
                ", title='" + title + '\'' +
                ", hash='" + hash + '\'' +
                ", lines='" + lines + '\'' +
                ", notifications='" + notifications + '\'' +
                ", repeatDays=" + repeatDays +
                ", repeatWeeks=" + repeatWeeks +
                "} " + super.toString();
    }
}
