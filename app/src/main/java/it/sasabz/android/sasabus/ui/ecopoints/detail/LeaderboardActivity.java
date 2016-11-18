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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.NetUtils;
import it.sasabz.android.sasabus.data.network.auth.AuthHelper;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.data.network.rest.model.LeaderboardPlayer;
import it.sasabz.android.sasabus.data.network.rest.response.LeaderboardResponse;
import it.sasabz.android.sasabus.ui.ecopoints.LoginActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.LeaderboardDetailsAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String TAG = "LeaderboardActivity";

    @BindView(R.id.recycler) RecyclerView recyclerView;

    private ArrayList<LeaderboardPlayer> mItems;
    private LeaderboardDetailsAdapter mAdapter;

    private int pageIndex;

    private BroadcastReceiver logoutReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthHelper.isLoggedIn()) {
            LogUtils.e(TAG, "Token is null, showing login activity");
            finish();
            startActivity(new Intent(this, LoginActivity.class));

            return;
        }

        logoutReceiver = AuthHelper.registerLogoutReceiver(this);

        setContentView(R.layout.activity_eco_points_leaderboard);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItems = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });

        mAdapter = new LeaderboardDetailsAdapter(this, mItems, recyclerView, () -> {
            pageIndex++;
            parseData();
        });

        recyclerView.setAdapter(mAdapter);

        mAdapter.setLoading(true);
        mAdapter.mListener.loadMore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AuthHelper.unregisterLogoutReceiver(this, logoutReceiver);
    }

    private void parseData() {
        if (!NetUtils.isOnline(this)) {
            return;
        }

        EcoPointsApi ecoPointsApi = RestClient.ADAPTER.create(EcoPointsApi.class);
        ecoPointsApi.getLeaderboard(pageIndex)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LeaderboardResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        AuthHelper.checkIfUnauthorized(LeaderboardActivity.this, e);
                    }

                    @Override
                    public void onNext(LeaderboardResponse response) {
                        mItems.removeAll(Collections.singleton((LeaderboardPlayer) null));

                        mAdapter.notifyDataSetChanged();

                        mItems.addAll(response.leaderboard);
                        mAdapter.notifyDataSetChanged();

                        if (response.leaderboard.size() < LeaderboardDetailsAdapter.PAGE_SIZE) {
                            mAdapter.setNoMoreItems(true);
                        }

                        mAdapter.setLoading(false);
                    }
                });
    }
}
