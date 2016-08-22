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

package it.sasabz.android.sasabus.ui.line;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.line.LineCourse;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.RealtimeApi;
import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.provider.API;
import it.sasabz.android.sasabus.provider.ApiUtils;
import it.sasabz.android.sasabus.provider.apis.Trips;
import it.sasabz.android.sasabus.provider.model.BusStop;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.LineCourseAdapter;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Displays a list with all the bus stops where a vehicle will stop / has stopped.
 * If the vehicle is already in service the bus stops where the bus passed will be marked in a
 * lighter color compared to the bus stops where the bus has yet to stop.
 * <p>
 * The path will always be calculated offline by using the integrated {@link API}, but depending
 * if the bus is in service the current bus stop will be loaded from the realtime api.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class LineCourseActivity extends RxAppCompatActivity {

    @BindView(R.id.refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.lines_course_recycler) RecyclerView mRecyclerView;
    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;
    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.error_data) RelativeLayout mErrorData;

    private int vehicle;
    private int[] stationIds;
    private int tempBusStop;
    private int lineId;
    private String time;

    private final ArrayList<LineCourse> mItems = new ArrayList<>();
    private LineCourseAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.changeLanguage(this);

        setContentView(R.layout.activity_line_course);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.course_details);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        vehicle = intent.getExtras().getInt(Config.EXTRA_VEHICLE);
        stationIds = intent.getExtras().getIntArray(Config.EXTRA_STATION_ID);

        time = intent.getExtras().getString("time");
        lineId = intent.getExtras().getInt(Config.EXTRA_LINE_ID);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_amber, R.color.primary_red, R.color.primary_green, R.color.primary_indigo);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (time != null) {
                parsePlanData(stationIds, time, true);
            } else {
                parseDataFromVehicle(vehicle, true);
            }
        });

        mAdapter = new LineCourseAdapter(this, mItems);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            int errorDataVisibility = savedInstanceState.getInt("ERROR_DATA");
            int errorWifiVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_WIFI);
            int errorGeneralVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_GENERAL);

            if (errorWifiVisibility != View.GONE || errorGeneralVisibility != View.GONE ||
                    errorDataVisibility != View.GONE) {
                //noinspection ResourceType
                mErrorData.setVisibility(errorDataVisibility);
                //noinspection ResourceType
                mErrorWifi.setVisibility(errorWifiVisibility);
                //noinspection ResourceType
                mErrorGeneral.setVisibility(errorGeneralVisibility);

                return;
            } else {
                ArrayList<LineCourse> temp = savedInstanceState
                        .getParcelableArrayList(Config.BUNDLE_LIST);

                if (temp != null && !temp.isEmpty()) {
                    mItems.addAll(temp);
                    mAdapter.notifyDataSetChanged();

                    return;
                }
            }
        }

        if (time != null) {
            parsePlanData(stationIds, time, false);
        } else {
            parseDataFromVehicle(vehicle, false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
        outState.putInt("ERROR_DATA", mErrorData.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
    }


    private void parseDataFromVehicle(int vehicle, boolean manualRefresh) {
        tempBusStop = 0;

        if (!NetUtils.isOnline(this)) {
            mErrorWifi.setVisibility(View.VISIBLE);
            mErrorGeneral.setVisibility(View.GONE);

            mItems.clear();
            mAdapter.notifyDataSetChanged();

            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));

            return;
        }

        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));

        getPath(vehicle)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .flatMap(busStops -> {
                    if (!busStops.isEmpty()) {
                        for (int i = 0; i < busStops.size(); i++) {
                            if (busStops.get(i).getId() == tempBusStop) {
                                BusStop stop = busStops.get(i);

                                return parseFromPlanData(new int[]{stop.getId()}, stop.getTime(), lineId, busStops, false);
                            }
                        }
                    }

                    return Observable.empty();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LineCourse>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        mItems.clear();
                        mAdapter.notifyDataSetChanged();

                        if ("data".equals(e.getMessage())) {
                            mErrorData.setVisibility(View.VISIBLE);
                        } else {
                            mErrorGeneral.setVisibility(View.VISIBLE);
                        }

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }

                    @Override
                    public void onNext(List<LineCourse> items) {
                        mItems.clear();
                        mItems.addAll(items);

                        if (manualRefresh) {
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mAdapter.notifyItemRangeInserted(0, mItems.size());
                        }

                        mErrorGeneral.setVisibility(View.GONE);
                        mErrorWifi.setVisibility(View.GONE);
                        mErrorData.setVisibility(View.GONE);

                        for (int i = 0; i < mItems.size(); i++) {
                            LineCourse item = mItems.get(i);
                            if (item.isActive()) {
                                mRecyclerView.smoothScrollToPosition(i);
                                break;
                            }
                        }

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }
                });
    }

    private void parsePlanData(int[] stationIds, String time, boolean manualRefresh) {
        parseFromPlanData(stationIds, time, lineId, null, true)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LineCourse>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e != null) {
                            Utils.handleException(e);
                        }

                        mErrorGeneral.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }

                    @Override
                    public void onNext(List<LineCourse> items) {
                        mItems.clear();
                        mItems.addAll(items);

                        if (manualRefresh) {
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mAdapter.notifyItemRangeInserted(0, mItems.size());
                        }

                        mErrorGeneral.setVisibility(View.GONE);
                        mErrorWifi.setVisibility(View.GONE);
                        mErrorData.setVisibility(View.GONE);

                        for (int i = 0; i < mItems.size(); i++) {
                            LineCourse item = mItems.get(i);
                            if (item.isActive()) {
                                mRecyclerView.smoothScrollToPosition(i);
                                break;
                            }
                        }

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }
                });
    }

    private Observable<List<BusStop>> getPath(int vehicle) {
        return Observable.create(new Observable.OnSubscribe<List<BusStop>>() {
            @Override
            public void call(Subscriber<? super List<BusStop>> subscriber) {
                if (!API.todayExists(LineCourseActivity.this)) {
                    SettingsUtils.markDataUpdateAvailable(LineCourseActivity.this, true);

                    subscriber.onError(new Throwable("data"));
                    return;
                }

                try {
                    RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
                    Response<RealtimeResponse> response = realtimeApi.vehicle(vehicle).execute();

                    RealtimeResponse realtimeResponse = response.body();

                    // Ignore empty response.
                    if (realtimeResponse.buses.isEmpty()) {
                        subscriber.onError(null);
                    }

                    RealtimeBus bus = response.body().buses.get(0);

                    List<BusStop> path = Trips.getPath(getApplicationContext(), bus.trip);

                    tempBusStop = bus.busStop;

                    subscriber.onNext(path);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * Extracts a course from the offline API
     *
     * @param stationIds the ids of the stop where the bus is currently located or where the user
     *                   wants to get step onto the bus (basically from which bus stop he requested
     *                   the course details)
     * @param time       the time when the bus departs at the bus stop in the parameter above
     * @param line       the line which will pass at the defined bus stop and time
     * @param busStops   optionally the path of the bus can be passed, if it is already known in
     *                   advance, in order to avoid calculating it again
     */
    private Observable<List<LineCourse>> parseFromPlanData(int[] stationIds, String time, int line,
                                                           List<BusStop> busStops, boolean allBlack) {
        return Observable.create(new Observable.OnSubscribe<List<LineCourse>>() {
            @Override
            public void call(Subscriber<? super List<LineCourse>> subscriber) {
                if (!API.todayExists(LineCourseActivity.this)) {
                    SettingsUtils.markDataUpdateAvailable(LineCourseActivity.this, true);

                    subscriber.onError(new Throwable("data"));
                    return;
                }

                List<LineCourse> items = new ArrayList<>();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 1);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                TimeZone timeZone = TimeZone.getTimeZone("Europe/Rome");

                if (timeZone.inDaylightTime(calendar.getTime())) {
                    calendar.add(Calendar.MILLISECOND, timeZone.getDSTSavings());
                }

                long unixTime = calendar.getTimeInMillis() / 1000;

                List<BusStop> path;

                if (busStops == null) {
                    path = API.getPath(LineCourseActivity.this,
                            unixTime + ApiUtils.getSeconds(time), line);
                } else {
                    path = busStops;
                }

                boolean isActive = allBlack;

                for (BusStop stop : path) {
                    String stationString = BusStopRealmHelper.getName(stop.getId());
                    String municString = BusStopRealmHelper.getMunic(stop.getId());

                    boolean dot = false;

                    for (int id : stationIds) {
                        if (stop.getId() == id) {
                            dot = true;
                            isActive = true;
                            break;
                        }
                    }

                    items.add(new LineCourse(stop.getId(), stationString,
                            municString, stop.getTime(), isActive, dot));
                }

                subscriber.onNext(items);
                subscriber.onCompleted();
            }
        });
    }
}