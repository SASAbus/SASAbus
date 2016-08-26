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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.WorkerThread;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.beacon.survey.SurveyActivity;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.BeaconsApi;
import it.sasabz.android.sasabus.network.rest.api.CloudApi;
import it.sasabz.android.sasabus.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.network.rest.api.SurveyApi;
import it.sasabz.android.sasabus.network.rest.api.ValidityApi;
import it.sasabz.android.sasabus.network.rest.model.CloudPlannedTrip;
import it.sasabz.android.sasabus.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.network.rest.model.ScannedBeacon;
import it.sasabz.android.sasabus.network.rest.response.CloudResponseGet;
import it.sasabz.android.sasabus.network.rest.response.ValidityResponse;
import it.sasabz.android.sasabus.provider.PlanData;
import it.sasabz.android.sasabus.realm.user.Beacon;
import it.sasabz.android.sasabus.realm.user.EarnedBadge;
import it.sasabz.android.sasabus.realm.user.PlannedTrip;
import it.sasabz.android.sasabus.realm.user.Survey;
import it.sasabz.android.sasabus.realm.user.Trip;
import it.sasabz.android.sasabus.realm.user.TripToDelete;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observer;

/**
 * A helper class for dealing with data synchronization. All operations occur on the
 * thread they're called from, so it's best to wrap calls in an {@link android.os.AsyncTask}, or
 * better yet, a {@link android.app.Service}. Helper started with {@link #performSyncAsync()} will
 * be run on a worker thread instead.
 *
 * @author Alex Lardschneider
 */
public class SyncHelper {

    private static final int SYNC_INTERVAL_DAYS = 1;

    private static final String TAG = "SyncHelper";

    private final Context mContext;
    private final JobService mService;
    private final JobParameters mParams;

    private Realm realm;

    SyncHelper(Context context, JobService service, JobParameters parameters) {
        Preconditions.checkNotNull(context, "context == null");

        mContext = context;
        mService = service;
        mParams = parameters;
    }

    public SyncHelper(Context context) {
        this(context, null, null);
    }

    /**
     * Performs the sync process asynchronously, thus not risking to block the main thread.
     * Performs the same operations as {@link #performSync()}.
     */
    public void performSyncAsync() {
        new Thread(this::performSync).start();
    }

    /**
     * Main method which is responsible for the data sync. Each sync operation is split into
     * individual parts, so they can be executed without affecting other operations, e.g. by
     * throwing an {@link Exception}.
     * <p>
     * Individual operations will make network calls, so never call this method on the main thread
     * or you'll risk blocking it or crashing the app. If you want to perform the sync process
     * asynchronously, call {@link #performSyncAsync()} which will call this method wrapped in
     * a {@link Thread}.
     *
     * @return {@code true} if any data has been changed, {@code false} if not.
     */
    @WorkerThread
    boolean performSync() {
        LogUtils.e(TAG, "Starting sync");

        realm = Realm.getDefaultInstance();

        if (!NetUtils.isOnline(mContext)) {
            LogUtils.e(TAG, "Not attempting remote sync because device is OFFLINE");
            return false;
        }

        boolean dataChanged = false;

        // Sync consists of 1 or more of these operations. We try them one by one and tolerate
        // individual failures on each.
        final int OP_TRIP_DATA_SYNC = 0;
        final int OP_PLANNED_TRIP_DATA_SYNC = 1;
        final int OP_PLAN_DATA_SYNC = 2;
        final int OP_SURVEY_SYNC = 3;
        final int OP_BEACON_SYNC = 4;
        final int OP_BADGE_SYNC = 5;

        int[] opsToPerform = {
                OP_TRIP_DATA_SYNC,
                OP_PLANNED_TRIP_DATA_SYNC,
                OP_PLAN_DATA_SYNC,
                OP_SURVEY_SYNC,
                OP_BADGE_SYNC,
        };

        for (int op : opsToPerform) {
            try {
                switch (op) {
                    case OP_TRIP_DATA_SYNC:
                        dataChanged |= doTripSync();
                        break;
                    case OP_PLANNED_TRIP_DATA_SYNC:
                        dataChanged |= doPlannedTripSync();
                        break;
                    case OP_PLAN_DATA_SYNC:
                        dataChanged |= doPlanDataSync();
                        break;
                    case OP_SURVEY_SYNC:
                        dataChanged |= doSurveySync();
                        break;
                    case OP_BEACON_SYNC:
                        dataChanged |= doBeaconSync();
                        break;
                    case OP_BADGE_SYNC:
                        dataChanged |= doBadgeSync();
                        break;
                    default:
                        throw new IllegalStateException("Unknown operation " + op);
                }
            } catch (Throwable throwable) {
                Utils.logException(throwable);

                LogUtils.e(TAG, "Error performing remote sync");
            }
        }

        LogUtils.e(TAG, "End of sync (" + (dataChanged ? "data changed" : "no data changed") + ')');

        realm.close();

        if (mService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mService.jobFinished(mParams, false);
        }

        return dataChanged;
    }

