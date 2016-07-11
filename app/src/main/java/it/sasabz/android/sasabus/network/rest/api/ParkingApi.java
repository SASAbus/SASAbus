package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.ParkingResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface ParkingApi {

    @GET(Endpoint.PARKING)
    Observable<ParkingResponse> getParking(@Path("language") String language);

    @GET(Endpoint.PARKING_ID)
    Observable<ParkingResponse> getParking(@Path("language") String language, @Path("id") int id);
}