package it.sasabz.android.sasabus.network.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public final class Profile implements Parcelable {

    public final String id;
    public final String username;

    @SerializedName("class")
    public String cls;

    public final int points;
    public final int badges;
    public final int rank;
    public final int profile;

    private Profile(Parcel in) {
        id = in.readString();
        username = in.readString();
        cls = in.readString();
        points = in.readInt();
        badges = in.readInt();
        rank = in.readInt();
        profile = in.readInt();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(username);
        parcel.writeString(cls);
        parcel.writeInt(points);
        parcel.writeInt(badges);
        parcel.writeInt(rank);
        parcel.writeInt(profile);
    }
}