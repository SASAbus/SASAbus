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

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface RealtimeApi {

    @GET(Endpoint.REALTIME)
    Observable<RealtimeResponse> get();

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