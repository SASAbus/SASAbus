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

package it.sasabz.android.sasabus.data.network.rest.api;

import it.sasabz.android.sasabus.data.network.rest.Endpoint;
import it.sasabz.android.sasabus.data.network.rest.response.LinesAllResponse;
import it.sasabz.android.sasabus.data.network.rest.response.RealtimeResponse;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface LinesApi {

    @GET(Endpoint.LINES_ALL)
    Observable<LinesAllResponse> allLines();

    @GET(Endpoint.LINES)
    Observable<LinesAllResponse> line(@Path("id") int id);

    @GET(Endpoint.LINES_FILTER)
    Observable<LinesAllResponse> filterLines(@Path("lines") String lines);

    @GET(Endpoint.LINES_HYDROGEN)
    Observable<RealtimeResponse> hydrogen();
}