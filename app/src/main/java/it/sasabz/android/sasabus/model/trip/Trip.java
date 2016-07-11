package it.sasabz.android.sasabus.model.trip;

public class Trip {

    private final int line;
    private final int variant;
    private final int trip;
    private final int vehicle;
    private final int startStation;
    private final int stopStation;
    private final long startTime;
    private final long stopTime;
    private final String tripList;
    private final String hash;
    private final float fuelPrice;

    private String origin;
    private String destination;

    public Trip(String hash, int lineId, int variant, int tripId, int vehicle,
                int startStation, int stopStation, long startTime, long stopTime, String tripList,
                float fuelPrice) {

        line = lineId;
        this.variant = variant;
        trip = tripId;
        this.vehicle = vehicle;
        this.startStation = startStation;
        this.stopStation = stopStation;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.tripList = tripList;
        this.hash = hash;
        this.fuelPrice = fuelPrice;
    }

    public Trip(it.sasabz.android.sasabus.realm.user.Trip trip) {
        line = trip.getLine();
        variant = trip.getVariant();
        this.trip = trip.getTrip();
        vehicle = trip.getVehicle();
        startStation = trip.getOrigin();
        stopStation = trip.getDestination();
        startTime = trip.getDeparture();
        stopTime = trip.getArrival();
        tripList = trip.getPath();
        hash = trip.getHash();
        fuelPrice = trip.getFuelPrice();
    }

    public int getLine() {
        return line;
    }

    public int getVariant() {
        return variant;
    }

    public int getTrip() {
        return trip;
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getStartStation() {
        return startStation;
    }

    public int getStopStation() {
        return stopStation;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public String getTripList() {
        return tripList;
    }

    public String getHash() {
        return hash;
    }

    public float getFuelPrice() {
        return fuelPrice;
    }

    public CharSequence getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public CharSequence getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trip trip = (Trip) o;

        return hash != null ? hash.equals(trip.hash) : trip.hash == null;

    }

    @Override
    public int hashCode() {
        return hash != null ? hash.hashCode() : 0;
    }

    @Override
    public String toString() {
        return hash;
    }
}
