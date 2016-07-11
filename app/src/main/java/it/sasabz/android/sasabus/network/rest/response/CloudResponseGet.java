package it.sasabz.android.sasabus.network.rest.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CloudResponseGet {

    @SerializedName("trips")
    public List<String> hashes;

    @SerializedName("planned_trips")
    public List<String> plannedHashes;

    @Override
    public String toString() {
        return "CloudResponseGet{" +
                "hashes=" + hashes +
                ", plannedHashes=" + plannedHashes +
                '}';
    }
}
