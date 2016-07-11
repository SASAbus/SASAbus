package it.sasabz.android.sasabus.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Utility class to help with identifying the current device and to get info about it
 * i.e if bluetooth is enabled or if the device is a tablet.
 *
 * @author Alex Lardschneider
 */
public final class DeviceUtils {

    private DeviceUtils() {
    }

    /**
     * Gets the devices bluetooth name
     *
     * @return bluetooth name, or {@code unknown} when no adapter is available
     */
    // TODO: 24/03/16 Remove try/catch block as soon as reason for crash is found.
    public static String getDeviceName() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter != null) {
                String bluetoothName = bluetoothAdapter.getName();

                if (bluetoothName != null) {
                    return bluetoothName;
                }
            }
        } catch (Exception e) {
            Utils.handleException(e, "Exception: %s", "getDeviceName");
        }

        return "unknown";
    }

    /**
     * Gets the devices bluetooth name
     *
     * @return bluetooth name, or {@code none} when no adapter is available
     */
    public static boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Detects if the current device is a tablet by calculating it from
     * its screen width
     *
     * @param context the context to access resources
     * @return {@code true} if tablet, {@code false} otherwise
     */
    public static boolean isTablet(Context context) {
        Preconditions.checkNotNull(context, "isTablet() context == null");

        return (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Get the device's screen width
     *
     * @param context AppApplication context
     * @return screen width in pixels
     */
    public static int getScreenWidth(Context context) {
        Preconditions.checkNotNull(context, "getScreenWidth() context == null");

        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * Get the device's screen height
     *
     * @param context AppApplication context
     * @return screen height in pixels
     */
    static int getScreenHeight(Context context) {
        Preconditions.checkNotNull(context, "getScreenHeight() context == null");

        return context.getResources().getDisplayMetrics().heightPixels;
    }
}
