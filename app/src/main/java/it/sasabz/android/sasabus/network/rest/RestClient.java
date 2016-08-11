package it.sasabz.android.sasabus.network.rest;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.network.NetworkInterceptor;
import it.sasabz.android.sasabus.util.Preconditions;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The {@link Retrofit RestClient} which will be used to make all rest requests.
 * Uses {@link OkHttpClient} as networking client.
 *
 * @author Alex Lardschneider
 */
public final class RestClient {

    public static Retrofit ADAPTER;

    private RestClient() {
    }

    public static void init(Context context) {
        Preconditions.checkNotNull(context, "init context == null");

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new NetworkInterceptor(context))
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS);

        ADAPTER = new Retrofit.Builder()
                .baseUrl(Endpoint.API)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(builder.build())
                .build();
    }
}
