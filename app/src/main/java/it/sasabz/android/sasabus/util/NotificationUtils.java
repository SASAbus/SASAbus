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

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.beacon.survey.SurveyActivity;
import it.sasabz.android.sasabus.model.BusStopDetail;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.network.rest.model.Badge;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.ui.MapActivity;
import it.sasabz.android.sasabus.ui.NewsActivity;
import it.sasabz.android.sasabus.ui.busstop.BusStopDetailActivity;
import it.sasabz.android.sasabus.ui.ecopoints.EcoPointsActivity;
import it.sasabz.android.sasabus.ui.plannedtrip.PlannedTripsViewActivity;
import it.sasabz.android.sasabus.ui.trips.TripDetailActivity;

/**
 * Utility class to display notifications. Also handles scheduling planned trip notifications.
 *
 * @author Alex Lardschneider
 */
public final class NotificationUtils {

    private static final String TAG = "NotificationUtils";

    private static final int VIBRATION_TIME_MILLIS = 500;

    private NotificationUtils() {
    }

    /**
     * Shows a notification if a bus beacon in range is detected and the user is near the beacon.
     *
     * @param context   application context
     * @param vehicleId id of the bus to display
     */
    public static void bus(Context context, int vehicleId, CharSequence title) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_bus)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.notification_bus_sub))
                .setAutoCancel(false)
                .setOngoing(true)
                .setVibrate(null)
                .setColor(ContextCompat.getColor(context, R.color.green_500))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        Intent resultIntent = new Intent(context, MapActivity.class);
        resultIntent.putExtra(Config.EXTRA_VEHICLE, vehicleId);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                vehicleId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_BUS, mBuilder.build());
    }

    /**
     * Shows a notification if a bus stop beacon in range is detected and the user is near the beacon
     * for more than {@link it.sasabz.android.sasabus.beacon.busstop.BusStopBeaconHandler
     * #BEACON_NOTIFICATION_TIME_DELTA} seconds
     *
     * @param context   application context
     * @param busStopId id of the bus stop to display
     * @param items     the {@link List} which contains the {@link BusStopDetail} items which are
     *                  displayed in the expanded notification.
     */
    public static void busStop(Context context, int busStopId, List<BusStopDetail> items) {
        Preconditions.checkNotNull(context, "busStop() context == null");

        String stationName = BusStopRealmHelper.getName(busStopId);

        String contentText = context.getString(items.isEmpty() ?
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

        if (SettingsUtils.isBusStopVibrationEnabled(context)) {
            mBuilder.setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS});
        } else {
            mBuilder.setVibrate(null);
        }

        Intent resultIntent = new Intent(context, BusStopDetailActivity.class);
        resultIntent.putExtra(Config.EXTRA_STATION_ID, busStopId);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                busStopId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RemoteViews expandedView = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);

            expandedView.setTextViewText(R.id.notification_title, context.getString(R.string.notification_expanded_title, stationName));

            if (!items.isEmpty()) {
                BusStopDetail stopDetail = items.get(0);

                expandedView.setViewVisibility(R.id.notification_departure_1, View.VISIBLE);
                expandedView.setTextViewText(R.id.notification_departure_1_line, stopDetail.getLine());
                expandedView.setTextViewText(R.id.notification_departure_1_time, stopDetail.getTime());
                expandedView.setTextViewText(R.id.notification_departure_1_last, context.getString(R.string.notification_heading, stopDetail.getLastStation()));

                if (stopDetail.getDelay() > 3) {
                    expandedView.setTextColor(R.id.notification_departure_1_delay, ContextCompat.getColor(context, R.color.primary_red));
                } else if (stopDetail.getDelay() > 0) {
                    expandedView.setTextColor(R.id.notification_departure_1_delay, ContextCompat.getColor(context, R.color.primary_amber_dark));
                }

                if (stopDetail.getDelay() != Config.BUS_STOP_DETAILS_NO_DELAY) {
                    expandedView.setTextViewText(R.id.notification_departure_1_delay, stopDetail.getDelay() + "'");
                }
            }

            if (items.size() > 1) {
                BusStopDetail stopDetail = items.get(1);

                expandedView.setViewVisibility(R.id.notification_departure_2, View.VISIBLE);
                expandedView.setTextViewText(R.id.notification_departure_2_line, stopDetail.getLine());
                expandedView.setTextViewText(R.id.notification_departure_2_time, stopDetail.getTime());
                expandedView.setTextViewText(R.id.notification_departure_2_last, context.getString(R.string.notification_heading, stopDetail.getLastStation()));

                if (stopDetail.getDelay() > 3) {
                    expandedView.setTextColor(R.id.notification_departure_2_delay, ContextCompat.getColor(context, R.color.primary_red));
                } else if (stopDetail.getDelay() > 0) {
                    expandedView.setTextColor(R.id.notification_departure_2_delay, ContextCompat.getColor(context, R.color.primary_amber_dark));
                }

                if (stopDetail.getDelay() != Config.BUS_STOP_DETAILS_NO_DELAY) {
                    expandedView.setTextViewText(R.id.notification_departure_2_delay, stopDetail.getDelay() + "'");
                }
            }

            notification = mBuilder.build();

            if (!items.isEmpty()) {
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

        message = Utils.sanitizeString(message.toString());

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

    /**
     * Shows a notification if a trip has been successfully saved.
     *
     * @param context  application context
     * @param hash the trip uuid to display
     */
    public static void trip(Context context, String hash) {
        Preconditions.checkNotNull(context, "trip() context == null");
        Preconditions.checkNotNull(hash, "hash == null");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_timeline)
                .setContentTitle(context.getString(R.string.notification_trip_title))
                .setContentText(context.getString(R.string.notification_trip_sub))
                .setAutoCancel(true)
                .setLights(Color.BLUE, 500, 5000)
                .setColor(ContextCompat.getColor(context, R.color.primary_blue_dark))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        Intent resultIntent = new Intent(context, TripDetailActivity.class);
        resultIntent.putExtra(Config.EXTRA_TRIP_HASH, hash);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Config.NOTIFICATION_TRIP_SUCCESS,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_TRIP_SUCCESS, mBuilder.build());
    }

    public static void survey(Context context, String hash) {
        Preconditions.checkNotNull(context, "survey() context == null");
        Preconditions.checkNotNull(hash, "hash == null");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_assessment_white_48dp)
                .setContentTitle(context.getString(R.string.notification_survey_title))
                .setContentText(context.getString(R.string.notification_survey_subtitle))
                .setAutoCancel(true)
                .setLights(Color.GREEN, 500, 5000)
                .setColor(ContextCompat.getColor(context, R.color.primary_teal))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        Intent resultIntent = new Intent(context, SurveyActivity.class);
        resultIntent.putExtra(Config.EXTRA_TRIP_HASH, hash);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Config.NOTIFICATION_SURVEY,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_SURVEY, mBuilder.build());
    }

    public static void badge(Context context, it.sasabz.android.sasabus.beacon.ecopoints.badge.Badge badge) {
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

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Config.NOTIFICATION_BADGE,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_BADGE, mBuilder.build());
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

            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    Config.NOTIFICATION_BADGE,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Config.NOTIFICATION_BADGE, mBuilder.build());
        } catch (InterruptedException | ExecutionException e) {
            Utils.handleException(e);
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
            LogUtils.e(TAG, "Called error notification with production build.");
            Utils.handleException(new Throwable("Called error notification with " +
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
     * Shows a brief information about when a planned trip line will depart at the selected bus stop.
     * As does not have to depart exactly at the time the user selected, telling him when the line
     * will actually depart is important.
     *
     * @param context Context to access the {@link NotificationManager}.
     * @param hash    Planned trip hash.
     * @param line    The selected line.
     * @param title   The title the user chose
     * @param time    the time when the line will depart at the selected bus stop.
     */
    public static void plannedTripDepartureAt(Context context, String hash, int line,
                                              CharSequence title, String time) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.notification_planned_trip_departs_at_content,
                        Lines.lidToName(line), time))
                .setSmallIcon(R.drawable.ic_bus)
                .setAutoCancel(true)
                .setLights(ContextCompat.getColor(context, R.color.primary_teal), 500, 5000)
                .setColor(ContextCompat.getColor(context, R.color.primary_teal))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        Intent resultIntent = new Intent(context, PlannedTripsViewActivity.class);
        resultIntent.putExtra(Config.EXTRA_PLANNED_TRIP_HASH, hash);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Config.NOTIFICATION_PLANNED_TRIP_DEPARTURE_AT * hash.hashCode(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_TRIP_DEPARTURE, mBuilder.build());
    }

    /**
     * Shows a reminder that a planned trip will depart soon.
     *
     * @param context Context to access the {@link NotificationManager}.
     * @param id      Planned trip id.
     * @param line    The selected line.
     * @param delay   The current delay of the bus.
     * @param vehicle Vehicle id to allow the user to view the bus on the map.
     * @param title   The title the user chose
     * @param minutes The minutes when the bus will depart.
     */
    public static void plannedTripDepartureIn(Context context, int id, int line, int delay,
                                              int vehicle, CharSequence title, int minutes) {

        String text;
        if (delay == 0 && vehicle == 0) {
            text = context.getString(R.string.notification_planned_trip_departs_in_content_1,
                    Lines.lidToName(line), minutes);
        } else {
            text = context.getString(R.string.notification_planned_trip_departs_in_content_2,
                    Lines.lidToName(line), minutes, delay);
        }


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_bus)
                .setAutoCancel(true)
                .setLights(ContextCompat.getColor(context, R.color.primary_teal), 500, 5000)
                .setColor(ContextCompat.getColor(context, R.color.primary_teal))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVibrate(new long[]{VIBRATION_TIME_MILLIS, VIBRATION_TIME_MILLIS})
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        Intent resultIntent = new Intent(context, MapActivity.class);
        resultIntent.putExtra(Config.EXTRA_VEHICLE, vehicle);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Config.NOTIFICATION_PLANNED_TRIP_DEPARTURE_IN * id,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_TRIP_DEPARTURE, mBuilder.build());
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
        cancel(context, Config.NOTIFICATION_BUS);
    }
}
