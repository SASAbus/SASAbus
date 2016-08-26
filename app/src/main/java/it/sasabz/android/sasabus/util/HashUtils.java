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

import android.content.Context;
import android.provider.Settings;

/**
 * Utility class to form weak hashes like md5 identifier for trips or app signatures.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public final class HashUtils {

    private HashUtils() {
    }

    public static String getHashForIdentifier(Context context, String identifier) {
        return Utils.md5(identifier + ':' +
                Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID) + ':' +
                System.currentTimeMillis()).substring(0, 8);
    }

    private static String byte2HexF(byte... arr) {
        StringBuilder str = new StringBuilder(arr.length << 1);

        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = '0' + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < arr.length - 1) str.append(':');
        }

        return str.toString();
    }
}
