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

package it.sasabz.android.sasabus.data.model.line;

import android.os.Parcel;
import android.os.Parcelable;

public class LineDetail implements Parcelable {
    private final String currentStation;
    private final int delay;
    private final String lastStation;
    private final String lastTime;
    private final String additionalData;
    private final int vehicle;
    private final boolean color;
    private final int tripId;
    private final int currentBusStop;

    public LineDetail(String currentStation, int delay, String lastStation, String lastTime,
                      String additionalData, int vehicle, boolean color, int tripId, int currentBusStop) {

        this.currentStation = currentStation;
        this.delay = delay;
        this.lastStation = lastStation;
        this.lastTime = lastTime;
        this.additionalData = additionalData;
        this.vehicle = vehicle;
        this.color = color;
        this.tripId = tripId;
        this.currentBusStop = currentBusStop;
    }

    private LineDetail(Parcel in) {
        currentStation = in.readString();
        delay = in.readInt();
        lastStation = in.readString();
        lastTime = in.readString();
        additionalData = in.readString();
        vehicle = in.readInt();
        color = in.readByte() != 0;
        tripId = in.readInt();
        currentBusStop = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currentStation);
        dest.writeInt(delay);
        dest.writeString(lastStation);
        dest.writeString(lastTime);
        dest.writeString(additionalData);
        dest.writeInt(vehicle);
        dest.writeInt(tripId);
        dest.writeInt(currentBusStop);

        dest.writeByte((byte) (color ? 1 : 0));
    }

    public String getCurrentStation() {
        return currentStation;
    }

    public int getDelay() {
        return delay;
    }

    public String getLastStation() {
        return lastStation;
    }

    public CharSequence getLastTime() {
        return lastTime;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public int getVehicle() {
        return vehicle;
    }

    public boolean isColor() {
        return color;
    }

    public int getTripId() {
        return tripId;
    }

    public int getCurrentBusStop() {
        return currentBusStop;
    }

    public static final Parcelable.Creator<LineDetail> CREATOR = new Parcelable.Creator<LineDetail>() {

        @Override
        public LineDetail createFromParcel(Parcel in) {
            return new LineDetail(in);
        }

        @Override
        public LineDetail[] newArray(int size) {
            return new LineDetail[size];
        }
    };
}