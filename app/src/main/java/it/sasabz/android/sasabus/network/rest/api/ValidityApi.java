package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.ValidityResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface ValidityApi {

    @GET(Endpoint.VALIDITY_DATA)
    Call<ValidityResponse> data(@Path("date") String date);

    @GET(Endpoint.VALIDITY_TIMETABLES)
    Observable<ValidityResponse> timetables(@Path("date") String date);
}