package it.sasabz.android.sasabus.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import it.sasabz.android.sasabus.util.Preconditions;

/**
 * Utility class which helps with making network calls or check the current network status.
 *
 * @author Alex Lardschneider
 */
public final class NetUtils {

    private NetUtils() {
    }

    public static final String HOST = "https://sasa-bz.appspot.com";

    /**
     * Indicates whether network connectivity exists and it is possible to establish
     * connections and pass data.
     *
     * @param context AppApplication context
     * @return {@code true} if network connectivity exists, {@code false} otherwise.
     */
    public static boolean isOnline(Context context) {
        Preconditions.checkNotNull(context, "isOnline() context == null");

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }

        return false;
    }

    /**
     * Checks if wifi is connected and is the active internet connection. This is important as
     * we don't want to downloadTrips images if the user opted to only downloadTrips them on wifi.
     *
     * @param context Context to access the {@link ConnectivityManager}.
     * @return {@code true} if wifi is the active internet connection, {@code false} otherwise.
     */
    public static boolean isWifiConnected(Context context) {
        Preconditions.checkNotNull(context, "isWifiConnected() context == null");

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
