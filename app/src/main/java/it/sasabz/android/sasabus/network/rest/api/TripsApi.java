package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.StatsResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface TripsApi {

    @GET(Endpoint.TRIPS_VEHICLE)
    Observable<StatsResponse> vehicle(@Path("id") int vehicle);
}