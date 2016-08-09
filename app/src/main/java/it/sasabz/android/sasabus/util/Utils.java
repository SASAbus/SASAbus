package it.sasabz.android.sasabus.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Html;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLException;

import io.realm.Realm;
import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.beacon.bus.BusBeacon;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.realm.user.Trip;
import it.sasabz.android.sasabus.ui.widget.SearchSnippet;

/**
 * Utility class which holds various methods to help with things like logging exceptions.
 *
 * @author Alex Lardschneider
 */
public final class Utils {

    private Utils() {
    }

    public static boolean isFDroid() {
        return BuildConfig.FLAVOR.equals("fdroid");
    }

    /**
     * Changes the language of the current activity or fragment
     *
     * @param context AppApplication context
     */
    public static void changeLanguage(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        if (!SettingsUtils.getLanguage(context).toLowerCase().equals("system")) {
            configuration.locale = new Locale(SettingsUtils.getLanguage(context));
        } else {
            configuration.locale = new Locale(Locale.getDefault().getLanguage());
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    /**
     * Removes all non UTF-8 and non-printable characters from a string.
     *
     * @param s the string to sanitize
     * @return a string without non-printable chars
     */
    static CharSequence sanitizeString(String s) {
        return Html.fromHtml(s).toString().replaceAll("\\p{C}", "");
    }

    public static String locale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0).getLanguage();
        }

        //noinspection deprecation
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    /**
     * Returns a {@link String} encoded in MD5.
     *
     * @param s the s to encode
     * @return the encoded string or an empty string if the encoding fails
     */
    public static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String string = Integer.toHexString(0xFF & b);

                while (string.length() < 2) {
                    string = '0' + string;
                }

                hexString.append(string);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if all prerequisites are met for the beacon handler to be started.
     *
     * @param context Context to access device info and preferences.
     * @return a boolean value indicating whether the beacon handler can be started.
     */
    public static boolean isBeaconEnabled(Context context) {
        return SettingsUtils.isBeaconEnabled(context) &&
                DeviceUtils.isBluetoothEnabled() &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Logs a {@link Throwable}. If the current build is a debug version print the stack trace, else
     * log the exception using {@link Crashlytics}.
     *
     * @param t the {@link Throwable} to log.
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void handleException(Throwable t) {
        if (BuildConfig.DEBUG) {
            t.printStackTrace();
        } else {
            if (t instanceof SocketTimeoutException) return;
            if (t instanceof SocketException) return;
            if (t instanceof UnknownHostException) return;
            if (t instanceof SSLException) return;

            if (t.getMessage() != null) {
                if (t instanceof IOException && t.getMessage().equals("PROTOCOL_ERROR")) return;
                if (t instanceof IOException && t.getMessage().equals("Canceled")) return;
                if (t instanceof IOException && t.getMessage().equals("CANCEL")) return;
                if (t instanceof JSONException && t.getMessage().contains("<!DOCTYPE")) return;
                if (t instanceof JSONException && t.getMessage().contains("End of input")) return;
                if (t instanceof JSONException && t.getMessage().contains("shutdown")) return;
                if (t instanceof JSONException && t.getMessage().contains("Socket closed")) return;
                if (t instanceof JSONException && t.getMessage().contains("<html><head>")) return;
            }

            Crashlytics.getInstance().core.logException(t);
        }
    }

    /**
     * Logs a {@link Throwable}. If the current build is a debug version print the stack trace, else
     * log the exception using {@link Crashlytics}.
     *
     * @param t the {@link Throwable} to log.
     */
    public static void handleException(Throwable t, String format, Object... params) {
        handleException(new Throwable(String.format(format, params), t));
    }

    /**
     * Converts a {@link Uri} into a {@code String} representing the absolute path
     * to the image.
     *
     * @param context Context to access the {@link android.content.ContentResolver}.
     * @param uri     the uri of the image.
     * @return the file path of the image.
     */
    static String getPathFromUri(Context context, Uri uri) {
        Cursor cursor = null;

        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);

            if (cursor != null) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(index);
            }

            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Returns the play services connection status.
     *
     * @param context Context to access Google Play apis.
     * @return an {@link Integer} representing the connection status.
     * @see ConnectionResult#SUCCESS
     * @see ConnectionResult#API_UNAVAILABLE
     */
    public static int getPlayServicesStatus(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to downloadTrips the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);

