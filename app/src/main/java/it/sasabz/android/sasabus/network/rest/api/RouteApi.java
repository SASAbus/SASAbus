package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.RouteResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface RouteApi {

    @GET(Endpoint.ROUTE)
    Observable<RouteResponse> route(@Path("language") String language,
                                    @Path("from") String from,
                                    @Path("to") String to,
                                    @Path("date") String date,
                                    @Path("time") String time,
                                    @Path("walk") int walk,
                                    @Path("results") int results);
}