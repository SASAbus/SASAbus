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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Locale;

import it.sasabz.android.sasabus.beacon.bus.BusBeacon;
import it.sasabz.android.sasabus.data.network.auth.AuthHelper;
import timber.log.Timber;

/**
 * Utility class to form weak hashes like md5 identifier for trips or app signatures.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public final class HashUtils {

    private HashUtils() {
    }

    public static String getRandomString(int length) {
        return md5(new BigInteger(130, new SecureRandom()).toString(32)).substring(0, length);
    }

    public static String getHashForTrip(Context context, BusBeacon beacon) {

        // Use the trip id as a identifier for this trip, as a trip with that id only drives once
        // a day.
        int trip = beacon.trip;

        Calendar start = Calendar.getInstance();
        start.setTime(beacon.getStartDate());

        // Use the day of the year to uniquely identify the trip. The trip id alone is not enough
        // to identify this trip, as a bus which drives the next day can have the same trip id
        // as the one we're generating the hash for.
        int dayOfYear = start.get(Calendar.DAY_OF_YEAR);

        // Use the year to prevent beacons with the same id having the same hash if they happened
        // in a different year.
        int year = start.get(Calendar.YEAR);

        // Use the origin and destination bus stops to differentiate a trip which drives the same
        // line and trip as another one, but starts and ends at different bus stops (e.g. even though
        // a trip from stazione to ospedale might have the same trip id on the same day, the trip
        // from Stazione to Piazza Walther must have a different hash than a trip from Piazza Vittoria
        // to Via Sorrento).
        int origin = beacon.origin;

        String accountId = AuthHelper.getUserId(context);
        if (accountId == null) {
            // If the user isn't logged in, choose a random account id and add it to the hash.
            // As the trips are not synced if the user is not logged in, it won't matter if
            // the account id is random as the final hash is only used for sync.

            accountId = new BigInteger(130, new SecureRandom()).toString(32);
        }

        // The raw trip hash. The final hash will be a md5 version of this hash.
        String identifier = String.format(Locale.ROOT, "%s:%s:%s:%s:%s",
                trip, dayOfYear, year, accountId, origin);

        Timber.i("Generating hash for bus " + beacon.id + ": " + identifier);

        return md5(identifier).substring(0, 16);
    }

    public static String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(text.getBytes());
            byte[] digest = md.digest();

            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Could not create SHA256", e);
        }
    }

    private static String bytesToHex(byte... bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
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
}
