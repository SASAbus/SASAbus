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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Event implements Parcelable {

    public String id;

    public String title;
    public String description;
    public String details;
    public String location;
    public String website;
    public String image;

    @SerializedName("color_primary")
    public String colorPrimary;

    @SerializedName("color_primary_dark")
    public String colorPrimaryDark;

    @SerializedName("color_accent")
    public String colorAccent;

    @SerializedName("light_status_bar")
    public boolean lightStatusBar;

    @SerializedName("qr_code")
    public String qrCode;

    public double latitude;
    public double longitude;
    public int radius;

    public long begin;
    public long end;

    public boolean completed;
    public boolean redeemed;

    public List<EventPoint> points;

    public List<TileList> polyline;

    protected Event(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        details = in.readString();
        location = in.readString();
        website = in.readString();
        image = in.readString();
        colorPrimary = in.readString();
        colorPrimaryDark = in.readString();
        colorAccent = in.readString();
        lightStatusBar = in.readByte() != 0;
        qrCode = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        radius = in.readInt();
        begin = in.readLong();
        end = in.readLong();
        completed = in.readByte() != 0;
        redeemed = in.readByte() != 0;
        points = in.createTypedArrayList(EventPoint.CREATOR);

        Type type = new TypeToken<List<List<Double>>>() {
        }.getType();
        polyline = new Gson().fromJson(in.readString(), type);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(details);
        dest.writeString(location);
        dest.writeString(website);
        dest.writeString(image);
        dest.writeString(colorPrimary);
        dest.writeString(colorPrimaryDark);
        dest.writeString(colorAccent);
        dest.writeByte((byte) (lightStatusBar ? 1 : 0));
        dest.writeString(qrCode);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(radius);
        dest.writeLong(begin);
        dest.writeLong(end);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeByte((byte) (redeemed ? 1 : 0));
        dest.writeTypedList(points);

        dest.writeString(new Gson().toJson(polyline));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public static class TileList extends ArrayList<Double> implements Parcelable {

        private static final long serialVersionUID = -2020530177560380518L;

        public TileList() {
        }

        protected TileList(Parcel in) {
            in.readList(this, null);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeList(this);
        }

        public static final Creator<TileList> CREATOR = new Creator<TileList>() {
            public TileList createFromParcel(Parcel in) {
                return new TileList(in);
            }

            public TileList[] newArray(int size) {
                return new TileList[size];
            }
        };
    }
}