package it.sasabz.android.sasabus.util;

import android.util.Log;

import it.sasabz.android.sasabus.BuildConfig;

/**
 * Utility class to log errors/debug information. Logs can be disabled by changing
 * {@link #LOGGING_ENABLED} to reduce logging performance impacts in release builds.
 *
 * @author Alex Lardschneider
 */
public final class LogUtils {

    private static final boolean LOGGING_ENABLED = BuildConfig.DEBUG;

    // Private constructor to prevent creating an object of this class.
    private LogUtils() {
    }

    public static void d(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message, cause);
        }
    }
}