package it.sasabz.android.sasabus.beacon.busstop;

import java.util.Date;

/**
 * Model which represents a bus stop beacon and holds information about it.
 *
 * @author Alex Lardschneider
 */
public class BusStopBeacon {

    public final int id;
    private final Date startDate;

    long seenSeconds;
    long lastSeen;

    boolean isNotificationShown;

    public double distance;

    BusStopBeacon(int id) {
        this.id = id;
        startDate = new Date();

        seen();
    }

    /**
     * Sets the current distance from the device to the beacon.
     *
     * @param distance the distance.
     */
    void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Updates {@link #seenSeconds} and {@link #lastSeen} to the current time stamp.
     */
    void seen() {
        long millis = System.currentTimeMillis();

        seenSeconds = (millis - startDate.getTime()) / 1000;
        lastSeen = millis;
    }

    /**
     * Sets {@link #isNotificationShown} to {@code true}.
     */
    void setNotificationShown() {
        isNotificationShown = true;
    }
}