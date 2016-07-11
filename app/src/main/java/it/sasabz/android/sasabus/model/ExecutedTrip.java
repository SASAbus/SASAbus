package it.sasabz.android.sasabus.model;

public class ExecutedTrip {

    private final long date;
    private final int tripId;
    private final int lineId;
    private final String line;
    private final int departure;
    private final String firstStop;
    private final String lastStop;

    public ExecutedTrip(long date, int tripId, int lineId, String line, int departure, String firstStop, String lastStop) {
        this.date = date;
        this.tripId = tripId;
        this.lineId = lineId;
        this.line = line;
        this.departure = departure;
        this.firstStop = firstStop;
        this.lastStop = lastStop;
    }

    public long getDate() {
        return date;
    }

    public int getTripId() {
        return tripId;
    }

    public int getLineId() {
        return lineId;
    }

    public CharSequence getLine() {
        return line;
    }

    public int getDeparture() {
        return departure;
    }

    public CharSequence getFirstStop() {
        return firstStop;
    }

    public CharSequence getLastStop() {
        return lastStop;
    }
}
