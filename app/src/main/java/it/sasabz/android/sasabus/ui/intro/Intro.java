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

package it.sasabz.android.sasabus.ui.intro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

import it.sasabz.android.sasabus.AppApplication;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.sync.SyncHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.MapActivity;
import it.sasabz.android.sasabus.ui.busstop.BusStopActivity;
import it.sasabz.android.sasabus.ui.intro.data.IntroFragmentData;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.CustomTabsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;

/**
 * The actual intro activity where all the intro fragments are attached. Handles permission
 * check, clicking on links in the legal fragment and finishing of the intro.
 *
 * @author Alex Lardschneider
 */
public class Intro extends AppIntro {

    private static final int PERMISSIONS_ACCESS_LOCATION = 123;

    private static final String SCREEN_LABEL = "Intro";

    private static final String TERMS_URL = NetUtils.HOST + "/terms";
    private static final String PRIVACY_URL = NetUtils.HOST + "/privacy";

    private CustomTabsHelper tabsHelper;

    @Override
    public void init() {
        addSlide(new IntroFragmentData());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addSlide(AppIntroFragment.newInstance(getString(R.string.intro_permissions), getString(R.string.intro_permission_text),
                    R.drawable.permission));
        }

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.primary_light_blue));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors.add(ContextCompat.getColor(this, R.color.primary_light_green));
        }

        setAnimationColors(colors);

        tabsHelper = new CustomTabsHelper(this);
        tabsHelper.start();
    }

    @Override
    public void onDonePressed() {
        checkPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_ACCESS_LOCATION && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            ((AppApplication) getApplication()).startBeacon();

            finishIntro();

            AnalyticsHelper.sendEvent(SCREEN_LABEL, "Permission granted");
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionRationale();
        }
    }

    /**
     * Shows a dialog telling the user why they should grant this permission and which
     * drawback rejecting it has.
     */
    private void showPermissionRationale() {
        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(R.string.snackbar_permission_denied)
                .setMessage(R.string.dialog_permission_location_sub)
                .setNegativeButton(R.string.dialog_permission_deny, (dialog, which) -> {
                    AnalyticsHelper.sendEvent(SCREEN_LABEL, "Permission denied");

                    dialog.dismiss();
                    finishIntro();
                })
                .setPositiveButton(R.string.dialog_permission_allow, (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_ACCESS_LOCATION))
                .create()
                .show();
    }

    /**
     * Checks if the app has the permission to access location. If not, ask for it.
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_LOCATION);
            AnalyticsHelper.sendEvent(SCREEN_LABEL, "Checking permission");
        } else {
            finishIntro();
        }
    }

    /**
     * Finishes the intro screen and navigates to {@link MapActivity}.
     */
    private void finishIntro() {
        new SyncHelper(this).performSyncAsync();

        SettingsUtils.markIntroAsShown(this);

        Intent intent = new Intent(this, BusStopActivity.class);
        intent.setAction(BaseActivity.ACTION_NO_CHANGELOG);
        startActivity(intent);

        finish();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        LogUtils.e(SCREEN_LABEL, intent.getScheme());

        if ("terms".equals(intent.getScheme())) {
            tabsHelper.launchUrl(Uri.parse(TERMS_URL));
        } else if ("privacy".equals(intent.getScheme())) {
            tabsHelper.launchUrl(Uri.parse(PRIVACY_URL));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        tabsHelper.stop();
    }
}
