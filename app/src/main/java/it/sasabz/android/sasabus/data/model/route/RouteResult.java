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

package it.sasabz.android.sasabus.data.model.route;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class RouteResult implements Parcelable {
    private final int changes;
    private final String departureTime;
    private final String arrivalTime;
    private final int duration;
    private final List<RouteLeg> legs;

    public RouteResult(int changes, String departure, String arrival, int duration, List<RouteLeg> legs) {
        this.changes = changes;
        departureTime = departure;
        arrivalTime = arrival;
        this.duration = duration;
        this.legs = legs;
    }

    private RouteResult(Parcel in) {
        changes = in.readInt();
        departureTime = in.readString();
        arrivalTime = in.readString();
        duration = in.readInt();
        legs = in.readArrayList(RouteLeg.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(changes);
        dest.writeString(departureTime);
        dest.writeString(arrivalTime);
        dest.writeInt(duration);
        dest.writeList(legs);
    }

    public CharSequence getDepartureTime() {
        return departureTime;
    }

    public CharSequence getArrivalTime() {
        return arrivalTime;
    }

    public int getDuration() {
        return duration;
    }

    public List<RouteLeg> getLegs() {
        return legs;
    }

    public static final Parcelable.Creator<RouteResult> CREATOR = new Parcelable.Creator<RouteResult>() {

        @Override
        public RouteResult createFromParcel(Parcel in) {
            return new RouteResult(in);
        }

        @Override
        public RouteResult[] newArray(int size) {
            return new RouteResult[size];
        }
    };
}