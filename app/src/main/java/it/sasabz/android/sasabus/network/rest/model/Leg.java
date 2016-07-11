package it.sasabz.android.sasabus.network.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Leg {

    @SerializedName("duration")
    public int duration;

    @SerializedName("app")
    public App app;

    @SerializedName("departure")
    public DepartureArrival departure;

    @SerializedName("arrival")
    public DepartureArrival arrival;

    @SerializedName("path")
    public List<List<Double>> path;

    public static class App {

        @SerializedName("id")
        public int id;

        @SerializedName("vehicle")
        public String vehicle;

        @SerializedName("line")
        public String line;

        @SerializedName("legend")
        public String legend;
    }
}