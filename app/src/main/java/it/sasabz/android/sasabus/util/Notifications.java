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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.davale.sasabus.core.model.Departure;
import com.davale.sasabus.core.realm.BusStopRealmHelper;
import com.davale.sasabus.core.realm.model.BusStop;
import com.davale.sasabus.core.util.Strings;
import com.google.gson.Gson;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.beacon.ecopoints.badge.InAppBadge;
import it.sasabz.android.sasabus.beacon.survey.SurveyActivity;
import it.sasabz.android.sasabus.data.network.rest.model.Badge;
import it.sasabz.android.sasabus.data.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.ui.NewsActivity;
import it.sasabz.android.sasabus.ui.departure.DepartureActivity;
import it.sasabz.android.sasabus.ui.ecopoints.EcoPointsActivity;
import timber.log.Timber;

/**
 * Utility class to display notifications. Also handles scheduling planned trip notifications.
 *
 * @author Alex Lardschneider
 */
public final class Notifications {

    private static final int NOTIFICATION_SURVEY = 1 << 20;

    public static final int NOTIFICATION_BUS = 1 << 18;

    private static final int NOTIFICATION_BADGE = 1 << 17;

    private static final int NOTIFICATION_EVENT = 1 << 16;

    private static final int VIBRATION_TIME_MILLIS = 500;

    private Notifications() {
    }

