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

package it.sasabz.android.sasabus.beacon.bus;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.beacon.Beacon;
import it.sasabz.android.sasabus.model.BusStop;
import it.sasabz.android.sasabus.model.JsonSerializable;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;

public class BusBeacon implements Beacon, JsonSerializable {

    private final String TAG = "BusBeacon";

    public static final int TYPE_BEACON = 0;
    static final int TYPE_REALTIME = 1;

    private static final long DELAY_FETCH_INTERVAL = TimeUnit.SECONDS.toMillis(30);

    private String hash;
    public String title;

    public final int id;

    private final long startTimeMillis;

    public double distance;
    public float fuelPrice;

    public long lastSeen;
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

    private long lastDelayFetch;

    public BusStop busStop;

    BusBeacon(int id) {
        this.id = id;

        startTimeMillis = new Date().getTime();
        busStops = new ArrayList<>();

        seen();
    }

    @Override
    public void seen() {
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;

        LogUtils.e(TAG, "Set hash " + hash + " for trip " + id);
    }

    boolean shouldFetchDelay() {
        return lastDelayFetch + DELAY_FETCH_INTERVAL < System.currentTimeMillis();
    }

    void updateLastDelayFetch() {
        lastDelayFetch = System.currentTimeMillis();
    }
}