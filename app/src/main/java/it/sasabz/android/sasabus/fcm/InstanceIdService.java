package it.sasabz.android.sasabus.fcm;

import it.sasabz.android.sasabus.util.LogUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Called if InstanceID token is updated. This may occur if the security of
 * the previous token had been compromised. Note that this is also called
 * when the InstanceID token is initially generated, so this is where
 * you retrieve the token.
 *
 * @author Alex Lardschneider
 */
public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "InstanceIdService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();

        LogUtils.e(TAG, "Got token: " + token);

        FcmUtils.sendTokenToServer(this, token);
        FcmSettings.setGcmToken(this, token);

        FirebaseMessaging.getInstance().subscribeToTopic("general");

        LogUtils.e(TAG, "Registration complete");
    }
}