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

package it.sasabz.android.sasabus.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.WorkerThread
import com.davale.sasabus.core.vdv.PlannedData
import it.sasabz.android.sasabus.data.network.model.CloudBeacon
import io.realm.Realm
import it.sasabz.android.sasabus.data.network.NetUtils
import it.sasabz.android.sasabus.data.network.auth.AuthHelper
import it.sasabz.android.sasabus.data.network.rest.Endpoint
import it.sasabz.android.sasabus.data.network.RestClient
import it.sasabz.android.sasabus.data.network.api.TelemetryApi
import it.sasabz.android.sasabus.data.network.rest.api.EcoPointsApi
import it.sasabz.android.sasabus.data.network.rest.api.ValidityApi
import it.sasabz.android.sasabus.data.realm.user.EarnedBadge
import it.sasabz.android.sasabus.data.realm.user.TelemetryBeacon
import it.sasabz.android.sasabus.util.Preconditions
import it.sasabz.android.sasabus.util.Settings
import it.sasabz.android.sasabus.util.Utils
import it.sasabz.android.sasabus.util.rx.NextObserver
import rx.Observer
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A helper class for dealing with data synchronization. All operations occur on the
 * thread they're called from, so it's best to wrap calls in an [android.os.AsyncTask], or
 * better yet, a [android.app.Service]. Helper started with [.performSyncAsync] will
 * be run on a worker thread instead.
 *
 * @author Alex Lardschneider
 */
class SyncHelper internal constructor(private val mContext: Context, private val mService: JobService?, private val mParams: JobParameters?) {

    private var realm: Realm? = null

    init {
        Preconditions.checkNotNull(mContext, "context == null")
    }

    constructor(context: Context) : this(context, null, null)

    /**
     * Performs the sync process asynchronously, thus not risking to block the main thread.
     * Performs the same operations as [.performSync].
     */
    fun performSyncAsync() {
        Thread(Runnable { this.performSync() }).start()
    }

    /**
     * Main method which is responsible for the data sync. Each sync operation is split into
     * individual parts, so they can be executed without affecting other operations, e.g. by
     * throwing an [Exception].
     *
     *
     * Individual operations will make network calls, so never call this method on the main thread
     * or you'll risk blocking it or crashing the app. If you want to perform the sync process
     * asynchronously, call [.performSyncAsync] which will call this method wrapped in
     * a [Thread].
     *
     * @return `true` if any data has been changed, `false` if not.
     */
    @WorkerThread
    fun performSync(): Boolean {
        Timber.e("Starting sync")

        realm = Realm.getDefaultInstance()

        if (!NetUtils.isOnline(mContext)) {
            Timber.e("Not attempting remote sync because device is OFFLINE")
            return false
        }

        if (!NetUtils.isOnline(mContext)) {
            Timber.e("Not attempting remote sync because device is OFFLINE")
            return false
        }

        var dataChanged = false

        // Sync consists of 1 or more of these operations. We try them one by one and tolerate
        // individual failures on each.
        val OP_BEACON_SYNC = 4
        val OP_BADGE_SYNC = 5
        val OP_PLAN_DATA_SYNC = 2

        // Only sync trips and badges if the user is logged in, as that requires the
        // authentication header with the JWT.
        val opsToPerform = if (AuthHelper.isLoggedIn())
            intArrayOf(OP_BADGE_SYNC, OP_PLAN_DATA_SYNC, OP_BEACON_SYNC)
        else
            intArrayOf(OP_PLAN_DATA_SYNC, OP_BEACON_SYNC)


        for (op in opsToPerform) {
            try {
                when (op) {
                    OP_PLAN_DATA_SYNC -> dataChanged = dataChanged or doPlanDataSync()
                    OP_BADGE_SYNC -> dataChanged = dataChanged or doBadgeSync()
                    OP_BEACON_SYNC -> doBeaconSync()
                    else -> throw IllegalStateException("Unknown operation " + op)
                }
            } catch (throwable: Throwable) {
                Utils.logException(throwable)

                Timber.e("Error performing remote sync")
            }

        }

        Timber.e("End of sync (%s)", if (dataChanged) "data changed" else "no data changed")

        realm!!.close()

        if (mService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mService.jobFinished(mParams, false)
        }

        return dataChanged
    }

