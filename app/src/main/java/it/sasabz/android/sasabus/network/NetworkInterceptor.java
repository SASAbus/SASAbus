package it.sasabz.android.sasabus.network;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.util.HashUtils;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Intercepts each call to the rest api and adds the user agent header to the request.
 * The user agent consists of the app name, followed by the app version name and code.
 * A token is needed to identify the user which made the request when trying to download trips.
 * The auth header is used to check if the request was made from a valid client and block
 * 3rd party users from using the api.
 *
 * @author Alex Lardschneider
 */
public class NetworkInterceptor implements Interceptor {

    private String userAgent;

    private final Context context;

    public NetworkInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request requestWithUserAgent = originalRequest
                .newBuilder()
                .header("User-Agent", getUserAgent())
                .build();

        LogUtils.w("OkHttp", requestWithUserAgent.method() + " url: " + originalRequest.url());

        return chain.proceed(requestWithUserAgent);
    }

    private String getUserAgent() {
        if (userAgent != null) {
            return userAgent;
        }

        String token = Utils.md5(Build.MODEL + Settings.Secure.getString(context
                .getContentResolver(), Settings.Secure.ANDROID_ID) + Build.SERIAL).substring(0, 8);

        userAgent = "SASAbus/" + BuildConfig.VERSION_NAME +
                " version=" + BuildConfig.VERSION_CODE +
                " token=" + token;

        return userAgent;
    }
}