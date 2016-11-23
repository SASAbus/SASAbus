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

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;

import it.sasabz.android.sasabus.R;
import timber.log.Timber;

/**
 * Helper class to use custom tabs. Handles common functionality like warming the service up,
 * which can, but must not happen before opening a link with {@link #launchUrl(Uri)}.
 *
 * This helper should be terminated by using {@link #stop()} to prevent leaks.
 *
 * @author Alex Lardschneider
 */
public class CustomTabsHelper {

    private static final String TAG = "CustomTabsHelper";
    private static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";

    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;
    private CustomTabsIntent customTabsIntent;

    private final Activity activity;

    public CustomTabsHelper(Activity activity) {
        Preconditions.checkNotNull(activity, "activity == null");

        this.activity = activity;
    }

    public void start() {
        start(ContextCompat.getColor(activity, R.color.primary));
    }

    public void start(int toolbarColor) {
        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                //Pre-warming
                mClient = customTabsClient;
                mClient.warmup(0L);

                //Initialize a session as soon as possible.
                mCustomTabsSession = mClient.newSession(null);

                Timber.e("Custom tabs warmup done");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(activity, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);

        customTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(toolbarColor)
                .setShowTitle(true)
                .build();
    }

    public void launchUrl(Uri url) {
        customTabsIntent.launchUrl(activity, url);
    }

    public void stop() {
        activity.unbindService(mCustomTabsServiceConnection);
    }
}
