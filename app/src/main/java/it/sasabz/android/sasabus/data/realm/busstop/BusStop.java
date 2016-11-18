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

package it.sasabz.android.sasabus.data.realm.busstop;

import android.content.Context;

import io.realm.RealmObject;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Holds all the bus stops which the SASA Spa-AG operates. Most of the time the bus stops
 * will be grouped by their {@link #family}, which is a unique identifier for each bus stop
 * with the same name and municipality.
 *
 * @author Alex Lardschneider
 */
public class BusStop extends RealmObject {

    private int id;
    private int family;

    private String nameDe;
    private String nameIt;
    private String municDe;
    private String municIt;

    private float lat;
    private float lng;

    public BusStop() {
    }

    public BusStop(int id, String name, String munic, float lat, float lng, int family) {
        this.id = id;
        nameDe = name;
        nameIt = name;
        municDe = munic;
        municIt = munic;
        this.lat = lat;
        this.lng = lng;
        this.family = family;
    }

    public int getId() {
        return id;
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

    public CharSequence getMunic(Context context) {
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

    public int getFamily() {
        return family;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public void setFamily(int family) {
        this.family = family;
    }

    public void setNameDe(String nameDe) {
        this.nameDe = nameDe;
    }

    public void setNameIt(String nameIt) {
        this.nameIt = nameIt;
    }

    public void setMunicDe(String municDe) {
        this.municDe = municDe;
    }

    public void setMunicIt(String municIt) {
        this.municIt = municIt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusStop busStop = (BusStop) o;

        return family == busStop.family;
    }

    @Override
    public int hashCode() {
        return family;
    }
}
