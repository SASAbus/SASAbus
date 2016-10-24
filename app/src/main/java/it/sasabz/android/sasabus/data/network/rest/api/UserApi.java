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

import com.google.gson.annotations.SerializedName;

import it.sasabz.android.sasabus.data.model.JsonSerializable;
import it.sasabz.android.sasabus.data.network.rest.Endpoint;
import it.sasabz.android.sasabus.data.network.rest.response.LoginResponse;
import it.sasabz.android.sasabus.data.network.rest.response.PasswordResponse;
import it.sasabz.android.sasabus.data.network.rest.response.RegisterResponse;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface UserApi {

    @POST(Endpoint.USER_LOGIN)
    Observable<LoginResponse> login(@Body LoginBody body);

    @POST(Endpoint.USER_REGISTER)
    Observable<RegisterResponse> register(@Body RegisterBody body);

    @GET(Endpoint.USER_VERIFY)
    Observable<LoginResponse> verify(
            @Path("email") String email,
            @Path("token") String token
    );

    @FormUrlEncoded
    @POST(Endpoint.CHANGE_PASSWORD)
    Observable<PasswordResponse> changePassword(
            @Field("old_pwd") String oldPassword,
            @Field("new_pwd") String newPassword,
            @Field("fcm_token") String fcmToken
    );

    @FormUrlEncoded
    @POST(Endpoint.USER_LOGOUT)
    Observable<Void> logout(@Field("fcm_token") String fcmToken);

    @GET(Endpoint.USER_LOGOUT_ALL)
    Observable<Void> logoutAll();

    @DELETE(Endpoint.USER_DELETE)
    Observable<Void> delete();

    class LoginBody implements JsonSerializable {

        final String email;
        final String password;

        @SerializedName("fcm_token")
        final String fcmToken;

        public LoginBody(String email, String password, String fcmToken) {
            this.email = email;
            this.password = password;
            this.fcmToken = fcmToken;
        }
    }

    class RegisterBody implements JsonSerializable {

        final String email;
        final String username;
        final String password;
        final int birthdate;
        final boolean male;

        @SerializedName("fcm_token")
        final String fcmToken;

        public RegisterBody(String email, String username, String password, String fcmToken, int birthdate, boolean male) {
            this.email = email;
            this.username = username;
            this.password = password;
            this.fcmToken = fcmToken;
            this.birthdate = birthdate;
            this.male = male;
        }
    }
}