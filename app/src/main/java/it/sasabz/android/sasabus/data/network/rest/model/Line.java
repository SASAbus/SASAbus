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

public final class Line implements Parcelable {

    public final int id;
    public final int days;

    public final String name;
    public final String origin;
    public final String destination;
    public final String city;
    public final String info;
    public final String zone;

    private Line(Parcel in) {
        id = in.readInt();
        days = in.readInt();
        name = in.readString();
        origin = in.readString();
        destination = in.readString();
        city = in.readString();
        info = in.readString();
        zone = in.readString();
    }

    public static final Creator<Line> CREATOR = new Creator<Line>() {
        @Override
        public Line createFromParcel(Parcel in) {
            return new Line(in);
        }

        @Override
        public Line[] newArray(int size) {
            return new Line[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getDays() {
        return days;
    }

    public String getName() {
        return name;
    }

    public CharSequence getOrigin() {
        return origin;
    }

    public CharSequence getDestination() {
        return destination;
    }

    public CharSequence getCity() {
        return city;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(days);
        dest.writeString(name);
        dest.writeString(origin);
        dest.writeString(destination);
        dest.writeString(city);
        dest.writeString(info);
        dest.writeString(zone);
    }
}
