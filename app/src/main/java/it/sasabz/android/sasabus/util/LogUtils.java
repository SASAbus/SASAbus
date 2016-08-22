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