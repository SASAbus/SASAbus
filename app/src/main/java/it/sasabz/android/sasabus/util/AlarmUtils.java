package it.sasabz.android.sasabus.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import it.sasabz.android.sasabus.receiver.NotificationReceiver;

import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public final class AlarmUtils {

    private static final String TAG = "AlarmUtils";

    public static final String EXTRA_PLANNED_TRIP_HASH = "it.sasabz.android.sasabus.action.EXTRA_PLANNED_TRIP_ID";
    public static final String EXTRA_PLANNED_TRIP = "it.sasabz.android.sasabus.action.EXTRA_PLANNED_TRIP";

    private static final int SCHEDULE_TIMER_MILLIS = 6 * 60 * 60 * 1000;

    private AlarmUtils() {
    }

    /**
     * Schedules all planned trips by loading them from the database and scheduling
     * each single trip.
     *
     * @param context Context to access the database.
     */
    public static void scheduleTrips(Context context) {
        Preconditions.checkNotNull(context, "context == null");

        Realm realm = Realm.getDefaultInstance();

        RealmResults<it.sasabz.android.sasabus.realm.user.PlannedTrip> results =
                realm.where(it.sasabz.android.sasabus.realm.user.PlannedTrip.class).findAll();

        for (it.sasabz.android.sasabus.realm.user.PlannedTrip trip : results) {
            if (!trip.getNotifications().isEmpty()) {
                scheduleSingleTrip(context, trip);
            }
        }
    }

    /**
     * Schedules a single trip. Also handles the repeating of the trips.
     *
     * @param context Context to access {@link AlarmManager}.
     * @param trip    The planned trip to schedule.
     */
    private static void scheduleSingleTrip(Context context, it.sasabz.android.sasabus.realm.user.PlannedTrip trip) {
        Preconditions.checkNotNull(context, "context == null");
        Preconditions.checkNotNull(trip, "trip == null");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_CALCULATE_TRIPS);
        intent.putExtra(EXTRA_PLANNED_TRIP_HASH, trip.getHash());

        List<Integer> notifications = Utils.stringToList(trip.getNotifications(), ",");
        notifications.add(0, 0);

        long millis = trip.getTimestamp() * 1000;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        Calendar midnight = Calendar.getInstance();
        midnight.setTimeInMillis(calendar.getTimeInMillis());

        midnight.set(Calendar.HOUR_OF_DAY, 2);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        long millisFromMidnight = calendar.getTimeInMillis() - midnight.getTimeInMillis();

        if (millisFromMidnight > SCHEDULE_TIMER_MILLIS) {
            millisFromMidnight = SCHEDULE_TIMER_MILLIS;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, trip.getHash().hashCode(),
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long repeatInterval = 0;
        if ((trip.getRepeatDays() & it.sasabz.android.sasabus.model.trip.PlannedTrip.FLAG_MONDAY) == it.sasabz.android.sasabus.model.trip.PlannedTrip.FLAG_MONDAY) {
            repeatInterval = AlarmManager.INTERVAL_DAY;
            LogUtils.e(TAG, "Repeat: every day");
        }

        if (trip.getRepeatWeeks() > 0) {
            repeatInterval = AlarmManager.INTERVAL_DAY * 7;
            LogUtils.e(TAG, "Repeat: every week");
        }

        long timerMillis = millis - millisFromMidnight;

        boolean scheduled = false;
        if (repeatInterval > 0) {
            if (System.currentTimeMillis() > timerMillis + SCHEDULE_TIMER_MILLIS) {
                timerMillis += repeatInterval;
            }

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timerMillis,
                    repeatInterval, pendingIntent);

            scheduled = true;
        } else {
            if (System.currentTimeMillis() < timerMillis + SCHEDULE_TIMER_MILLIS) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timerMillis, pendingIntent);
                scheduled = true;
            }
        }

        if (scheduled) {
            LogUtils.e(TAG, "Scheduled trip calculation at " + timerMillis + " for trip \"" +
                    trip.getTitle() + '"');
        } else {
            LogUtils.e(TAG, "Scheduling trip calculation at " + timerMillis + " for trip \"" +
                    trip.getTitle() + "\" failed.");
        }
    }

    /**
     * Schedules a single trip. Also handles the repeating of the trips.
     *
     * @param context Context to access {@link AlarmManager}.
     * @param trip    The planned trip to schedule.
     */
    public static void cancelTrip(Context context, it.sasabz.android.sasabus.model.trip.PlannedTrip trip) {
        Preconditions.checkNotNull(context, "context == null");
        Preconditions.checkNotNull(trip, "trip == null");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel the notification to calculate trip departure
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_CALCULATE_TRIPS);
        intent.putExtra(EXTRA_PLANNED_TRIP_HASH, trip.getHash());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                trip.getHash().hashCode(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.cancel(pendingIntent);

        // Cancel all the regular notifications which inform the user about departure.
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.setAction(NotificationReceiver.ACTION_SHOW_TRIPS);
        notificationIntent.putExtra(EXTRA_PLANNED_TRIP, trip);

        List<Integer> notifications = trip.getNotifications();

        for (Integer notification : notifications) {
            pendingIntent = PendingIntent.getBroadcast(context,
                    trip.getHash().hashCode() * notification,
                    notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            alarmManager.cancel(pendingIntent);
        }

        LogUtils.e(TAG, "Cancelled planned trip " + trip.getHash());
    }
}
