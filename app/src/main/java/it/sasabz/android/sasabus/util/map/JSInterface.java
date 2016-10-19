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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import java.io.File;

import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.ui.bus.BusDetailActivity;
import it.sasabz.android.sasabus.ui.busstop.BusStopDetailActivity;
import it.sasabz.android.sasabus.ui.line.LineCourseActivity;
import it.sasabz.android.sasabus.ui.line.LineDetailsActivity;
import it.sasabz.android.sasabus.ui.route.RouteMapPickerActivity;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;

import static it.sasabz.android.sasabus.ui.busstop.BusStopActivity.INTENT_DISPLAY_FAVORITES;

class JSInterface {

    private static final String TAG = "JSInterface";

    private final Context context;

    private final File rootFolder;

    JSInterface(Context context) {
        this.context = context;

        rootFolder = MapDownloadHelper.getRootFolder(context);

        LogUtils.e(TAG, rootFolder.getAbsolutePath());
    }

    @JavascriptInterface
    public String getMapTilesRootUrl() {
        return "file://" + rootFolder.getAbsolutePath();
    }

    @JavascriptInterface
    public void onVehicleClick(int vehicle) {
        Intent intent = new Intent(context, BusDetailActivity.class);
        intent.putExtra(Config.EXTRA_VEHICLE, vehicle);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void onLineClick(int lineId) {
        Intent intent = new Intent(context, LineDetailsActivity.class);
        intent.putExtra(Config.EXTRA_LINE_ID, lineId);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void onLineCourseClick(int vehicle, int lineId, int busStop) {
        Intent intent = new Intent(context, LineCourseActivity.class);
        intent.putExtra(Config.EXTRA_VEHICLE, vehicle);
        intent.putExtra(Config.EXTRA_STATION_ID, new int[]{busStop});
        intent.putExtra(Config.EXTRA_LINE_ID, lineId);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public String getDelayString(int delay) {
        return context.getString(R.string.bottom_sheet_delayed, delay);
    }

    @JavascriptInterface
    public String getBusDetailsString() {
        return context.getString(R.string.bus_details).toUpperCase();
    }

    @JavascriptInterface
    public String getLineDetailsString() {
        return context.getString(R.string.lines_detail).toUpperCase();
    }

    @JavascriptInterface
    public String getCourseDetailsString() {
        return context.getString(R.string.course_details).toUpperCase();
    }

    @JavascriptInterface
    public String getNowAtString(String stop) {
        return context.getString(R.string.line_current_stop, stop);
    }


    @JavascriptInterface
    public String getLineString(int line) {
        return context.getString(R.string.line_format, String.valueOf(line));
    }

    @JavascriptInterface
    public String getHeadingToString(String stop) {
        return context.getString(R.string.line_heading, stop);
    }

    @JavascriptInterface
    public void onBusStopDetailsClick(int id) {
        Intent intent = new Intent(context, BusStopDetailActivity.class);
        intent.putExtra(Config.EXTRA_STATION_ID, id);
        ((Activity) context).startActivityForResult(intent, INTENT_DISPLAY_FAVORITES);
    }

    @JavascriptInterface
    public String getBusStopDetailsString() {
        return context.getString(R.string.station_details).toUpperCase();
    }

    @JavascriptInterface
    public void onBusStopSelectClick(int id) {
        ((RouteMapPickerActivity) context).selectBusStop(id);
    }

    @JavascriptInterface
    public String getSelectString() {
        return context.getString(R.string.station_select).toUpperCase();
    }

    @JavascriptInterface
    public boolean shouldUseOnlineMap() {
        return !MapDownloadHelper.mapExists || !SettingsUtils.shouldShowMapDialog(context);
    }
}
