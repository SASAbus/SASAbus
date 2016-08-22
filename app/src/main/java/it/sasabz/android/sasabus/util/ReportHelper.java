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

package it.sasabz.android.sasabus.util;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.io.File;
import java.util.Map;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.ReportApi;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tslamic.github.io.adn.DeviceNames;

/**
 * Helper class to send reports for either app errors or problems in a vehicle.
 *
 * @author Alex Lardschneider
 */
public final class ReportHelper {

    private static final int SELECT_PHOTO = 1002;
    private static final int PERMISSIONS_ACCESS_STORAGE = 100;

    private final Activity mActivity;
    private final View mSnackBarLayout;

    private final String mType;

    public ReportHelper(Activity activity, View snackBarLayout, String type) {
        mActivity = activity;
        mSnackBarLayout = snackBarLayout;
        mType = type;
    }

    public void send(String email, String message, @Nullable Uri screenshotUri) {
        send(email, message, screenshotUri, 0);
    }

    public void send(String email, String message, @Nullable Uri screenshotUri, int vehicle) {
        ProgressDialog barProgressDialog = new ProgressDialog(mActivity, R.style.DialogStyle);
        barProgressDialog.setMessage(mActivity.getString(R.string.dialog_report_sending));
        barProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        barProgressDialog.setIndeterminate(true);
        barProgressDialog.setCancelable(false);
        barProgressDialog.show();

        ReportApi reportApi = RestClient.ADAPTER.create(ReportApi.class);
        ReportBody body = new ReportBody(mActivity, email, message, vehicle);

        Observable<Void> observable;

        if (screenshotUri != null) {
            RequestBody image = RequestBody.create(MediaType.parse("image/png"),
                    new File(Utils.getPathFromUri(mActivity, screenshotUri)));

            observable = reportApi.send(mType, body, image);
        } else {
            observable = reportApi.sendNoImage(mType, body);
        }

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        barProgressDialog.dismiss();

                        Snackbar snackbar = Snackbar.make(mSnackBarLayout,
                                R.string.snackbar_report_error, Snackbar.LENGTH_LONG);

                        TextView textView = (TextView) snackbar.getView()
                                .findViewById(android.support.design.R.id.snackbar_text);

                        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.white));

                        snackbar.setAction(R.string.snackbar_retry, v -> send(email, message, screenshotUri, vehicle));

                        snackbar.setActionTextColor(ContextCompat.getColor(mActivity, R.color.primary));
                        snackbar.show();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        barProgressDialog.dismiss();

                        Snackbar snackbar = Snackbar.make(mSnackBarLayout,
                                R.string.snackbar_report_success, Snackbar.LENGTH_LONG);

                        TextView textView = (TextView) snackbar.getView()
                                .findViewById(android.support.design.R.id.snackbar_text);

                        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.white));

                        snackbar.show();
                    }
                });
    }

    /**
     * Starts an {@link Intent} to let the user pick a screenshot to send.
     */
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        // TODO: 19/05/16 Better handling of ActivityNotFoundException
        try {
            mActivity.startActivityForResult(intent, SELECT_PHOTO);
        } catch (ActivityNotFoundException e) {
            Utils.handleException(e);
        }
    }

    public void showPermissionRationale() {
        showPermissionRationale(mActivity);
    }

    /**
     * Requests the permission to access the external storage to getPublicKey
     * the selected image and convert it into a {@link Uri}.
     */
    public static void showPermissionRationale(Activity activity) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style.DialogStyle)
                .setTitle(R.string.snackbar_permission_denied)
                .setMessage(R.string.dialog_permission_storage_sub)
                .setPositiveButton(R.string.dialog_permission_deny, (dialog1, which) -> dialog1.dismiss())
                .setNegativeButton(R.string.dialog_permission_allow, (dialog1, which) -> ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_ACCESS_STORAGE));

        dialog.create().show();
    }

    public static boolean validatePassword(Context context, TextInputLayout layout,
                                           CharSequence password) {
        if (TextUtils.isEmpty(password)) {
            layout.setError(context.getString(R.string.register_password_empty));
            return false;
        }

        if (password.length() < 6) {
            layout.setError(context.getString(R.string.register_password_too_short));
            return false;
        }

        layout.setError(null);

        return true;
    }

    public static boolean isEmailValid(CharSequence email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static class ReportBody implements ExclusionStrategy {

        final int androidVersionCode;
        final int playServicesStatus;
        final int appVersionCode;
        final int vehicle;

        final String androidVersionName;
        final String deviceName;
        final String deviceModel;
        final String screenSize;
        final String androidId;
        final String serial;
        final String locale;
        final String appVersionName;

        final String email;
        final String message;

        boolean hasBle;
        final boolean locationPermission;
        final boolean storagePermission;

        final Map<String, ?> preferences;

        protected ReportBody(Context context, String email, String message, int vehicle) {
            this.email = email;
            this.message = message;
            this.vehicle = vehicle;

            androidVersionName = Build.VERSION.RELEASE;
            androidVersionCode = Build.VERSION.SDK_INT;

            deviceName = DeviceNames.getCurrentDeviceName("Unknown Device");
            deviceModel = Build.MODEL;

            serial =  Build.SERIAL;
            androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            screenSize = DeviceUtils.getScreenWidth(context) + "x" +
                    DeviceUtils.getScreenHeight(context);

            locale = context.getResources().getConfiguration().locale.toString();

            playServicesStatus = Utils.getPlayServicesStatus(context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                hasBle = context.getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
            }

            appVersionCode = BuildConfig.VERSION_CODE;
            appVersionName = BuildConfig.VERSION_NAME;

            locationPermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            storagePermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            preferences = PreferenceManager.getDefaultSharedPreferences(context).getAll();
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return vehicle == 0 && f.getName().equals("vehicle");
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }
}
