package it.sasabz.android.sasabus.network.rest.model;

import com.google.gson.annotations.SerializedName;

public class DepartureArrival {

    @SerializedName("municipality")
    public String municipality;

    @SerializedName("name")
    public String name;

    @SerializedName("date")
    public String date;

    @SerializedName("time")
    public String time;

    @SerializedName("lat")
    public float lat;

    @SerializedName("lng")
    public float lng;
}