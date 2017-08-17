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

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Leg {

    @SerializedName("duration")
    public int duration;

    @SerializedName("app")
    public App app;

    @SerializedName("departure")
    public DepartureArrival departure;

    @SerializedName("arrival")
    public DepartureArrival arrival;

    @SerializedName("path")
    public List<List<Double>> path;

    public static class App {

        @SerializedName("id")
        public int id;

        @SerializedName("vehicle")
        public String vehicle;

        @SerializedName("title")
        public String line;

        @SerializedName("legend")
        public String legend;
    }
}