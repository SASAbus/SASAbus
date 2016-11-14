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

package it.sasabz.android.sasabus.ui.parking;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.Parking;
import it.sasabz.android.sasabus.data.network.NetUtils;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.ParkingApi;
import it.sasabz.android.sasabus.data.network.rest.response.ParkingResponse;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.ParkingConfigureAdapter;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Shows a list of available parking spots in bolzano with name, location, total spots and free
 * spots. More details are available by pressing on the parking to launch {@link ParkingDetailActivity}.
 *
 * @author Alex Lardschneider
 */
public class ParkingConfigureActivity extends AppCompatActivity {

    private static final String TAG = "ParkingConfigureActivity";

    private int mAppWidgetId;

    private final ArrayList<Parking> mItems = new ArrayList<>();
    private ParkingConfigureAdapter mAdapter;

    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;
    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.refresh) SwipeRefreshLayout mRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_parking);

        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        mAdapter = new ParkingConfigureAdapter(this, mItems, mAppWidgetId);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        mRefresh.setOnRefreshListener(this::parseData);
        mRefresh.setColorSchemeResources(Config.REFRESH_COLORS);

        if (savedInstanceState != null) {
            int errorWifiVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_WIFI);
            int errorGeneralVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_GENERAL);

            if (errorWifiVisibility != View.GONE || errorGeneralVisibility != View.GONE) {
                // noinspection ResourceType
                mErrorGeneral.setVisibility(errorGeneralVisibility);
                // noinspection ResourceType
                mErrorWifi.setVisibility(errorWifiVisibility);

                return;
            } else {
                List<Parking> list = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);

                if (list != null && !list.isEmpty()) {
                    mItems.addAll(list);
                    mAdapter.notifyDataSetChanged();

                    return;
                }
            }
        }

        parseData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
        outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
    }

    private void parseData() {
        if (!NetUtils.isOnline(this)) {
            mErrorWifi.setVisibility(View.VISIBLE);
            mErrorGeneral.setVisibility(View.GONE);

            if (mAdapter != null) {
                mItems.clear();
                mAdapter.notifyDataSetChanged();
            }

            mRefresh.setRefreshing(false);
            return;
        }

        mRefresh.setRefreshing(true);

        ParkingApi parkingApi = RestClient.ADAPTER.create(ParkingApi.class);
        parkingApi.getParking()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParkingResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        mErrorGeneral.setVisibility(View.VISIBLE);

                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }

                        mRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onNext(ParkingResponse parkingResponse) {
                        mItems.clear();
                        mItems.addAll(parkingResponse.parking);

                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }

                        mErrorGeneral.setVisibility(View.GONE);
                        mErrorWifi.setVisibility(View.GONE);

                        mRefresh.setRefreshing(false);
                    }
                });
    }
}