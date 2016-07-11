package it.sasabz.android.sasabus.receiver;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.RealtimeApi;
import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.provider.API;
import it.sasabz.android.sasabus.provider.ApiUtils;
import it.sasabz.android.sasabus.provider.model.PlannedDeparture;
import it.sasabz.android.sasabus.util.AlarmUtils;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.schedulers.Schedulers;

/**
 * Called when a planned trip calculation was scheduled. This receiver will calculate the next
 * departure after the user specified time at the specified bus stop, and then schedule all the
 * notifications.
 *
 * Called when a notification for a planned trip needs to be displayed. If the bus is currently
 * in service (which it most probably is 10 min before the bus is at the selected bus stop),
 * the notification will include the bus delay and launch the map if clicked.
 *
 * @author Alex Lardschneider
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    public static final String ACTION_CALCULATE_TRIPS = "it.sasabz.android.sasabus.CALCULATE_TRIPS";
    public static final String ACTION_SHOW_TRIPS = "it.sasabz.android.sasabus.SHOW_TRIPS";
    public static final String ACTION_HIDE_NOTIFICATION = "it.sasabz.android.sasabus.HIDE_NOTIFICATION";

    public static final String EXTRA_NOTIFICATION_ID = "it.sasabz.android.sasabus.EXTRA_NOTIFICATION_ID";

    private static final int NOTIFICATION_MILLIS = 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e(TAG, "onReceive() " + intent.getAction());

        // Action to tell us we should calculate the time when the trip arrives
        // at a bus stop. This is usually done 6h before the trip departs, except when
        // the 6h < now - midnight, as we have to calculate the trip on the day the trip is
        // going to happen.
        if (ACTION_CALCULATE_TRIPS.equals(intent.getAction())) {
            calculateTrip(context, intent);
        } else if (ACTION_SHOW_TRIPS.equals(intent.getAction())) {
            showTripNotification(context, intent);
        } else if (ACTION_HIDE_NOTIFICATION.equals(intent.getAction())) {
            hideNotification(context, intent);
        }
    }

    /**
     * Action to tell us we should calculate the time when the trip arrives
     * at a bus stop. This is usually done 6h before the trip departs, except when
     * the 6h < now - midnight, as we have to calculate the trip on the day the trip is
     * going to happen.
     *
     * @param context
     * @param intent
     */
    private void calculateTrip(Context context, Intent intent) {
        String hash = intent.getStringExtra(AlarmUtils.EXTRA_PLANNED_TRIP_HASH);

        // Ignore trips with hash null, most probably the extra intent is missing.
        if (hash == null) {
            return;
        }

        Realm realm = Realm.getDefaultInstance();

        it.sasabz.android.sasabus.realm.user.PlannedTrip plannedTrip =
                realm.where(it.sasabz.android.sasabus.realm.user.PlannedTrip.class)
                        .equalTo("hash", hash).findFirst();

        LogUtils.e(TAG, "Trip: " + plannedTrip);

        if (plannedTrip != null) {
            it.sasabz.android.sasabus.model.trip.PlannedTrip trip = new it.sasabz.android.sasabus.model.trip.PlannedTrip(plannedTrip);

            Calendar tripCalendar = Calendar.getInstance();
            tripCalendar.setTimeInMillis(trip.getTimestamp() * 1000);

            // Set the hours and minutes when the trip is planned, not when it
            // will actually start.
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, tripCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, tripCalendar.get(Calendar.MINUTE));

            // Calendar which is set to midnight of the current day.
            Calendar midnight = Calendar.getInstance();
            midnight.set(Calendar.HOUR_OF_DAY, 0);
            midnight.set(Calendar.MINUTE, 0);
            midnight.set(Calendar.SECOND, 0);
            midnight.set(Calendar.MILLISECOND, 0);

            // Seconds from midnight when the trip is planned. We need to have the midnight
            // seconds as the api is only able to handle those.
            long secondsFromMidnight = (calendar.getTimeInMillis() - midnight.getTimeInMillis()) / 1000;

            // The trip when the line will actually arrive at the selected bus stop, which does not
            // have to be identical to the one the user selected. It will take the next trip
            // which arrives at the selected bus stop after the selected time.
            PlannedDeparture plannedDeparture = API.getNextTrip(context, trip.getLines(),
                    trip.getBusStop(), (int) secondsFromMidnight);

            LogUtils.e(TAG, "time: " + calendar.getTimeInMillis() +
                    ", planned trip: " + plannedDeparture);

            if (plannedDeparture != null) {
                trip.setLineId(plannedDeparture.getLine());
                trip.setTime(plannedDeparture.getTime());
                trip.setTripId(plannedDeparture.getTrip());

                // Show a notification when the bus will arrive at the bus stop and
                // which line it will be.
                long epochInMillis = trip.getTimestamp() * 1000;
                Calendar now = Calendar.getInstance();
                Calendar timeToCheck = Calendar.getInstance();
                timeToCheck.setTimeInMillis(epochInMillis);

                // As the alarms have to be rescheduled on each reboot, we only want to show the
                // "trip departs in" notification the first time.
                if (now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)) {

                    NotificationUtils.plannedTripDepartureAt(context, trip.getHash(), plannedDeparture.getLine(),
                            trip.getTitle(), ApiUtils.getTime(plannedDeparture.getTime()));
                }

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent notificationIntent = new Intent(context, NotificationReceiver.class);
                notificationIntent.setAction(ACTION_SHOW_TRIPS);
                notificationIntent.putExtra(AlarmUtils.EXTRA_PLANNED_TRIP, trip);

                List<Integer> notifications = trip.getNotifications();

                // Schedule all the selected notification times.
                for (Integer notification : notifications) {
                    // The millis after midnight when the notification should be shown.
                    long millis = midnight.getTimeInMillis() + plannedDeparture.getTime() * 1000L;

                    // The unix timestamp when the notification should be shown.
                    long notificationMillis = millis - notification * 60L * 1000;

                    // Ignore notifications which should already have been displayed.
                    if (System.currentTimeMillis() < notificationMillis - NOTIFICATION_MILLIS) {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                trip.getHash().hashCode() * notification,
                                notificationIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT);

                        // As of Kitkat (19), using set does not guarantee that the notifications
                        // will be delivered on time, so we have to use {@code setExact} for that.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationMillis, pendingIntent);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationMillis, pendingIntent);
                        }

                        LogUtils.e(TAG, "Scheduled alarm at " + notificationMillis + " for trip \"" +
                                trip.getTitle() + '"');
                    } else {
                        LogUtils.e(TAG, "Ignoring alarm at " + notificationMillis +
                                " as it was out of bounds");
                    }
                }
            }
        }

        realm.close();
    }

    private void showTripNotification(Context context, Intent intent) {
        it.sasabz.android.sasabus.model.trip.PlannedTrip trip = intent.getParcelableExtra(AlarmUtils.EXTRA_PLANNED_TRIP);

        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        // Unix time when the trip will depart.
        long tripTime = midnight.getTimeInMillis() + trip.getTime() * 1000L;

        Calendar now = Calendar.getInstance();

        // Minutes in which the bus departs.
        int minutes = (int) (tripTime - now.getTimeInMillis()) / 1000 / 60;

        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.trip(trip.getTripId())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        NotificationUtils.plannedTripDepartureIn(context, trip.getHash().hashCode(),
                                trip.getLineId(), 0, 0, trip.getTitle(), minutes + 1);
                    }

                    @Override
                    public void onNext(RealtimeResponse realtimeResponse) {
                        int vehicle = 0;
                        int delay = 0;

                        if (!realtimeResponse.buses.isEmpty()) {
                            RealtimeBus bus = realtimeResponse.buses.get(0);

                            vehicle = bus.vehicle;
                            delay = bus.delayMin;
                        }

                        NotificationUtils.plannedTripDepartureIn(context, trip.getHash().hashCode(),
                                trip.getLineId(), delay, vehicle, trip.getTitle(), minutes + 1);
                    }
                });
    }

    private void hideNotification(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);

        if (notificationId == 0) {
            LogUtils.e(TAG, "Notification id == 0");
            return;
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(notificationId);

        LogUtils.e(TAG, "Cancelled notification with id " + notificationId);
    }
}