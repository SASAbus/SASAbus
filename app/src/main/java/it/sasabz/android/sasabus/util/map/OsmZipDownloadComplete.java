package it.sasabz.android.sasabus.util.map;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import it.sasabz.android.sasabus.util.Utils;

public class OsmZipDownloadComplete extends BroadcastReceiver {

    private long downloadId;
    private File zipFile;

    OsmZipDownloadComplete(long downloadId, File zipFile) {
        this.downloadId = downloadId;
        this.zipFile = zipFile;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long currDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

            if (currDownloadId == this.downloadId) {
                context.unregisterReceiver(this);
                new Thread(() -> {
                    try {
                        extractZipContent(OsmZipDownloadComplete.this.zipFile);
                    } catch (IOException e) {
                        Utils.handleException(e);
                    }
                }).start();
            }
        }
    }

    private void extractZipContent(File zipFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(zipFile);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

        ZipEntry zipEntry;

        int len;
        byte[] buf = new byte[100000];

        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (!zipEntry.isDirectory()) {
                String name = zipEntry.getName();
                File tile = new File(zipFile.getParentFile(), name);
                tile.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(tile);

                while ((len = zipInputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }

                fileOutputStream.close();
            }
        }

        zipInputStream.close();
        fileInputStream.close();
    }
}
