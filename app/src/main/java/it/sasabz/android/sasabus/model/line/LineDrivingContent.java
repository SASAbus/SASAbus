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

package it.sasabz.android.sasabus.model.line;

import android.os.Parcel;
import android.os.Parcelable;

public class LineDrivingContent implements Parcelable {
    private final String busStop;
    private final int delay;

    public LineDrivingContent(String busStop, int delay) {
        this.busStop = busStop;
        this.delay = delay;
    }

    private LineDrivingContent(Parcel in) {
        busStop = in.readString();
        delay = in.readInt();
    }

    public CharSequence getBusStop() {
        return busStop;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(busStop);
        dest.writeInt(delay);
    }

    public static final Parcelable.Creator<LineDrivingContent> CREATOR = new Parcelable.Creator<LineDrivingContent>() {

        @Override
        public LineDrivingContent createFromParcel(Parcel in) {
            return new LineDrivingContent(in);
        }

        @Override
        public LineDrivingContent[] newArray(int size) {
            return new LineDrivingContent[size];
        }
    };
}