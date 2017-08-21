package it.sasabz.android.sasabus.data.realm.user

import io.realm.RealmObject

open class TelemetryBeacon : RealmObject() {

    open var macAddress: String = ""

    open var uuid: String = ""

    open var major: Int = 0
    open var minor: Int = 0
    open var battery: Int = 0

    open var sysId: String = ""
    open var firmware: String = ""
    open var hardware: String = ""

    open var recordedAt: Long = 0
}
