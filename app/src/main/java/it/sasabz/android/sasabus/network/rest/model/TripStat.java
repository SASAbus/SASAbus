package it.sasabz.android.sasabus.network.rest.model;

import com.google.gson.annotations.SerializedName;

public class TripStat {

    @SerializedName("time")
    private int time;

    @SerializedName("trip")
    private int trip;

    @SerializedName("line_id")
    private int lineId;

    @SerializedName("line_name")
    private String lineName;

    @SerializedName("variant")
    private int variant;

    @SerializedName("inserted")
    private int inserted;

    @SerializedName("departure")
    private int departure;

    @SerializedName("origin")
    private int origin;

    @SerializedName("destination")
    private int destination;

    @SerializedName("zone")
    private String zone;

    public int getTime() {
        return time;
    }

    public int getTrip() {
        return trip;
    }

    public int getLineId() {
        return lineId;
    }

    public String getLineName() {
        return lineName;
    }

    public int getVariant() {
        return variant;
    }

    public int getInserted() {
        return inserted;
    }

    public int getDeparture() {
        return departure;
    }

    public int getOrigin() {
        return origin;
    }

    public int getDestination() {
        return destination;
    }

    public String getZone() {
        return zone;
    }
}