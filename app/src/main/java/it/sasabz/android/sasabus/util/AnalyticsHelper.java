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

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import it.sasabz.android.sasabus.BuildConfig;

/**
 * Centralized Analytics interface to ensure proper initialization and
 * consistent analytics application across the app.
 *
 * @author Alex Lardschneider
 */
public final class AnalyticsHelper {

    private static final String TAG = "AnalyticsHelper";

    /**
     * The tracking id used to differentiate this project from other ones
     */
    private static final String GA_TRACKING_ID = "UA-61528546-7";

    /**
     * Context to access the analytics tracker
     */
    @SuppressLint("StaticFieldLeak")
    private static Context sAppContext;

    /**
     * Analytics tracker which handles sending of events and screen views.
     */
    private static Tracker mTracker;

    private AnalyticsHelper() {
    }

    /**
     * Log a specific screen view under the {@code screenName} string.
     */
    public static void sendScreenView(String screenName) {
        if (!BuildConfig.DEBUG && isInitialized()) {
            mTracker.setScreenName(screenName);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());

            LogUtils.e(TAG, "Screen View recorded: " + screenName);
        }
    }

    /**
     * Log an specific event under the {@code category} and {@code action}.
     */
    public static void sendEvent(String category, String action) {
        if (!BuildConfig.DEBUG) {
            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();

            if (isInitialized()) {
                mTracker.send(eventBuilder
                        .setCategory(category)
                        .setAction(action)
                        .build());

                LogUtils.e(TAG, "Event recorded: category: " + category +
                        ", action: " + action);
            }
        }
    }

    /**
     * Sets up Analytics to be initialized when the user agrees to TOS.  If the user has already
     * done so (all runs of the app except the first run), initialize analytics Immediately. Note
     * that {@code applicationContext} must be the Application level {@link Context} or this class will
     * leak the context.
     *
     * @param applicationContext The context that will later be used to initialize Analytics.
     */
    public static void prepareAnalytics(Context applicationContext) {
        sAppContext = applicationContext.getApplicationContext();

        if (!BuildConfig.FLAVOR.equals("fdroid")) {
            initializeAnalyticsTracker(sAppContext);
        }
    }

    /**
     * Initialize the analytics tracker in use by the application. This should only be called
     * once, when the TOS is signed. The {@code context} parameter MUST be the
     * application context or an object leak could occur.
     *
     * @param context Context used to access the analytics {@link Tracker}.
     */
    private static synchronized void initializeAnalyticsTracker(Context context) {
        sAppContext = context.getApplicationContext();

        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(context).newTracker(GA_TRACKING_ID);

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);

            if (BuildConfig.DEBUG) {
                analytics.setDryRun(true);
                analytics.setLocalDispatchPeriod(120);
            } else {
                analytics.setLocalDispatchPeriod(1800);
            }

            try {
                mTracker = analytics.newTracker(GA_TRACKING_ID);
                mTracker.enableAdvertisingIdCollection(true);
                mTracker.enableAutoActivityTracking(false);
                mTracker.enableExceptionReporting(true);
                mTracker.setAppName(BuildConfig.APPLICATION_ID);
                mTracker.setAppVersion(BuildConfig.VERSION_NAME);
                mTracker.setSessionTimeout(300);
            } catch (Exception e) {
                Utils.logException(e);

                // If anything goes wrong, force an opt-out of tracking. It's better to accidentally
                // protect privacy than accidentally collect data.
                disableAnalytics();
            }
        }
    }

    /**
     * Return the current initialization state which indicates whether events can be logged.
     */
    private static boolean isInitialized() {
        // Google Analytics is initialized when this class has a reference to an app context and
        // an Analytics tracker has been created.
        return sAppContext != null // Is there an app context?
                && mTracker != null; // Is there a tracker?
    }

    /**
     * Disables Analytics.
     */
    private static void disableAnalytics() {
        GoogleAnalytics instance = GoogleAnalytics.getInstance(sAppContext);

        if (instance != null) {
            instance.setAppOptOut(true);
            LogUtils.e(TAG, "Analytics disabled");
        }

    }
}
