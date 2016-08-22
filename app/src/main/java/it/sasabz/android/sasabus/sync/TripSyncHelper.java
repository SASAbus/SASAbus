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

package it.sasabz.android.sasabus.sync;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.CloudApi;
import it.sasabz.android.sasabus.network.rest.model.CloudPlannedTrip;
import it.sasabz.android.sasabus.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.network.rest.response.CloudResponsePost;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.realm.user.PlannedTrip;
import it.sasabz.android.sasabus.realm.user.TripToDelete;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Utility class to help with syncing trips to the server.
 *
 * @author Alex Lardschneider
 */
final class TripSyncHelper {

    private static final String TAG = "TripSyncHelper";

    private TripSyncHelper() {
    }


    // ======================================= TRIPS ===============================================

    /**
     * Attempts to download the trips defined by {@code trips}.
     *
     * @param trips the trips to download. Each trip will be requested from the server by its id.
     * @return {@code true} if one or more trips have been downloaded, {@code false} otherwise.
     * @throws IOException if downloading the trips failed.
     */
    public static boolean download(List<String> trips) throws IOException {
        LogUtils.w(TAG, "Downloading " + trips.size() + " trips");

        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        Response<CloudResponsePost> response = cloudApi.downloadTrips(trips).execute();

        if (response.body() != null) {
            LogUtils.w(TAG, "Download: " + response.body());

            Collection<CloudTrip> cloudTrips = response.body().trips;

            for (CloudTrip cloudTrip : cloudTrips) {
                UserRealmHelper.insertTrip(cloudTrip);
            }

            if (cloudTrips.size() != trips.size()) {
                LogUtils.e(TAG, "Downloaded " + cloudTrips.size() + " trips, " +
                        "should have been " + trips.size());
            } else {
                LogUtils.w(TAG, "Downloaded " + cloudTrips.size() + " trips");
            }
        } else {
            ResponseBody body = response.errorBody();
            LogUtils.e(TAG, "Error while downloading trips: " + (body != null ? body.string() : null));

            return false;
        }

        return true;
    }

    /**
     * Attempts to upload the trips defined by {@code trips}. All the trips will be serialized
     * into a json array by using retrofit and gson.
     *
     * @param trips the trips to upload.
     * @return {@code true} if one or more trips have been uploaded, {@code false} otherwise.
     * @throws IOException if downloading the trips failed.
     */
    static boolean upload(List<CloudTrip> trips) throws IOException {
        LogUtils.w(TAG, "Uploading " + trips.size() + " trips");

        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        Response<Void> response = cloudApi.uploadTrips(trips).execute();

        if (response.body() == null) {
            ResponseBody body = response.errorBody();
            LogUtils.e(TAG, "Error while uploading trips: " + (body != null ? body.string() : null));

            return false;
        }

        return true;
    }

    /**
     * Deletes a trip from the cloud. This method it used to remove a trip from the cloud
     * which could not have been deleted on the cloud when the user deleted if from the app,
     * usually because of a connection error.
     *
     * @param hash the hash of the trip to delete
     * @return {@code true} if the trip has been deleted, {@code false} otherwise.
     * @throws IOException if removing the trip failed.
     */
    static boolean delete(String hash) throws IOException {
        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        Response<Void> response = cloudApi.deleteTrip(hash).execute();

        // Sending the request to delete the trip succeeded so we can remove the entry
        // from the database.
        if (response.isSuccessful()) {
            LogUtils.w(TAG, "Removed trip " + hash);

            Realm realm = Realm.getDefaultInstance();

            realm.beginTransaction();
            realm.where(TripToDelete.class)
                    .equalTo("type", TripToDelete.TYPE_TRIP)
                    .equalTo("hash", hash)
                    .findFirst().deleteFromRealm();
            realm.commitTransaction();

            return true;
        } else {
            LogUtils.e(TAG, "Error removing trip " + hash);
            return false;
        }
    }


    // =================================== PLANNED TRIPS ===========================================

