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

import it.sasabz.android.sasabus.util.Utils;

public class Vehicle {

    private static final String[] FUEL_IT = {
            "Idrogeno",
            "Diesel",
            "Metano"
    };

    private static final String[] FUEL_DE = {
            "Wasserstoff",
            "Diesel",
            "Methan"
    };

    private static final String[] FUEL_EN = {
            "Hydrogen",
            "Diesel",
            "Methane"
    };

    private final String manufacturer;
    private final String model;
    private final int fuel;
    private final int emission;
    private final String code;

    Vehicle(String manufacturer, String model, int fuel, int emission, String code) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.fuel = fuel;
        this.emission = emission;
        this.code = code;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getFuelString(Context context) {
        switch (Utils.locale(context)) {
            case "it":
                return FUEL_IT[fuel];
            case "de":
                return FUEL_DE[fuel];
            default:
                return FUEL_EN[fuel];
        }
    }

    public int getEmission() {
        return emission;
    }

    public String getCode() {
        return code;
    }
}