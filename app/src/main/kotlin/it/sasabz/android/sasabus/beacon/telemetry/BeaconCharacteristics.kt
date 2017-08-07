package it.sasabz.android.sasabus.beacon.telemetry

class BeaconCharacteristics internal constructor(uuid: ByteArray, major: ByteArray, minor: ByteArray,
                                                 battery: ByteArray, sysId: ByteArray,
                                                 firmware: ByteArray, vararg hardware: Byte) {

    val uuid: String

    val major: Int
    val minor: Int
    val battery: Int

    val sysId: String
    val firmware: String
    val hardware: String

    init {
        this.uuid = hexToString(*uuid)

        this.major = hexToInt(*major)
        this.minor = hexToInt(*minor)

        this.battery = hexToInt(*battery)

        this.sysId = hexToString(*sysId)
        this.firmware = hexToString(*firmware)
        this.hardware = hexToString(*hardware)

    }

    private fun hexToInt(vararg value: Byte): Int {
        val sb = StringBuilder(value.size)

        value.indices.reversed()
                .map { value[it] }
                .forEach { sb.append(String.format("%02X", it)) }

        return Integer.parseInt(sb.toString(), 16)
    }

    private fun hexToString(vararg value: Byte): String {
        val sb = StringBuilder(value.size)

        for (byteChar in value) {
            sb.append(String.format("%02X", byteChar))
        }

        return sb.toString()
    }
}
