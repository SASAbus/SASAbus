package it.sasabz.android.sasabus.model;

public class Bus {

    private final int id;
    private final Vehicle vehicle;

    public Bus(int id, Vehicle vehicle) {
        this.id = id;
        this.vehicle = vehicle;
    }

    public int getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}