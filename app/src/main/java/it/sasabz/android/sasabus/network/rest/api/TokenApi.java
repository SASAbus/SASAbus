package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.ValidityResponse;

import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public interface TokenApi {

    @PUT(Endpoint.TOKEN)
    Observable<ValidityResponse> send(@Path("token") String token);
}