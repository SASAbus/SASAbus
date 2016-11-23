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

package it.sasabz.android.sasabus.beacon.event;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.beacon.IBeaconHandler;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.EventsApi;
import it.sasabz.android.sasabus.data.network.rest.response.EventBeaconResponse;
import it.sasabz.android.sasabus.ui.ecopoints.event.EventDetailsActivity;
import it.sasabz.android.sasabus.util.Notifications;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public final class EventBeaconHandler implements IBeaconHandler {

    public static final String UUID = "abaf1d52-cafd-42b1-b02c-9060b3027e51";

    public static final String IDENTIFIER = "EVENT";

    private final Context mContext;

    private final Map<String, EventBeacon> mBeaconMap = new ConcurrentHashMap<>();

    private static final int TIMER_INTERVAL = 5000;
    private static final int BEACON_REMOVAL_TIME = (int) TimeUnit.SECONDS.toMillis(60);

    @SuppressLint("StaticFieldLeak")
    private static EventBeaconHandler sInstance;

    private final EventsApi EVENTS_API;

    /**
     * Timer which runs in an interval of {@link #TIMER_INTERVAL} millis and checks if beacons
     * are out of bounds and need to be removed.
     */
    private Timer TIMER;

    /**
     * The handler which runs all the post delayed operations like removing a trip from the
     * map when it goes out of range.
     */
    private final Handler HANDLER = new Handler();
    private final Runnable STOP_TIMER = new StopRunnable();

    private EventBeaconHandler(Context context) {
        mContext = context.getApplicationContext();

        EVENTS_API = RestClient.ADAPTER.create(EventsApi.class);
    }

    public static EventBeaconHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new EventBeaconHandler(context);
        }

        return sInstance;
    }

    @Override
    public void didRangeBeacons(Collection<Beacon> beacons) {
        for (Beacon beacon : beacons) {
            int major = beacon.getId2().toInt();
            int minor = beacon.getId3().toInt();

            validateBeacon(beacon, major, minor);
        }
    }

    @Override
    public void validateBeacon(Beacon beacon, int major, int minor) {
        String key = major + ":" + minor;

        if (mBeaconMap.containsKey(key)) {
            EventBeacon beaconInfo = mBeaconMap.get(key);

            beaconInfo.seen();
            beaconInfo.setDistance(beacon.getDistance());

            Timber.e("Beacon " + major + ":" + minor + ", seen: " +
                    beaconInfo.seenSeconds + ", distance: " + beaconInfo.distance);
        } else {
            EventBeacon eventBeacon = new EventBeacon(major, minor);
            mBeaconMap.put(key, eventBeacon);

            Timber.e("Added beacon " + major);

            sendBeacon(eventBeacon);
        }
    }

    @Override
    public void stop() {
        Timber.e("Stopping bus stop beacon handler");

        HANDLER.postDelayed(STOP_TIMER, BEACON_REMOVAL_TIME + TIMER_INTERVAL);
    }

    public void start() {
        Timber.e("Starting timer");

        mBeaconMap.clear();

        HANDLER.removeCallbacks(STOP_TIMER);

        if (TIMER != null) {
            TIMER.cancel();
            TIMER.purge();
        }

        TIMER = new Timer();
        TIMER.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Timber.e("Running timer");

                for (Map.Entry<String, EventBeacon> entry : mBeaconMap.entrySet()) {
                    EventBeacon beacon = entry.getValue();

                    if (beacon.lastSeen < System.currentTimeMillis() - BEACON_REMOVAL_TIME) {
                        Timber.e("Removed beacon " + beacon.major + ":" + beacon.minor);

                        mBeaconMap.remove(beacon.major + ":" + beacon.minor);
                    }
                }
            }
        }, BEACON_REMOVAL_TIME, TIMER_INTERVAL);
    }

    private void sendBeacon(EventBeacon beacon) {
        EVENTS_API.putBeacon(beacon.major, beacon.minor)
                .subscribeOn(Schedulers.io())
                .doOnError(Utils::logException)
                .retry(3)
                .subscribe(new Observer<EventBeaconResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);
                    }

                    @Override
                    public void onNext(EventBeaconResponse response) {
                        Timber.e("Uploaded beacon: " + beacon.major + ":" + beacon.minor);

                        if (response.event == null) {
                            Timber.e("event == null, most probably beacon " +
                                    beacon.major + ":" + beacon.minor + " was already sent");
                            return;
                        }

                        int color = Color.parseColor('#' + response.color);

                        Notifications.eventBeacon(mContext, response.event, response.point, color);

                        Timber.e("Sending point broadcast");

                        Intent pointIntent = new Intent(EventDetailsActivity.BROADCAST_BEACON_SEEN);
                        pointIntent.putExtra(EventDetailsActivity.EXTRA_BEACON_POINT, response.point);
                        pointIntent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, response.eventId);

                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(pointIntent);

                        if (!TextUtils.isEmpty(response.qrCode)) {
                            Timber.e("Got QR code, sending broadcast");

                            Intent qrIntent = new Intent(EventDetailsActivity.BROADCAST_EVENT_COMPLETED);
                            qrIntent.putExtra(EventDetailsActivity.EXTRA_QR_CODE, response.qrCode);
                            qrIntent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, response.eventId);

                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(qrIntent);
                        }
                    }
                });
    }

    private class StopRunnable implements Runnable {
        @Override
        public void run() {
            Timber.e("Stopped timer");

            if (TIMER != null) {
                TIMER.cancel();
                TIMER.purge();
            }

            mBeaconMap.clear();

            sInstance = null;
        }
    }
}
