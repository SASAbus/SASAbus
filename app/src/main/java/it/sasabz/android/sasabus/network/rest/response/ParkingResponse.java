package it.sasabz.android.sasabus.network.rest.response;

import java.util.List;

import it.sasabz.android.sasabus.model.Parking;

public class ParkingResponse {

    public List<Parking> parking;

    @Override
    public String toString() {
        return "ParkingResponse{" +
                "parking=" + parking +
                '}';
    }
}
