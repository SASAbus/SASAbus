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

package it.sasabz.android.sasabus.ui.ecopoints.detail;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.NetUtils;
import it.sasabz.android.sasabus.data.network.auth.AuthHelper;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.data.network.rest.model.Badge;
import it.sasabz.android.sasabus.data.network.rest.response.BadgesResponse;
import it.sasabz.android.sasabus.ui.ecopoints.LoginActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.BadgeAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BadgesActivity extends AppCompatActivity {

    private static final String TAG = "BadgesActivity";

    @BindView(R.id.recycler) RecyclerView recyclerView;
    @BindView(R.id.refresh) SwipeRefreshLayout mRefresh;

    @BindView(R.id.error_general) RelativeLayout errorGeneral;
    @BindView(R.id.error_wifi) RelativeLayout errorWifi;

    private ArrayList<Badge> mItems;
    private BadgeAdapter mAdapter;

    private BroadcastReceiver logoutReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthHelper.isLoggedIn()) {
            Timber.e("Token is null, showing login activity");
            finish();
            startActivity(new Intent(this, LoginActivity.class));

            return;
        }

        logoutReceiver = AuthHelper.registerLogoutReceiver(this);

        setContentView(R.layout.activity_eco_points_badges);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRefresh.setColorSchemeResources(Config.REFRESH_COLORS);
        mRefresh.setOnRefreshListener(this::parseData);

        mItems = new ArrayList<>();
        mAdapter = new BadgeAdapter(this, mItems);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });

        parseData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AuthHelper.unregisterLogoutReceiver(this, logoutReceiver);
    }

    private void parseData() {
        if (!NetUtils.isOnline(this)) {
            errorWifi.setVisibility(View.VISIBLE);
            errorGeneral.setVisibility(View.GONE);

            mItems.clear();
            mAdapter.notifyDataSetChanged();

            mRefresh.setRefreshing(false);

            return;
        }

        mRefresh.setRefreshing(true);

        EcoPointsApi ecoPointsApi = RestClient.ADAPTER.create(EcoPointsApi.class);
        ecoPointsApi.getAllBadges()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BadgesResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        AuthHelper.checkIfUnauthorized(BadgesActivity.this, e);

                        mItems.clear();
                        mAdapter.notifyDataSetChanged();

                        errorGeneral.setVisibility(View.VISIBLE);
                        errorWifi.setVisibility(View.GONE);

                        mRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onNext(BadgesResponse badgesResponse) {
                        mItems.clear();
                        mItems.addAll(badgesResponse.badges);

                        mAdapter.notifyDataSetChanged();

                        errorGeneral.setVisibility(View.GONE);
                        errorWifi.setVisibility(View.GONE);

                        mRefresh.setRefreshing(false);
                    }
                });
    }
}
