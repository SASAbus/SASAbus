package it.sasabz.android.sasabus.network.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class Line implements Parcelable {

    private final int id;
    private final int days;

    private final String name;
    private final String origin;
    private final String destination;
    private final String city;
    private final String info;
    private final String zone;

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
