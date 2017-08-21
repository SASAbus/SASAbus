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

package it.sasabz.android.sasabus.data.network

import android.content.Context

import java.util.concurrent.TimeUnit

import it.sasabz.android.sasabus.data.network.rest.Endpoint
import it.sasabz.android.sasabus.util.Preconditions
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

/**
 * The [RestClient][Retrofit] which will be used to make all rest requests.
 * Uses [OkHttpClient] as networking client.
 *
 * @author Alex Lardschneider
 */
object RestClient {

    var ADAPTER: Retrofit? = null

    fun init(context: Context) {
        Preconditions.checkNotNull(context, "init context == null")

        val builder = OkHttpClient.Builder()
                .addNetworkInterceptor(NetworkInterceptor(context))
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)

        ADAPTER = Retrofit.Builder()
                .baseUrl(Endpoint.API)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(builder.build())
                .build()
    }


    fun getTelemetryAdapter(context: Context): Retrofit {
        val url = "https://davale-sasa.appspot.com/v1/"

        Timber.i("Telemetry api url is '%s'", url)

        val builder = OkHttpClient.Builder()
                .addNetworkInterceptor(NetworkInterceptor(context))
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)

        return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(builder.build())
                .build()
    }
}
