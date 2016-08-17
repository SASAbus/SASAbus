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

import static android.content.Context.DOWNLOAD_SERVICE;

public class MapDownloadHelper {

    private final String TAG = "MapDownloadHelper";

    private static final String MAP_URL = "http://opensasa.info/files/maptiles";
    private static final String OSM_ZIP_NAME = "osm-tiles.zip";

    private final Context context;

    private static File rootFolder;

    private WebView webView;

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

    MapDownloadHelper(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;

        getRootFolder(context);
    }

    void checkMapFirstTime() {
        if (rootFolder.listFiles().length < 2) {

            LogUtils.e(TAG, "Missing map");

            new AlertDialog.Builder(context, R.style.DialogStyle)
                    .setTitle(R.string.dialog_map_download_title)
                    .setMessage(R.string.dialog_map_download_message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> downloadMap())
                    .setNegativeButton(R.string.dialog_map_download_negative, (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        } else {
            LogUtils.e(TAG, "Map exists");
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
