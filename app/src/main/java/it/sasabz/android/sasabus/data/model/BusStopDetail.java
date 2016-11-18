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

public class BusStopDetail implements Parcelable {

    private final int lineId;
    private final int trip;
    private final String line;
    private final String time;
    private final String lastStation;
    private final String additionalData;
    private int delay;
    private int vehicle;

    private boolean reveal;

    public BusStopDetail(int lineId, int tripId, String line, String time, String lastStation,
                         int delay, String additionalData) {

        this.lineId = lineId;
        this.trip = tripId;
        this.line = line;
        this.time = time;
        this.lastStation = lastStation;
        this.additionalData = additionalData;
        this.delay = delay;
    }

    private BusStopDetail(Parcel in) {
        lineId = in.readInt();
        trip = in.readInt();
        line = in.readString();
        time = in.readString();
        lastStation = in.readString();
        additionalData = in.readString();
        delay = in.readInt();
        vehicle = in.readInt();

        reveal = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(lineId);
        dest.writeInt(trip);
        dest.writeString(line);
        dest.writeString(time);
        dest.writeString(lastStation);
        dest.writeString(additionalData);
        dest.writeInt(delay);
        dest.writeInt(vehicle);

        dest.writeByte((byte) (reveal ? 1 : 0));
    }

    public int getLineId() {
        return lineId;
    }

    public int getTrip() {
        return trip;
    }

    public CharSequence getLine() {
        return line;
    }

    public String getTime() {
        return time;
    }

    public String getLastStation() {
        return lastStation;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public int getVehicle() {
        return vehicle;
    }

    public void setVehicle(int vehicle) {
        this.vehicle = vehicle;
    }

    public boolean isReveal() {
        return reveal;
    }

    public void setReveal(boolean reveal) {
        this.reveal = reveal;
    }

    public static final Parcelable.Creator<BusStopDetail> CREATOR = new Parcelable.Creator<BusStopDetail>() {

        @Override
        public BusStopDetail createFromParcel(Parcel in) {
            return new BusStopDetail(in);
        }

        @Override
        public BusStopDetail[] newArray(int size) {
            return new BusStopDetail[size];
        }
    };
}