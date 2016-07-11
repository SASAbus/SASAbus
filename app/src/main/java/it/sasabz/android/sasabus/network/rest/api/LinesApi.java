package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.LinesAllResponse;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface LinesApi {

    @GET(Endpoint.LINES_ALL)
    Observable<LinesAllResponse> allLines(@Path("language") String language);

    @GET(Endpoint.LINES)
    Observable<LinesAllResponse> line(@Path("language") String language,
                                             @Path("id") int id);

    @GET(Endpoint.LINES_FILTER)
    Observable<LinesAllResponse> filterLines(@Path("language") String language,
                                             @Path("lines") String lines);

    @GET(Endpoint.LINES_HYDROGEN)
    Observable<RealtimeResponse> hydrogen();
}