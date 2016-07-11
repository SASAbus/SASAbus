package it.sasabz.android.sasabus.fcm.command;

import android.content.Context;
import android.support.annotation.NonNull;

import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;

import java.util.Map;

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
