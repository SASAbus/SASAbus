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

package it.sasabz.android.sasabus.beacon.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.beacon.bus.CurrentTrip;
import it.sasabz.android.sasabus.model.BusStop;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.provider.ApiUtils;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.ui.MapActivity;
import it.sasabz.android.sasabus.util.UIUtils;

public class TripNotification {

    private static final int[] BIG_VIEW_ROW_IDS = {
            R.id.notification_busstop_row0,
            R.id.notification_busstop_row1,
            R.id.notification_busstop_row2,
            R.id.notification_busstop_row3,
            R.id.notification_busstop_row4,
            R.id.notification_busstop_row5,
            R.id.notification_busstop_row6,
            R.id.notification_busstop_row7
    };

    private static final int[] BIG_VIEW_ROUTE_IMAGE_IDS = {
            R.id.image_route0,
            R.id.image_route1,
            R.id.image_route2,
            R.id.image_route3,
            R.id.image_route4,
            R.id.image_route5,
            R.id.image_route6,
            R.id.image_route7
    };

    private static final int[] BIG_VIEW_TIME_TEXT_IDS = {
            R.id.txt_time0,
            R.id.txt_time1,
            R.id.txt_time2,
            R.id.txt_time3,
            R.id.txt_time4,
            R.id.txt_time5,
            R.id.txt_time6,
            R.id.txt_time7
    };

    private static final int[] BIG_VIEW_BUS_STOP_TEXT_IDS = {
            R.id.txt_bus_stop_name_0,
            R.id.txt_bus_stop_name_1,
            R.id.txt_bus_stop_name_2,
            R.id.txt_bus_stop_name_3,
            R.id.txt_bus_stop_name_4,
            R.id.txt_bus_stop_name_5,
            R.id.txt_bus_stop_name_6,
            R.id.txt_bus_stop_name_7
    };

    public static void showNotification(Context context, CurrentTrip trip) {
        Intent intent = new Intent(context, MapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContent(getBaseNotificationView(context, trip))
                .setSmallIcon(R.drawable.ic_bus)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent);

        Intent resultIntent = new Intent(context, MapActivity.class);
        resultIntent.putExtra(Config.EXTRA_VEHICLE, trip.getId());

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                trip.getId(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = getBigNotificationView(context, trip);
        }

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(Config.NOTIFICATION_BUS, notification);
    }

    private static void setCommonNotification(Context context, RemoteViews remoteViews, CurrentTrip trip) {
        remoteViews.setTextViewText(R.id.notification_bus_line,
                Lines.lidToName(trip.beacon.lineId));

        remoteViews.setImageViewBitmap(R.id.notification_bus_image, getNotificationIcon(context,
                Color.parseColor('#' + Lines.getColorForId(trip.beacon.lineId))));

        int delay = trip.getDelay();
        String delayString;

        if (delay > 0) {
            delayString = context.getString(R.string.bottom_sheet_delayed, delay);
        } else if (delay < 0) {
            delayString = context.getString(R.string.bottom_sheet_early, delay * -1);
        } else {
            delayString = context.getString(R.string.bottom_sheet_punctual);
        }

        remoteViews.setTextViewText(R.id.notification_bus_delay, delayString);
        remoteViews.setTextColor(R.id.notification_bus_delay,
                UIUtils.getColorForDelay(context, delay));
    }

