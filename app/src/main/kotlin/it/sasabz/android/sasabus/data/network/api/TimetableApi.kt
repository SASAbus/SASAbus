package it.sasabz.android.sasabus.data.network.api

import it.sasabz.android.sasabus.data.network.response.TimetableResponse
import it.sasabz.android.sasabus.data.network.rest.Endpoint
import retrofit2.http.GET
import rx.Observable

interface TimetableApi {

    @GET(Endpoint.TIMETABLE_LIST)
    fun all(): Observable<TimetableResponse>
}