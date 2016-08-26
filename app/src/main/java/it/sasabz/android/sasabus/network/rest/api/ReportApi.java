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
import it.sasabz.android.sasabus.util.ReportHelper;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

public interface ReportApi {

    String TYPE_DEFAULT = "default";
    String TYPE_BUS = "bus";

    @Multipart
    @POST(Endpoint.REPORT)
    Observable<Void> send(@Path("type") String type, @Part("body") ReportHelper.ReportBody body,
                          @Part("image") RequestBody image);

    @Multipart
    @POST(Endpoint.REPORT)
    Observable<Void> sendNoImage(@Path("type") String type, @Part("body") ReportHelper.ReportBody body);
}