    /**
     * Syncs trips to the server. The sync process is split into two parts: upload and
     * download. All the trips which are not on the server will be uploaded.
     * All the trips which are on the server but not saved locally will be downloaded.
     *
     * @return {@code true} if at least one trip was downloaded or uploaded, {@code false}
     * otherwise.
     * @throws IOException if contacting the server failed.
     */
    private boolean doTripSync() throws Exception {
        LogUtils.e(TAG, "Starting trip sync");

        RealmResults<Trip> trips = realm.where(Trip.class).findAll();

        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        Response<CloudResponseGet> response = cloudApi.compareTrips().execute();

        boolean dataChanged = false;

        if (response.body() != null) {
            List<Trip> serverMissing = new ArrayList<>(trips);
            List<String> clientMissing = new ArrayList<>();
            List<String> onServer = response.body().hashes;

            for (String uuid : onServer) {
                if (!containsTrip(trips, uuid)) {
                    clientMissing.add(uuid);
                }
            }

            for (int i = serverMissing.size() - 1; i >= 0; i--) {
                Trip trip = serverMissing.get(i);
                if (onServer.contains(trip.getHash())) {
                    serverMissing.remove(trip);
                }
            }

            LogUtils.d(TAG, "clientMissing: " + Arrays.toString(clientMissing.toArray()));
            LogUtils.d(TAG, "serverMissing: " + Arrays.toString(serverMissing.toArray()));

            if (!clientMissing.isEmpty()) {
                dataChanged = TripSyncHelper.download(clientMissing);
            }

            if (!serverMissing.isEmpty()) {
                dataChanged |= TripSyncHelper.upload(mContext, tripToCloudTrip(serverMissing));
            }

            LogUtils.e(TAG, "Finished trip sync");
        } else {
            LogUtils.e(TAG, "Error downloading trips: " + response.errorBody().string());
        }

        // Delete trips which might not have been deleted on the server
        RealmResults<TripToDelete> tripsToDelete = realm.where(TripToDelete.class)
                .equalTo("type", TripToDelete.TYPE_TRIP).findAll();

        for (TripToDelete tripToDelete : tripsToDelete) {
            dataChanged |= TripSyncHelper.delete(tripToDelete.getHash());
        }

        return dataChanged;
    }

    /**
     * Syncs planned trips to the server. The sync process is split into two parts: upload and
     * download. All the planned trips which are not on the server will be uploaded.
     * All the planned trips which are on the server but not saved locally will be downloaded.
     *
     * @return {@code true} if at least one planned trip was downloaded or uploaded, {@code false}
     * otherwise.
     * @throws IOException if contacting the server failed.
     */
    private boolean doPlannedTripSync() throws IOException {
        LogUtils.e(TAG, "Starting planned trip sync");

        RealmResults<PlannedTrip> trips = realm.where(PlannedTrip.class).findAll();

        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        Response<CloudResponseGet> response = cloudApi.comparePlannedTrips().execute();

        boolean dataChanged = false;

        if (response.body() != null) {
            List<PlannedTrip> serverMissing = new ArrayList<>(trips);
            List<String> clientMissing = new ArrayList<>();
            List<String> onServer = response.body().plannedHashes;

            // Check which trips are missing on the app
            for (String uuid : onServer) {
                if (!containsPTrip(trips, uuid)) {
                    clientMissing.add(uuid);
                }
            }

            // Check which trips are missing on the server
            for (int i = serverMissing.size() - 1; i >= 0; i--) {
                PlannedTrip trip = serverMissing.get(i);
                if (onServer.contains(trip.getHash())) {
                    serverMissing.remove(trip);
                }
            }

            LogUtils.d(TAG, "clientMissing: " + Arrays.toString(clientMissing.toArray()));
            LogUtils.d(TAG, "serverMissing: " + Arrays.toString(serverMissing.toArray()));

            if (!clientMissing.isEmpty()) {
                dataChanged = TripSyncHelper.downloadPlanned(clientMissing);
            }

            if (!serverMissing.isEmpty()) {
                dataChanged |= TripSyncHelper.uploadPlanned(tripToCloudPTrip(serverMissing));
            }

            LogUtils.e(TAG, "Finished planned trip sync");
        } else {
            LogUtils.e(TAG, "Error downloading planned trips: " + response.errorBody().string());
        }

        // Delete planned trips which might not have been deleted on the server
        RealmResults<TripToDelete> tripsToDelete = realm.where(TripToDelete.class)
                .equalTo("type", TripToDelete.TYPE_PLANNED_TRIP).findAll();

        for (TripToDelete tripToDelete : tripsToDelete) {
            dataChanged |= TripSyncHelper.deletePlanned(tripToDelete.getHash());
        }

        return dataChanged;
    }

