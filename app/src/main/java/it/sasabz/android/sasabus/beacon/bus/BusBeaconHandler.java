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

package it.sasabz.android.sasabus.beacon.bus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.altbeacon.beacon.Beacon;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.beacon.BeaconStorage;
import it.sasabz.android.sasabus.beacon.IBeaconHandler;
import it.sasabz.android.sasabus.beacon.busstop.BusStopBeaconHandler;
import it.sasabz.android.sasabus.beacon.notification.TripNotificationAction;
import it.sasabz.android.sasabus.model.BusStop;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.RealtimeApi;
import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.util.HashUtils;
import it.sasabz.android.sasabus.util.IllegalTripException;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.schedulers.Schedulers;

public final class BusBeaconHandler implements IBeaconHandler {

    private static final String TAG = "BusBeaconHandler";

    /**
     * The uuid which identifies a bus beacon.
     */
    public static final String UUID = "e923b236-f2b7-4a83-bb74-cfb7fa44cab8";

    /**
     * the identifier used to identify the region the beacon scanner is listening in.
     */
    public static final String IDENTIFIER = "BUS";

    private static final int TIMEOUT = 10000;

    private static final int BUS_LAST_SEEN_THRESHOLD = 180000;
    private static final int SECONDS_IN_BUS = 90;
    private static final int MIN_NOTIFICATION_SECONDS = 60;
    private static final int MAX_BEACON_DISTANCE = 5;

    private final Context mContext;
    private final BeaconStorage mPrefsManager;

    @SuppressLint("StaticFieldLeak")
    public static TripNotificationAction notificationAction;

    @SuppressLint("StaticFieldLeak")
    private static BusBeaconHandler sInstance;

    private byte mCycleCounter;

    private final Map<Integer, BusBeacon> mBeaconMap = new ConcurrentHashMap<>();

