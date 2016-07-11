package it.sasabz.android.sasabus.beacon;

import android.content.Context;
import android.os.Handler;

import org.altbeacon.beacon.Beacon;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.R;
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

class BusBeaconHandler {

    private static final String TAG = "BusBeaconHandler";

    /**
     * The uuid which identifies a bus beacon.
     */
    static final String UUID = "e923b236-f2b7-4a83-bb74-cfb7fa44cab8";

    /**
     * the identifier used to identify the region the beacon scanner is listening in.
     */
    static final String IDENTIFIER = "BUS";

    private static final int BUS_LAST_SEEN_THRESHOLD = 180000;
    private static final int SECONDS_IN_BUS = 90;
    private static final int MIN_NOTIFICATION_SECONDS = 60;
    private static final int MAX_BEACON_DISTANCE = 5;

    private final Context mContext;
    private final BeaconStorage mPrefsManager;

    private byte mCycleCounter;

    private final Map<Integer, BusBeacon> mBeaconMap = new ConcurrentHashMap<>();

    BusBeaconHandler(Context context) {
        mContext = context;
        mPrefsManager = BeaconStorage.getInstance(context);
        mBeaconMap.putAll(mPrefsManager.getBeaconMap());

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

    private void beaconInRange(Beacon beacon) {
        BusBeacon busBeacon;

        int major = beacon.getId2().toInt();

        if (mBeaconMap.keySet().contains(major)) {
            busBeacon = mBeaconMap.get(major);

            busBeacon.seen();
            busBeacon.setDistance(beacon.getDistance());

            LogUtils.w(TAG, "Beacon " + major + ", seen: " + busBeacon.getSeenSeconds() +
                    ", distance: " + busBeacon.getDistance());


            /*
             * Checks if a beacon needs to download bus info because it is suitable for
             * a trip.
             */
            if (busBeacon.getOrigin() == 0 && NetUtils.isOnline(mContext) &&
                    beacon.getDistance() <= MAX_BEACON_DISTANCE) {

                getBusInformation(busBeacon);
            }
        } else {
            busBeacon = new BusBeacon(major, HashUtils.getHashForIdentifier(mContext, "trip"));

            mBeaconMap.put(major, busBeacon);

            LogUtils.e(TAG, "Added beacon " + major);

            UserRealmHelper.addBeacon(beacon, it.sasabz.android.sasabus.realm.user.Beacon.TYPE_BUS);

            if (NetUtils.isOnline(mContext) && beacon.getDistance() <= MAX_BEACON_DISTANCE) {
                getBusInformation(busBeacon);
            }
        }
    }

    void updateBeacons(Iterable<Beacon> beacons) {
        for (Beacon beacon : beacons) {
            beaconInRange(beacon);
        }

        deleteInvisibleBeacons();

        BusBeacon firstBeacon = null;

        for (Map.Entry<Integer, BusBeacon> entry : mBeaconMap.entrySet()) {
            BusBeacon beacon = entry.getValue();

            if ((firstBeacon == null || beacon.getStartDate().before(firstBeacon.getStartDate()))
                    && beacon.getLastSeen() + 30000 > System.currentTimeMillis()) {
                firstBeacon = beacon;
            }
        }

        if (firstBeacon != null) {
            if (mPrefsManager.hasCurrentTrip() &&
                    mPrefsManager.getCurrentTrip().getBeacon().getId() == firstBeacon.getId()) {

                if (firstBeacon.getLastSeen() + 10000 >= System.currentTimeMillis()) {
                    LogUtils.w(TAG, "Seen: " + (firstBeacon.getLastSeen() + 10000 - System.currentTimeMillis()));

                    CurrentTrip currentTrip = mPrefsManager.getCurrentTrip();
                    currentTrip.setBeacon(firstBeacon);

                    if (!currentTrip.isNotificationShown() &&
                            SettingsUtils.isBusNotificationEnabled(mContext)) {

                        currentTrip.setNotificationShown(true);

                        NotificationUtils.bus(mContext, firstBeacon.getId(), firstBeacon.getTitle());
                    }

                    mPrefsManager.setCurrentTrip(currentTrip);
                }
            } else if (mCycleCounter % 4 == 0 && firstBeacon.isSuitableForTrip() &&
                    firstBeacon.getDistance() <= MAX_BEACON_DISTANCE) {

                isBeaconCurrentTrip(firstBeacon);
                mCycleCounter = 0;
            }
        }

        mCycleCounter++;

        mPrefsManager.writeBeaconMap(mBeaconMap);
    }

    void inspectBeacons() {
        new Thread(() -> {
            synchronized (this) {
                try {
                    wait(5000);
                } catch (Exception ignored) {
                }
            }
            updateBeacons(Collections.emptyList());
            synchronized (this) {
                try {
                    wait(30000);
                } catch (Exception ignored) {
                }
            }
            updateBeacons(Collections.emptyList());
        }).start();

        updateBeacons(Collections.emptyList());
    }

    private void getBusInformation(BusBeacon beacon) {
        if (beacon.isOriginPending() || !beacon.canRetry()) {
            return;
        }

        LogUtils.e(TAG, "getBusInformation " + beacon.getId());

        beacon.setOriginPending(true);
        beacon.retry();

        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.vehicleRx(beacon.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);
                        beacon.setOriginPending(false);
                    }

                    @Override
                    public void onNext(RealtimeResponse response) {
                        LogUtils.e(TAG, "getBusInformation response: " + response);

                        if (response.buses.isEmpty()) {
                            // Assume this bus is not driving at the moment and return.
                            // If this bus is still not driving after 3 retries ignore it.
                            LogUtils.e(TAG, "Bus " + beacon.getId() + " not driving");
                            return;
                        }

                        RealtimeBus bus = response.buses.get(0);

                        beacon.setOrigin(bus.busStop);
                        beacon.setLineId(bus.lineId);
                        beacon.setTripId(bus.trip);
                        beacon.setVariant(bus.variant);
                        beacon.setFuelPrice(1.35F);

                        if (bus.path.isEmpty()) {
                            beacon.setOriginPending(false);

                            Throwable t = new IllegalTripException("Triplist for " + beacon.getId() + " empty");
                            Utils.handleException(t);

                            return;
                        }

                        beacon.setBusStops(bus.path);

                        String destination = BusStopRealmHelper
                                .getName(bus.path.get(bus.path.size() - 1));

                        String title = mContext.getString(R.string.notification_bus_title,
                                Lines.lidToName(bus.lineId), destination);

                        beacon.setTitle(title);

                        LogUtils.e(TAG, "Got bus info for " + beacon.getId() + ", but stop " + bus.busStop);

                        beacon.setSuitableForTrip(true);
                        beacon.setOriginPending(false);
                    }
                });
    }

    private void deleteInvisibleBeacons() {
        LogUtils.w(TAG, "deleteInvisibleBeacons");

        CurrentTrip currentTrip = mPrefsManager.getCurrentTrip();

        for (Map.Entry<Integer, BusBeacon> entry : mBeaconMap.entrySet()) {
            BusBeacon beacon = entry.getValue();

            if (beacon.getLastSeen() + BUS_LAST_SEEN_THRESHOLD < System.currentTimeMillis()) {
                mBeaconMap.remove(entry.getKey());

                LogUtils.e(TAG, "Removed beacon " + entry.getKey());

                if (mPrefsManager.hasCurrentTrip() &&
                        currentTrip.getId() == entry.getValue().getId()) {

                    if (beacon.getSeenSeconds() > SECONDS_IN_BUS) {
                        LogUtils.e(TAG, "TripsSQLiteOpenHelper");

                        addTrip(beacon);
                    }

                    mPrefsManager.setCurrentTrip(null);
                }
            } else if (beacon.getLastSeen() + 10000 < System.currentTimeMillis()) {
                if (mPrefsManager.hasCurrentTrip() &&
                        currentTrip.getId() == entry.getValue().getId()) {

                    if (currentTrip.isNotificationShown()) {
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
        if (beacon.getDestination() == 0) {
            Utils.throwTripError(mContext, beacon.getId() + " stopStation == 0");

            return;
        }

        /*
         * Gets the index of the stop station from the stop list.
         */
        int index = beacon.getBusStops().indexOf(beacon.getDestination());
        if (index == -1) {
            String message = beacon.getId() + " index == -1, stopStation: " +
                    beacon.getDestination() + ", stopList: " +
                    Arrays.toString(beacon.getBusStops().toArray());

            Utils.throwTripError(mContext, message);

            return;
        }

        /*
         * As the realtime api outputs the next station of the trip, we need to
         * go back by one in the trip list. If the bus is at the second bus stop,
         * the api already outputs it at the third.
         */
        if (index > 0) {
            beacon.setDestination(beacon.getBusStops().get(index - 1));
        } else {
            beacon.setDestination(beacon.getBusStops().get(index));
        }

        if (Utils.insertTripIfValid(mContext, beacon) &&
                SettingsUtils.isTripNotificationEnabled(mContext)) {

            NotificationUtils.trip(mContext, beacon.getHash());

            LogUtils.e(TAG, "Saved trip " + beacon.getId());

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
            LogUtils.e(TAG, "Could not save trip " + beacon.getId());
        }
    }

    private void isBeaconCurrentTrip(BusBeacon beacon) {
        LogUtils.e(TAG, "isBeaconCurrentTrip");

        if (beacon.getSeenSeconds() > MIN_NOTIFICATION_SECONDS) {
            LogUtils.e(TAG, "Added trip because it was in range for more than " +
                    MIN_NOTIFICATION_SECONDS + 's');

            mPrefsManager.setCurrentTrip(new CurrentTrip(beacon));

            return;
        }

        if (beacon.isCurrentTripPending()) {
            return;
        }

        beacon.setCurrentTripPending(true);

        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.vehicleRx(beacon.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        beacon.setCurrentTripPending(false);
                    }

                    @Override
                    public void onNext(RealtimeResponse response) {
                        LogUtils.e(TAG, "isBeaconCurrentTrip response: " + response);

                        beacon.setCurrentTripPending(false);

                        // Ignore trip.
                        if (response.buses.isEmpty()) {
                            return;
                        }

                        if (beacon.getOrigin() != response.buses.get(0).busStop) {
                            LogUtils.e(TAG, "Setting new bus stop for " + beacon.getId());

                            if (mPrefsManager.hasCurrentTrip() && mPrefsManager.getCurrentTrip().getBeacon().getId() != beacon.getId()) {
                                BusBeacon preBeaconInfo = mPrefsManager.getCurrentTrip().getBeacon();
                                if (preBeaconInfo.getSeenSeconds() > SECONDS_IN_BUS) {
                                    addTrip(preBeaconInfo);
                                }
                            }

                            mPrefsManager.setCurrentTrip(new CurrentTrip(beacon));

                            // Cancel all bus stop notifications
                            for (int i = 0; i < 6000; i++) {
                                NotificationUtils.cancel(mContext, i);
                            }
                        }
                    }
                });
    }

    private void getStopStation(BusBeacon beacon) {
        LogUtils.e(TAG, "getStopStation " + beacon.getId());

        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.vehicleRx(beacon.getId())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);
                    }

                    @Override
                    public void onNext(RealtimeResponse realtimeResponse) {
                        if (!realtimeResponse.buses.isEmpty()) {
                            RealtimeBus bus = realtimeResponse.buses.get(0);

                            beacon.setDestination(bus.busStop);

                            LogUtils.e(TAG, "Stop station for " + beacon.getId() + ": " +
                                    bus.busStop);
                        }
                    }
                });
    }
}