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

package it.sasabz.android.sasabus.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

public class Departure implements Parcelable {

    public static final int NO_DELAY = 1 << 12;
    public static final int OPERATION_RUNNING = 1 << 11;

    public final int lineId;
    public final int trip;
    public final int busStopGroup;

    public final String line;
    public final String destination;

    public final String time;
    public Spanned formattedTime;

    public int delay;
    public int vehicle;
    public int currentBusStop;

    public Departure(int lineId, int trip, int busStopGroup, String line, String time, String destination) {
        this.lineId = lineId;
        this.trip = trip;
        this.line = line;
        this.time = time;
        this.busStopGroup = busStopGroup;
        this.destination = destination;

        delay = OPERATION_RUNNING;
    }

    private Departure(Parcel in) {
        lineId = in.readInt();
        trip = in.readInt();
        line = in.readString();
        time = in.readString();
        destination = in.readString();
        delay = in.readInt();
        vehicle = in.readInt();
        busStopGroup = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(lineId);
        dest.writeInt(trip);
        dest.writeString(line);
        dest.writeString(time);
        dest.writeString(destination);
        dest.writeInt(delay);
        dest.writeInt(vehicle);
        dest.writeInt(busStopGroup);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Departure> CREATOR = new Creator<Departure>() {
        @Override
        public Departure createFromParcel(Parcel in) {
            return new Departure(in);
        }

        @Override
        public Departure[] newArray(int size) {
            return new Departure[size];
        }
    };
}