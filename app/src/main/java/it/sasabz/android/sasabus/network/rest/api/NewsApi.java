package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.NewsResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface NewsApi {

    @GET(Endpoint.NEWS)
    Observable<NewsResponse> getNews(@Path("language") String language);
}