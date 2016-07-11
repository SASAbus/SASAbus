package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface RealtimeApi {

    @GET(Endpoint.REALTIME)
    Observable<RealtimeResponse> get(@Path("language") String language);

    @GET(Endpoint.REALTIME_VEHICLE)
    Call<RealtimeResponse> vehicle(@Path("id") int vehicle);

    @GET(Endpoint.REALTIME_VEHICLE)
    Observable<RealtimeResponse> vehicleRx(@Path("id") int vehicle);

    @GET(Endpoint.REALTIME_DELAYS)
    Observable<RealtimeResponse> delaysRx();

    @GET(Endpoint.REALTIME_TRIP)
    Observable<RealtimeResponse> trip(@Path("id") int trip);

    @GET(Endpoint.REALTIME_DELAYS)
    Call<RealtimeResponse> delays();

    @GET(Endpoint.REALTIME_LINE)
    Call<RealtimeResponse> line(@Path("id") int lineId);
}