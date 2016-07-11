package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds all the trips.
 *
 * @author Alex Lardschneider
 */
public class Trip extends RealmObject {

    private String hash;
    private String path;

    private int line;
    private int variant;
    private int vehicle;
    private int trip;

    private int origin;
    private int destination;

    private long departure;
    private long arrival;

    private float fuelPrice;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getVariant() {
        return variant;
    }

    public void setVariant(int variant) {
        this.variant = variant;
    }

    public int getVehicle() {
        return vehicle;
    }

    public void setVehicle(int vehicle) {
        this.vehicle = vehicle;
    }

    public int getTrip() {
        return trip;
    }

    public void setTrip(int trip) {
        this.trip = trip;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public long getDeparture() {
        return departure;
    }

    public void setDeparture(long departure) {
        this.departure = departure;
    }

    public long getArrival() {
        return arrival;
    }

    public void setArrival(long arrival) {
        this.arrival = arrival;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public float getFuelPrice() {
        return fuelPrice;
    }

    public void setFuelPrice(float fuelPrice) {
        this.fuelPrice = fuelPrice;
    }
}
