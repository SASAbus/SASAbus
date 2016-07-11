package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.PathResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface PathsApi {

    @GET(Endpoint.PATHS)
    Observable<PathResponse> getPath(@Path("id") int id);
}