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

package it.sasabz.android.sasabus.model.trip;

import android.os.Parcel;
import android.os.Parcelable;

public class PlannedTripNotification implements Parcelable {

    private String text;
    private int minutes;
    private boolean image;
    private boolean light;

    public PlannedTripNotification(String text, int minutes, boolean image, boolean light) {
        this.text = text;
        this.minutes = minutes;
        this.image = image;
        this.light = light;
    }

    private PlannedTripNotification(Parcel in) {
        text = in.readString();
        minutes = in.readInt();
        image = in.readByte() != 0;
        light = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeInt(minutes);
        dest.writeByte((byte) (image ? 1 : 0));
        dest.writeByte((byte) (light ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PlannedTripNotification> CREATOR = new Creator<PlannedTripNotification>() {
        @Override
        public PlannedTripNotification createFromParcel(Parcel in) {
            return new PlannedTripNotification(in);
        }

        @Override
        public PlannedTripNotification[] newArray(int size) {
            return new PlannedTripNotification[size];
        }
    };

    public CharSequence getText() {
        return text;
    }

    public boolean isImage() {
        return image;
    }

    public boolean isLight() {
        return light;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    public void setLight(boolean light) {
        this.light = light;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}