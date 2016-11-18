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

import java.util.List;

import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.CloudApi;
import it.sasabz.android.sasabus.data.network.rest.model.Badge;
import it.sasabz.android.sasabus.data.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.data.network.rest.response.TripUploadResponse;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Notifications;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.Scheduler;

/**
 * Utility class to help with syncing trips to the server.
 *
 * @author Alex Lardschneider
 */
public final class TripSyncHelper {

    private static final String TAG = "TripSyncHelper";

    private TripSyncHelper() {
    }

    /**
     * Attempts to upload the trips defined by {@code trips}. All the trips will be serialized
     * into a json array by using retrofit and gson.
     *
     * @param trips the trips to upload.
     * @return {@code true} if one or more trips have been uploaded, {@code false} otherwise.
     */
    public static boolean upload(Context context, List<CloudTrip> trips, Scheduler scheduler) {
        LogUtils.w(TAG, "Uploading " + trips.size() + " trips");

        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        cloudApi.uploadTrips(trips)
                .subscribeOn(scheduler)
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
                                Notifications.badge(context, badge);
                            }
                        }).start();
                    }
                });

        return true;
    }
}
