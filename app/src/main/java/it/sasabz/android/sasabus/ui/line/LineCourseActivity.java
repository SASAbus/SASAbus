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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import com.davale.sasabus.core.realm.BusStopRealmHelper;
import com.davale.sasabus.core.realm.model.BusStop;
import com.davale.sasabus.core.util.DeviceUtils;
import com.davale.sasabus.core.vdv.Api;
import com.davale.sasabus.core.vdv.data.VdvException;
import com.davale.sasabus.core.vdv.model.VdvBusStop;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.line.LineCourse;
import it.sasabz.android.sasabus.data.network.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.RealtimeApi;
import it.sasabz.android.sasabus.data.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.data.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Settings;
import it.sasabz.android.sasabus.util.Utils;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Displays a list with all the bus stops where a mVehicle will stop/has stopped.
 * If the mVehicle is already in service the bus stops where the bus passed will be marked in a
 * lighter color compared to the bus stops where the bus has yet to stop.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class LineCourseActivity extends RxAppCompatActivity {

    private static final String EXTRA_CURRENT_BUS_STOP = "com.davale.sasabus.EXTRA_CURRENT_BUS_STOP";

    private static final String TAG = "LineCourseActivity";
    private static final String ERROR_DATA = "data";

    private static int mTripId;
    private static int mBusStopGroup;
    private static int mCurrentBusStop;
    private static int vehicle;

    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;
    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.error_data) RelativeLayout mErrorData;

    @BindView(R.id.refresh) SwipeRefreshLayout mRefresh;
    @BindView(R.id.lines_course_recycler) RecyclerView mRecyclerView;

    private ArrayList<LineCourse> mItems;
    private LineCourseAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_line_course);

        ButterKnife.bind(this);
        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_course_details);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        mTripId = intent.getIntExtra(Config.EXTRA_TRIP_ID, -1);
        mBusStopGroup = intent.getIntExtra(Config.EXTRA_BUS_STOP_GROUP, -1);
        vehicle = intent.getIntExtra(Config.EXTRA_VEHICLE, -1);
        mCurrentBusStop = intent.getIntExtra(EXTRA_CURRENT_BUS_STOP, -1);

        mRefresh.setColorSchemeResources(Config.REFRESH_COLORS);
        mRefresh.setOnRefreshListener(this::parsePlanData);

        if (savedInstanceState != null) {
            int errorDataVisibility = savedInstanceState.getInt("ERROR_DATA");
            int errorWifiVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_WIFI);
            int errorGeneralVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_GENERAL);

            //noinspection ResourceType
            mErrorData.setVisibility(errorDataVisibility);
            //noinspection ResourceType
            mErrorWifi.setVisibility(errorWifiVisibility);
            //noinspection ResourceType
            mErrorGeneral.setVisibility(errorGeneralVisibility);

            mItems = savedInstanceState
                    .getParcelableArrayList(Config.BUNDLE_LIST);
        } else {
            mItems = new ArrayList<>();
            mRefresh.setRefreshing(true);
        }

        mAdapter = new LineCourseAdapter(this, mItems);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        parsePlanData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);

        outState.putInt("ERROR_DATA", mErrorData.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
    }

    /**
     * Parses the line course from the plan data. This is used to get the course of a non
     * tracked mVehicle or a mVehicle which will drive in the future.
     */
    private void parsePlanData() {
        parseFromPlanData(vehicle, mBusStopGroup, mCurrentBusStop, mTripId)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LineCourse>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!ERROR_DATA.equals(e.getMessage())) {
                            Utils.logException(e);
                        }

                        LineCourseActivity.this.onError(e);
                    }

                    @Override
                    public void onNext(List<LineCourse> items) {
                        onResult(items);
                    }
                });
    }

    private void onResult(Collection<LineCourse> lines) {
        boolean isEmpty = mItems.isEmpty();

        mItems.clear();
        mItems.addAll(lines);

        if (isEmpty) {
            mAdapter.notifyItemRangeInserted(0, mItems.size());
        } else {
            mAdapter.notifyDataSetChanged();
        }

        mErrorGeneral.setVisibility(View.GONE);
        mErrorWifi.setVisibility(View.GONE);
        mErrorData.setVisibility(View.GONE);

        for (int i = 0; i < mItems.size(); i++) {
            LineCourse item = mItems.get(i);

            if (item.active && (item.dot || item.bus)) {
                int offset = DeviceUtils.getScreenHeight(this) / 3;

                ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                        .scrollToPositionWithOffset(i, offset);

                break;
            }
        }

        mRefresh.setRefreshing(false);
    }

    private void onError(Throwable throwable) {
        mItems.clear();
        mAdapter.notifyDataSetChanged();

        if ("data".equals(throwable.getMessage())) {
            mErrorData.setVisibility(View.VISIBLE);
        } else if ("internet".equals(throwable.getMessage())) {
            mErrorWifi.setVisibility(View.VISIBLE);
        } else {
            mErrorGeneral.setVisibility(View.VISIBLE);
        }

        mRefresh.setRefreshing(false);
    }

    /**
     * Extracts a course from the offline API
     *
     * @param busStopGroup   the ids of the stop where the bus is currently located or where the user
     *                       wants to get step onto the bus (basically from which bus stop he requested
     *                       the course details)
     * @param currentBusStop the ids of the stop where the bus is currently located or where the user
     *                       wants to get step onto the bus (basically from which bus stop he requested
     *                       the course details)
     * @param tripId         the ID of a trip
     */
    private Observable<List<LineCourse>> parseFromPlanData(int vehicle, int busStopGroup,
                                                           int currentBusStop, int tripId) {
        return Observable.fromCallable(() -> {
            int currentBusStopNew = currentBusStop;

            if (!Api.todayExists(this)) {
                Settings.markDataUpdateAvailable(LineCourseActivity.this, true);

                throw new VdvException(ERROR_DATA);
            }

            if (vehicle != 0) {
                Timber.w("Getting bus position for %s", vehicle);

                RealtimeApi realtimeApi = RestClient.INSTANCE.getADAPTER().create(RealtimeApi.class);
                Call<RealtimeResponse> call = realtimeApi.vehicle(vehicle);
                Response<RealtimeResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    RealtimeResponse body = response.body();

                    if (!body.buses.isEmpty()) {
                        RealtimeBus bus = body.buses.get(0);
                        currentBusStopNew = bus.busStop;

                        Timber.w("Got bus position for %s: %s", vehicle, currentBusStopNew);
                    } else {
                        Timber.w("Bus %s is not in service", vehicle);
                    }
                } else {
                    Timber.w("Could not get bus position: %s, %s", response.code(),
                            response.errorBody());
                }
            }

            List<LineCourse> items = new ArrayList<>();
            List<VdvBusStop> path;

            path = Api.getTrip(tripId).calcTimedPath();

            boolean active = currentBusStopNew == 0;

            for (VdvBusStop stop : path) {
                BusStop busStop = BusStopRealmHelper.getBusStop(stop.getId());

                boolean dot = false;
                boolean bus = false;

                // Iterate all times to see at which bus stop the bus currently is, and mark it
                // to make it stand out in the list (by either a dot or a bus depending if it
                // currently is in service).

                if (busStop.getId() == currentBusStopNew) {
                    active = true;
                    bus = true;
                }

                if (busStop.getFamily() == busStopGroup) {
                    dot = true;
                }

                items.add(new LineCourse(stop.getId(), busStop, stop.getTime(), active, dot, bus));
            }

            return items;
        });
    }

    public static Intent intent(Context context, int tripId, int busStopGroup,
                                int currentBusStop, int vehicle) {

        Intent intent = new Intent(context, LineCourseActivity.class);
        intent.putExtra(Config.EXTRA_TRIP_ID, tripId);
        intent.putExtra(Config.EXTRA_BUS_STOP_GROUP, busStopGroup);
        intent.putExtra(Config.EXTRA_VEHICLE, vehicle);
        intent.putExtra(EXTRA_CURRENT_BUS_STOP, currentBusStop);
        return intent;
    }
}