package it.sasabz.android.sasabus.ui.ecopoints;

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
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.network.rest.model.LeaderboardPlayer;
import it.sasabz.android.sasabus.network.rest.response.LeaderboardResponse;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.LeaderboardDetailsAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EcoPointsLeaderboardActivity extends AppCompatActivity {

    private static final String TAG = "EcoPointsLeaderboardActivity";

    @BindView(R.id.recycler) RecyclerView recyclerView;

    private ArrayList<LeaderboardPlayer> mItems;
    private LeaderboardDetailsAdapter mAdapter;

    private int pageIndex;

    private BroadcastReceiver logoutReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthHelper.isTokenValid()) {
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
            mItems.add(null);
            mAdapter.notifyItemInserted(mItems.size() - 1);

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
                        Utils.handleException(e);

                        AuthHelper.checkIfUnauthorized(EcoPointsLeaderboardActivity.this, e);
                    }

                    @Override
                    public void onNext(LeaderboardResponse leaderboardResponse) {
                        mItems.removeAll(Collections.singleton((LeaderboardPlayer) null));

                        mAdapter.notifyDataSetChanged();

                        mItems.addAll(leaderboardResponse.leaderboard);
                        mAdapter.notifyDataSetChanged();

                        mAdapter.setLoading(false);
                    }
                });
    }
}
