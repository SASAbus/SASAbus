package it.sasabz.android.sasabus.model;

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

    private static final String[] COLOR_IT = {
            "Bianco e viola",
            "Giallo",
            "Arancione"
    };

    private static final String[] COLOR_DE = {
            "Weiß und violett",
            "Gelb",
            "Orange"
    };

    private static final String[] COLOR_EN = {
            "White and purple",
            "Yellow",
            "Orange"
    };

    private static final String[] COLOR_LIGHT = {
            "1976D2",
            "FDD835",
            "FF9800"
    };

    private static final String[] COLOR_DARK = {
            "0D47A1",
            "FBC02D",
            "F57C00"
    };

    private final String manufacturer;
    private final String model;
    private final int fuel;
    private final int color;
    private final int emission;
    private final String code;

    Vehicle(String manufacturer, String model, int fuel, int color, int emission, String code) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.fuel = fuel;
        this.color = color;
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

    public String getColorString(Context context) {
        switch (Utils.locale(context)) {
            case "it":
                return COLOR_IT[color];
            case "de":
                return COLOR_DE[color];
            default:
                return COLOR_EN[color];
        }
    }

    public int getEmission() {
        return emission;
    }

    public String getCode() {
        return code;
    }
}