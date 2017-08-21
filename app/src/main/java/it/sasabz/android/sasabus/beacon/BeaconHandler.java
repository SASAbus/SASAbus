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

package it.sasabz.android.sasabus.beacon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.RemoteException;

import com.davale.sasabus.beacon.telemetry.TelemetryBeaconHandler;
import com.davale.sasabus.core.util.DeviceUtils;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Arrays;

import it.sasabz.android.sasabus.beacon.bus.BusBeaconHandler;
import it.sasabz.android.sasabus.beacon.busstop.BusStopBeaconHandler;
import it.sasabz.android.sasabus.beacon.event.EventBeaconHandler;
import it.sasabz.android.sasabus.util.Utils;
import timber.log.Timber;

/**
 * Beacon handler which scans for beacons in range. This scanner will be automatically stated
 * by the {@code AltBeacon} library as soon as it detects either a bus or bus stop beacon.
 *
 * @author Alex Lardschneider
 */
public final class BeaconHandler implements BeaconConsumer, BootstrapNotifier {

    private static final String TAG = "BeaconHandler";

    private final Context mContext;

    private BeaconManager mBeaconManager;

    private Region mRegionBus;
    private Region mRegionBusStop;
    private Region mRegionEvent;

    private BusBeaconHandler mBusBeaconHandler;
    private BusStopBeaconHandler mBusStopBeaconHandler;
    private EventBeaconHandler mEventBeaconHandler;

    private TelemetryBeaconHandler mTelemetry;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private RegionBootstrap mBootstrap;

    @SuppressLint("StaticFieldLeak")
    private static BeaconHandler INSTANCE;

    public static boolean isListening;


    private BeaconHandler(Context context) {
        Timber.e("Creating beacon handlers");

        mContext = context.getApplicationContext();
    }

    public static BeaconHandler get(Context context) {
        if (INSTANCE == null) {
            synchronized (BeaconHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BeaconHandler(context);
                }
            }
        }

        return INSTANCE;
    }


    @Override
    public void onBeaconServiceConnect() {
        Timber.e("onBeaconServiceConnect()");

        mBeaconManager.addRangeNotifier((beacons, region) -> {
            if (!isListening) {
                Timber.e("didRangeBeaconsInRegion: not listening");
                return;
            }

            if (region.getUniqueId().equals(BusBeaconHandler.IDENTIFIER)) {
                mBusBeaconHandler.didRangeBeacons(beacons);
            }

            if (region.getUniqueId().equals(BusStopBeaconHandler.IDENTIFIER)) {
                mBusStopBeaconHandler.didRangeBeacons(beacons);
            }

            if (region.getUniqueId().equals(EventBeaconHandler.IDENTIFIER)) {
                mEventBeaconHandler.didRangeBeacons(beacons);
            }
        });
    }

    @Override
    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region region) {
    }

    @Override
    public void didEnterRegion(Region region) {
        Timber.e("didEnterRegion() %s", region.getUniqueId());

        if (mTelemetry != null) {
            mTelemetry.start();
        }

        try {
            if (region.getUniqueId().equals(BusBeaconHandler.IDENTIFIER)) {
                mBeaconManager.startRangingBeaconsInRegion(mRegionBus);
            }

            if (region.getUniqueId().equals(BusStopBeaconHandler.IDENTIFIER)) {
                mBeaconManager.startRangingBeaconsInRegion(mRegionBusStop);
                mBusStopBeaconHandler.start();
            }

            if (region.getUniqueId().equals(EventBeaconHandler.IDENTIFIER)) {
                mEventBeaconHandler.start();
                mBeaconManager.startRangingBeaconsInRegion(mRegionEvent);
            }
        } catch (RemoteException e) {
            Utils.logException(e);
        }
    }

    @Override
    public void didExitRegion(Region region) {
        Timber.e("didExitRegion() %s", region.getUniqueId());

        if (mTelemetry != null) {
            mTelemetry.stop();
        }

        try {
            if (region.getUniqueId().equals(BusBeaconHandler.IDENTIFIER)) {
                mBusBeaconHandler.inspectBeacons();
                mBeaconManager.stopRangingBeaconsInRegion(mRegionBus);
            }

            if (region.getUniqueId().equals(BusStopBeaconHandler.IDENTIFIER)) {
                mBeaconManager.stopRangingBeaconsInRegion(mRegionBusStop);
                mBusStopBeaconHandler.stop();
            }

            if (region.getUniqueId().equals(EventBeaconHandler.IDENTIFIER)) {
                mBeaconManager.stopRangingBeaconsInRegion(mRegionEvent);
                mEventBeaconHandler.stop();
            }
        } catch (RemoteException e) {
            Utils.logException(e);
        }
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return mContext.bindService(service, conn, flags);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        mContext.unbindService(conn);
    }


    public void start() {
        Timber.e("start()");

        if (isListening) {
            Timber.e("Already listening for beacons");
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Timber.e("Not above Android Jelly Bean");
            return;
        }

        if (!DeviceUtils.hasPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Timber.e("Missing location permission");
            return;
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Timber.e("Unable to find a valid bluetooth adapter");
            return;
        }

        if (!adapter.isEnabled()) {
            Timber.e("Bluetooth adapter is disabled");
            return;
        }

        if (adapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
            Timber.e("Bluetooth adapter is turning off");
            return;
        }

        if (adapter.getState() != BluetoothAdapter.STATE_ON) {
            Timber.e("Bluetooth adapter is not in state STATE_ON");
            return;
        }

        mBusBeaconHandler = BusBeaconHandler.getInstance(mContext);
        mBusStopBeaconHandler = BusStopBeaconHandler.getInstance(mContext);
        mEventBeaconHandler = EventBeaconHandler.getInstance(mContext);

        mTelemetry = TelemetryBeaconHandler.getInstance(mContext);

        mBeaconManager = BeaconManager.getInstanceForApplication(mContext);
        mBeaconManager.setRegionStatePeristenceEnabled(false);
        mBeaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        mBeaconManager.setForegroundScanPeriod(3000);
        mBeaconManager.setForegroundBetweenScanPeriod(0);

        mBeaconManager.setBackgroundScanPeriod(3000);
        mBeaconManager.setBackgroundBetweenScanPeriod(0);

        mRegionBus = new Region(BusBeaconHandler.IDENTIFIER, Identifier.parse(BusBeaconHandler.UUID), null, null);
        mRegionBusStop = new Region(BusStopBeaconHandler.IDENTIFIER, Identifier.parse(BusStopBeaconHandler.UUID), null, null);
        mRegionEvent = new Region(EventBeaconHandler.IDENTIFIER, Identifier.parse(EventBeaconHandler.UUID), null, null);

        mBootstrap = new RegionBootstrap(this, Arrays.asList(
                mRegionBus,
                mRegionBusStop,
                mRegionEvent
        ));

        mBeaconManager.bind(this);

        isListening = true;
    }

    public void stop() {
        Timber.e("stop()");

        if (!isListening) {
            Timber.e("Not listening, call to stop() will be ignored");
        }

        if (mTelemetry != null) {
            mTelemetry.stop();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mBusStopBeaconHandler != null) {
                mBusStopBeaconHandler.stop();
            }

            if (mEventBeaconHandler != null) {
                mEventBeaconHandler.stop();
            }

            if (mBeaconManager != null) {
                mBeaconManager.unbind(this);
            }

            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }
}