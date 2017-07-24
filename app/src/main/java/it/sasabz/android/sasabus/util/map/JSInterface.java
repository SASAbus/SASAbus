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

package it.sasabz.android.sasabus.util.map;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.davale.sasabus.core.realm.BusStopRealmHelper;
import com.davale.sasabus.core.realm.model.BusStop;

import java.io.File;

import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.ui.bus.BusDetailActivity;
import it.sasabz.android.sasabus.ui.departure.DepartureActivity;
import it.sasabz.android.sasabus.ui.line.LineCourseActivity;
import it.sasabz.android.sasabus.ui.line.LineDetailsActivity;
import it.sasabz.android.sasabus.ui.route.RouteMapPickerActivity;
import it.sasabz.android.sasabus.util.Settings;
import timber.log.Timber;

class JSInterface {

    private final Context mContext;

    private final File mRootFolder;

    JSInterface(Context context) {
        this.mContext = context;

        mRootFolder = MapDownloadHelper.getRootFolder(context);

        Timber.e(mRootFolder.getAbsolutePath());
    }

    @JavascriptInterface
    public String getMapTilesRootUrl() {
        return "file://" + mRootFolder.getAbsolutePath();
    }

    @JavascriptInterface
    public void onVehicleClick(int vehicle) {
        Intent intent = new Intent(mContext, BusDetailActivity.class);
        intent.putExtra(Config.EXTRA_VEHICLE, vehicle);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void onLineClick(int lineId) {
        Intent intent = new Intent(mContext, LineDetailsActivity.class);
        intent.putExtra(Config.EXTRA_LINE_ID, lineId);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void onLineCourseClick(int vehicle, int busStop, int tripId) {
        mContext.startActivity(LineCourseActivity.intent(mContext, tripId,
                0, busStop, vehicle));
    }

    @JavascriptInterface
    public String getDelayString(int delay) {
        return mContext.getString(R.string.bottom_sheet_delayed, delay);
    }

    @JavascriptInterface
    public String getBusDetailsString() {
        return mContext.getString(R.string.title_bus_details).toUpperCase();
    }

    @JavascriptInterface
    public String getLineDetailsString() {
        return mContext.getString(R.string.title_line_details).toUpperCase();
    }

    @JavascriptInterface
    public String getCourseDetailsString() {
        return mContext.getString(R.string.title_course_details).toUpperCase();
    }

    @JavascriptInterface
    public String getNowAtString(String stop) {
        return mContext.getString(R.string.line_current_stop, stop);
    }


    @JavascriptInterface
    public String getLineString(String line) {
        return mContext.getString(R.string.line_format, line);
    }

    @JavascriptInterface
    public String getHeadingToString(String stop) {
        return mContext.getString(R.string.line_heading, stop);
    }

    @JavascriptInterface
    public void onBusStopDetailsClick(int id) {
        BusStop busStop = BusStopRealmHelper.getBusStop(id);
        Intent intent = DepartureActivity.intent(mContext, busStop.getFamily());
    }

    @JavascriptInterface
    public String getBusStopDetailsString() {
        return mContext.getString(R.string.station_details).toUpperCase();
    }

    @JavascriptInterface
    public void onBusStopSelectClick(int id) {
        ((RouteMapPickerActivity) mContext).selectBusStop(id);
    }

    @JavascriptInterface
    public String getSelectString() {
        return mContext.getString(R.string.station_select).toUpperCase();
    }

    @JavascriptInterface
    public boolean shouldUseOnlineMap() {
        return !MapDownloadHelper.mapExists || !Settings.shouldShowMapDialog(mContext);
    }
}
