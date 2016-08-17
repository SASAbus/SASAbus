package it.sasabz.android.sasabus.provider;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import rx.Observable;
import rx.Subscriber;

/**
 * Utility class that checks if the stored open data is valid.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public final class PlanData {

    private static final String TAG = "PlanData";

    private static final String FILENAME_ONLINE = "/assets/archives/vdv";
    private static final String FILENAME_OFFLINE = "data.zip";

    private static DataResult result;

    /**
     * All the plan data files we need.
     */
    private static final String[] FILES = {
            "FIRMENKALENDER",
            "LID_VERLAUF",
            "ORT_HZT",
            "REC_FRT",
            "REC_FRT_HZT",
            "SEL_FZT_FELD"
    };

    private PlanData() {
    }

    /**
     * Sets that the plan data is valid.
     */
    public static void setDataValid() {
        if (result == null) {
            result = new DataResult(true);
        } else {
            result.setValue();
        }
    }

    /**
     * Checks if any file is missing and needs to be downloaded again.
     *
     * @return a boolean value indicating whether any file is missing
     */
    public static boolean planDataExists(Context context) {
        Preconditions.checkNotNull(context, "planDataExists() context == null");

        if (result != null) {
            return result.isValue();
        }

        File filesDir = IOUtils.getDataDir(context);

        for (String fileName : FILES) {
            File file = new File(filesDir.getAbsolutePath(), fileName + ".json");

            if (!file.exists()) {
                LogUtils.e(TAG, "Missing file " + fileName);
                result = new DataResult(false);
                return false;
            }
        }

        result = new DataResult(true);

        return true;
    }

    /**
     * This class stores the result of the plan data check, so that we do not have
     * to recheck it as often.
     */
    private static class DataResult {
        private boolean value;

        DataResult(boolean value) {
            this.value = value;
        }

        public boolean isValue() {
            return value;
        }

        void setValue() {
            value = true;
        }
    }

    public static Observable<Void> downloadPlanData(Context context) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    LogUtils.e(TAG, "Starting plan data download");

                    File file = new File(IOUtils.getDataDir(context), FILENAME_OFFLINE);

                    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                        subscriber.onError(new Throwable("Cannot create directory file"));
                        return;
                    }

                    downloadFile(file);

                    IOUtils.unzipFile(FILENAME_OFFLINE, file.getParent());

                    //noinspection ResultOfMethodCallIgnored
                    file.delete();

                    // Call onNext with null as return type is Void
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static void downloadFile(File file) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(Endpoint.API + FILENAME_ONLINE).build();
        Response response = client.newCall(request).execute();

        BufferedSink sink = Okio.buffer(Okio.sink(file));

        sink.writeAll(response.body().source());
        sink.close();
    }
}
