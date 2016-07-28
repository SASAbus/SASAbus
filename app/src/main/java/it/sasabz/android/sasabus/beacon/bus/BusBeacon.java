package it.sasabz.android.sasabus.beacon.bus;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.model.BusStop;
import it.sasabz.android.sasabus.model.JsonSerializable;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;

public class BusBeacon implements JsonSerializable {

    private final String TAG = "BusBeacon";

    public static final int TYPE_BEACON = 0;
    static final int TYPE_REALTIME = 1;

    private static final long DELAY_FETCH_INTERVAL = TimeUnit.SECONDS.toMillis(30);

    public final String hash;
    public String title;

    public final int id;

    private final long startTimeMillis;

    public double distance;
    public float fuelPrice;

    public long lastSeen;
    private long lastDelayFetch;
    long seenSeconds;

    public int trip;
    public int variant;
    public int lineId;
    public int delay;
    public int origin;
    public int destination;

    private int retryCount;

    public final List<Integer> busStops;

    boolean isOriginPending;
    boolean isCurrentTripPending;
    public boolean isSuitableForTrip;

    public BusStop busStop;

    BusBeacon(int id, String hash) {
        this.id = id;
        this.hash = hash;

        startTimeMillis = new Date().getTime();
        busStops = new ArrayList<>();

        seen();
    }

    void seen() {
        Date now = new Date();
        seenSeconds = (now.getTime() - getStartDate().getTime()) / 1000;
        lastSeen = now.getTime();
    }

    public Date getStartDate() {
        return new Date(startTimeMillis);
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVariant(int variant) {
        this.variant = variant;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    boolean canRetry() {
        return retryCount < 3;
    }

    boolean shouldFetchDelay() {
        return lastDelayFetch + DELAY_FETCH_INTERVAL < System.currentTimeMillis();
    }

    void setTrip(int trip) {
        this.trip = trip;
    }

    void setDistance(double distance) {
        this.distance = distance;
    }

    void setOrigin(int origin) {
        this.origin = origin;
    }

    void setOriginPending(boolean originPending) {
        isOriginPending = originPending;
    }

    void setCurrentTripPending(boolean currentTripPending) {
        isCurrentTripPending = currentTripPending;
    }

    void setFuelPrice(float fuelPrice) {
        this.fuelPrice = fuelPrice;
    }

    void setBusStops(Collection<Integer> busStops) {
        this.busStops.clear();
        this.busStops.addAll(busStops);
    }

    void appendBusStops(List<Integer> busStops) {
        if (busStops == null || busStops.isEmpty()) {
            LogUtils.e(TAG, "BusStops null or empty");
            return;
        }

        if (this.busStops.get(this.busStops.size() - 1).equals(busStops.get(0))) {
            busStops = busStops.subList(1, busStops.size());
        }

        this.busStops.addAll(busStops);
    }

    void setDestination(int destination) {
        this.destination = destination;
    }

    void retry() {
        retryCount++;
    }

    void setSuitableForTrip(Context context, boolean suitableForTrip) {
        if (!suitableForTrip) {
            LogUtils.e(TAG, "Beacon is not suitable for a trip, dismissing notification");
            NotificationUtils.cancelBus(context);
        }

        isSuitableForTrip = suitableForTrip;
    }

    void setBusStop(BusStop busStop, int type) {
        this.busStop = busStop;
        //int busStopType = type;
    }

    void updateLastDelayFetch() {
        lastDelayFetch = System.currentTimeMillis();
    }
}