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
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.network.rest.model.Badge;
import it.sasabz.android.sasabus.network.rest.response.BadgesResponse;
import it.sasabz.android.sasabus.ui.ecopoints.detail.BadgesActivity;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.BadgeAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BadgesFragment extends RxFragment implements View.OnClickListener {

    @BindView(R.id.eco_points_badge_details) TextView fullBadgesText;

    @BindView(R.id.eco_points_card_3_progress) ProgressBar card3Progress;
    @BindView(R.id.eco_points_card_4_progress) ProgressBar card4Progress;

    @BindView(R.id.recycler_badges_next) RecyclerView nextBadgesRecycler;
    @BindView(R.id.recycler_badges_earned) RecyclerView earnedBadgesRecycler;

    @BindView(R.id.eco_points_no_earned_badges) TextView noEarnedBadges;

    private ArrayList<Badge> nextBadges;
    private BadgeAdapter nextBadgeAdapter;

    private ArrayList<Badge> earnedBadges;
    private BadgeAdapter earnedBadgeAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_eco_points_badges, container, false);
        ButterKnife.bind(this, view);

        nextBadges = new ArrayList<>();
        nextBadgeAdapter = new BadgeAdapter(getActivity(), nextBadges);

        nextBadgesRecycler.setAdapter(nextBadgeAdapter);
        nextBadgesRecycler.setNestedScrollingEnabled(false);
        nextBadgesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        earnedBadges = new ArrayList<>();
        earnedBadgeAdapter = new BadgeAdapter(getActivity(), earnedBadges);

        earnedBadgesRecycler.setAdapter(earnedBadgeAdapter);
        earnedBadgesRecycler.setNestedScrollingEnabled(false);
        earnedBadgesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        fullBadgesText.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        parseNextBadges();
        parseEarnedBadges();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.eco_points_badge_details:
                startActivity(new Intent(getActivity(), BadgesActivity.class));
                break;
        }
    }

    private void parseNextBadges() {
        if (!NetUtils.isOnline(getActivity())) {
            return;
        }

        nextBadges.clear();
        nextBadgeAdapter.notifyDataSetChanged();

        EcoPointsApi ecoPointsApi = RestClient.ADAPTER.create(EcoPointsApi.class);
        ecoPointsApi.getNextBadges()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BadgesResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        AuthHelper.checkIfUnauthorized(getActivity(), e);
                    }

                    @Override
                    public void onNext(BadgesResponse response) {
                        card3Progress.setVisibility(View.GONE);

                        nextBadges.clear();
                        nextBadges.addAll(response.badges);

                        nextBadgeAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void parseEarnedBadges() {
        if (!NetUtils.isOnline(getActivity())) {
            return;
        }

        earnedBadges.clear();
        earnedBadgeAdapter.notifyDataSetChanged();

        EcoPointsApi ecoPointsApi = RestClient.ADAPTER.create(EcoPointsApi.class);
        ecoPointsApi.getEarnedBadges()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BadgesResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        AuthHelper.checkIfUnauthorized(getActivity(), e);
                    }

                    @Override
                    public void onNext(BadgesResponse response) {
                        card4Progress.setVisibility(View.GONE);

                        earnedBadges.clear();
                        earnedBadges.addAll(response.badges);

                        if (earnedBadges.isEmpty()) {
                            noEarnedBadges.setVisibility(View.VISIBLE);
                        }

                        earnedBadgeAdapter.notifyDataSetChanged();
                    }
                });
    }
}
