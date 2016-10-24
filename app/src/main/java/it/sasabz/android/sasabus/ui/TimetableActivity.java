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

package it.sasabz.android.sasabus.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.line.Lines;
import it.sasabz.android.sasabus.data.network.rest.Endpoint;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.ValidityApi;
import it.sasabz.android.sasabus.data.network.rest.response.ValidityResponse;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.IOUtils;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Settings;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.TimetableAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Displays all the available timetables, inclusive a map of bz/me, in a list so the user
 * can open the timetables with an external pdf reader.
 * Handles downloading of timetables by using {@link Observable RxJava} and updating them if a
 * timetable change occurs.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class TimetableActivity extends BaseActivity implements Observer<Integer> {

    private static final String TAG = "TimetableActivity";

    private static final String FILENAME_OFFLINE = "timetables.zip";

    /**
     * List to hold the timetable items.
     */
    private ArrayList<String> mItems;

    /**
     * The {@link RecyclerView} adapter.
     */
    private TimetableAdapter mAdapter;

    /**
     * Snackbar to notify the user that a timetable update is available.
     */
    private Snackbar mSnackbar;

    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timetable);

        AnalyticsHelper.sendScreenView(TAG);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);

        mItems = new ArrayList<>();
        mAdapter = new TimetableAdapter(this, mItems);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);

        if (timetablesExist()) {
            parseData();
        } else {
            startDownload();
        }
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_TIMETABLES;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (progressBar != null) {
            progressBar.dismiss();
        }
    }

    @Override
    public void onNext(Integer progress) {
        // Cheaty hack to know when to switch to extracting text
        if (progress == Integer.MAX_VALUE) {
            progressBar.setMessage(getString(R.string.timetable_extract));
        } else {
            progressBar.setProgress(progress);
        }
    }

    @Override
    public void onCompleted() {
        LogUtils.e(TAG, "onCompleted()");

        Settings.setTimetableDate(this);

        progressBar.dismiss();
        parseData();
    }

    @Override
    public void onError(Throwable throwable) {
        Utils.logException(throwable);

        progressBar.dismiss();

        mSnackbar = Snackbar.make(getMainContent(), R.string.snackbar_timetable_error, Snackbar.LENGTH_INDEFINITE);
        mSnackbar.setActionTextColor(ContextCompat.getColor(this, R.color.primary));
        mSnackbar.setAction(R.string.snackbar_retry, v -> startDownload());

        mSnackbar.show();
    }


    /**
     * Adds the timetable lines to the adapter and checks if a timetable
     * update is available. If yes, show a {@link Snackbar} to notify the user.
     */
    private void parseData() {
        String[] lines = Lines.timetableLines;

        mItems.clear();
        Collections.addAll(mItems, lines);
        mAdapter.notifyDataSetChanged();

        String date = Settings.getTimetableDate(this);

        ValidityApi validityApi = RestClient.ADAPTER.create(ValidityApi.class);
        validityApi.timetables(date)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ValidityResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);
                    }

                    @Override
                    public void onNext(ValidityResponse validityResponse) {
                        if (!validityResponse.isValid) {
                            LogUtils.e(TAG, "Timetable update available");
                            Settings.markDataUpdateAvailable(TimetableActivity.this, true);

                            mSnackbar = Snackbar.make(getMainContent(), R.string.timetable_update_text, Snackbar.LENGTH_INDEFINITE);
                            mSnackbar.setActionTextColor(ContextCompat.getColor(TimetableActivity.this, R.color.snackbar_action_text));
                            mSnackbar.setAction(R.string.timetable_update_action, v -> startDownload());

                            mSnackbar.show();
                        } else {
                            LogUtils.e(TAG, "No timetable update available");
                        }
                    }
                });
    }

    /**
     * Checks for missing timetable files.
     *
     * @return {@code true} if a timetable file is missing, {@code false} otherwise.
     */
    private boolean timetablesExist() {
        String[] lines = Lines.timetableLines;
        Collection<String> list = new ArrayList<>();
        Collections.addAll(list, lines);

        File filesDir = IOUtils.getTimetablesDir(this);

        for (String line : list) {
            File file = new File(filesDir.getAbsolutePath(), line + ".pdf");

            if (!file.exists()) {
                LogUtils.e(TAG, "Missing file: " + file.getAbsolutePath());
                return false;
            }
        }

        return true;
    }

    /**
     * Starts the download and shows a progress dialog.
     */
    private void startDownload() {
        progressBar = new ProgressDialog(this, R.style.DialogStyle);
        progressBar.setMessage(getString(R.string.timetable_download));
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setCancelable(false);
        progressBar.show();

        downloadFileObservable()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    /**
     * {@link Observable} which handles the timetable downloading in a nice reactive way.
     * After the file gets downloaded it will be unzipped in the /files/timetable folder in
     * the external storage dir.
     *
     * @return a {@link Observable} which handles timetable download.
     */
    private Observable<Integer> downloadFileObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    LogUtils.e(TAG, "Starting plan data download");

                    File file = new File(IOUtils.getTimetablesDir(TimetableActivity.this), FILENAME_OFFLINE);

                    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                        subscriber.onError(new Throwable("Cannot create directory file"));
                        return;
                    }

                    downloadFile(subscriber, file);

                    IOUtils.unzipFile(FILENAME_OFFLINE, file.getParent());

                    //noinspection ResultOfMethodCallIgnored
                    file.delete();

                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * Downloads the timetable and writes it to the disk by using {@link Okio} to deliver high
     * performance file writing. As the observable which handles progress updates cannot keep
     * up with the high download speed, the {@link Observer} will be notified every 4th read
     * cycle about the current progress.
     *
     * @param subscriber the {@link Subscriber} which subscribes to {@link #downloadFileObservable()}
     * @param file the File where the timetables should be saved in form of a zip.
     * @throws IOException if downloading or writing the file fails.
     */
    private void downloadFile(Observer<? super Integer> subscriber, File file) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .build();

        Request request = new Request.Builder().url(Endpoint.API + "/assets/archives/timetables").build();
        Response response = client.newCall(request).execute();

        ResponseBody body = response.body();
        long contentLength = body.contentLength();
        BufferedSource source = body.source();

        BufferedSink sink = Okio.buffer(Okio.sink(file));

        int BYTE_SIZE = 8192;
        int progressCounter = 0;

        long bytesRead = 0;
        while (source.read(sink.buffer(), BYTE_SIZE) != -1) {
            bytesRead += BYTE_SIZE;

            if (progressCounter % 8 == 0) {
                int progress = (int) (bytesRead * 100 / contentLength);
                subscriber.onNext(progress);
            }

            progressCounter++;
        }

        subscriber.onNext(Integer.MAX_VALUE);

        sink.writeAll(source);
        sink.close();
    }
}
