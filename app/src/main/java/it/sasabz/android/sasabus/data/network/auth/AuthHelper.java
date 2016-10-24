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

package it.sasabz.android.sasabus.data.network.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.DataInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import it.sasabz.android.sasabus.data.network.auth.jjwt.Claims;
import it.sasabz.android.sasabus.data.network.auth.jjwt.Jws;
import it.sasabz.android.sasabus.data.network.auth.jjwt.Jwts;
import it.sasabz.android.sasabus.data.network.auth.jjwt.SignatureException;
import it.sasabz.android.sasabus.ui.ecopoints.LoginActivity;
import it.sasabz.android.sasabus.util.LogUtils;
import retrofit2.adapter.rxjava.HttpException;

public final class AuthHelper {

    private static PublicKey publicKey;

    private static final String TAG = "AuthHelper";

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    private AuthHelper() {
    }

    private static final String PREF_AUTH_TOKEN = "pref_auth_token";
    private static final String PREF_USER_ID = "pref_user_id";

    public static final String INTENT_BROADCAST_LOGOUT =
            "it.sasabz.android.sasabus.INTENT_BROADCAST_LOGOUT";

    public static void init(Context context) {
        sContext = context;

        publicKey = getPublicKey();
    }

    private static PublicKey getPublicKey() {
        try {
            InputStream fileInputStream = sContext.getAssets().open("keys/public_key.der");
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            byte[] keyBytes = new byte[fileInputStream.available()];
            dataInputStream.readFully(keyBytes);
            dataInputStream.close();

            KeySpec encodedKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory instance = KeyFactory.getInstance("RSA");

            return instance.generatePublic(encodedKeySpec);
        } catch (Exception e) {
            throw new RuntimeException("Could not load key", e);
        }
    }

    public static void checkIfUnauthorized(Activity activity, Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;

            if (httpException.code() == 401) {
                LogUtils.e(TAG, "Unauthorized response, clearing credentials");

                clearCredentials();

                activity.finish();
                activity.startActivity(new Intent(activity, LoginActivity.class));
            }
        }
    }


    // ======================================== LOGOUT =============================================

    public static void logout(Activity activity) {
        if (!isLoggedIn()) {
            LogUtils.e(TAG, "Attempt to log out a player who is not logged in");
        }

        clearCredentials();

        LogUtils.e(TAG, "Logged out user");

        activity.finish();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        activity.startActivity(intent);
    }

    private static BroadcastReceiver getLogoutReceiver(Activity activity) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtils.e(TAG, "Got logout broadcast");
                logout(activity);
            }
        };
    }

    public static BroadcastReceiver registerLogoutReceiver(Activity activity) {
        BroadcastReceiver receiver = getLogoutReceiver(activity);

        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver,
                new IntentFilter(INTENT_BROADCAST_LOGOUT));

        return receiver;
    }

    public static void unregisterLogoutReceiver(Activity activity, BroadcastReceiver receiver) {
        if (receiver == null) {
            LogUtils.e(TAG, "Attempt to unregister a null receiver in class " +
                    activity.getClass().getSimpleName());
            return;
        }

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver);
    }


    // ====================================== PREFERENCES ==========================================

    @Nullable
    public static String getUserId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_USER_ID, null);
    }

    @SuppressLint("CommitPrefEdits")
    private static void setUserId(Context context, String userId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_USER_ID, userId).commit();
    }

    @Nullable
    private static String getAuthToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_AUTH_TOKEN, null);
    }

    @SuppressLint("CommitPrefEdits")
    private static void setAuthToken(Context context, String token) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_AUTH_TOKEN, token).commit();
    }


    // ====================================TOKEN VERIFICATION ======================================

    @Nullable
    public static String getTokenIfValid() {
        if (isLoggedIn()) {
            return getAuthToken(sContext);
        }

        return null;
    }

    public static boolean setInitialToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(token);

            String userId = claims.getBody().getSubject();

            if (TextUtils.isEmpty(userId)) {
                LogUtils.e(TAG, "User id is empty");

                clearCredentials();

                return false;
            }

            LogUtils.d(TAG, "Token is valid, got user id: " + userId);

            setUserId(sContext, userId);
            setAuthToken(sContext, token);

            return true;
        } catch (SignatureException e) {
            e.printStackTrace();

            clearCredentials();

            Log.e(TAG, "Key is invalid, clearing credentials");

            return false;
        }
    }

    public static boolean isLoggedIn() {
        String token = getAuthToken(sContext);

        return !TextUtils.isEmpty(token) && isTokenValid(token);
    }

    private static boolean isTokenValid(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(token);

            String userId = claims.getBody().getSubject();
            String savedUserId = getUserId(sContext);

            if (TextUtils.isEmpty(savedUserId)) {
                LogUtils.e(TAG, "Saved user id is empty");
                clearCredentials();
                return false;
            }

            if (TextUtils.isEmpty(userId)) {
                LogUtils.e(TAG, "Token user id is empty");
                clearCredentials();
                return false;
            }

            if (!userId.equals(savedUserId)) {
                LogUtils.e(TAG, "Saved user id and token user id don't match, should be " +
                        savedUserId + ", got " + userId + " instead");

                clearCredentials();

                return false;
            }

            LogUtils.d(TAG, "Token is valid, got user id: " + userId);

            return true;
        } catch (SignatureException e) {
            e.printStackTrace();

            clearCredentials();

            Log.e(TAG, "Key is invalid, clearing credentials");

            return false;
        }
    }

    public static void clearCredentials() {
        setUserId(sContext, null);
        setAuthToken(sContext, null);

        LogUtils.e(TAG, "Cleared credentials");
    }
}
