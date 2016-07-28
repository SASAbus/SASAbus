package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.TrafficLightResponse;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface TrafficLightApi {

    @GET(Endpoint.TRAFFIC_LIGHT)
    Observable<TrafficLightResponse> trafficLight(@Path("language") String language, @Path("city") String city);
}
