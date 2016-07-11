package it.sasabz.android.sasabus.util.map;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.util.LogUtils;

import static android.content.Context.DOWNLOAD_SERVICE;

public class MapDownloadHelper {

    private final String TAG = "MapDownloadHelper";

    public static final String MAP_URL = "http://opensasa.info/files/maptiles";
    public static final String OSM_ZIP_NAME = "osm-tiles.zip";

    private final Context context;

    static File rootFolder;

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

    public MapDownloadHelper(Context context) {
        this.context = context;

        getRootFolder(context);
    }

    public void checkMapFirstTime() throws IOException {
        if (rootFolder.listFiles().length == 0) {
            LogUtils.e(TAG, "Missing map");

            new AlertDialog.Builder(context, R.style.DialogStyle)
                    .setTitle("Map download")
                    .setMessage("The app needs to download the OpenStreetMap map tiles, which are about 60MByte of data. " +
                            "If you don\\'t have a flat data plan, we suggest you to use a wi-fi network to avoid expensive costs. " +
                            "Would you like to do it now?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        downloadOSMTiles();
                        //MainActivity.this.initUI();
                    })
                    .setNegativeButton("Later", (dialog, which) -> {
                        //MapDownloadHelper.this.initUI();
                    })
                    .create()
                    .show();
        } else {
            LogUtils.e(TAG, "Map exists");
            //MainActivity.this.initUI();
        }
    }

    private void downloadOSMTiles() {
        LogUtils.e(TAG, "Downloading map tiles");

        String downloadZip = MAP_URL + "/" + OSM_ZIP_NAME;
        File destination = new File(rootFolder, OSM_ZIP_NAME);

        Uri mapUrl = Uri.parse(downloadZip);

        if (!destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(mapUrl);
        request.setDestinationUri(Uri.fromFile(destination));
        long downloadId = dm.enqueue(request);

        context.registerReceiver(new OsmZipDownloadComplete(context, downloadId, destination),
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
