package it.sasabz.android.sasabus.beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class BusBeacon {

    private final String hash;

    private final int id;
    private final long startDate;

    private double distance;
    private float fuelPrice;

    private long lastSeen;
    private long seconds;

    private int tripId;
    private int variant;
    private int lineId;
    private int retryCount;

    private int origin;
    private int destination;

    private String title;

    private final List<Integer> busStops;

    private boolean originPending;
    private boolean currentTripPending;
    private boolean suitableForTrip;

    BusBeacon(int id, String hash) {
        this.id = id;
        this.hash = hash;

        startDate = new Date().getTime();
        busStops = new ArrayList<>();

        seen();
    }

    public int getId() {
        return id;
    }

    long getSeenSeconds() {
        return seconds;
    }

    void seen() {
        Date now = new Date();
        seconds = (now.getTime() - getStartDate().getTime()) / 1000;
        lastSeen = now.getTime();
    }

    public int getTripId() {
        return tripId;
    }

    void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public Date getStartDate() {
        return new Date(startDate);
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public String getHash() {
        return hash;
    }

    double getDistance() {
        return distance;
    }

    void setDistance(double distance) {
        this.distance = distance;
    }

    public int getOrigin() {
        return origin;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    void setOrigin(int origin) {
        this.origin = origin;
    }

    boolean isOriginPending() {
        return originPending;
    }

    void setOriginPending(boolean originPending) {
        this.originPending = originPending;
    }

    boolean isCurrentTripPending() {
        return currentTripPending;
    }

    void setCurrentTripPending(boolean currentTripPending) {
        this.currentTripPending = currentTripPending;
    }

    public int getVariant() {
        return variant;
    }

    public void setVariant(int variant) {
        this.variant = variant;
    }

    public float getFuelPrice() {
        return fuelPrice;
    }

    void setFuelPrice(float fuelPrice) {
        this.fuelPrice = fuelPrice;
    }

    public List<Integer> getBusStops() {
        return busStops;
    }

    void setBusStops(Collection<Integer> busStops) {
        this.busStops.clear();
        this.busStops.addAll(busStops);
    }

    public int getDestination() {
        return destination;
    }

    void setDestination(int destination) {
        this.destination = destination;
    }

    boolean canRetry() {
        return retryCount <= 3;
    }

    void retry() {
        retryCount++;
    }

    boolean isSuitableForTrip() {
        return suitableForTrip;
    }

    void setSuitableForTrip(boolean suitableForTrip) {
        this.suitableForTrip = suitableForTrip;
    }
}