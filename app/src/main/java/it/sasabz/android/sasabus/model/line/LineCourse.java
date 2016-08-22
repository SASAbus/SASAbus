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

package it.sasabz.android.sasabus.model.line;

import android.os.Parcel;
import android.os.Parcelable;

public class LineCourse implements Parcelable {
    private final int id;
    private final String busStop;
    private final String munic;
    private final String time;
    private final boolean isActive;
    private final boolean dot;

    public LineCourse(int id, String busStop, String munic, String time, boolean isActive, boolean dot) {
        this.id = id;
        this.busStop = busStop;
        this.munic = munic;
        this.time = time;
        this.isActive = isActive;
        this.dot = dot;
    }

    private LineCourse(Parcel in) {
        id = in.readInt();
        busStop = in.readString();
        munic = in.readString();
        time = in.readString();

        isActive = in.readByte() != 0;
        dot = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(busStop);
        dest.writeString(munic);
        dest.writeString(time);

        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeByte((byte) (dot ? 1 : 0));
    }

    public int getId() {
        return id;
    }

    public String getBusStop() {
        return busStop;
    }

    public String getMunic() {
        return munic;
    }

    public String getTime() {
        return time;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isDot() {
        return dot;
    }

    public static final Parcelable.Creator<LineCourse> CREATOR = new Parcelable.Creator<LineCourse>() {

        @Override
        public LineCourse createFromParcel(Parcel in) {
            return new LineCourse(in);
        }

        @Override
        public LineCourse[] newArray(int size) {
            return new LineCourse[size];
        }
    };
}