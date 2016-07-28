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
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;

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
        LogUtils.w(TAG, "Received GCM notification message");
        LogUtils.w(TAG, "Parsing GCM notification command: " + data);

        JSONObject json = new JSONObject();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                Utils.handleException(e);
            }
        }

        Gson gson = new Gson();
        NotificationCommandModel command;

        try {
            command = gson.fromJson(json.toString(), NotificationCommandModel.class);

            if (command == null) {
                LogUtils.e(TAG, "Failed to parse command (gson returned null).");
                return;
            }

            LogUtils.w(TAG, "Id: " + command.id);
            LogUtils.w(TAG, "Audience: " + command.audience);
            LogUtils.w(TAG, "TitleIt: " + command.titleIt);
            LogUtils.w(TAG, "TitleDe: " + command.titleDe);
            LogUtils.w(TAG, "MessageIt: " + command.messageIt);
            LogUtils.w(TAG, "MessageDe: " + command.messageDe);
            LogUtils.w(TAG, "Expiry: " + command.expiry);
            LogUtils.w(TAG, "URL: " + command.url);
            LogUtils.w(TAG, "Dialog titleIt: " + command.dialogTitleIt);
            LogUtils.w(TAG, "Dialog titleDe: " + command.dialogTitleDe);
            LogUtils.w(TAG, "Dialog textIt: " + command.dialogTextIt);
            LogUtils.w(TAG, "Dialog textDe: " + command.dialogTextDe);
            LogUtils.w(TAG, "Dialog yesIt: " + command.dialogYesIt);
            LogUtils.w(TAG, "Dialog yesDe: " + command.dialogYesDe);
            LogUtils.w(TAG, "Dialog noIt: " + command.dialogNoIt);
            LogUtils.w(TAG, "Dialog noDe: " + command.dialogNoDe);
            LogUtils.w(TAG, "Min version code: " + command.minVersion);
            LogUtils.w(TAG, "Max version code: " + command.maxVersion);
            LogUtils.w(TAG, "Color: " + command.color);
        } catch (Exception e) {
            Utils.handleException(e);

            LogUtils.e(TAG, "Failed to parse GCM notification command.");
            return;
        }

        // Do not show this notification on fdroid build as it doesn't support FCM.
        if (data.get("flavor").equals(BuildConfig.FLAVOR) && Utils.isFDroid()) {
            LogUtils.e(TAG, "Fdroid is not supported.");
            return;
        }

        LogUtils.i(TAG, "Processing notification command.");
        processCommand(context, command);
    }

    private void processCommand(Context context, NotificationCommandModel command) {
        String locale = context.getResources().getConfiguration().locale.toString();

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
            LogUtils.w(TAG, "Skipping command because of wrong package name, is "
                    + command.packageName + ", should be " + BuildConfig.APPLICATION_ID);
            return;
        }

        // Check app version
        if (command.minVersion != 0 || command.maxVersion != 0) {
            LogUtils.i(TAG, "Command has version range.");

            int minVersion = command.minVersion;
            int maxVersion = command.maxVersion != 0 ? command.maxVersion : Integer.MAX_VALUE;

            try {
                LogUtils.i(TAG, "Version range: " + minVersion + " - " + maxVersion);
                LogUtils.i(TAG, "My version code: " + BuildConfig.VERSION_CODE);

                if (BuildConfig.VERSION_CODE < minVersion) {
                    LogUtils.w(TAG, "Skipping command because our version is too old, "
                            + BuildConfig.VERSION_CODE + " < " + minVersion);
                    return;
                }
                if (BuildConfig.VERSION_CODE > maxVersion) {
                    LogUtils.i(TAG, "Skipping command because our version is too new, "
                            + BuildConfig.VERSION_CODE + " > " + maxVersion);
                    return;
                }
            } catch (NumberFormatException ex) {
                LogUtils.e(TAG, "Version spec badly formatted: min=" + command.minVersion
                        + ", max=" + command.maxVersion);
                return;
            } catch (Exception ex) {
                LogUtils.e(TAG, "Unexpected problem doing version check.", ex);
                return;
            }
        }

        // Check if we are the right audience
        if ("all".equals(command.audience)) {
            LogUtils.i(TAG, "Relevant (audience is 'all').");
        } else if ("debug".equals(command.audience)) {
            if (!BuildConfig.DEBUG) {
                LogUtils.w(TAG, "App is not in debug mode");
                return;
            }

            LogUtils.i(TAG, "Relevant (audience is 'debug').");
        } else {
            LogUtils.e(TAG, "Invalid audience on GCM notification command: " + command.audience);
            return;
        }

        // Check if it expired
        Date expiry = new Date(command.expiry * 1000L);

        if (expiry.getTime() < System.currentTimeMillis()) {
            LogUtils.w(TAG, "Got expired GCM notification command. Expiry: " + expiry);
            return;
        } else {
            LogUtils.i(TAG, "Message is still valid (expiry is in the future: " + expiry + ')');
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
            LogUtils.e(TAG, "Color spec badly formatted: color=" + command.color + ", using default");
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
