package it.sasabz.android.sasabus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public final class Parking implements Parcelable {

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
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", freeSlots=" + freeSlots +
                ", totalSlots=" + totalSlots +
                '}';
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