package it.sasabz.android.sasabus.data.network.api

import it.sasabz.android.sasabus.data.network.model.CloudBeacon
import it.sasabz.android.sasabus.data.network.rest.Endpoint

import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable

interface TelemetryApi {

    @POST(Endpoint.TELEMETRY_BEACON)
    fun sendBeacon(@Body beacons: MutableList<CloudBeacon>): Observable<Void>
}