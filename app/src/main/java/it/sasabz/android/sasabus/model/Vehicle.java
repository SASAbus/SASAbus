package it.sasabz.android.sasabus.model;

public class Vehicle {

    private final String vendor;
    private final String model;
    private final String fuel;
    private final String color;
    private final int group;

    public Vehicle(String vendor, String model, String fuel, String color, int group) {
        this.vendor = vendor;
        this.model = model;
        this.fuel = fuel;
        this.color = color;
        this.group = group;
    }

    public CharSequence getVendor() {
        return vendor;
    }

    public CharSequence getModel() {
        return model;
    }

    public CharSequence getFuel() {
        return fuel;
    }

    public CharSequence getColor() {
        return color;
    }

    public int getGroup() {
        return group;
    }
}