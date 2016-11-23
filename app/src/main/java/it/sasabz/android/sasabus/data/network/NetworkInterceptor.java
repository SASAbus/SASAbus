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

package it.sasabz.android.sasabus.data.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.io.IOException;
import java.util.List;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.data.network.auth.AuthHelper;
import it.sasabz.android.sasabus.util.Utils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Intercepts each call to the rest api and adds the user agent header to the request.
 * The user agent consists of the app name, followed by the app version name and code.
 * A token is needed to identify the user which made the request when trying to download trips.
 * The auth header is used to check if the request was made from a valid client and block
 * 3rd party users from using the api.
 *
 * @author Alex Lardschneider
 */
public class NetworkInterceptor implements Interceptor {

    private final Context mContext;

    private String mAndroidId;

    public NetworkInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request.Builder newRequest = originalRequest
                .newBuilder()
                .header("User-Agent", "SASAbus Android")
                .addHeader("X-Device-Id", getAndroidId())
                .addHeader("X-Device", Build.MODEL)
                .addHeader("X-Language", Utils.locale(mContext))
                .addHeader("X-Serial", Build.SERIAL)
                .addHeader("X-Version-Code", String.valueOf(BuildConfig.VERSION_CODE))
                .addHeader("X-Version-Name", BuildConfig.VERSION_NAME);

        if (requiresAuthHeader(originalRequest.url().encodedPathSegments())) {
            String token = AuthHelper.getTokenIfValid();

            if (token == null) {
                Timber.e("Token is invalid");
            } else {
                newRequest.addHeader("Authorization", "Bearer " + token);
            }
        }

        Request request = newRequest.build();

        Timber.w(request.method() + " url " + originalRequest.url());

        return chain.proceed(request);
    }

    @SuppressLint("HardwareIds")
    private String getAndroidId() {
        if (mAndroidId != null) {
            return mAndroidId;
        }

        mAndroidId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return mAndroidId;
    }

    private boolean requiresAuthHeader(List<String> segments) {
        String url = segments.get(1);

        //noinspection RedundantIfStatement
        if (url.equals("eco") || url.equals("sync")) {
            return true;
        }

        if (url.equals("auth")) {
            if (segments.get(2).equals("password")) return true;
            if (segments.get(2).equals("logout")) return true;
            if (segments.get(2).equals("delete")) return true;
        }

        return false;
    }
}