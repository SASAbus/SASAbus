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
import android.support.annotation.NonNull;

import java.util.Map;

import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;

/**
 * Handles incoming news. Because the user is always registered to the GCM topic which will receive
 * all messages, news will arrive even if they are disabled in the preferences. That's why we need
 * to make sure to check if the user has enabled news notifications before showing them.
 *
 * @author Alex Lardschneider
 */
public class NewsCommand implements FcmCommand {

    private static final String TAG = "NotificationCommand";

    @Override
    public void execute(Context context, @NonNull Map<String, String> data) {
        LogUtils.w(TAG, "Received GCM news message");
        LogUtils.w(TAG, "Parsing GCM notification command: " + data);

        if (!SettingsUtils.isNewsPushEnabled(context)) {
            LogUtils.e(TAG, "Ignoring news command as news are disabled in preferences");
            return;
        }

        String language = context.getResources().getConfiguration().locale.toString();
        if (language.length() > 2) {
            language = language.substring(0, 2);
        }

        int id = Integer.parseInt(data.get("id"));
        String title = data.get(language.contains("de") ? "title_de" : "title_it");
        String message = data.get(language.contains("de") ? "message_de" : "message_it");
        String zone = data.get("zone");

        LogUtils.e(TAG, "Notification: id: " + id + ", title: " +
                title + ", message: " + message + ", zone: " + zone);

        NotificationUtils.news(context, id, zone, title, message);
    }
}
