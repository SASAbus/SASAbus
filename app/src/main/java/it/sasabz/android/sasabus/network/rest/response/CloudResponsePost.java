package it.sasabz.android.sasabus.network.rest.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.sasabz.android.sasabus.network.rest.model.CloudPlannedTrip;
import it.sasabz.android.sasabus.network.rest.model.CloudTrip;

public class CloudResponsePost {

    public List<CloudTrip> trips;

    @SerializedName("planned_trips")
    public List<CloudPlannedTrip> plannedTrips;

    @Override
    public String toString() {
        return "CloudResponsePost{" +
                "trips=" + trips +
                ", plannedTrips=" + plannedTrips +
                '}';
    }
}
