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

import it.sasabz.android.sasabus.data.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.data.realm.busstop.BusStop;

public class LineCourse implements Parcelable {

    private final int id;

    public final BusStop busStop;
    public final String time;

    public final boolean active;
    public final boolean dot;
    public final boolean bus;

    public String lineText;

    public LineCourse(int id, BusStop busStop, String time, boolean active, boolean dot, boolean bus) {
        this.id = id;
        this.busStop = busStop;
        this.time = time;
        this.active = active;
        this.dot = dot;
        this.bus = bus;
    }

    private LineCourse(Parcel in) {
        id = in.readInt();
        time = in.readString();
        active = in.readByte() != 0;
        dot = in.readByte() != 0;
        bus = in.readByte() != 0;
        lineText = in.readString();

        busStop = BusStopRealmHelper.getBusStop(id);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(time);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeByte((byte) (dot ? 1 : 0));
        dest.writeByte((byte) (bus ? 1 : 0));
        dest.writeString(lineText);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LineCourse> CREATOR = new Creator<LineCourse>() {
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