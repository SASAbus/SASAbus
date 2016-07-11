package it.sasabz.android.sasabus.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Settings about Google Cloud Messaging.
 *
 * @author Alex Lardschneider
 */
final class FcmSettings {

    private static final String PREF_GCM_TOKEN = "pref_gcm_token";

    private static final String PREF_SENT_GCM_TOKEN = "pref_sent_gcm_token";

    private FcmSettings() {
    }

    /**
     * Saved the user's gcm token.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     * @param token   The token to save.
     */
    public static void setGcmToken(Context context, String token) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_GCM_TOKEN, token).apply();
    }

    /**
     * Returns the saved gcm token.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     * @return the saved gcm token, or {@code null} if it hasn't been saved yet.
     */
    @Nullable
    public static String getGcmToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_GCM_TOKEN, null);
    }

    /**
     * Sets {@code value} indicating if the gcm token has been sent to the server
     * successfully.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     * @param value   a boolean indicating if it has been saved.
     */
    public static void setGcmTokenSent(Context context, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_SENT_GCM_TOKEN, value).apply();
    }

    /**
     * Returns if the gcm token has been sent to the server
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     * @return {@code true} if it has been sent, {@code false} otherwise.
     */
    public static boolean isGcmTokenSent(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SENT_GCM_TOKEN, true);
    }
}
