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

import android.os.Parcel;
import android.os.Parcelable;

public class EventPoint implements Parcelable {

    public int id;

    public String title;
    public String description;

    public double latitude;
    public double longitude;

    public boolean scanned;

    protected EventPoint(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        scanned = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeByte((byte) (scanned ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EventPoint> CREATOR = new Creator<EventPoint>() {
        @Override
        public EventPoint createFromParcel(Parcel in) {
            return new EventPoint(in);
        }

        @Override
        public EventPoint[] newArray(int size) {
            return new EventPoint[size];
        }
    };
}