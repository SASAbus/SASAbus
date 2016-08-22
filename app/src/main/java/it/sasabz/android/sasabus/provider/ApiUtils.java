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

package it.sasabz.android.sasabus.provider;

import java.util.List;
import java.util.Locale;

import it.sasabz.android.sasabus.util.Preconditions;

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
