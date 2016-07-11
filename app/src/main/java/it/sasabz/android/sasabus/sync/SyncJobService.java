package it.sasabz.android.sasabus.sync;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import it.sasabz.android.sasabus.util.LogUtils;

/**
 * Job which will be scheduled by {@link android.app.job.JobScheduler}. This job will substitute
 * the old fashion alarm manager way to do data sync, as the scheduler will batch syncs together
 * and save battery this way.
 *
 * This job will only occur when an internet connection is available and the device is connected
 * to a charger. The job will run approx. every 1440 minutes.
 *
 * Remember to stop this job by calling {@link #jobFinished(JobParameters, boolean)} if it runs
 * in a separate thread.
 *
 * @author Alex Lardschneider
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SyncJobService extends JobService {

    private static final String TAG = "SyncJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        LogUtils.i(TAG, "onStartJob()");

        new SyncHelper(this, this, params).performSyncAsync();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LogUtils.i(TAG, "onStopJob()");

        return true;
    }
} 