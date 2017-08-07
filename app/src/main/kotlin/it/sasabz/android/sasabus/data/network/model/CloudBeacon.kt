package it.sasabz.android.sasabus.data.network.model

import com.google.gson.annotations.SerializedName
import it.sasabz.android.sasabus.data.realm.user.TelemetryBeacon

class CloudBeacon(beacon: TelemetryBeacon) {

    private val uuid: String = beacon.uuid
    private val major: Int = beacon.major
    private val minor: Int = beacon.minor


    private val battery: Int = beacon.battery
    private val firmware: String = beacon.firmware
    private val hardware: String = beacon.hardware


    @SerializedName("recorded_at")
    private val recordedAt: Long = beacon.recordedAt

    @SerializedName("mac_address")
    private val macAddress: String = beacon.macAddress

    @SerializedName("system_id")
    private val sysId: String = beacon.sysId
}