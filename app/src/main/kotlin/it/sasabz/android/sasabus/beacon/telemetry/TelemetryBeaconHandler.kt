package com.davale.sasabus.beacon.telemetry

import android.content.Context

import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleDevice
import com.polidea.rxandroidble.RxBleScanResult

import it.sasabz.android.sasabus.beacon.telemetry.BeaconCharacteristics
import it.sasabz.android.sasabus.data.realm.UserRealmHelper

import java.util.ArrayList
import java.util.UUID

import rx.Observable
import rx.Observer
import rx.Subscription
import timber.log.Timber

class TelemetryBeaconHandler private constructor(private val mContext: Context) {

    private var mScanSubscription: Subscription? = null
    private var mConnectionSubscription: Subscription? = null

    private val mScanned = ArrayList<String>()


    fun start() {
        Timber.e("Starting telemetry")

        val rxBleClient = RxBleClient.create(mContext)

        mScanSubscription = rxBleClient.scanBleDevices()
                .filter { result -> BEACON_NAME == result.bleDevice.name }
                .filter { result -> !mScanned.contains(result.bleDevice.macAddress) }
                .subscribe(object : Observer<RxBleScanResult> {
                    override fun onCompleted() = Unit

                    override fun onError(e: Throwable) = Timber.e("Could not search for beacons: %s", e.message)

                    override fun onNext(result: RxBleScanResult) {
                        Timber.e("Found beacon: %s %s", result.bleDevice.name,
                                result.bleDevice.macAddress)

                        mScanned.add(result.bleDevice.macAddress)

                        connectToBeacon(result.bleDevice)
                    }
                })
    }

    fun stop() {
        Timber.e("Stopping telemetry")

        if (mScanSubscription != null && !mScanSubscription!!.isUnsubscribed) {
            mScanSubscription!!.unsubscribe()
        }

        if (mConnectionSubscription != null && !mConnectionSubscription!!.isUnsubscribed) {
            mConnectionSubscription!!.unsubscribe()
        }
    }

    private fun connectToBeacon(device: RxBleDevice) {
        Timber.e("Connecting to %s", device.macAddress)

        mConnectionSubscription = device.establishConnection(mContext, false)
                .flatMap { connection ->
                    Timber.e("Established connection to %s", device.macAddress)

                    Observable.combineLatest<ByteArray, ByteArray, ByteArray, ByteArray, ByteArray, ByteArray, ByteArray, BeaconCharacteristics>(
                            connection.readCharacteristic(UID),
                            connection.readCharacteristic(MAJOR),
                            connection.readCharacteristic(MINOR),
                            connection.readCharacteristic(BATTERY),
                            connection.readCharacteristic(SYSID),
                            connection.readCharacteristic(FIRMWARE),
                            connection.readCharacteristic(HARDWARE),
                            ::BeaconCharacteristics)
                }
                .subscribe(object : Observer<BeaconCharacteristics> {
                    override fun onCompleted() = Unit

                    override fun onError(e: Throwable) = Timber.e("Could not connect to %s, %s", device.macAddress, e.message)

                    override fun onNext(beacon: BeaconCharacteristics) {
                        Timber.e("UUID: %s", beacon.uuid)
                        Timber.e("MAJOR: %s", beacon.major)
                        Timber.e("MINOR: %s", beacon.minor)

                        Timber.e("BATTERY: %s", beacon.battery)
                        Timber.e("SYSID: %s", beacon.sysId)
                        Timber.e("FIRMWARE: %s", beacon.firmware)
                        Timber.e("HARDWARE: %s", beacon.hardware)

                        UserRealmHelper.saveTelemetryBeacon(device, beacon)

                        mConnectionSubscription!!.unsubscribe()
                        mConnectionSubscription = null
                    }
                })
    }

    companion object {

        private val UID = UUID.fromString("2aaceb00-c5a5-44fd-0100-3fd42d703a4f")
        private val MAJOR = UUID.fromString("2aaceb00-c5a5-44fd-0200-3fd42d703a4f")
        private val MINOR = UUID.fromString("2aaceb00-c5a5-44fd-0300-3fd42d703a4f")

        private val BATTERY = UUID.fromString("2aaceb00-c5a5-44fd-0400-3fd42d703a4f")
        private val SYSID = UUID.fromString("2aaceb00-c5a5-44fd-0500-3fd42d703a4f")
        private val FIRMWARE = UUID.fromString("2aaceb00-c5a5-44fd-0600-3fd42d703a4f")
        private val HARDWARE = UUID.fromString("2aaceb00-c5a5-44fd-0700-3fd42d703a4f")

        private val BEACON_NAME = "OnyxBeacon"

        private var INSTANCE: TelemetryBeaconHandler? = null

        @JvmStatic
        fun getInstance(context: Context): TelemetryBeaconHandler {
            if (INSTANCE == null) {
                synchronized(TelemetryBeaconHandler::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = TelemetryBeaconHandler(context)
                    }
                }
            }

            return INSTANCE!!
        }
    }
}
