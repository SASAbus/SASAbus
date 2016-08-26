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
import it.sasabz.android.sasabus.network.rest.response.BadgesResponse;
import it.sasabz.android.sasabus.network.rest.response.LeaderboardResponse;
import it.sasabz.android.sasabus.network.rest.response.ProfilePictureResponse;
import it.sasabz.android.sasabus.network.rest.response.ProfileResponse;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

public interface EcoPointsApi {

    @GET(Endpoint.ECO_POINTS_BADGES)
    Observable<BadgesResponse> getAllBadges();

    @GET(Endpoint.ECO_POINTS_BADGES_NEXT)
    Observable<BadgesResponse> getNextBadges();

    @GET(Endpoint.ECO_POINTS_BADGES_EARNED)
    Observable<BadgesResponse> getEarnedBadges();

    @PUT(Endpoint.ECO_POINTS_BADGES_SEND)
    Observable<Void> sendBadge(@Path("id") int id);

    @GET(Endpoint.ECO_POINTS_LEADERBOARD)
    Observable<LeaderboardResponse> getLeaderboard(@Path("page") int page);

    @GET(Endpoint.ECO_POINTS_PROFILE)
    Observable<ProfileResponse> getProfile();

    @GET(Endpoint.ECO_POINTS_PROFILE_ID)
    Observable<ProfileResponse> getProfile(@Path("id") int id);

    @GET(Endpoint.ECO_POINTS_PROFILE_PICTURE_DEFAULT)
    Observable<ProfilePictureResponse> getProfilePictures();

    @FormUrlEncoded
    @POST(Endpoint.ECO_POINTS_PROFILE_PICTURE_DEFAULT)
    Observable<Void> upload(@Field("url") String url);

    @Multipart
    @POST(Endpoint.ECO_POINTS_PROFILE_PICTURE_CUSTOM)
    Observable<Void> upload(@Part("picture") RequestBody file);
}