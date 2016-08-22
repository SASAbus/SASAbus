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

package it.sasabz.android.sasabus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public final class Parking implements Parcelable {

    @SerializedName("id")
    private final int id;

    @SerializedName("name")
    private final String name;

    @SerializedName("address")
    private final String address;

    @SerializedName("phone")
    private final String phone;

    @SerializedName("latitude")
    private final double lat;

    @SerializedName("longitude")
    private final double lng;

    @SerializedName("free")
    private final int freeSlots;

    @SerializedName("total")
    private final int totalSlots;

    private Parking(Parcel in) {
        id = in.readInt();
        name = in.readString();
        address = in.readString();
        phone = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        freeSlots = in.readInt();
        totalSlots = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeInt(freeSlots);
        dest.writeInt(totalSlots);
    }

    @Override
    public String toString() {
        return "Parking{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", freeSlots=" + freeSlots +
                ", totalSlots=" + totalSlots +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }

    public int getFreeSlots() {
        return freeSlots;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public static final Parcelable.Creator<Parking> CREATOR = new Parcelable.Creator<Parking>() {

        @Override
        public Parking createFromParcel(Parcel in) {
            return new Parking(in);
        }

        @Override
        public Parking[] newArray(int size) {
            return new Parking[size];
        }
    };
}