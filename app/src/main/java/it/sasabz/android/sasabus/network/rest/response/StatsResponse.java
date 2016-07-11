package it.sasabz.android.sasabus.network.rest.response;

import it.sasabz.android.sasabus.network.rest.model.TripStat;

import java.util.List;

public class StatsResponse {

    public List<TripStat> trips;

    @Override
    public String toString() {
        return "StatsResponse{" +
                "trips=" + trips +
                '}';
    }
}
