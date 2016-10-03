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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.BeaconsApi;
import it.sasabz.android.sasabus.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.network.rest.api.ValidityApi;
import it.sasabz.android.sasabus.network.rest.model.ScannedBeacon;
import it.sasabz.android.sasabus.network.rest.response.ValidityResponse;
import it.sasabz.android.sasabus.provider.PlanData;
import it.sasabz.android.sasabus.realm.user.Beacon;
import it.sasabz.android.sasabus.realm.user.EarnedBadge;
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

        if (!NetUtils.isOnline(mContext)) {
            LogUtils.e(TAG, "Not attempting remote sync because device is OFFLINE");
            return false;
        }

        boolean dataChanged = false;

        // Sync consists of 1 or more of these operations. We try them one by one and tolerate
        // individual failures on each.
        final int OP_BEACON_SYNC = 4;
        final int OP_BADGE_SYNC = 5;
        final int OP_PLAN_DATA_SYNC = 2;

        // Only sync trips and badges if the user is logged in, as that requires the
        // authentication header with the JWT.
        int[] opsToPerform = AuthHelper.isLoggedIn() ? new int[]{
                OP_BADGE_SYNC,
                OP_PLAN_DATA_SYNC,
                OP_BEACON_SYNC
        } : new int[]{
                OP_PLAN_DATA_SYNC,
                OP_BEACON_SYNC
        };


        for (int op : opsToPerform) {
            try {
                switch (op) {
                    case OP_PLAN_DATA_SYNC:
                        dataChanged |= doPlanDataSync();
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

            PlanData.download(mContext)
                    .subscribe(new Observer<Void>() {
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
}
