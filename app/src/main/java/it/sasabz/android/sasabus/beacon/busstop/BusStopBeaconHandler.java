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

package it.sasabz.android.sasabus.beacon.busstop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.v4.util.Pair;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import it.sasabz.android.sasabus.beacon.BeaconStorage;
import it.sasabz.android.sasabus.beacon.IBeaconHandler;
import it.sasabz.android.sasabus.beacon.bus.BusBeacon;
import it.sasabz.android.sasabus.beacon.bus.BusBeaconHandler;
import it.sasabz.android.sasabus.beacon.ecopoints.BadgeHelper;
import it.sasabz.android.sasabus.data.model.BusStop;
import it.sasabz.android.sasabus.data.model.Departure;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.RealtimeApi;
import it.sasabz.android.sasabus.data.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.data.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.data.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.data.realm.UserRealmHelper;
import it.sasabz.android.sasabus.data.vdv.DepartureMonitor;
import it.sasabz.android.sasabus.data.vdv.model.VdvDeparture;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Notifications;
import it.sasabz.android.sasabus.util.Settings;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.schedulers.Schedulers;

/**
 * Class which handles bus stop beacons.
 * Any bus stop beacon in range will be added to {@link #mBeaconMap}.
 * <p>
 * Shows a notification when a bus beacon is in range for more than {@link #BEACON_NOTIFICATION_TIME_DELTA}
 * and the distance is smaller than {@link #BEACON_NOTIFICATION_DISTANCE}.
 *
 * @author Alex Lardschneider
 */
public final class BusStopBeaconHandler implements IBeaconHandler {

    private static final String TAG = "BusStopBeaconHandler";

    /**
     * The uuid which identifies a bus stop beacon.
     */
    public static final String UUID = "8f771fca-e25a-4a7f-af4e-1745a7be89ef";

    /**
     * the identifier used to identify the region the beacon scanner is listening in.
     */
    public static final String IDENTIFIER = "BUS_STOP";

    private static final int BEACON_NOTIFICATION_TIME_DELTA = 20;
    private static final int BEACON_NOTIFICATION_DISTANCE = 5;
    private static final int BEACON_REMOVAL_TIME = 20000;

    private static final int TIMER_INTERVAL = 5000;

    /**
     * ClusterMarker which saves all the beacons together with the beacon major as key. This map needs
     * to be concurrent because two thread might be accessing it at the same time,
     * like when downloading the trip data and a beacon cycle is done at the same time.
     */
    private final Map<Integer, BusStopBeacon> mBeaconMap = new ConcurrentHashMap<>();

    /**
     * <strong>Application Context</strong> to access various things.
     */
    @SuppressWarnings("all")
    private Context mContext = null;

    /**
     * The nearest bus stop.
     */
    private Pair<Integer, BusStop> currentBusStop;

    /**
     * Singleton instance for this class.
     */
    @SuppressLint("StaticFieldLeak")
    private static BusStopBeaconHandler sInstance;

    /**
     * The handler which runs all the post delayed operations like removing a trip from the
     * map when it goes out of range.
     */
    private static final Handler HANDLER = new Handler();

    /**
     * Timer which runs in an interval of {@link #TIMER_INTERVAL} millis and checks if beacons
     * are out of bounds and need to be removed.
     */
    private Timer TIMER;

    /**
     * Runnable which stops the timer {@link #BEACON_REMOVAL_TIME} + {@link #TIMER_INTERVAL} seenSeconds
     * after the user went out of beacon scanning region.
     */
    private final Runnable STOP_TIMER = () -> {
        LogUtils.e(TAG, "Stopped timer");

        if (TIMER != null) {
            TIMER.cancel();
            TIMER.purge();
        }

        for (int i = 0; i < 6000; i++) {
            Notifications.cancel(mContext, i);
        }

        mBeaconMap.clear();
    };

