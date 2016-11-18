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
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import it.sasabz.android.sasabus.AppApplication;
import timber.log.Timber;

public final class MapUtils {

    private MapUtils() {
    }


    // ======================================== VARIOUS ============================================

    public static Location getLastKnownLocation(Activity activity) {
        if (!DeviceUtils.hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Timber.e("Missing location permission");
            return null;
        }

        GoogleApiClient client = ((AppApplication) activity.getApplication()).getGoogleApiClient();
        if (!client.isConnected()) {
            Timber.e("Client not yet connected, returning null");
            return null;
        }

        //noinspection MissingPermission
        return LocationServices.FusedLocationApi.getLastLocation(client);
    }

    public static float distance(double lat1, double lon1, double lat2, double lon2) {
        Location l1 = new Location("");
        l1.setLatitude(lat1);
        l1.setLongitude(lon1);

        Location l2 = new Location("");
        l2.setLatitude(lat2);
        l2.setLongitude(lon2);

        return l1.distanceTo(l2);
    }
}
