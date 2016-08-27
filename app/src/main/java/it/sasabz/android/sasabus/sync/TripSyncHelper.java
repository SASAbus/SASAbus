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

import android.content.Context;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.CloudApi;
import it.sasabz.android.sasabus.network.rest.model.Badge;
import it.sasabz.android.sasabus.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.network.rest.response.CloudResponsePost;
import it.sasabz.android.sasabus.network.rest.response.TripUploadResponse;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.realm.user.TripToDelete;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;
import it.sasabz.android.sasabus.util.Utils;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observer;

/**
 * Utility class to help with syncing trips to the server.
 *
 * @author Alex Lardschneider
 */
final class TripSyncHelper {

    private static final String TAG = "TripSyncHelper";

    private TripSyncHelper() {
    }

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

        if (response.isSuccessful()) {
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
            LogUtils.e(TAG, response.code() + " error while downloading trips: " +
                    (body != null ? body.string() : null));

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
     */
    static boolean upload(Context context, List<CloudTrip> trips) {
        LogUtils.w(TAG, "Uploading " + trips.size() + " trips");

        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        cloudApi.uploadTrips(trips)
                .subscribe(new Observer<TripUploadResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);
                    }

                    @Override
                    public void onNext(TripUploadResponse response) {
                        LogUtils.e(TAG, "Got " + response.badges.size() + " new badges to display");

                        new Thread(() -> {
                            for (Badge badge : response.badges) {
                                NotificationUtils.badge(context, badge);
                            }
                        }).start();
                    }
                });

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
}
