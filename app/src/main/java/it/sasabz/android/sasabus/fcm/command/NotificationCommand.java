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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.receiver.NotificationReceiver;
import it.sasabz.android.sasabus.ui.MapActivity;
import it.sasabz.android.sasabus.util.Utils;
import timber.log.Timber;

/**
 * General purpose command which can display a highly customizable notification. The notification
 * can be targeted to only very specific devices by using {@link NotificationCommandModel#audience}
 * and {@link NotificationCommandModel#minVersion}.
 * <p>
 * A expiry time can also be specified. If the notification command arrives after the specified time,
 * either because the device was offline or not reachable by GCM, it will ignored. The notification
 * will be hidden after the expiry time.
 * <p>
 * An invalid notification will be ignored.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class NotificationCommand implements FcmCommand {

    private static final String TAG = "NotificationCommand";

    static class NotificationCommandModel {

        int id;
        int minVersion;
        int maxVersion;
        int expiry;
        int issuedAt;

        String color;
        String audience;
        String url;

        @SerializedName("package")
        String packageName;

        String titleIt;
        String titleDe;

        String messageIt;
        String messageDe;

        String dialogTitleIt;
        String dialogTitleDe;

        String dialogTextIt;
        String dialogTextDe;

        String dialogYesIt;
        String dialogYesDe;

        String dialogNoIt;
        String dialogNoDe;
    }

    @Override
    public void execute(Context context, @NonNull Map<String, String> data) {
        Timber.w("Received GCM notification message");
        Timber.w("Parsing GCM notification command: " + data);

        JSONObject json = new JSONObject();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                Utils.logException(e);
            }
        }

        Gson gson = new Gson();
        NotificationCommandModel command;

        try {
            command = gson.fromJson(json.toString(), NotificationCommandModel.class);

            if (command == null) {
                Timber.e("Failed to parse command (gson returned null).");
                return;
            }

            Timber.w("Id: " + command.id);
            Timber.w("Audience: " + command.audience);
            Timber.w("TitleIt: " + command.titleIt);
            Timber.w("TitleDe: " + command.titleDe);
            Timber.w("MessageIt: " + command.messageIt);
            Timber.w("MessageDe: " + command.messageDe);
            Timber.w("Expiry: " + command.expiry);
            Timber.w("URL: " + command.url);
            Timber.w("Dialog titleIt: " + command.dialogTitleIt);
            Timber.w("Dialog titleDe: " + command.dialogTitleDe);
            Timber.w("Dialog textIt: " + command.dialogTextIt);
            Timber.w("Dialog textDe: " + command.dialogTextDe);
            Timber.w("Dialog yesIt: " + command.dialogYesIt);
            Timber.w("Dialog yesDe: " + command.dialogYesDe);
            Timber.w("Dialog noIt: " + command.dialogNoIt);
            Timber.w("Dialog noDe: " + command.dialogNoDe);
            Timber.w("Min version code: " + command.minVersion);
            Timber.w("Max version code: " + command.maxVersion);
            Timber.w("Color: " + command.color);
        } catch (Exception e) {
            Utils.logException(e);

            Timber.e("Failed to parse GCM notification command.");
            return;
        }

        // Do not show this notification on fdroid build as it doesn't support FCM.
        if (data.get("flavor").equals(BuildConfig.FLAVOR) && Utils.isFDroid()) {
            Timber.e("Fdroid is not supported.");
            return;
        }

        Timber.i("Processing notification command.");
        processCommand(context, command);
    }

    private void processCommand(Context context, NotificationCommandModel command) {
        String locale = Utils.locale(context);

        String title;
        String message;
        String dialogTitle;
        String dialogText;
        String dialogYes;
        String dialogNo;

        switch (locale) {
            case "de":
                title = command.titleDe;
                message = command.messageDe;
                dialogTitle = command.dialogTitleDe;
                dialogText = command.dialogTextDe;
                dialogYes = command.dialogYesDe;
                dialogNo = command.dialogNoDe;
                break;
            default:
                title = command.titleIt;
                message = command.messageIt;
                dialogTitle = command.dialogTitleIt;
                dialogText = command.dialogTextIt;
                dialogYes = command.dialogYesIt;
                dialogNo = command.dialogNoIt;
        }

        // Check package
        if (!TextUtils.isEmpty(command.packageName) && !command.packageName.equals(BuildConfig.APPLICATION_ID)) {
            Timber.w("Skipping command because of wrong package name, is "
                    + command.packageName + ", should be " + BuildConfig.APPLICATION_ID);
            return;
        }

        // Check app version
        if (command.minVersion != 0 || command.maxVersion != 0) {
            Timber.i("Command has version range.");

            int minVersion = command.minVersion;
            int maxVersion = command.maxVersion != 0 ? command.maxVersion : Integer.MAX_VALUE;

            try {
                Timber.i("Version range: " + minVersion + " - " + maxVersion);
                Timber.i("My version code: " + BuildConfig.VERSION_CODE);

                if (BuildConfig.VERSION_CODE < minVersion) {
                    Timber.w("Skipping command because our version is too old, "
                            + BuildConfig.VERSION_CODE + " < " + minVersion);
                    return;
                }
                if (BuildConfig.VERSION_CODE > maxVersion) {
                    Timber.i("Skipping command because our version is too new, "
                            + BuildConfig.VERSION_CODE + " > " + maxVersion);
                    return;
                }
            } catch (NumberFormatException ex) {
                Timber.e("Version spec badly formatted: min=" + command.minVersion
                        + ", max=" + command.maxVersion);
                return;
            } catch (Exception ex) {
                Timber.e("Unexpected problem doing version check.", ex);
                return;
            }
        }

        // Check if we are the right audience
        if ("all".equals(command.audience)) {
            Timber.i("Relevant (audience is 'all').");
        } else if ("debug".equals(command.audience)) {
            if (!BuildConfig.DEBUG) {
                Timber.w("App is not in debug mode");
                return;
            }

            Timber.i("Relevant (audience is 'debug').");
        } else {
            Timber.e("Invalid audience on GCM notification command: " + command.audience);
            return;
        }

        // Check if it expired
        Date expiry = new Date(command.expiry * 1000L);

        if (expiry.getTime() < System.currentTimeMillis()) {
            Timber.w("Got expired GCM notification command. Expiry: " + expiry);
            return;
        } else {
            Timber.i("Message is still valid (expiry is in the future: " + expiry + ')');
        }

        // decide the intent that will be fired when the user clicks the notification
        Intent intent;
        if (TextUtils.isEmpty(dialogTitle) || TextUtils.isEmpty(dialogText)) {
            // notification leads directly to the URL, no dialog
            if (TextUtils.isEmpty(command.url)) {
                intent = new Intent(context, MapActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(command.url));
            }
        } else {
            // use a dialog
            intent = new Intent(context, MapActivity.class).setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

            intent.putExtra(MapActivity.EXTRA_DIALOG_TITLE, dialogTitle);
            intent.putExtra(MapActivity.EXTRA_DIALOG_MESSAGE, dialogText);

            intent.putExtra(MapActivity.EXTRA_DIALOG_YES,
                    dialogYes == null ? "OK" : dialogYes);
            intent.putExtra(MapActivity.EXTRA_DIALOG_NO,
                    TextUtils.isEmpty(dialogNo) ? "" : dialogNo);
            intent.putExtra(MapActivity.EXTRA_DIALOG_URL,
                    TextUtils.isEmpty(command.url) ? "" : command.url);
        }

        String notificationTitle = TextUtils.isEmpty(title) ?
                context.getString(R.string.app_name) : title;

        String notificationMessage = TextUtils.isEmpty(message) ? "" : message;

        int color = ContextCompat.getColor(context, R.color.primary);
        try {
            if (!TextUtils.isEmpty(command.color)) {
                color = Color.parseColor('#' + command.color);
            }
        } catch (Exception e) {
            Timber.e("Color spec badly formatted: color=" + command.color + ", using default");
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_info_outline_white_24dp)
                .setTicker(notificationMessage)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setColor(color)
                .setContentIntent(PendingIntent.getActivity(context, command.id, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMessage))
                .build();

        notificationManager.notify(command.id, notification);


        // Cancel the notification after it expires.
        Intent cancelIntent = new Intent(context, NotificationReceiver.class);
        cancelIntent.setAction(NotificationReceiver.ACTION_HIDE_NOTIFICATION);
        cancelIntent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION_ID, command.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, command.id,
                cancelIntent, 0);

        long millis = command.issuedAt * 1000L + command.expiry * 1000L;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
    }
}
