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

package it.sasabz.android.sasabus.model.route;

import android.os.Parcel;
import android.os.Parcelable;

import it.sasabz.android.sasabus.model.BusStop;

public class RouteLeg implements Parcelable {
    private final int duration;
    private final int id;
    private final String vehicle;
    private final String line;
    private final String legend;
    private final BusStop departure;
    private final String departureTime;
    private final BusStop arrival;
    private final String arrivalTime;

    public RouteLeg(int duration, int id, String vehicle, String line, String legend,
                    BusStop departure, String departureTime, BusStop arrival,
                    String arrivalTime) {
        this.duration = duration;
        this.id = id;
        this.vehicle = vehicle;
        this.line = line;
        this.legend = legend;
        this.departure = departure;
        this.departureTime = departureTime;
        this.arrival = arrival;
        this.arrivalTime = arrivalTime;
    }

    private RouteLeg(Parcel in) {
        duration = in.readInt();
        id = in.readInt();
        vehicle = in.readString();
        line = in.readString();
        legend = in.readString();
        departure = in.readParcelable(BusStop.class.getClassLoader());
        departureTime = in.readString();
        arrival = in.readParcelable(BusStop.class.getClassLoader());
        arrivalTime = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(duration);
        dest.writeInt(id);
        dest.writeString(vehicle);
        dest.writeString(line);
        dest.writeString(legend);
        dest.writeParcelable(departure, 0);
        dest.writeString(departureTime);
        dest.writeParcelable(arrival, 0);
        dest.writeString(arrivalTime);
    }

    public int getDuration() {
        return duration;
    }

    public int getId() {
        return id;
    }

    public CharSequence getVehicle() {
        return vehicle;
    }

    public CharSequence getLine() {
        return line;
    }

    public String getLegend() {
        return legend;
    }

    public BusStop getDeparture() {
        return departure;
    }

    public CharSequence getDepartureTime() {
        return departureTime;
    }

    public BusStop getArrival() {
        return arrival;
    }

    public CharSequence getArrivalTime() {
        return arrivalTime;
    }

    public static final Creator<RouteLeg> CREATOR = new Creator<RouteLeg>() {

        @Override
        public RouteLeg createFromParcel(Parcel in) {
            return new RouteLeg(in);
        }

        @Override
        public RouteLeg[] newArray(int size) {
            return new RouteLeg[size];
        }
    };
}