    /**
     * Uploads telemetry data about beacons
     */
    private fun doBeaconSync() {
        Timber.e("Starting beacon sync")

        val realm = Realm.getDefaultInstance();
        val beacons = realm.where(TelemetryBeacon::class.java).findAll()

        if (beacons.isEmpty()) {
            Timber.e("No beacons to upload")
            return
        }

        val size = beacons.size

        Timber.e("Uploading %d beacons", size)

        val retrofit = RestClient.getTelemetryAdapter(mContext)
        val telemetryApi = retrofit.create(TelemetryApi::class.java)

        val cloudBeacons = beacons
                .map(::CloudBeacon)
                .toMutableList()

        telemetryApi.sendBeacon(cloudBeacons)
                .subscribe(object : NextObserver<Void?>() {
                    override fun onNext(aVoid: Void?) {
                        Timber.e("Uploaded %d beacons", size)

                        realm.beginTransaction()
                        beacons.deleteAllFromRealm()
                        realm.commitTransaction()
                    }
                })

        realm.close()
    }

    /**
     * Tells the server that the user has earnt a badge.
     *
     * @return `true` if one or more badges have been sent, `false` otherwise.
     */
    private fun doBadgeSync(): Boolean {
        Timber.e("Starting badge sync")

        val result = realm!!.where(EarnedBadge::class.java)
                .equalTo("sent", false).findAll()

        if (result.isEmpty()) {
            Timber.e("No badges to upload")
            return false
        }

        val size = result.size

        Timber.e("Uploading %s badges", size)

        val dataChanged = booleanArrayOf(false)

        for (badge in result) {
            val api = RestClient.ADAPTER!!.create(EcoPointsApi::class.java)
            api.sendBadge(badge.id)
                    .subscribe { aVoid ->
                        realm!!.beginTransaction()
                        badge.isSent = true
                        realm!!.commitTransaction()

                        dataChanged[0] = dataChanged[0] or true
                    }
        }

        Timber.e("Uploaded %s badges", size)

        return dataChanged[0]
    }

    /**
     * Checks if the plan data exists and is valid. If it does not exist or is not valid,
     * attempt to download it.
     *
     * @return `true` if the plan data download attempt has been made (does not mean that
     * it was successful), `false` if the data is up to date.
     * @throws IOException if there is an error checking for a plan data update.
     */
    @Throws(IOException::class)
    private fun doPlanDataSync(): Boolean {
        Timber.e("Starting plan data sync")

        PlannedData.checkIfDataValid(mContext, Endpoint.API_VALIDITY, onUpdate = {
            Timber.e("Downloading plan data")

            PlannedData.download(mContext, Endpoint.API)
                    .subscribe(object : Observer<Int> {
                        override fun onCompleted() {

                        }

                        override fun onError(e: Throwable) {
                            Utils.logException(e)
                        }

                        override fun onNext(aVoid: Int?) {
                            Timber.e("Downloaded plan data")
                        }
                    })
        })

        return true
    }

    companion object {

        private val SYNC_INTERVAL_DAYS = 1

        private val SYNC_ALARM_ID = 1 shl 17

        /**
         * Schedules a sync by using [AlarmManager]. The sync will run at night where most
         * people leave their phones plugged in and the phone is on idle.
         *
         *
         * Sync will run between `01:00` and {05:00} to prevent overloading the server.
         * The time will be determined by [java.util.Random.next].
         *
         *
         * As it is better to use the [JobScheduler] to perform sync on post Lollipop devices,
         * using [JobScheduler] will be preferred over using the standard [AlarmManager].
         *
         * @param context Context to access [AlarmManager].
         */
        fun scheduleSync(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

                val builder = JobInfo.Builder(1,
                        ComponentName(context.packageName, SyncJobService::class.java.name))

                builder.setPeriodic(TimeUnit.DAYS.toMillis(SYNC_INTERVAL_DAYS.toLong()))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setRequiresCharging(true)
                        .setRequiresDeviceIdle(true)

                val code = jobScheduler.schedule(builder.build())
                if (code <= 0) {
                    Timber.e("Could not scheduled job: %s", code)
                }
            } else {
                val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val intent = Intent(context, SyncHelper::class.java)

                val pendingIntent = PendingIntent.getService(context, SYNC_ALARM_ID,
                        intent, PendingIntent.FLAG_CANCEL_CURRENT)

                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, Random().nextInt(4) + 1)
                calendar.set(Calendar.MINUTE, 0)

                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                        TimeUnit.DAYS.toMillis(SYNC_INTERVAL_DAYS.toLong()), pendingIntent)

                Timber.w("Sync will run at %s", calendar.time)
            }
        }
    }
}