    private static RemoteViews getBaseNotificationView(Context context, CurrentTrip trip) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.notification_current_trip_base);

        setCommonNotification(context, remoteViews, trip);

        List<BusStop> path = trip.getPath();
        List<it.sasabz.android.sasabus.provider.model.BusStop> times = trip.getTimes();

        BusStop currentBusStop = trip.beacon.busStop;

        int index = -1;
        for (int i = 0, pathSize = path.size(); i < pathSize; i++) {
            BusStop busStop = path.get(i);
            if (busStop.getGroup() == currentBusStop.getGroup()) {
                index = i;
                break;
            }
        }

        remoteViews.setTextViewText(R.id.notification_bus_stop_time,
                ApiUtils.getTime(times.get(index).getSeconds()));

        String busStationName = BusStopRealmHelper.getName(
                trip.beacon.busStop.getId());

        remoteViews.setTextViewText(R.id.notification_bus_stop_name, busStationName);
        remoteViews.setTextViewText(R.id.notification_bus_stop_name, busStationName);

        if (index == path.size() - 1) {
            remoteViews.setImageViewResource(R.id.notification_bus_stop_image, R.drawable.an_punkt);
        } else if (index == 0) {
            remoteViews.setImageViewResource(R.id.notification_bus_stop_image, R.drawable.dot_orange_departure);
        } else {
            remoteViews.setImageViewResource(R.id.notification_bus_stop_image, R.drawable.dot_orange_middle);
        }

        return remoteViews;
    }

    private static RemoteViews getBigNotificationView(Context context, CurrentTrip trip) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.notification_current_trip_big);

        setCommonNotification(context, remoteViews, trip);

        List<BusStop> path = trip.getPath();
        List<it.sasabz.android.sasabus.provider.model.BusStop> times = trip.getTimes();

        BusStop currentBusStop = trip.beacon.busStop;

        int index = -1;
        for (int i = 0, pathSize = path.size(); i < pathSize; i++) {
            BusStop busStop = path.get(i);
            if (busStop.getGroup() == currentBusStop.getGroup()) {
                index = i;
                break;
            }
        }

        remoteViews.setViewVisibility(R.id.image_route_points, View.VISIBLE);

        // If the bus is not at the start the notification will display the last bus stop
        // the bus passed by in the first row. The current bus stop will be displayed on the
        // second row instead.
        if (index == 0) {
            BusStop busStop = path.get(0);

            remoteViews.setImageViewResource(BIG_VIEW_ROUTE_IMAGE_IDS[0], R.drawable.dot_orange_departure);
            remoteViews.setTextViewText(BIG_VIEW_BUS_STOP_TEXT_IDS[0], busStop.getName(context));
            remoteViews.setTextViewText(BIG_VIEW_TIME_TEXT_IDS[0], times.get(0).getTime());

            remoteViews.setTextColor(BIG_VIEW_BUS_STOP_TEXT_IDS[0], Color.BLACK);
            remoteViews.setTextColor(BIG_VIEW_TIME_TEXT_IDS[0], Color.BLACK);
        } else if (index > 0) {
            BusStop busStop = path.get(index - 1);

            remoteViews.setImageViewResource(BIG_VIEW_ROUTE_IMAGE_IDS[0], R.drawable.dot_orange_departure);
            remoteViews.setTextViewText(BIG_VIEW_BUS_STOP_TEXT_IDS[0], busStop.getName(context));
            remoteViews.setTextViewText(BIG_VIEW_TIME_TEXT_IDS[0], times.get(index - 1).getTime());

            remoteViews.setTextColor(BIG_VIEW_BUS_STOP_TEXT_IDS[0], Color.GRAY);
            remoteViews.setTextColor(BIG_VIEW_TIME_TEXT_IDS[0], Color.GRAY);
        }

        remoteViews.setViewVisibility(BIG_VIEW_ROW_IDS[0], View.VISIBLE);

        // If the bus is at the first bus stop, there is no previous bus stop to display
        // in grey color. We need to increase the index so we don't display the departure bus
        // stop twice, one time as current bus stop and one time as previous one.
        // The boolean is there to indicate that we are at the first bus stop, which is needed
        // to hide the bus dot image, as the bus isn't between any two bus stops.
        boolean isAtDepartureBusStop = index == 0;
        if (isAtDepartureBusStop) {
            index++;
        }

        int length = BIG_VIEW_ROW_IDS.length;
        for (int i = 1; i < length; i++) {
            if (index + i <= path.size()) {
                remoteViews.setViewVisibility(BIG_VIEW_ROW_IDS[i], View.VISIBLE);

                int tempIndex;

                // Last bus stop
                if (i == 7 || index + i > path.size() - 1) {
                    tempIndex = path.size() - 1;
                    BusStop busStop = path.get(tempIndex);

                    remoteViews.setImageViewResource(BIG_VIEW_ROUTE_IMAGE_IDS[i], R.drawable.an_punkt);
                    remoteViews.setTextViewText(BIG_VIEW_BUS_STOP_TEXT_IDS[i], busStop.getName(context));
                    remoteViews.setTextViewText(BIG_VIEW_TIME_TEXT_IDS[i], times.get(tempIndex).getTime());

                    remoteViews.setTextColor(BIG_VIEW_BUS_STOP_TEXT_IDS[i], Color.BLACK);
                    remoteViews.setTextColor(BIG_VIEW_TIME_TEXT_IDS[i], Color.BLACK);

                    continue;
                }

                if (i == 1) {
                    tempIndex = index;
                    BusStop busStop = path.get(tempIndex);

                    // If the bus is at the departure bus stop, replace the orange dotted bus image
                    // with a normal orange dotted image.
                    if (isAtDepartureBusStop) {
                        remoteViews.setImageViewResource(BIG_VIEW_ROUTE_IMAGE_IDS[1], R.drawable.dot_orange_middle);
                    } else {
                        remoteViews.setImageViewResource(BIG_VIEW_ROUTE_IMAGE_IDS[1], R.drawable.middle_bus);
                    }

                    remoteViews.setTextViewText(BIG_VIEW_BUS_STOP_TEXT_IDS[1], busStop.getName(context));
                    remoteViews.setTextViewText(BIG_VIEW_TIME_TEXT_IDS[1], times.get(tempIndex).getTime());

                    remoteViews.setTextColor(BIG_VIEW_BUS_STOP_TEXT_IDS[1], Color.BLACK);
                    remoteViews.setTextColor(BIG_VIEW_TIME_TEXT_IDS[1], Color.BLACK);

                    continue;
                }

                tempIndex = index == 0 ? index + i : index + i - 1;
                BusStop busStop = path.get(tempIndex);

                remoteViews.setImageViewResource(BIG_VIEW_ROUTE_IMAGE_IDS[i], R.drawable.dot_orange_middle);
                remoteViews.setTextViewText(BIG_VIEW_BUS_STOP_TEXT_IDS[i], busStop.getName(context));
                remoteViews.setTextViewText(BIG_VIEW_TIME_TEXT_IDS[i], times.get(tempIndex).getTime());

                remoteViews.setTextColor(BIG_VIEW_BUS_STOP_TEXT_IDS[i], Color.BLACK);
                remoteViews.setTextColor(BIG_VIEW_TIME_TEXT_IDS[i], Color.BLACK);
            } else {
                remoteViews.setViewVisibility(R.id.image_route_points, View.GONE);
                remoteViews.setViewVisibility(BIG_VIEW_ROW_IDS[i], View.GONE);
            }
        }

        return remoteViews;
    }

    private static Bitmap getNotificationIcon(Context context, int color) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        GradientDrawable circularImage = (GradientDrawable) ContextCompat.getDrawable(context,
                R.drawable.circle_image);

        circularImage.setStroke(Math.round(4 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)),
                color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            circularImage.setColor(color);
        }

        int size = Math.round(64 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        Bitmap circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);
        circularImage.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        circularImage.draw(canvas);

        return circularBitmap;
    }
}
