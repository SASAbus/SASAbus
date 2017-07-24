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

package it.sasabz.android.sasabus.data.network.rest.api;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.davale.sasabus.core.util.DeviceUtils;

import java.util.Map;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.data.model.JsonSerializable;
import it.sasabz.android.sasabus.data.network.rest.Endpoint;
import it.sasabz.android.sasabus.util.Utils;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;
import tslamic.github.io.adn.DeviceNames;

public interface ReportApi {

    String TYPE_DEFAULT = "default";
    String TYPE_BUS = "bus";

    @Multipart
    @POST(Endpoint.REPORT)
    Observable<Void> send(@Path("type") String type,
                          @Part("body") ReportBody body,
                          @Part("image") RequestBody image
    );

    @Multipart
    @POST(Endpoint.REPORT)
    Observable<Void> sendNoImage(@Path("type") String type, @Part("body") ReportBody body);

    class ReportBody implements JsonSerializable {

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

        public ReportBody(Context context, String email, String message, int vehicle) {
            this.email = email;
            this.message = message;
            this.vehicle = vehicle;

            androidVersionName = Build.VERSION.RELEASE;
            androidVersionCode = Build.VERSION.SDK_INT;

            deviceName = DeviceNames.getCurrentDeviceName("Unknown Device");
            deviceModel = Build.MODEL;

            serial = Build.SERIAL;
            androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            screenSize = DeviceUtils.getScreenWidth(context) + "x" +
                    DeviceUtils.getScreenHeight(context);

            locale = Utils.locale(context);

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
    }
}