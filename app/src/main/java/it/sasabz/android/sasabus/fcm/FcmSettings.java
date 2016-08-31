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
public final class FcmSettings {

    private static final String PREF_GCM_TOKEN = "pref_gcm_token";

    private FcmSettings() {
    }

    /**
     * Saved the user's gcm token.
     *
     * @param context Context to be used to edit the {@link SharedPreferences}.
     * @param token   The token to save.
     */
    static void setGcmToken(Context context, String token) {
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
}
