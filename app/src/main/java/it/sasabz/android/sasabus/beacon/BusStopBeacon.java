package it.sasabz.android.sasabus.beacon;

import java.util.Date;

/**
 * Model which represents a bus stop beacon and holds information about it.
 *
 * @author Alex Lardschneider
 */
public class BusStopBeacon {

    private final int id;
    private final Date startDate;

    private long seconds;
    private long lastSeen;

    private boolean notificationShown;

    private double distance;

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
     * Returns the last set distance of this beacon.
     *
     * @return the distance.
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Returns the id of this beacon, corresponding to the station id, e.g. 5029.
     *
     * @return the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns how long this beacon was visible in seconds.
     *
     * @return the seconds the beacon was visible.
     */
    long getSeenSeconds() {
        return seconds;
    }

    /**
     * Updates {@link #seconds} and {@link #lastSeen} to the current time stamp.
     */
    void seen() {
        long millis = System.currentTimeMillis();

        seconds = (millis - startDate.getTime()) / 1000;
        lastSeen = millis;
    }

    /**
     * Checks if a notification for this beacon was ever shown. This value cannot be changed
     * to {@code false} once it has been changed to {@code true}. This is too keep the beacon handler
     * to display this notification again even if the user has dismissed it already.
     *
     * @return {@code true} if the notification has been shown at least once.
     */
    boolean isNotificationShown() {
        return notificationShown;
    }

    /**
     * Sets {@link #notificationShown} to {@code true}.
     */
    void setNotificationShown() {
        notificationShown = true;
    }

    /**
     * Returns the unix timestamp in millis when the beacon was last seen.
     *
     * @return the time stamp.
     */
    long getLastSeen() {
        return lastSeen;
    }
}