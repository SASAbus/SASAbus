package it.sasabz.android.sasabus.network.rest.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TripsResponse {

    @SerializedName("trip")
    public Trip trip;

    public int getLineId() {
        return trip.lineId;
    }

    public int getVariant() {
        return trip.variant;
    }

    public int getTrip() {
        return trip.trip;
    }

    public int getBusStop() {
        return trip.busStop;
    }

    public List<Integer> getPath() {
        return trip.path;
    }

    @Override
    public String toString() {
        return trip != null ? trip.toString() : "trip: null";
    }

    public static class Trip {

        @SerializedName("line_id")
        private int lineId;

        @SerializedName("variant")
        private int variant;

        @SerializedName("id")
        private int trip;

        @SerializedName("bus_stop")
        private int busStop;

        @SerializedName("path")
        private List<Integer> path;

        @Override
        public String toString() {
            return "Trip{" +
                    "lineId=" + lineId +
                    ", variant=" + variant +
                    ", trip=" + trip +
                    ", busStop=" + busStop +
                    ", path=" + path +
                    '}';
        }
    }
}
