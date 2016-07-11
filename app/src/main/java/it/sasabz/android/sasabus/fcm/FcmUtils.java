package it.sasabz.android.sasabus.fcm;

import android.content.Context;

import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.TokenApi;
import it.sasabz.android.sasabus.network.rest.response.ValidityResponse;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import it.sasabz.android.sasabus.util.Utils;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

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

    public static void fixFcmRegistration() {
        new Thread(() -> {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
                LogUtils.e(TAG, "Fixed FCM registration");
            } catch (IOException e) {
                Utils.handleException(e);
            }
        }).start();
    }
}
