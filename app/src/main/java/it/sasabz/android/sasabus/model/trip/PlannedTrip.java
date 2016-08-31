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

package it.sasabz.android.sasabus.model.trip;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import it.sasabz.android.sasabus.network.rest.model.CloudPlannedTrip;
import it.sasabz.android.sasabus.util.Strings;

public class PlannedTrip implements Parcelable {

    public static final int FLAG_MONDAY = 0x40000000;
    public static final int FLAG_TUESDAY = 0x20000000;
    public static final int FLAG_WEDNESDAY = 0x10000000;
    public static final int FLAG_THURSDAY = 0x08000000;
    public static final int FLAG_FRIDAY = 0x04000000;
    public static final int FLAG_SATURDAY = 0x02000000;
    public static final int FLAG_SUNDAY = 0x01000000;

    private int busStop;

    private long timestamp;

    private String title;
    private String hash;

    private List<Integer> lines;
    private List<Integer> notifications;

    private int repeatDays;
    private int repeatWeeks;

    private int lineId;
    private int time;
    private int tripId;

    public PlannedTrip(int busStop, long timestamp, String title, String hash,
                       List<Integer> lines, List<Integer> notifications, int repeatDays,
                       int repeatWeeks) {

        this.busStop = busStop;
        this.timestamp = timestamp;

        this.title = title;
        this.hash = hash;

        this.lines = lines;
        this.repeatDays = repeatDays;
        this.repeatWeeks = repeatWeeks;
        this.notifications = notifications;
    }

    private PlannedTrip(Parcel in) {
        busStop = in.readInt();
        timestamp = in.readLong();

        title = in.readString();
        hash = in.readString();

        lines = in.readArrayList(Integer.class.getClassLoader());
        notifications = in.readArrayList(Integer.class.getClassLoader());

        repeatDays = in.readInt();
        repeatWeeks = in.readInt();

        lineId = in.readInt();
        time = in.readInt();
        tripId = in.readInt();
    }

    public PlannedTrip(it.sasabz.android.sasabus.realm.user.PlannedTrip plannedTrip) {
        busStop = plannedTrip.getBusStop();
        timestamp = plannedTrip.getTimestamp();
        title = plannedTrip.getTitle();
        hash = plannedTrip.getHash();
        lines = Strings.stringToList(plannedTrip.getLines(), Strings.DEFAULT_DELIMITER);
        notifications = Strings.stringToList(plannedTrip.getNotifications(), Strings.DEFAULT_DELIMITER);
        repeatDays = plannedTrip.getRepeatDays();
        repeatWeeks = plannedTrip.getRepeatWeeks();
    }

    public PlannedTrip(CloudPlannedTrip trip) {
        busStop = trip.getBusStop();
        timestamp = trip.getTimeStamp();
        title = trip.getTitle();
        hash = trip.getHash();
        lines = trip.getLines();
        notifications = trip.getNotifications();
        repeatDays = trip.getRepeatDays();
        repeatWeeks = trip.getRepeatWeeks();
    }

    public static final Creator<PlannedTrip> CREATOR = new Creator<PlannedTrip>() {
        @Override
        public PlannedTrip createFromParcel(Parcel in) {
            return new PlannedTrip(in);
        }

        @Override
        public PlannedTrip[] newArray(int size) {
            return new PlannedTrip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(busStop);
        dest.writeLong(timestamp);

        dest.writeString(title);
        dest.writeString(hash);

        dest.writeList(lines);
        dest.writeList(notifications);

        dest.writeInt(repeatDays);
        dest.writeInt(repeatWeeks);

        dest.writeInt(lineId);
        dest.writeInt(time);
        dest.writeInt(tripId);
    }

    public PlannedTrip() {
    }

    public int getBusStop() {
        return busStop;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setBusStop(int busStop) {
        this.busStop = busStop;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Integer> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Integer> notifications) {
        this.notifications = notifications;
    }

    public List<Integer> getLines() {
        return lines;
    }

    public void setLines(List<Integer> lines) {
        this.lines = lines;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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
                ", timestamp=" + timestamp +
                ", title='" + title + '\'' +
                ", hash='" + hash + '\'' +
                ", lines=" + lines +
                ", notifications=" + notifications +
                ", repeatDays=" + repeatDays +
                ", repeatWeeks=" + repeatWeeks +
                ", lineId=" + lineId +
                ", time=" + time +
                ", tripId=" + tripId +
                '}';
    }
}
