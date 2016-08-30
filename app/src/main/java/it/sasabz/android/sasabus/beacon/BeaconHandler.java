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
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import it.sasabz.android.sasabus.beacon.bus.BusBeaconHandler;
import it.sasabz.android.sasabus.beacon.busstop.BusStopBeaconHandler;
import it.sasabz.android.sasabus.beacon.event.EventBeaconHandler;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;

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

    private final BusBeaconHandler mBusBeaconHandler;
    private final BusStopBeaconHandler mBusStopBeaconHandler;
    private final EventBeaconHandler mEventBeaconHandler;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private RegionBootstrap mRegionBusBootstrap;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private RegionBootstrap mRegionBusStopBootstrap;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private RegionBootstrap mRegionEventBootstrap;

    @SuppressLint("StaticFieldLeak")
    private static BeaconHandler sInstance;

    /**
     * Creates a new instance of {@code BeaconHandler} used to listen for beacons and display
     * bus and station information in the app
     *
     * @param context application context
     */
    private BeaconHandler(Context context) {
        mContext = context.getApplicationContext();

        mBusBeaconHandler = BusBeaconHandler.getInstance(context);
        mBusStopBeaconHandler = BusStopBeaconHandler.getInstance(context);
        mEventBeaconHandler = EventBeaconHandler.getInstance(context);
    }

    /**
     * Returns the current {@code BeaconHandler} instance. If there is no instance yet,
     * create a new instance and start listening
     *
     * @param context AppApplication context
     * @return current instance in use, or {@code null} if the scanner has no location permission.
     */
    public static BeaconHandler get(Context context) {
        if (sInstance == null) {
            LogUtils.e(TAG, "Creating beacon handlers");

            sInstance = new BeaconHandler(context);
        }

        return sInstance;
    }

    @Override
    public void onBeaconServiceConnect() {
        LogUtils.e(TAG, "onBeaconServiceConnect()");

        mBeaconManager.setRangeNotifier((beacons, region) -> {
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
        startRangingBeacon(region);
    }

    @Override
    public void didExitRegion(Region region) {
        stopRangingBeacon(region);
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return mContext.bindService(service, conn, flags);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        mContext.unbindService(conn);
    }

    /**
     * Starts getting detailed beacon information
     */
    private void startRangingBeacon(Region region) {
        LogUtils.e(TAG, "startRanging() " + region.getUniqueId());

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

    /**
     * Stops getting detailed beacon information
     */
    private void stopRangingBeacon(Region region) {
        LogUtils.e(TAG, "stopRanging() " + region.getUniqueId());

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

    /**
     * Starts listening for available beacons
     */
    void startListening() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LogUtils.e(TAG, "Missing location permission");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            LogUtils.e(TAG, "startListening()");

            mBeaconManager = BeaconManager.getInstanceForApplication(mContext);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

            mBeaconManager.setForegroundScanPeriod(3000);
            mBeaconManager.setForegroundBetweenScanPeriod(0);

            mBeaconManager.setBackgroundScanPeriod(3000);
            mBeaconManager.setBackgroundBetweenScanPeriod(0);

            mRegionBus = new Region(BusBeaconHandler.IDENTIFIER, Identifier.parse(BusBeaconHandler.UUID), null, null);
            mRegionBusBootstrap = new RegionBootstrap(this, mRegionBus);

            mRegionBusStop = new Region(BusStopBeaconHandler.IDENTIFIER, Identifier.parse(BusStopBeaconHandler.UUID), null, null);
            mRegionBusStopBootstrap = new RegionBootstrap(this, mRegionBusStop);

            mRegionEvent = new Region(EventBeaconHandler.IDENTIFIER, Identifier.parse(EventBeaconHandler.UUID), null, null);
            mRegionEventBootstrap = new RegionBootstrap(this, mRegionEvent);

            mBeaconManager.bind(this);
        }
    }

    /**
     * Stops listening for beacons and cancels all currently displayed notifications.
     */
    public void stopListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mBusStopBeaconHandler != null) {
                mBusStopBeaconHandler.stop();
            }

            if (mBeaconManager != null) {
                mBeaconManager.unbind(this);
            }

            if (mEventBeaconHandler != null) {
                mEventBeaconHandler.stop();
            }

            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }
}