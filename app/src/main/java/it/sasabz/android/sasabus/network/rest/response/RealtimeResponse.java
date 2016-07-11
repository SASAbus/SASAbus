package it.sasabz.android.sasabus.network.rest.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;

public class RealtimeResponse {

    @SerializedName("app")
    public String status;

    public String message;

    public String link;

    public int duration;

    public List<RealtimeBus> buses;
}
