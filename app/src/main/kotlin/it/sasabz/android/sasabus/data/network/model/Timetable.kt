package it.sasabz.android.sasabus.data.network.model

import com.google.gson.annotations.SerializedName

class Timetable {

    var line: String? = null
    var city: String? = null
    var name: String = ""

    @SerializedName("valid_from")
    var validFrom: String? = null

    @SerializedName("valid_to")
    var validTo: String? = null
}