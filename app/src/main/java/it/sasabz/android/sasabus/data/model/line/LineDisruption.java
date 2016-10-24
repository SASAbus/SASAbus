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

package it.sasabz.android.sasabus.data.model.line;

import android.os.Parcel;
import android.os.Parcelable;

public class LineDisruption implements Parcelable {

    private final int id;
    private boolean selected;

    public LineDisruption(int id) {
        this.id = id;
    }

    private LineDisruption(Parcel in) {
        id = in.readInt();
        selected = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(selected ? 1 : 0);
    }

    public boolean isSelected() {
        return selected;
    }

    public int getId() {
        return id;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static final Creator<LineDisruption> CREATOR = new Creator<LineDisruption>() {

        @Override
        public LineDisruption createFromParcel(Parcel in) {
            return new LineDisruption(in);
        }

        @Override
        public LineDisruption[] newArray(int size) {
            return new LineDisruption[size];
        }
    };
}