    /**
     * Shows a notification if a bus stop beacon in range is detected and the user is near the beacon
     * for more than {@link it.sasabz.android.sasabus.beacon.busstop.BusStopBeaconHandler
     * #BEACON_NOTIFICATION_TIME_DELTA} seconds
     *
     * @param context   application context
     * @param busStopId id of the bus stop to display
     * @param departures     the {@link List} which contains the {@link Departure} departures which are
     *                  displayed in the expanded notification.
     */
    public static void busStop(Context context, int busStopId, List<Departure> departures) {
        Preconditions.checkNotNull(context, "busStop() context == null");

        String stationName = BusStopRealmHelper.getName(busStopId);

        String contentText = context.getString(departures.isEmpty() ?
                R.string.notification_bus_stop_sub_click : R.string.notification_bus_stop_sub_pull);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_station)
                .setContentTitle(stationName)
                .setContentText(contentText)
                .setAutoCancel(false)
                .setLights(Color.RED, 500, 5000)
                .setColor(ContextCompat.getColor(context, R.color.red_500))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        if (Settings.isBusStopVibrationEnabled(context)) {
            mBuilder.setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS});
        } else {
            mBuilder.setVibrate(null);
        }

        BusStop busStop = BusStopRealmHelper.getBusStop(busStopId);
        Intent resultIntent = DepartureActivity.intent(context, busStop.getFamily());

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                busStopId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RemoteViews expandedView = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);

            expandedView.setTextViewText(R.id.notification_title, context.getString(R.string.notification_expanded_title, stationName));

            if (!departures.isEmpty()) {
                Departure stopDetail = departures.get(0);

                expandedView.setViewVisibility(R.id.notification_departure_1, View.VISIBLE);
                expandedView.setTextViewText(R.id.notification_departure_1_line, stopDetail.getLine());
                expandedView.setTextViewText(R.id.notification_departure_1_time, stopDetail.getTime());
                expandedView.setTextViewText(R.id.notification_departure_1_last, context.getString(R.string.notification_heading, stopDetail.getDestination()));

                if (stopDetail.getDelay() > 3) {
                    expandedView.setTextColor(R.id.notification_departure_1_delay, ContextCompat.getColor(context, R.color.material_red_500));
                } else if (stopDetail.getDelay() > 0) {
                    expandedView.setTextColor(R.id.notification_departure_1_delay, ContextCompat.getColor(context, R.color.material_amber_700));
                }

                if (stopDetail.getDelay() != Departure.NO_DELAY) {
                    expandedView.setTextViewText(R.id.notification_departure_1_delay, stopDetail.getDelay() + "'");
                }
            }

            if (departures.size() > 1) {
                Departure stopDetail = departures.get(1);

                expandedView.setViewVisibility(R.id.notification_departure_2, View.VISIBLE);
                expandedView.setTextViewText(R.id.notification_departure_2_line, stopDetail.getLine());
                expandedView.setTextViewText(R.id.notification_departure_2_time, stopDetail.getTime());
                expandedView.setTextViewText(R.id.notification_departure_2_last, context.getString(R.string.notification_heading, stopDetail.getDestination()));

                if (stopDetail.getDelay() > 3) {
                    expandedView.setTextColor(R.id.notification_departure_2_delay, ContextCompat.getColor(context, R.color.material_red_500));
                } else if (stopDetail.getDelay() > 0) {
                    expandedView.setTextColor(R.id.notification_departure_2_delay, ContextCompat.getColor(context, R.color.material_amber_700));
                }

                if (stopDetail.getDelay() != Departure.NO_DELAY) {
                    expandedView.setTextViewText(R.id.notification_departure_2_delay, stopDetail.getDelay() + "'");
                }
            }

            notification = mBuilder.build();

            if (!departures.isEmpty()) {
                notification.bigContentView = expandedView;
            }
        } else {
            notification = mBuilder.build();
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(busStopId, notification);
    }

    /**
     * Shows a notification when a new news entry is available.
     *
     * @param context application context
     * @param zone    the zone this news entry affects.
     * @param title   title of this news entry
     * @param message message of this news entry
     */
    public static void news(Context context, int id, CharSequence zone, CharSequence title,
                            CharSequence message) {
        Preconditions.checkNotNull(context, "news() context == null");
        Preconditions.checkNotNull(zone, "zone == null");
        Preconditions.checkNotNull(title, "title == null");
        Preconditions.checkNotNull(message, "message == null");

        message = Strings.sanitizeString(message.toString());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_event_note_white_48dp)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setLights(ContextCompat.getColor(context, R.color.default_icon_color), 500, 5000)
                .setColor(ContextCompat.getColor(context, R.color.default_icon_color))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        Intent resultIntent = new Intent(context, NewsActivity.class);
        resultIntent.putExtra(Config.EXTRA_SHOW_NEWS, true);
        resultIntent.putExtra(Config.EXTRA_NEWS_ID, id);
        resultIntent.putExtra(Config.EXTRA_NEWS_ZONE, zone);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                id,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, mBuilder.build());
    }

    public static void eventBeacon(Context context, CharSequence event, int point, int color) {
        String subtitle = context.getString(R.string.notification_event_beacon_subtitle, point);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_bluetooth_white_48dp)
                .setContentTitle(event)
                .setContentText(subtitle)
                .setAutoCancel(true)
                .setLights(color, 500, 5000)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        Intent resultIntent = new Intent(context, EcoPointsActivity.class);
        resultIntent.putExtra(EcoPointsActivity.EXTRA_SHOW_EVENTS, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_EVENT * point,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_EVENT * point, mBuilder.build());
    }

    public static void survey(Context context, CloudTrip trip) {
        Preconditions.checkNotNull(context, "survey() context == null");
        Preconditions.checkNotNull(trip, "trip == null");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_assessment_white_48dp)
                .setContentTitle(context.getString(R.string.notification_survey_title))
                .setContentText(context.getString(R.string.notification_survey_subtitle))
                .setAutoCancel(true)
                .setLights(Color.GREEN, 500, 5000)
                .setColor(ContextCompat.getColor(context, R.color.material_teal_500))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        Intent resultIntent = new Intent(context, SurveyActivity.class);
        resultIntent.putExtra(Config.EXTRA_TRIP, new Gson().toJson(trip));

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_SURVEY,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_SURVEY, mBuilder.build());
    }

    public static void badge(Context context, InAppBadge badge) {
        Preconditions.checkNotNull(context, "context == null");
        Preconditions.checkNotNull(badge, "badge == null");

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), badge.icon());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(badge.icon())
                .setLargeIcon(bitmap)
                .setContentTitle(context.getString(badge.title()))
                .setContentText(context.getString(badge.summary()))
                .setAutoCancel(true)
                .setLights(ContextCompat.getColor(context, R.color.primary), 500, 5000)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        Intent resultIntent = new Intent(context, EcoPointsActivity.class);
        resultIntent.putExtra(EcoPointsActivity.EXTRA_SHOW_BADGES, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_BADGE,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_BADGE, mBuilder.build());
    }

    @WorkerThread
    public static void badge(Context context, Badge badge) {
        Preconditions.checkNotNull(context, "context == null");
        Preconditions.checkNotNull(badge, "badge == null");

        try {
            Bitmap bitmap = Glide.with(context)
                    .load(badge.iconUrl)
                    .asBitmap()
                    .into(100, 100)
                    .get();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.badge_red_stop)
                    .setLargeIcon(bitmap)
                    .setContentTitle(badge.title)
                    .setContentText(badge.description)
                    .setAutoCancel(true)
                    .setLights(ContextCompat.getColor(context, R.color.primary), 500, 5000)
                    .setColor(ContextCompat.getColor(context, R.color.primary))
                    .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                    .setCategory(NotificationCompat.CATEGORY_EVENT);

            Intent resultIntent = new Intent(context, EcoPointsActivity.class);
            resultIntent.putExtra(Config.EXTRA_BADGE, badge);
            resultIntent.putExtra(EcoPointsActivity.EXTRA_SHOW_BADGES, true);

            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    badge.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(badge.id, mBuilder.build());
        } catch (InterruptedException | ExecutionException e) {
            Utils.logException(e);
        }
    }

    /**
     * Debug method used to display an error why the queued trip wasn't saved
     *
     * @param context application context
     * @param e       the throwable error
     */
    public static void error(Context context, Throwable e) {
        if (!BuildConfig.DEBUG) {
            Timber.e("Called error notification with production build.");
            Utils.logException(new Throwable("Called error notification with " +
                    "production build."));

            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(e.getClass().getSimpleName())
                .setContentText(e.getMessage())
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_timeline)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(e.getMessage()))
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(new Random().nextInt(100) + 10000, mBuilder.build());
    }

    /**
     * Cancels a shown notification.
     *
     * @param context Context to access the {@link NotificationManager}.
     * @param id      the notification id to cancel.
     */
    public static void cancel(Context context, int id) {
        Preconditions.checkNotNull(context, "cancel() context == null");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(id);
    }

    public static void cancelBus(Context context) {
        cancel(context, NOTIFICATION_BUS);
    }
}
