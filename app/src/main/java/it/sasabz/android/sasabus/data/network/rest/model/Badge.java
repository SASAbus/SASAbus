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

package it.sasabz.android.sasabus.data.network.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Badge implements Parcelable {

    public final int id;
    public final String title;
    public final String description;

    @SerializedName("icon_url")
    public final String iconUrl;

    public final int progress;
    public final int points;
    public final int users;

    @SerializedName("new")
    private final boolean isNewBadge;

    private final boolean locked;

    private Badge(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        iconUrl = in.readString();
        progress = in.readInt();
        points = in.readInt();
        users = in.readInt();
        isNewBadge = in.readByte() != 0;
        locked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(iconUrl);
        dest.writeInt(progress);
        dest.writeInt(points);
        dest.writeInt(users);
        dest.writeByte((byte) (isNewBadge ? 1 : 0));
        dest.writeByte((byte) (locked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Badge> CREATOR = new Creator<Badge>() {
        @Override
        public Badge createFromParcel(Parcel in) {
            return new Badge(in);
        }

        @Override
        public Badge[] newArray(int size) {
            return new Badge[size];
        }
    };
}