    /**
     * Creates a new sInstance of {@code BusStopBeaconHandler} used to listen for station beaconMap
     *
     * @param context AppApplication context
     */
    private BusStopBeaconHandler(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Returns the current {@code BusStopBeaconHandler} sInstance in use, if it doesn't exist, create one
     *
     * @param context AppApplication context
     * @return current sInstance in use
     */
    public static BusStopBeaconHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BusStopBeaconHandler(context);
        }

        return sInstance;
    }

    /**
     * Update station beaconMap in range
     *
     * @param beacons beaconMap to update
     */
    @Override
    public void didRangeBeacons(Collection<Beacon> beacons) {

        boolean found = false;
        for (Beacon beacon : beacons) {
            int major = beacon.getId2().toInt();
            int minor = beacon.getId3().toInt();

            if (major == 1 && minor != 1) {
                major = beacon.getId3().toInt();
                minor = beacon.getId2().toInt();
            }

            validateBeacon(beacon, major, minor);

            if (currentBusStop != null && currentBusStop.second.getId() == major) {
                found = true;
            }
        }

        if (currentBusStop != null && !found) {
            LogUtils.i(TAG, "Removed current bus stop " +
                    currentBusStop.second.getId());

            BusBeaconHandler.getInstance(mContext)
                    .currentBusStopOutOfRange(currentBusStop);
            currentBusStop = null;
        }

        List<Beacon> list = new ArrayList<>(beacons);
        Collections.sort(list, (lhs, rhs) -> (int) (lhs.getDistance() - rhs.getDistance()));

        if (!beacons.isEmpty()) {
            Beacon beacon = list.get(0);

            int major = beacon.getId2().toInt();
            if (major == 1 && beacon.getId3().toInt() != 1) {
                major = beacon.getId3().toInt();
            }

            setCurrentBusStop(major);
            list.clear();
        }

        showNotificationIfNeeded();
    }

    /**
     * Gets called when a beacon was found. Adds it to {@code beaconMap} when it does not exist yes
     * and checks if the beacon is valid to getPublicKey a location from.
     *
     * @param beacon the beacon to check
     * @param major  station id
     */
    @Override
    public void validateBeacon(Beacon beacon, int major, int minor) {
        if (mBeaconMap.containsKey(major)) {
            BusStopBeacon beaconInfo = mBeaconMap.get(major);

            beaconInfo.seen();
            beaconInfo.setDistance(beacon.getDistance());

            if (beaconInfo.distance > BEACON_NOTIFICATION_DISTANCE) {
                Notifications.cancel(mContext, major);
            }

            LogUtils.i(TAG, "Beacon " + major + ", seen: " + beaconInfo.seenSeconds +
                    ", distance: " + beaconInfo.distance);
        } else {
            mBeaconMap.put(major, new BusStopBeacon(major));

            LogUtils.e(TAG, "Added beacon " + major);

            UserRealmHelper.addBeacon(beacon,
                    it.sasabz.android.sasabus.data.realm.user.Beacon.TYPE_BUS_STOP);
        }
    }

    /**
     * Sets the current bus stop. This bus stop is used as a intermediate bus stop for the
     * bus notification to indicate where the bus currently is at.
     *
     * @param major the major of the bus stop.
     */
    private void setCurrentBusStop(int major) {
        if (currentBusStop != null && currentBusStop.second.getId() == major) {
            return;
        }

        it.sasabz.android.sasabus.data.realm.busstop.BusStop busStop =
                BusStopRealmHelper.getBusStopOrNull(major);

        if (busStop != null) {
            LogUtils.i(TAG, "Set " + major + " as current bus stop");
            currentBusStop = new Pair<>(BusBeacon.TYPE_BEACON, new BusStop(busStop));
        }
    }

    /**
     * Stops the timer task when no more beacons are in range to reduce power consumption.
     * The timer is stopped {@code #BEACON_REMOVAL_TIME * 2) millis after the
     * {@link it.sasabz.android.sasabus.beacon.BeaconHandler } reported that no more bus beacons
     * are in range.
     */
    @Override
    public void stop() {
        LogUtils.e(TAG, "Stopping bus stop beacon handler");

        HANDLER.postDelayed(STOP_TIMER, BEACON_REMOVAL_TIME + TIMER_INTERVAL);
    }

    /**
     * Starts the timer to search for beacons which are no longer in range and checks if
     * they are suitable for a trip and removed them from the {@link #mBeaconMap}.
     */
    public void start() {
        LogUtils.e(TAG, "Starting timer");

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
                LogUtils.i(TAG, "Running timer");

                for (Map.Entry<Integer, BusStopBeacon> entry : mBeaconMap.entrySet()) {
                    BusStopBeacon beacon = entry.getValue();

                    if (beacon.lastSeen < System.currentTimeMillis() - BEACON_REMOVAL_TIME) {
                        Notifications.cancel(mContext, beacon.id);

                        if (currentBusStop != null && beacon.id == currentBusStop.second.getId()) {
                            BusBeaconHandler.getInstance(mContext)
                                    .currentBusStopOutOfRange(currentBusStop);
                            currentBusStop = null;
                        }

                        LogUtils.e(TAG, "Removed beacon " + beacon.id);

                        mBeaconMap.remove(beacon.id);
                    }
                }
            }
        }, BEACON_REMOVAL_TIME, TIMER_INTERVAL);
    }

    /**
     * Check if a notification can be shown
     */
    private void showNotificationIfNeeded() {
        if (mBeaconMap.isEmpty()) {
            return;
        }

        List<BusStopBeacon> beacons = new ArrayList<>(mBeaconMap.values());
        Collections.sort(beacons, (lhs, rhs) -> (int) (lhs.distance - rhs.distance));

        for (BusStopBeacon beacon : beacons) {
            it.sasabz.android.sasabus.data.realm.busstop.BusStop busStop = BusStopRealmHelper
                    .getBusStopOrNull(beacon.id);

            if (busStop != null) {
                if (!canShowNotification(beacon)) {
                    return;
                }

                BadgeHelper.evaluate(mContext, beacon);

                LogUtils.e(TAG, "Notification station beacon " + beacon.id);

                Collection<VdvDeparture> vdvDepartures = new DepartureMonitor()
                        .atBusStop(beacon.id)
                        .includePastDepartures(180)
                        .maxElements(2)
                        .collect();

                List<Departure> departures = new ArrayList<>();

                for (VdvDeparture vdvDeparture : vdvDepartures) {
                    Departure departure = vdvDeparture.asDeparture(beacon.id);
                    departure.delay = Departure.NO_DELAY;
                    departures.add(departure);
                }

                RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
                realtimeApi.delaysRx()
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(new Observer<RealtimeResponse>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Utils.logException(e);

                                Notifications.busStop(mContext, beacon.id, departures);
                                beacon.setNotificationShown();
                            }

                            @Override
                            public void onNext(RealtimeResponse realtimeResponse) {
                                List<RealtimeBus> list = realtimeResponse.buses;

                                for (RealtimeBus bus : list) {
                                    for (Departure item : departures) {
                                        if (item.trip == bus.trip) {
                                            item.delay = bus.delayMin;
                                            item.vehicle = bus.vehicle;

                                            break;
                                        }
                                    }
                                }

                                Notifications.busStop(mContext, beacon.id, departures);
                                beacon.setNotificationShown();
                            }
                        });

                break;
            }
        }
    }

    /**
     * Determines if a bus stop notification can be shown.
     *
     * @param beacon the beacon to check.
     * @return {@code true} if a notification for the given beacon can be shown,
     * {@code false} otherwise.
     */
    private boolean canShowNotification(BusStopBeacon beacon) {
        return Settings.isBusStopNotificationEnabled(mContext) &&
                !beacon.isNotificationShown &&
                !BeaconStorage.getInstance(mContext).hasCurrentTrip() &&
                beacon.seenSeconds >= BEACON_NOTIFICATION_TIME_DELTA;
    }

    /**
     * Returns the current bus stop, which is used on the bus notification to indicate where
     * the bus currently is at.
     *
     * @return the current bus stop, in form of a {@link BusStop}.
     */
    public Pair<Integer, BusStop> getCurrentBusStop() {
        return currentBusStop;
    }
}