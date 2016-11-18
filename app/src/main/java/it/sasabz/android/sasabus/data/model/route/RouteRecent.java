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

package it.sasabz.android.sasabus.data.model.route;

public class RouteRecent {

    private final int id;

    private String originName;
    private String originMunic;
    private final int originId;

    private String destinationName;
    private String destinationMunic;
    private final int destinationId;

    public RouteRecent(int id, int originId, String originMunic,
                       int destinationId, String destinationMunic) {

        this.id = id;

        this.originId = originId;
        this.originMunic = originMunic;
        this.destinationId = destinationId;
        this.destinationMunic = destinationMunic;
    }

    public int getId() {
        return id;
    }

    public String getOriginMunic() {
        return originMunic;
    }

    public String getDestinationMunic() {
        return destinationMunic;
    }

    public int getOriginId() {
        return originId;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public void setOriginMunic(String originMunic) {
        this.originMunic = originMunic;
    }

    public void setDestinationMunic(String destinationMunic) {
        this.destinationMunic = destinationMunic;
    }
}