    /**
     * Checks if the plan data exists and is valid. If it does not exist or is not valid,
     * attempt to download it.
     *
     * @return {@code true} if the plan data download attempt has been made (does not mean that
     * it was successful), {@code false} if the data is up to date.
     * @throws IOException if there is an error checking for a plan data update.
     */
    private boolean doPlanDataSync() throws IOException {
        LogUtils.e(TAG, "Starting plan data sync");

        boolean shouldDownloadData = false;

        // Check if plan data exists. If not, we should immediately download it, else check if an
        // update is available and download it.
        if (!PlanData.planDataExists(mContext)) {
            LogUtils.e(TAG, "Plan data does not exist");

            shouldDownloadData = true;
        } else {
            String date = SettingsUtils.getDataDate(mContext);

            ValidityApi validityApi = RestClient.ADAPTER.create(ValidityApi.class);
            Response<ValidityResponse> response = validityApi.data(date).execute();

            if (response.body() != null) {
                if (!response.body().isValid) {
                    LogUtils.e(TAG, "Plan data update available");

                    SettingsUtils.markDataUpdateAvailable(mContext, true);

                    shouldDownloadData = true;
                } else {
                    LogUtils.e(TAG, "No plan data update available");
                }
            } else {
                ResponseBody body = response.errorBody();
                LogUtils.e(TAG, "Error while checking for plan data update: " + body.string());
            }
        }

        // Plan data does not exist or an update is available, download it now.
        if (shouldDownloadData) {
            LogUtils.e(TAG, "Downloading plan data");

            PlanData.downloadPlanData(mContext).subscribe(new Observer<Void>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Utils.logException(e);
                }

                @Override
                public void onNext(Void aVoid) {
                    LogUtils.e(TAG, "Downloaded plan data");
                }
            });
        }

        return shouldDownloadData;
    }

    /**
     * Uploads all batched surveys, those being surveys which could not be sent at the time
     * the user took the surveys.
     *
     * @return {@code true} if there were one or more surveys to upload, {@code false} otherwise.
     */
    private boolean doSurveySync() {
        LogUtils.e(TAG, "Starting survey sync");

        RealmResults<Survey> surveys = realm.where(Survey.class).findAll();

        if (surveys.isEmpty()) {
            LogUtils.e(TAG, "No surveys to upload");
            return false;
        }

        Gson gson = new Gson();

        LogUtils.e(TAG, "Uploading " + surveys.size() + " surveys");

        boolean[] dataChanged = {false};
        int[] surveyCount = {0};

        for (Survey survey : surveys) {
            SurveyApi surveyApi = RestClient.ADAPTER.create(SurveyApi.class);
            surveyApi.send(gson.fromJson(survey.getData(), SurveyActivity.ReportBody.class))
                    .subscribe(aVoid -> {
                        realm.beginTransaction();
                        survey.deleteFromRealm();
                        realm.commitTransaction();

                        dataChanged[0] |= true;
                        surveyCount[0]++;
                    });
        }

        LogUtils.e(TAG, "Uploaded " + surveyCount[0] + " surveys");

        return dataChanged[0];
    }

    /**
     * Syncs all the tracked beacons. If a user is near a bus or bus stop beacon, it automatically
     * gets inserted into the database. On app sync, the beacon data like UUID, major and minor
     * get sent to the server which then can be used to perform statistics.
     *
     * @return {@code true} if one or more beacons have been uploaded, {@code false} otherwise.
     */
    private boolean doBeaconSync() {
        LogUtils.e(TAG, "Starting beacon sync");

        RealmResults<Beacon> result = realm.where(Beacon.class).findAll();

        if (result.isEmpty()) {
            LogUtils.e(TAG, "No beacons to upload");
            return false;
        }

        int size = result.size();

        LogUtils.e(TAG, "Uploading " + size + " beacons");

        boolean[] dataChanged = {false};

        List<ScannedBeacon> beacons = new ArrayList<>();
        for (Beacon beacon : result) {
            ScannedBeacon scannedBeacon = new ScannedBeacon();

            scannedBeacon.type = beacon.getType();
            scannedBeacon.major = beacon.getMajor();
            scannedBeacon.minor = beacon.getMinor();
            scannedBeacon.timestamp = beacon.getTimeStamp();

            beacons.add(scannedBeacon);
        }

        BeaconsApi beaconsApi = RestClient.ADAPTER.create(BeaconsApi.class);
        beaconsApi.send(beacons)
                .subscribe(aVoid -> {
                    realm.beginTransaction();
                    result.deleteAllFromRealm();
                    realm.commitTransaction();

                    dataChanged[0] |= true;
                });

        LogUtils.e(TAG, "Uploaded " + size + " beacons");

        return dataChanged[0];
    }

    /**
     * Tells the server that the user has earnt a badge.
     *
     * @return {@code true} if one or more badges have been sent, {@code false} otherwise.
     */
    private boolean doBadgeSync() {
        LogUtils.e(TAG, "Starting badge sync");

        RealmResults<EarnedBadge> result = realm.where(EarnedBadge.class)
                .equalTo("sent", false).findAll();

        if (result.isEmpty()) {
            LogUtils.e(TAG, "No badges to upload");
            return false;
        }

        int size = result.size();

        LogUtils.e(TAG, "Uploading " + size + " badges");

        boolean[] dataChanged = {false};

        for (EarnedBadge badge : result) {
            EcoPointsApi api = RestClient.ADAPTER.create(EcoPointsApi.class);
            api.sendBadge(badge.getId())
                    .subscribe(aVoid -> {
                        realm.beginTransaction();
                        badge.setSent(true);
                        realm.commitTransaction();

                        dataChanged[0] |= true;
                    });
        }

        LogUtils.e(TAG, "Uploaded " + size + " badges");

        return dataChanged[0];
    }

    /**
     * Schedules a sync by using {@link AlarmManager}. The sync will run at night where most
     * people leave their phones plugged in and the phone is on idle.
     * <p>
     * Sync will run between {@code 01:00} and {05:00} to prevent overloading the server.
     * The time will be determined by {@link java.util.Random#next(int)}.
     * <p>
     * As it is better to use the {@link JobScheduler} to perform sync on post Lollipop devices,
     * using {@link JobScheduler} will be preferred over using the standard {@link AlarmManager}.
     *
     * @param context Context to access {@link AlarmManager}.
     */
    public static void scheduleSync(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler)
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            JobInfo.Builder builder = new JobInfo.Builder(1,
                    new ComponentName(context.getPackageName(), SyncJobService.class.getName()));

            builder.setPeriodic(TimeUnit.DAYS.toMillis(SYNC_INTERVAL_DAYS))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setRequiresCharging(true)
                    .setRequiresDeviceIdle(true);

            int code = jobScheduler.schedule(builder.build());
            if (code <= 0) {
                LogUtils.e(TAG, "Could not scheduled job: " + code);
            }
        } else {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, SyncHelper.class);

            PendingIntent pendingIntent = PendingIntent.getService(context, Config.SYNC_ALARM_ID,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, new Random().nextInt(4) + 1);
            calendar.set(Calendar.MINUTE, 0);

            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    TimeUnit.DAYS.toMillis(SYNC_INTERVAL_DAYS), pendingIntent);

            LogUtils.w(TAG, "Sync will run at " + calendar.getTime());
        }
    }

    /**
     * Check if a given {@link List} contains a {@link Trip} with a given {@link Trip#hash}.
     *
     * @param trips all the trips
     * @param hash  the hash to search for
     * @return a boolean value indicating whether the list containsTrip the uuid.
     */
    private static boolean containsTrip(Iterable<Trip> trips, String hash) {
        for (Trip trip : trips) {
            if (trip.getHash().equals(hash)) return true;
        }

        return false;
    }

    /**
     * Check if a given {@link List} contains a {@link PlannedTrip} with a given
     * {@link PlannedTrip#hash}.
     *
     * @param trips all the trips
     * @param hash  the hash to search for
     * @return a boolean value indicating whether the list containsTrip the uuid.
     */
    private static boolean containsPTrip(Iterable<PlannedTrip> trips, String hash) {
        for (PlannedTrip trip : trips) {
            if (trip.getHash().equals(hash)) return true;
        }

        return false;
    }

    /**
     * Converts a {@link Iterable} containing {@link Trip trips} to a {@link Iterable} containing
     * {@link CloudTrip cloud trips}.
     *
     * @param trips the trips to convert.
     * @return a {@link Iterable} containing the converted trips.
     */
    private List<CloudTrip> tripToCloudTrip(Iterable<Trip> trips) {
        List<CloudTrip> list = new ArrayList<>();

        for (Trip trip : trips) {
            list.add(new CloudTrip(trip));
        }

        return list;
    }

    /**
     * Converts a {@link Iterable} containing {@link PlannedTrip planned trips} to a
     * {@link Iterable} containing {@link CloudPlannedTrip cloud planned trips}.
     *
     * @param trips the trips to convert.
     * @return a {@link Iterable} containing the converted trips.
     */
    private List<CloudPlannedTrip> tripToCloudPTrip(Iterable<PlannedTrip> trips) {
        List<CloudPlannedTrip> list = new ArrayList<>();

        for (PlannedTrip trip : trips) {
            list.add(new CloudPlannedTrip(trip));
        }

        return list;
    }
}
