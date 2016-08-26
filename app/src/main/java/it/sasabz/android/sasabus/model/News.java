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

public final class News implements Parcelable {

    @SerializedName("id")
    private final int id;

    @SerializedName("title")
    private final String title;

    @SerializedName("message")
    private final String message;

    @SerializedName("zone")
    private String zone;

    private boolean highlighted;

    private News(Parcel in) {
        id = in.readInt();
        title = in.readString();
        message = in.readString();
        highlighted = in.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(message);
        dest.writeByte((byte) (highlighted ? 1 : 0));
    }

    public CharSequence getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlight() {
        highlighted = true;
    }

    public int getId() {
        return id;
    }

    public String getZone() {
        return zone;
    }

    public static final Parcelable.Creator<News> CREATOR = new Parcelable.Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", zone='" + zone + '\'' +
                ", highlighted=" + highlighted +
                '}';
    }
}