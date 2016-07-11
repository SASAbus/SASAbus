package it.sasabz.android.sasabus.network.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Route {

    @SerializedName("changes")
    public int changes;

    @SerializedName("departure")
    public String departure;

    @SerializedName("arrival")
    public String arrival;

    @SerializedName("duration")
    public int duration;

    @SerializedName("legs")
    public List<Leg> legs;
}