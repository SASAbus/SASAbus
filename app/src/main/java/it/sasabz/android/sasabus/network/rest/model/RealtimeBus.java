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

package it.sasabz.android.sasabus.network.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public final class RealtimeBus implements Parcelable {

    @SerializedName("line_name")
    public final String lineName;

    @SerializedName("line_id")
    public final int lineId;

    public final int variant;
    public final int vehicle;
    public final int trip;

    @SerializedName("bus_stop")
    public final int busStop;

    @SerializedName("delay_min")
    public final int delayMin;

    private final int departure;

    @SerializedName("updated_min_ago")
    private final int updatedMinAgo;

    public final double latitude;
    public final double longitude;

    @SerializedName("color_hue")
    private final int colorHue;

    @SerializedName("color_hex")
    public final String colorHex;

    public final int destination;

    public final List<Integer> path = new ArrayList<>();

    private final String zone;

    public String group;
    public String currentStopName;
    public String lastStopName;

    private RealtimeBus(Parcel in) {
        lineName = in.readString();
        lineId = in.readInt();
        variant = in.readInt();
        vehicle = in.readInt();
        trip = in.readInt();
        busStop = in.readInt();
        delayMin = in.readInt();
        departure = in.readInt();
        updatedMinAgo = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        colorHue = in.readInt();
        colorHex = in.readString();
        destination = in.readInt();

        zone = in.readString();
        group = in.readString();
        currentStopName = in.readString();
        lastStopName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lineName);
        dest.writeInt(lineId);
        dest.writeInt(variant);
        dest.writeInt(vehicle);
        dest.writeInt(trip);
        dest.writeInt(busStop);
        dest.writeInt(delayMin);
        dest.writeInt(departure);
        dest.writeInt(updatedMinAgo);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(colorHue);
        dest.writeString(colorHex);
        dest.writeInt(destination);

        dest.writeString(zone);
        dest.writeString(group);
        dest.writeString(currentStopName);
        dest.writeString(lastStopName);
    }

    public static final Creator<RealtimeBus> CREATOR = new Creator<RealtimeBus>() {
        @Override
        public RealtimeBus createFromParcel(Parcel in) {
            return new RealtimeBus(in);
        }

        @Override
        public RealtimeBus[] newArray(int size) {
            return new RealtimeBus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}