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

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Called if InstanceID token is updated. This may occur if the security of
 * the previous token had been compromised. Note that this is also called
 * when the InstanceID token is initially generated, so this is where
 * you retrieve the token.
 *
 * @author Alex Lardschneider
 */
public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "InstanceIdService";

    @Override
    public void onTokenRefresh() {
        if (Utils.isFDroid()) {
            return;
        }

        String token = FirebaseInstanceId.getInstance().getToken();

        LogUtils.e(TAG, "Got token: " + token);

        FcmSettings.setGcmToken(this, token);

        FirebaseMessaging.getInstance().subscribeToTopic("general");

        LogUtils.e(TAG, "Registration complete");
    }
}