package it.sasabz.android.sasabus.provider.model;

import it.sasabz.android.sasabus.provider.ApiUtils;

/**
 * Represents a departure from a bus stop, not considering the real-time delays. This only uses
 * the planned offline open data (in JSON format) of SASA SpA-AG.
 *
 * @author David Dejori
 */
public class PlannedDeparture {

    private final int line;
    private final int time;
    private final int trip;

    public PlannedDeparture(int line, int time, int trip) {
        this.line = line;
        this.time = time;
        this.trip = trip;
    }

    public int getLine() {
        return line;
    }

    public int getTime() {
        return time;
    }

    public int getTrip() {
        return trip;
    }

    @Override
    public String toString() {
        return "PlannedDeparture{" +
                "line=" + line +
                ", time=" + ApiUtils.getTime(time) +
                ", trip=" + trip +
                '}';
    }
}
