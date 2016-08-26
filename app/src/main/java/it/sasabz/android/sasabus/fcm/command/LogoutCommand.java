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

package it.sasabz.android.sasabus.fcm.command;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.util.Map;

import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.util.LogUtils;

/**
 * This command handles logout from eco points. If a user chooses to log out from all devices,
 * a fcm command will be sent to each device on which the user is logged in to clear the token.
 * <p>
 * Should this fail, a HTTP 401 code is returned as soon as the user tries to access the eco points,
 * which will log him out nonetheless. This could be considered a failsafe should the fcm command
 * fail or if the user doesn't have play services on his device.
 */
public class LogoutCommand implements FcmCommand {

    private static final String TAG = "LogoutCommand";

    @Override
    public void execute(Context context, @NonNull Map<String, String> data) {
        LogUtils.w(TAG, "Received GCM logout command");

        String user = data.get("user");

        if (!AuthHelper.isLoggedIn()) {
            LogUtils.e(TAG, "Cannot log out if no user is logged in on this device");
            return;
        }

        if (TextUtils.isEmpty(user)) {
            LogUtils.e(TAG, "Got an empty or null user id");
            return;
        }

        if (!user.equals(AuthHelper.getUserId(context))) {
            LogUtils.e(TAG, "User id does not match user id of logged in account");
            return;
        }

        AuthHelper.clearCredentials();

        // Send broadcast to log out if the user is in the app.
        Intent intent = new Intent(AuthHelper.INTENT_BROADCAST_LOGOUT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
