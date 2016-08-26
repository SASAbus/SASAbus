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

import java.util.List;

public class LineDriving implements Parcelable {
    private final int id;
    private final String name;
    private final String munic;
    private final List<LineDrivingContent> contentList;

    public LineDriving(int id, String name, String munic, List<LineDrivingContent> contentList) {
        this.id = id;
        this.name = name;
        this.munic = munic;
        this.contentList = contentList;
    }

    private LineDriving(Parcel in) {
        id = in.readInt();
        name = in.readString();
        munic = in.readString();
        contentList = in.readArrayList(LineDrivingContent.class.getClassLoader());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CharSequence getMunic() {
        return munic;
    }

    public Iterable<LineDrivingContent> getContentList() {
        return contentList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(munic);
        dest.writeList(contentList);
    }

    public static final Parcelable.Creator<LineDriving> CREATOR = new Parcelable.Creator<LineDriving>() {

        @Override
        public LineDriving createFromParcel(Parcel in) {
            return new LineDriving(in);
        }

        @Override
        public LineDriving[] newArray(int size) {
            return new LineDriving[size];
        }
    };
}