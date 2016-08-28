/*
 * Copyright (C) 2016 David Dejori, Alex Lardschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.sasabz.android.sasabus.network.rest.api;

import java.util.List;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.network.rest.response.CloudResponseGet;
import it.sasabz.android.sasabus.network.rest.response.CloudResponsePost;
import it.sasabz.android.sasabus.network.rest.response.TripUploadResponse;
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

    @DELETE(Endpoint.CLOUD_TRIPS_DELETE)
    Observable<Void> deleteTripRx(@Path("hash") String hash);

    @DELETE(Endpoint.CLOUD_TRIPS_DELETE)
    Call<Void> deleteTrip(@Path("hash") String hash);

    @PUT(Endpoint.CLOUD_TRIPS)
    Observable<TripUploadResponse> uploadTrips(@Body List<CloudTrip> body);
}