    /**
     * Attempts to download the planned trips defined by {@code trips}.
     *
     * @param trips the planned trips to download. Each trip will be requested from the
     *              server by its id.
     * @return {@code true} if one or more planned trips have been downloaded,
     * {@code false} otherwise.
     * @throws IOException if downloading the planned trips failed.
     */
    static boolean downloadPlanned(List<String> trips) throws IOException {
        LogUtils.w(TAG, "Downloading " + trips.size() + " planned trips");

        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        Response<CloudResponsePost> response = cloudApi.downloadPlannedTrips(trips).execute();

        if (response.body() != null) {
            LogUtils.w(TAG, "Download: " + response.body());

            Realm realm = Realm.getDefaultInstance();

            Collection<CloudPlannedTrip> plannedTrips = response.body().plannedTrips;

            for (CloudPlannedTrip trip : plannedTrips) {
                realm.beginTransaction();

                PlannedTrip plannedTrip = realm.createObject(PlannedTrip.class);
                plannedTrip.setHash(trip.getHash());
                plannedTrip.setTitle(trip.getTitle());
                plannedTrip.setBusStop(trip.getBusStop());
                plannedTrip.setTimeStamp(trip.getTimeStamp());
                plannedTrip.setLines(Utils.listToString(trip.getLines(), ","));
                plannedTrip.setNotifications(Utils.listToString(trip.getNotifications(), ","));
                plannedTrip.setRepeatDays(trip.getRepeatDays());
                plannedTrip.setRepeatWeeks(trip.getRepeatWeeks());

                realm.commitTransaction();
            }

            realm.close();

            if (plannedTrips.size() != trips.size()) {
                LogUtils.e(TAG, "Downloaded " + plannedTrips.size() + " planned trips, " +
                        "should have been " + trips.size());
            } else {
                LogUtils.w(TAG, "Downloaded " + plannedTrips.size() + " planned trips");
            }
        } else {
            ResponseBody body = response.errorBody();
            LogUtils.e(TAG, "Error while downloading planned trips: " +
                    (body != null ? body.string() : null));

            return false;
        }

        return true;
    }

    /**
     * Attempts to upload the planned trips defined by {@code trips}.
     * All the planned trips will be serialized into a json array by using retrofit and gson.
     *
     * @param trips the trips to upload.
     * @return {@code true} if one or more planned trips have been uploaded,
     * {@code false} otherwise.
     * @throws IOException if downloading the planned trips failed.
     */
    static boolean uploadPlanned(List<CloudPlannedTrip> trips) throws IOException {
        LogUtils.w(TAG, "Uploading " + trips.size() + " planned trips");

        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        Response<Void> response = cloudApi.uploadPlannedTrips(trips).execute();

        if (response.body() == null) {
            ResponseBody body = response.errorBody();
            LogUtils.e(TAG, "Error while uploading planned trips: " +
                    (body != null ? body.string() : null));

            return false;
        }

        return true;
    }

    /**
     * Deletes a planned trip from the cloud. This method it used to remove a planned trip from
     * the cloud which could not have been deleted on the cloud when the user deleted if
     * from the app, usually because of a connection error.
     *
     * @param hash the hash of the planned trip to delete
     * @return {@code true} if the planned trip has been deleted, {@code false} otherwise.
     * @throws IOException if removing the planned trip failed.
     */
    static boolean deletePlanned(String hash) throws IOException {
        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        Response<Void> response = cloudApi.deletePlannedTrip(hash).execute();

        // Sending the request to delete the planned trip succeeded so we can remove
        // the entry from the database.
        if (response.isSuccessful()) {
            LogUtils.w(TAG, "Removed planned trip " + hash);

            Realm realm = Realm.getDefaultInstance();

            realm.beginTransaction();
            realm.where(TripToDelete.class)
                    .equalTo("type", TripToDelete.TYPE_PLANNED_TRIP)
                    .equalTo("hash", hash)
                    .findFirst().deleteFromRealm();
            realm.commitTransaction();

            return true;
        } else {
            LogUtils.e(TAG, "Error removing planned trip " + hash);
            return false;
        }
    }
}