        return resultCode == ConnectionResult.SUCCESS;
    }

    @NonNull
    public static <T> CharSequence arrayToString(@NonNull T[] array, @NonNull String delimiter) {
        Preconditions.checkNotNull(array, "array == null");
        Preconditions.checkNotNull(delimiter, "delimiter == null");

        StringBuilder sb = new StringBuilder();

        for (T item : array) {
            sb.append(item).append(delimiter);
        }

        if (sb.length() >= delimiter.length()) {
            sb.setLength(sb.length() - delimiter.length());
        }

        return sb.toString();
    }

    /**
     * Method used to log when there was an error when saving the trip, due to
     * i.e a invalid start or stop station. Try to show a alert dialog, when not possible
     * try to show an error notification.
     *
     * @param text the trip error
     */
    public static void throwTripError(Context context, String text) {
        if (BuildConfig.DEBUG) {
            NotificationUtils.error(context, new IllegalTripException(text));
            LogUtils.e("Utils", "Trip error: " + text);
        }
    }

    /**
     * Formats a search query to be used with {@link SearchSnippet}.
     * A "{" marks the beginning of a found query string and "}" marks the end of it.
     *
     * @param name   the name of the result
     * @param query  the query string to search for and format
     * @param format the format start and end chars, have to be an even length
     * @return the formatted string if {@code name} contains {@code query}, otherwise
     * the unformatted raw {@code name} string.
     */
    @NonNull
    public static String formatQuery(String name, String query, String... format) {
        Preconditions.checkNotNull(name, "Format name cannot be null");
        Preconditions.checkNotNull(query, "Format query cannot be null");

        if (name.toLowerCase().contains(query.toLowerCase())) {
            int indexStart = name.toLowerCase().indexOf(query.toLowerCase());
            int indexEnd = indexStart + query.length() + 1;

            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.insert(indexStart, format[0]);
            sb.insert(indexEnd, format[1]);

            return sb.toString();
        }

        return name;
    }

    @NonNull
    public static <T> String listToString(@NonNull List<T> list, @NonNull String delimiter) {
        Preconditions.checkNotNull(list, "List cannot be null");
        Preconditions.checkNotNull(delimiter, "Delimiter cannot be null");

        StringBuilder sb = new StringBuilder();

        for (T item : list) {
            sb.append(item).append(delimiter);
        }

        if (sb.length() >= delimiter.length()) {
            sb.setLength(sb.length() - delimiter.length());
        }

        return sb.toString();
    }

    @NonNull
    public static List<Integer> stringToList(@NonNull String s, @NonNull String delimiter) {
        List<Integer> list = new ArrayList<>();

        if (s.isEmpty()) return list;

        String[] split = s.split(delimiter);

        for (String s1 : split) {
            list.add(Integer.valueOf(s1));
        }

        return list;
    }

    public static boolean insertTripIfValid(Context context, BusBeacon beacon) {
        if (beacon.destination == 0) {
            throwTripError(context, "Trip " + beacon.id + " invalid -> getStopStation == 0");
            return false;
        }

        if (beacon.origin == beacon.destination &&
                beacon.lastSeen - beacon.getStartDate().getTime() < 600000) {
            throwTripError(context, "Trip " + beacon.id + " invalid -> getOrigin == getStopStation: " +
                    beacon.origin + ", " + beacon.destination);
            return false;
        }

        Realm realm = Realm.getDefaultInstance();

        Trip trip = realm.where(Trip.class).equalTo("hash", beacon.hash).findFirst();

        //noinspection SimplifiableIfStatement
        if (trip != null) {
            // Trip is already in db.
            // We do not care about this error so do not show an error notification
            return false;
        }

        return UserRealmHelper.insertTrip(beacon);
    }
}
