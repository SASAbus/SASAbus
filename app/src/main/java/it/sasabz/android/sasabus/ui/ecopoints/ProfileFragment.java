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

package it.sasabz.android.sasabus.ui.ecopoints;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.network.rest.model.LeaderboardPlayer;
import it.sasabz.android.sasabus.network.rest.model.Profile;
import it.sasabz.android.sasabus.network.rest.response.LeaderboardResponse;
import it.sasabz.android.sasabus.network.rest.response.ProfileResponse;
import it.sasabz.android.sasabus.ui.ecopoints.detail.LeaderboardActivity;
import it.sasabz.android.sasabus.ui.ecopoints.detail.ProfileActivity;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.LeaderboardAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProfileFragment extends RxFragment implements View.OnClickListener {

    @BindView(R.id.eco_points_profile_layout) LinearLayout profileLayout;

    @BindView(R.id.eco_points_profile_picture) ImageView profilePicture;
    @BindView(R.id.eco_points_profile_name) TextView profileName;
    @BindView(R.id.eco_points_profile_level) TextView profileLevel;
    @BindView(R.id.eco_points_profile_points) TextView profilePoints;
    @BindView(R.id.eco_points_profile_badges) TextView profileBadges;
    @BindView(R.id.eco_points_profile_rank) TextView profileRank;

    @BindView(R.id.eco_points_profile_full_details) TextView fullProfileText;
    @BindView(R.id.eco_points_leaderboard_details) TextView fullLeaderboardText;

    @BindView(R.id.eco_points_card_1_progress) ProgressBar card1Progress;
    @BindView(R.id.eco_points_card_2_progress) ProgressBar card2Progress;

    @BindView(R.id.recycler_leaderboard) RecyclerView recyclerView;

    public ArrayList<LeaderboardPlayer> mItems;
    public LeaderboardAdapter mAdapter;

    public Profile profile;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_eco_points_profile, container, false);
        ButterKnife.bind(this, view);

        mItems = new ArrayList<>();
        mAdapter = new LeaderboardAdapter(getActivity(), mItems);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });

        fullProfileText.setOnClickListener(this);
        fullLeaderboardText.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        parseProfileInfo();
        parseLeaderboard();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.eco_points_profile_full_details:
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_PROFILE, profile);

                // Start the activity with startActivityForResult so we can be notified
                // if the profile picture has been uploaded and can change it accordingly.
                getActivity()
                        .startActivityForResult(intent, EcoPointsActivity.ECO_POINTS_PROFILE_RESULT);
                break;
            case R.id.eco_points_leaderboard_details:
                startActivity(new Intent(getActivity(), LeaderboardActivity.class));
                break;
        }
    }

    private void parseProfileInfo() {
        if (!NetUtils.isOnline(getActivity())) {
            return;
        }

        EcoPointsApi ecoPointsApi = RestClient.ADAPTER.create(EcoPointsApi.class);
        ecoPointsApi.getProfile()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ProfileResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        AuthHelper.checkIfUnauthorized(getActivity(), e);
                    }

                    @Override
                    public void onNext(ProfileResponse response) {
                        card1Progress.setVisibility(View.GONE);
                        profileLayout.setVisibility(View.VISIBLE);

                        Profile profile = response.profile;
                        ProfileFragment.this.profile = profile;

                        Glide.with(ProfileFragment.this)
                                .load(Endpoint.API + Endpoint.ECO_POINTS_PROFILE_PICTURE_USER + profile.profile)
                                .into(profilePicture);

                        profileName.setText(profile.username);
                        profileLevel.setText(profile.cls);

                        profileBadges.setText(String.valueOf(profile.badges));
                        profileRank.setText(String.valueOf(profile.rank));
                        profilePoints.setText(String.valueOf(profile.points));
                    }
                });
    }

    private void parseLeaderboard() {
        if (!NetUtils.isOnline(getActivity())) {
            return;
        }

        mItems.clear();
        mAdapter.notifyDataSetChanged();

        EcoPointsApi ecoPointsApi = RestClient.ADAPTER.create(EcoPointsApi.class);
        ecoPointsApi.getLeaderboard(1)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LeaderboardResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        AuthHelper.checkIfUnauthorized(getActivity(), e);
                    }

                    @Override
                    public void onNext(LeaderboardResponse leaderboardResponse) {
                        card2Progress.setVisibility(View.GONE);

                        mItems.clear();
                        mItems.addAll(leaderboardResponse.leaderboard);

                        mAdapter.notifyDataSetChanged();

                        new Handler().postDelayed(() -> {

                        }, 500);
                    }
                });
    }
}
