package it.sasabz.android.sasabus.provider;

import it.sasabz.android.sasabus.util.Preconditions;

import java.util.List;
import java.util.Locale;

/**
 * Some useful methods needed by the offline APIs in the provider package (this package).
 *
 * @author David Dejori
 */
public final class ApiUtils {

    private ApiUtils() {
    }

    public static String getTime(long seconds) {
        return String.format(Locale.ITALY, "%02d:%02d", seconds / 3600 % 24, seconds % 3600 / 60);
    }

    public static int getSeconds(String time) {
        Preconditions.checkNotNull(time, "time == null");

        String[] array = time.split(":");
        return Integer.parseInt(array[0]) * 3600 + Integer.parseInt(array[1]) * 60;
    }

    public static String implode(String separator, List<String> data, String fallback) {
        Preconditions.checkNotNull(separator, "separator == null");
        Preconditions.checkNotNull(data, "data == null");
        Preconditions.checkNotNull(fallback, "fallback == null");

        StringBuilder sb = new StringBuilder();

        if (data.isEmpty()) {
            return fallback;
        }

        for (int i = 0; i < data.size() - 1; i++) {
            sb.append(data.get(i));
            sb.append(separator);
        }

        sb.append(data.get(data.size() - 1));
        return sb.toString().trim();
    }
}
