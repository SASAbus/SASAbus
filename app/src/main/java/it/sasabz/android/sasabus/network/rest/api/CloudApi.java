package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.model.CloudPlannedTrip;
import it.sasabz.android.sasabus.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.network.rest.response.CloudResponseGet;
import it.sasabz.android.sasabus.network.rest.response.CloudResponsePost;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public interface CloudApi {

    @GET(Endpoint.CLOUD_TRIPS)
    Call<CloudResponseGet> compareTrips();

    @POST(Endpoint.CLOUD_TRIPS)
    Call<CloudResponsePost> downloadTrips(@Body List<String> trips);

    @PUT(Endpoint.CLOUD_TRIPS)
    Call<Void> uploadTrips(@Body List<CloudTrip> body);

    @DELETE(Endpoint.CLOUD_TRIPS_DELETE)
    Observable<Void> deleteTripRx(@Path("hash") String hash);

    @DELETE(Endpoint.CLOUD_TRIPS_DELETE)
    Call<Void> deleteTrip(@Path("hash") String hash);

    @GET(Endpoint.CLOUD_PLANNED_TRIPS)
    Call<CloudResponseGet> comparePlannedTrips();

    @POST(Endpoint.CLOUD_PLANNED_TRIPS)
    Call<CloudResponsePost> downloadPlannedTrips(@Body List<String> trips);

    @PUT(Endpoint.CLOUD_PLANNED_TRIPS)
    Call<Void> uploadPlannedTrips(@Body List<CloudPlannedTrip> body);

    @DELETE(Endpoint.CLOUD_PLANNED_TRIPS_DELETE)
    Observable<Void> deletePlannedTripRx(@Path("hash") String hash);

    @DELETE(Endpoint.CLOUD_PLANNED_TRIPS_DELETE)
    Call<Void> deletePlannedTrip(@Path("hash") String hash);
}