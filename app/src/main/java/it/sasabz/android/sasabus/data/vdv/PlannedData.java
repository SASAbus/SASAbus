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

package it.sasabz.android.sasabus.data.vdv;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.data.network.rest.Endpoint;
import it.sasabz.android.sasabus.data.vdv.data.VdvHandler;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import it.sasabz.android.sasabus.util.Settings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;
import rx.Observable;
import timber.log.Timber;

/**
 * Utility class that checks if the stored open data is valid.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public final class PlannedData {

    private static final String FILENAME_ONLINE = "assets/archives/planned-data";
    private static final String FILENAME_OFFLINE = "planned_data.zip";

    private static Boolean dataExists;

    private PlannedData() {
    }

    /**
     * Sets that the plan data is valid.
     */
    private static void setDataValid() {
        dataExists = true;
    }

    /**
     * Checks if any file is missing and needs to be downloaded again.
     *
     * @return a boolean value indicating whether any file is missing
     */
    public static boolean planDataExists(Context context) {
        Preconditions.checkNotNull(context, "planDataExists() context == null");

        if (dataExists == null) {
            File file = new File(IOUtils.getDataDir(context), "planned-data.json");
            if (file.exists()) {
                dataExists = true;
                return true;
            }

            Timber.e("Planned data (JSON file) is missing");
            dataExists = false;
            return false;
        }

        return dataExists;
    }

    public static Observable<Void> download(Context context) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Timber.e("Starting download of planned data (JSON file)");

                File file = new File(IOUtils.getDataDir(context), FILENAME_OFFLINE);
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new IOException("Could not create planned data parent folder");
                }

                downloadFile(file);

                if (!IOUtils.unzipFile(FILENAME_OFFLINE, file.getParent())) {
                    throw new IOException("Could not extract planned data");
                }

                //noinspection ResultOfMethodCallIgnored
                //file.delete();

                Settings.markDataUpdateAvailable(context, false);
                Settings.setDataDate(context);

                setDataValid();

                // Load plan data
                VdvHandler.reset();
                VdvHandler.load(context).subscribe();

                return null;
            }
        });
    }

    private static void downloadFile(File file) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();

        BufferedSink sink = Okio.buffer(Okio.sink(file));

        sink.writeAll(client
                .newCall(new Request.Builder().url(Endpoint.API + FILENAME_ONLINE).build())
                .execute()
                .body()
                .source());

        sink.close();
    }
}