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

import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;

import java.io.File;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.receiver.DownloadReceiver;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;

import static android.content.Context.DOWNLOAD_SERVICE;

public class MapDownloadHelper {

    private final String TAG = "MapDownloadHelper";

    private static final String MAP_URL = "http://opensasa.info/files/maptiles";
    private static final String OSM_ZIP_NAME = "osm-tiles.zip";

    private final Context context;

    private static File rootFolder;

    private final WebView webView;

    public static boolean mapExists;

    static File getRootFolder(Context context) {
        if (rootFolder == null) {
            File sdcardFilesDir = context.getExternalFilesDir(null);

            rootFolder = new File(sdcardFilesDir, "osm-tiles");

            if (!rootFolder.exists()) {
                rootFolder.mkdirs();
            }
        }

        return rootFolder;
    }

    MapDownloadHelper(Context context, WebView webView, RealtimeMapView mapView) {
        this.context = context;
        this.webView = webView;

        getRootFolder(context);
    }

    void checkForMap() {
        if (!SettingsUtils.shouldShowMapDialog(context)) {
            return;
        }

        if (rootFolder.listFiles() == null || rootFolder.listFiles().length < 2) {
            LogUtils.e(TAG, "Missing map");

            new AlertDialog.Builder(context, R.style.DialogStyle)
                    .setTitle(R.string.dialog_map_download_title)
                    .setMessage(R.string.dialog_map_download_message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> downloadMap())
                    .setNeutralButton(R.string.dialog_map_download_negative, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.dialog_button_dont_show_again, (dialog, which) -> {
                        SettingsUtils.disableMapDialog(context);
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        } else {
            LogUtils.e(TAG, "Map exists");
            mapExists = true;
        }
    }

    private void downloadMap() {
        LogUtils.e(TAG, "Downloading map tiles");

        String downloadZip = MAP_URL + "/" + OSM_ZIP_NAME;
        File destination = new File(rootFolder, OSM_ZIP_NAME);

        Uri mapUrl = Uri.parse(downloadZip);

        if (!destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        DownloadManager downloadManager = (DownloadManager)
                context.getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(mapUrl);
        request.setDestinationUri(Uri.fromFile(destination));

        long downloadId = downloadManager.enqueue(request);

        LogUtils.e(TAG, "Download id is: " + downloadId);

        context.registerReceiver(new DownloadReceiver(downloadId, destination, webView),
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
