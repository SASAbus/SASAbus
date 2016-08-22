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

package it.sasabz.android.sasabus.fcm;

import android.content.Context;

import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.TokenApi;
import it.sasabz.android.sasabus.network.rest.response.ValidityResponse;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.schedulers.Schedulers;

/**
 * Helper class to help with various things regarding Google Cloud Messaging (GCM).
 *
 * @author Alex Lardschneider
 */
public final class FcmUtils {

    private static final String TAG = "FcmUtils";

    private FcmUtils() {
    }

    public static void checkFcm(Context context) {
        if (!Utils.checkPlayServices(context)) {
            LogUtils.e(TAG, "No play services found");
            return;
        }

        String token = FcmSettings.getGcmToken(context);
        if (token != null && !FcmSettings.isGcmTokenSent(context)) {
            sendTokenToServer(context, token);
        }
    }

    /**
     * Sends the gcm token to the server.
     *
     * @param context Context to access {@link android.content.SharedPreferences}.
     * @param token   the token to send.
     */
    static void sendTokenToServer(Context context, String token) {
        Preconditions.checkNotNull(context, "sendTokenToServer() context == null");
        Preconditions.checkNotNull(token, "token == null");

        LogUtils.e(TAG, "Sending token");

        TokenApi tokenApi = RestClient.ADAPTER.create(TokenApi.class);
        tokenApi.send(token)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ValidityResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        FcmSettings.setGcmTokenSent(context, false);
                    }

                    @Override
                    public void onNext(ValidityResponse validityResponse) {
                        FcmSettings.setGcmTokenSent(context, true);

                        LogUtils.e(TAG, "Sent token");
                    }
                });

    }
}
