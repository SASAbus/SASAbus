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

package it.sasabz.android.sasabus.data.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.davale.sasabus.core.realm.model.SadBusStop;

import it.sasabz.android.sasabus.util.Utils;

/**
 * A representation of a bus stop.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class BusStop implements Parcelable {

    /**
     * A unique ID defined by SASA SpA-AG, some IDs are not assigned. Thus there may be gaps between
     * some IDs.
     */
    private final int id;

    /**
     * The German name of the bus stop.
     */
    private final String nameDe;

    /**
     * The Italian name of the bus stop.
     */
    private final String nameIt;

    /**
     * The municipality the bus stop is in (German).
     */
    private final String municDe;

    /**
     * The municipality the bus stop is in (Italian).
     */
    private final String municIt;

    /**
     * The latitude (y-coordinate) of the bus stop on the earth in WGS84.
     */
    private final float lat;

    /**
     * The longitude (x-coordinate) of the bus stop on the earth in WGS84.
     */
    private final float lng;

    /**
     * The group a bus stop is in. All bus stop having the same name, being in the same
     * municipality and also being nearby are in the same group.
     */
    private final int group;

    public BusStop(int id, String name, String munic, float lat, float lng, int group) {
        this.id = id;
        nameDe = name;
        nameIt = name;
        municDe = munic;
        municIt = munic;
        this.lat = lat;
        this.lng = lng;
        this.group = group;
    }

    public BusStop(int id, String nameDe, String nameIt, String municDe, String municIt, float lat, float lng, int group) {
        this.id = id;
        this.nameDe = nameDe;
        this.nameIt = nameIt;
        this.municDe = municDe;
        this.municIt = municIt;
        this.lat = lat;
        this.lng = lng;
        this.group = group;
    }

    public BusStop(BusStop station) {
        id = station.id;
        nameDe = station.nameDe.replace("{", "").replace("}", "");
        nameIt = station.nameIt.replace("{", "").replace("}", "");
        municDe = station.municDe.replace("{", "").replace("}", "");
        municIt = station.municIt.replace("{", "").replace("}", "");
        lat = station.lat;
        lng = station.lng;
        group = station.group;
    }

    public BusStop(SadBusStop station) {
        id = station.getId();
        nameDe = station.getNameDe().replace("{", "").replace("}", "");
        nameIt = station.getNameIt().replace("{", "").replace("}", "");
        municDe = station.getMunicDe().replace("{", "").replace("}", "");
        municIt = station.getMunicIt().replace("{", "").replace("}", "");
        lat = station.getLat();
        lng = station.getLng();
        group = 0;
    }

    private BusStop(Parcel in) {
        id = in.readInt();
        nameDe = in.readString();
        nameIt = in.readString();
        municDe = in.readString();
        municIt = in.readString();
        lat = in.readFloat();
        lng = in.readFloat();
        group = in.readInt();
    }

    public BusStop(com.davale.sasabus.core.realm.model.BusStop busStop) {
        id = busStop.getId();
        nameDe = busStop.getNameDe();
        nameIt = busStop.getNameIt();
        municDe = busStop.getMunicDe();
        municIt = busStop.getMunicIt();
        lat = busStop.getLat();
        lng = busStop.getLng();
        group = busStop.getFamily();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nameDe);
        dest.writeString(nameIt);
        dest.writeString(municDe);
        dest.writeString(municIt);
        dest.writeFloat(lat);
        dest.writeFloat(lng);
        dest.writeInt(group);
    }

    @Override
    public String toString() {
        return nameDe + ' ' + municDe;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return nameDe;
    }

    public String getName(Context context) {
        String locale = Utils.locale(context);
        return locale.contains("de") ? nameDe : nameIt;
    }

    public String getNameDe() {
        return nameDe;
    }

    public String getNameIt() {
        return nameIt;
    }

    public String getMunic() {
        return municDe;
    }

    public String getMunic(Context context) {
        String locale = Utils.locale(context);
        return locale.contains("de") ? municDe : municIt;
    }

    public String getMunicDe() {
        return municDe;
    }

    public String getMunicIt() {
        return municIt;
    }

    public float getLat() {
        return lat;
    }

    public float getLng() {
        return lng;
    }

    public int getGroup() {
        return group;
    }

    public static final Parcelable.Creator<BusStop> CREATOR = new Parcelable.Creator<BusStop>() {

        @Override
        public BusStop createFromParcel(Parcel in) {
            return new BusStop(in);
        }

        @Override
        public BusStop[] newArray(int size) {
            return new BusStop[size];
        }
    };
}