    private BusBeaconHandler(Context context) {
        mContext = context;
        mPrefsManager = BeaconStorage.getInstance(context);
        mBeaconMap.putAll(mPrefsManager.getBeaconMap());

        notificationAction = new TripNotificationAction(context);

        Handler handler = new Handler();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    LogUtils.w(TAG, "Running timer");

                    inspectBeacons();
                });
            }
        }, 0, 180000);
    }

    public static BusBeaconHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BusBeaconHandler(context);
        }

        return sInstance;
    }

    @Override
    public void updateBeacons(Collection<Beacon> beacons) {
        for (Beacon beacon : beacons) {
            validateBeacon(beacon, beacon.getId2().toInt());
        }

        deleteInvisibleBeacons();

        BusBeacon firstBeacon = null;

        for (Map.Entry<Integer, BusBeacon> entry : mBeaconMap.entrySet()) {
            BusBeacon beacon = entry.getValue();

            if ((firstBeacon == null || beacon.getStartDate().before(firstBeacon.getStartDate()))
                    && beacon.lastSeen + 30000 > System.currentTimeMillis()) {
                firstBeacon = beacon;
            }
        }

        if (firstBeacon != null && firstBeacon.isSuitableForTrip) {
            if (mPrefsManager.hasCurrentTrip() &&
                    mPrefsManager.getCurrentTrip().beacon.id == firstBeacon.id) {

                if (firstBeacon.lastSeen + TIMEOUT >= System.currentTimeMillis()) {
                    LogUtils.i(TAG, "Seen: " + (firstBeacon.lastSeen + TIMEOUT - System.currentTimeMillis()));

                    CurrentTrip currentTrip = mPrefsManager.getCurrentTrip();
                    currentTrip.setBeacon(firstBeacon);

                    Pair<Integer, BusStop> currentBusStop = BusStopBeaconHandler.getInstance(mContext)
                            .getCurrentBusStop();
                    if (currentBusStop != null) {
                        List<BusStop> path = currentTrip.getPath();

                        for (BusStop busStop : path) {
                            if (busStop.getGroup() == currentBusStop.second.getGroup()) {
                                firstBeacon.setBusStop(currentBusStop.second, currentBusStop.first);
                                currentTrip.update();

                                LogUtils.e(TAG, "Set current bus stop " + busStop.getId() +
                                        " for vehicle " + firstBeacon.id);

                                break;
                            }
                        }
                    }

                    if (!currentTrip.isNotificationShown && currentTrip.beacon.isSuitableForTrip &&
                            SettingsUtils.isBusNotificationEnabled(mContext)) {

                        currentTrip.setNotificationShown(true);

                        notificationAction.showNotification(currentTrip);
                    }

                    mPrefsManager.setCurrentTrip(currentTrip);
                }
            } else if (mCycleCounter % 5 == 0 && firstBeacon.distance <= MAX_BEACON_DISTANCE) {
                isBeaconCurrentTrip(firstBeacon);
                mCycleCounter = 0;
            }
        }

        mCycleCounter++;

        mPrefsManager.writeBeaconMap(mBeaconMap);
    }

    @Override
    public void validateBeacon(Beacon beacon, int major) {
        BusBeacon busBeacon;

        if (mBeaconMap.keySet().contains(major)) {
            busBeacon = mBeaconMap.get(major);

            busBeacon.seen();
            busBeacon.setDistance(beacon.getDistance());

            LogUtils.w(TAG, "Beacon " + major + ", seen: " + busBeacon.seenSeconds +
                    ", distance: " + busBeacon.distance);


            /*
             * Checks if a beacon needs to download bus info because it is suitable for
             * a trip.
             */
            if (busBeacon.origin == 0 && NetUtils.isOnline(mContext) &&
                    beacon.getDistance() <= MAX_BEACON_DISTANCE) {

                getBusInformation(busBeacon);
            }
        } else {
            busBeacon = new BusBeacon(major);

            mBeaconMap.put(major, busBeacon);

            UserRealmHelper.addBeacon(beacon, it.sasabz.android.sasabus.realm.user.Beacon.TYPE_BUS);

            LogUtils.e(TAG, "Added beacon " + major);

            if (NetUtils.isOnline(mContext) && beacon.getDistance() <= MAX_BEACON_DISTANCE) {
                getBusInformation(busBeacon);
            }
        }
    }

    public void inspectBeacons() {
        updateBeacons(Collections.emptyList());

        new Thread(() -> {
            synchronized (this) {
                try {
                    wait(5000);
                } catch (InterruptedException ignored) {
                }
            }

            updateBeacons(Collections.emptyList());

            synchronized (this) {
                try {
                    wait(30000);
                } catch (InterruptedException ignored) {
                }
            }

            updateBeacons(Collections.emptyList());
        }).start();
    }

    private void getBusInformation(BusBeacon beacon) {
        if (beacon.isOriginPending) {
            return;
        }

        if (!beacon.canRetry()) {
            beacon.setSuitableForTrip(mContext, false);
            return;
        }

        LogUtils.e(TAG, "getBusInformation " + beacon.id);

        beacon.setOriginPending(true);
        beacon.retry();

        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.vehicleRx(beacon.id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        beacon.setOriginPending(false);
                        beacon.setSuitableForTrip(mContext, false);
                    }

                    @Override
                    public void onNext(RealtimeResponse response) {
                        if (response.buses.isEmpty()) {
                            // Assume this bus is not driving at the moment and return.
                            // If this bus is still not driving after 3 retries ignore it.
                            LogUtils.e(TAG, "Vehicle " + beacon.id + " not driving");

                            beacon.setSuitableForTrip(mContext, false);
                            beacon.setOriginPending(false);

                            return;
                        }

                        RealtimeBus bus = response.buses.get(0);

                        LogUtils.e(TAG, "getBusInformation: " + bus.busStop);

                        if (bus.path.isEmpty()) {
                            beacon.setOriginPending(false);
                            beacon.setSuitableForTrip(mContext, false);

                            Throwable t = new IllegalTripException("Triplist for " + beacon.id
                                    + " empty");
                            Utils.logException(t);

                            return;
                        }

                        beacon.setOrigin(bus.busStop);
                        beacon.setLineId(bus.lineId);
                        beacon.setTrip(bus.trip);
                        beacon.setVariant(bus.variant);
                        beacon.setFuelPrice(1.35F);

                        beacon.setBusStop(new BusStop(BusStopRealmHelper
                                .getBusStop(bus.busStop)), BusBeacon.TYPE_REALTIME);

                        beacon.setBusStops(bus.path);

                        beacon.setDelay(bus.delayMin);

                        String destination = BusStopRealmHelper
                                .getName(bus.path.get(bus.path.size() - 1));

                        String title = mContext.getString(R.string.notification_bus_title,
                                Lines.lidToName(bus.lineId), destination);

                        beacon.setTitle(title);

                        String hash = HashUtils.getHashForTrip(mContext, beacon);
                        beacon.setHash(hash);

                        LogUtils.e(TAG, "Got bus info for " + beacon.id +
                                ", bus stop " + bus.busStop);

                        beacon.setSuitableForTrip(mContext, true);
                        beacon.setOriginPending(false);
                    }
                });
    }

    private void deleteInvisibleBeacons() {
        LogUtils.i(TAG, "deleteInvisibleBeacons");

        CurrentTrip currentTrip = mPrefsManager.getCurrentTrip();

        for (Map.Entry<Integer, BusBeacon> entry : mBeaconMap.entrySet()) {
            BusBeacon beacon = entry.getValue();

            if (beacon.lastSeen + BUS_LAST_SEEN_THRESHOLD < System.currentTimeMillis()) {
                mBeaconMap.remove(entry.getKey());

                LogUtils.e(TAG, "Removed beacon " + entry.getKey());

                if (mPrefsManager.hasCurrentTrip() &&
                        currentTrip.getId() == entry.getValue().id) {

                    if (beacon.seenSeconds > SECONDS_IN_BUS) {
                        addTrip(beacon);
                    }

                    mPrefsManager.setCurrentTrip(null);
                }
            } else if (beacon.lastSeen + TIMEOUT < System.currentTimeMillis()) {
                if (mPrefsManager.hasCurrentTrip() && currentTrip.getId() == beacon.id) {
                    if (currentTrip.isNotificationShown) {
                        currentTrip.setNotificationShown(false);
                        currentTrip.setBeacon(beacon);

                        LogUtils.e(TAG, "Dismissing notification for " + currentTrip.getId());

                        NotificationUtils.cancelBus(mContext);

                        getStopStation(beacon);

                        mPrefsManager.setCurrentTrip(currentTrip);
                    }
                }
            }
        }
    }

    private void addTrip(BusBeacon beacon) {
        if (beacon.destination == 0) {
            Utils.throwTripError(mContext, beacon.id + " stopStation == 0");

            return;
        }

        /*
         * Gets the index of the stop station from the stop list.
         */
        int index = beacon.busStops.indexOf(beacon.destination);
        if (index == -1) {
            String message = beacon.id + " index == -1, stopStation: " +
                    beacon.destination + ", stopList: " +
                    Arrays.toString(beacon.busStops.toArray());

            Utils.throwTripError(mContext, message);

            return;
        }

        /*
         * As the realtime api outputs the next station of the trip, we need to
         * go back by one in the trip list. If the bus is at the second bus stop,
         * the api already outputs it at the third.
         */
        if (index > 0) {
            beacon.setDestination(beacon.busStops.get(index - 1));
        } else {
            beacon.setDestination(beacon.busStops.get(index));
        }

        if (Utils.insertTripIfValid(mContext, beacon) &&
                SettingsUtils.isTripNotificationEnabled(mContext)) {

            NotificationUtils.trip(mContext, beacon.getHash());

            LogUtils.e(TAG, "Saved trip " + beacon.id);

            if (SettingsUtils.isSurveyEnabled(mContext)) {
                LogUtils.e(TAG, "Survey is enabled");

                long lastSurvey = SettingsUtils.getLastSurveyMillis(mContext);
                boolean showSurvey = false;

                switch (SettingsUtils.getSurveyInterval(mContext)) {
                    // Show every time
                    case 0:
                        LogUtils.e(TAG, "Survey interval: every time");

                        showSurvey = true;
                        break;
                    // Once a day
                    case 1:
                        LogUtils.e(TAG, "Survey interval: once a day");

                        if (System.currentTimeMillis() - lastSurvey > TimeUnit.DAYS.toMillis(1)) {
                            showSurvey = true;
                        }
                        break;
                    // Once a week
                    case 2:
                        LogUtils.e(TAG, "Survey interval: once a week");

                        if (System.currentTimeMillis() - lastSurvey > TimeUnit.DAYS.toMillis(7)) {
                            showSurvey = true;
                        }
                        break;
                    // Once a month
                    case 3:
                        LogUtils.e(TAG, "Survey interval: once a month");

                        if (System.currentTimeMillis() - lastSurvey > TimeUnit.DAYS.toMillis(30)) {
                            showSurvey = true;
                        }
                        break;
                }

                if (showSurvey) {
                    LogUtils.e(TAG, "Showing survey");
                    NotificationUtils.survey(mContext, beacon.getHash());

                    SettingsUtils.setLastSurveyMillis(mContext, System.currentTimeMillis());
                }
            }
        } else {
            LogUtils.e(TAG, "Could not save trip " + beacon.id);
        }
    }

    private void isBeaconCurrentTrip(BusBeacon beacon) {
        LogUtils.e(TAG, "isBeaconCurrentTrip");

        if (beacon.seenSeconds > MIN_NOTIFICATION_SECONDS) {
            LogUtils.e(TAG, "Added trip because it was in range for more than " +
                    MIN_NOTIFICATION_SECONDS + 's');

            mPrefsManager.setCurrentTrip(new CurrentTrip(mContext, beacon));

            return;
        }

        if (beacon.isCurrentTripPending) {
            return;
        }

        beacon.setCurrentTripPending(true);

        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.vehicleRx(beacon.id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        beacon.setCurrentTripPending(false);
                    }

                    @Override
                    public void onNext(RealtimeResponse response) {
                        beacon.setCurrentTripPending(false);

                        // Ignore trip.
                        if (response.buses.isEmpty()) {
                            return;
                        }

                        RealtimeBus bus = response.buses.get(0);

                        LogUtils.e(TAG, "isBeaconCurrentTrip response: " + bus.busStop);

                        if (beacon.origin != bus.busStop) {
                            LogUtils.e(TAG, "Setting new bus stop for " + beacon.id);

                            if (mPrefsManager.hasCurrentTrip() &&
                                    mPrefsManager.getCurrentTrip().beacon.id != beacon.id) {

                                BusBeacon preBeaconInfo = mPrefsManager.getCurrentTrip().beacon;
                                if (preBeaconInfo.seenSeconds > SECONDS_IN_BUS) {
                                    addTrip(preBeaconInfo);
                                }
                            }

                            beacon.setBusStop(new BusStop(BusStopRealmHelper
                                    .getBusStop(bus.busStop)), BusBeacon.TYPE_REALTIME);

                            mPrefsManager.setCurrentTrip(new CurrentTrip(mContext, beacon));

                            // Cancel all bus stop notifications
                            for (int i = 0; i < 6000; i++) {
                                NotificationUtils.cancel(mContext, i);
                            }
                        }
                    }
                });
    }

    private void getStopStation(BusBeacon beacon) {
        LogUtils.e(TAG, "getStopStation " + beacon.id);

        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.vehicleRx(beacon.id)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);
                    }

                    @Override
                    public void onNext(RealtimeResponse realtimeResponse) {
                        if (!realtimeResponse.buses.isEmpty()) {
                            RealtimeBus bus = realtimeResponse.buses.get(0);

                            beacon.setDestination(bus.busStop);

                            LogUtils.e(TAG, "Stop station for " + beacon.id + ": " +
                                    bus.busStop);
                        }
                    }
                });
    }

    public void currentBusStopOutOfRange(@NonNull Pair<Integer, BusStop> currentBusStop) {
        if (mPrefsManager.hasCurrentTrip()) {
            CurrentTrip currentTrip = mPrefsManager.getCurrentTrip();

            List<BusStop> path = currentTrip.getPath();

            int index = -1;
            for (int i = 0, pathSize = path.size(); i < pathSize; i++) {
                BusStop busStop = path.get(i);
                if (busStop.getGroup() == currentBusStop.second.getGroup()) {
                    index = i;

                    break;
                }
            }

            if (index == -1) {
                return;
            }

            if (index < path.size() - 1) {
                BusStop newBusStop = path.get(index + 1);

                currentTrip.beacon.setBusStop(newBusStop, BusBeacon.TYPE_BEACON);
                currentTrip.update();

                LogUtils.e(TAG, "Set " + newBusStop.getId() + ' ' +
                        newBusStop.getNameDe() + " as new bus stop for " + currentTrip.getId());
            }
        }
    }
}