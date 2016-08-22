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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.network.rest.model.Badge;
import it.sasabz.android.sasabus.network.rest.model.LeaderboardPlayer;
import it.sasabz.android.sasabus.network.rest.model.Profile;
import it.sasabz.android.sasabus.network.rest.response.BadgesResponse;
import it.sasabz.android.sasabus.network.rest.response.LeaderboardResponse;
import it.sasabz.android.sasabus.network.rest.response.ProfileResponse;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.BadgeAdapter;
import it.sasabz.android.sasabus.util.recycler.LeaderboardAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author David Dejori
 */
public class EcoPointsActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "EcoPointsActivity";

    public static final int ECO_POINTS_PROFILE_RESULT = 1001;

    @BindView(R.id.eco_points_profile_picture) ImageView profilePicture;
    @BindView(R.id.eco_points_profile_name) TextView profileName;
    @BindView(R.id.eco_points_profile_level) TextView profileLevel;
    @BindView(R.id.eco_points_profile_points) TextView profilePoints;
    @BindView(R.id.eco_points_profile_badges) TextView profileBadges;
    @BindView(R.id.eco_points_profile_rank) TextView profileRank;

    @BindView(R.id.eco_points_profile_full_details) TextView fullProfileText;
    @BindView(R.id.eco_points_leaderboard_details) TextView fullLeaderboardText;
    @BindView(R.id.eco_points_badge_details) TextView fullBadgesText;

    @BindView(R.id.eco_points_card_1_progress) ProgressBar card1Progress;
    @BindView(R.id.eco_points_card_2_progress) ProgressBar card2Progress;
    @BindView(R.id.eco_points_card_3_progress) ProgressBar card3Progress;
    @BindView(R.id.eco_points_card_4_progress) ProgressBar card4Progress;

    @BindView(R.id.eco_points_profile_layout) LinearLayout profileLayout;

    @BindView(R.id.recycler_leaderboard) RecyclerView leaderboardRecycler;
    @BindView(R.id.recycler_badges_next) RecyclerView nextBadgesRecycler;
    @BindView(R.id.recycler_badges_earned) RecyclerView earnedBadgesRecycler;

    @BindView(R.id.eco_points_no_earned_badges) TextView noEarnedBadges;

    private ArrayList<LeaderboardPlayer> leaderboardItems;
    private LeaderboardAdapter leaderboardAdapter;

    private ArrayList<Badge> nextBadges;
    private BadgeAdapter nextBadgeAdapter;

    private ArrayList<Badge> earnedBadges;
    private BadgeAdapter earnedBadgeAdapter;

    private Profile profile;

    private BroadcastReceiver logoutReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthHelper.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));

            return;
        }

        logoutReceiver = AuthHelper.registerLogoutReceiver(this);

        setContentView(R.layout.activity_eco_points);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        leaderboardItems = new ArrayList<>();
        leaderboardAdapter = new LeaderboardAdapter(this, leaderboardItems);

        leaderboardRecycler.setAdapter(leaderboardAdapter);
        leaderboardRecycler.setNestedScrollingEnabled(false);
        leaderboardRecycler.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });

        nextBadges = new ArrayList<>();
        nextBadgeAdapter = new BadgeAdapter(this, nextBadges);

        nextBadgesRecycler.setAdapter(nextBadgeAdapter);
        nextBadgesRecycler.setNestedScrollingEnabled(false);
        nextBadgesRecycler.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });

        earnedBadges = new ArrayList<>();
        earnedBadgeAdapter = new BadgeAdapter(this, earnedBadges);

        earnedBadgesRecycler.setAdapter(earnedBadgeAdapter);
        earnedBadgesRecycler.setNestedScrollingEnabled(false);
        earnedBadgesRecycler.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });

        fullProfileText.setOnClickListener(this);
        fullLeaderboardText.setOnClickListener(this);
        fullBadgesText.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent.hasExtra(Config.EXTRA_BADGE)) {
            Badge badge = intent.getParcelableExtra(Config.EXTRA_BADGE);
            showBadgeDialog(this, badge);
        }

        parseProfileInfo();
        parseLeaderboard();
        parseNextBadges();
        parseEarnedBadges();
    }

    @Override
    protected int getNavItem() {
        return NAVDRAWER_ITEM_ECO_POINTS;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.eco_points_profile_full_details:
                Intent intent = new Intent(this, EcoPointsProfileActivity.class);
                intent.putExtra(EcoPointsProfileActivity.EXTRA_PROFILE, profile);

                // Start the activity with startActivityForResult so we can be notified
                // if the profile picture has been uploaded and can change it accordingly.
                startActivityForResult(intent, ECO_POINTS_PROFILE_RESULT);
                break;
            case R.id.eco_points_leaderboard_details:
                startActivity(new Intent(this, EcoPointsLeaderboardActivity.class));
                break;
            case R.id.eco_points_badge_details:
                startActivity(new Intent(this, EcoPointsBadgesActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_eco_points, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_account_settings:
                Intent intent = new Intent(this, EcoPointsProfileActivity.class);
                intent.putExtra(EcoPointsProfileActivity.EXTRA_PROFILE, profile);

                // Start the activity with startActivityForResult so we can be notified
                // if the profile picture has been uploaded and can change it accordingly.
                startActivityForResult(intent, ECO_POINTS_PROFILE_RESULT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ECO_POINTS_PROFILE_RESULT && resultCode == RESULT_OK) {
            File profileFile = (File) data.getSerializableExtra(EcoPointsProfileActivity.EXTRA_PROFILE_FILE);
            String profileUrl = data.getStringExtra(EcoPointsProfileActivity.EXTRA_PROFILE_URL);

            if (profileFile != null) {
                Glide.with(this)
                        .load(profileFile)
                        .into(profilePicture);
            } else if (profileUrl != null) {
                Glide.with(this)
                        .load(profileUrl)
                        .into(profilePicture);
            } else {
                LogUtils.e(TAG, "Missing bundle extra 'profileFile' or 'profileUrl'");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AuthHelper.unregisterLogoutReceiver(this, logoutReceiver);
    }

    private void parseProfileInfo() {
        if (!NetUtils.isOnline(this)) {
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
                        Utils.handleException(e);

                        AuthHelper.checkIfUnauthorized(EcoPointsActivity.this, e);
                    }

                    @Override
                    public void onNext(ProfileResponse response) {
                        card1Progress.setVisibility(View.GONE);
                        profileLayout.setVisibility(View.VISIBLE);

                        Profile profile = response.profile;
                        EcoPointsActivity.this.profile = profile;

                        Glide.with(EcoPointsActivity.this)
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
        if (!NetUtils.isOnline(this)) {
            return;
        }

        leaderboardItems.clear();
        leaderboardAdapter.notifyDataSetChanged();

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
                        Utils.handleException(e);

                        AuthHelper.checkIfUnauthorized(EcoPointsActivity.this, e);
                    }

                    @Override
                    public void onNext(LeaderboardResponse leaderboardResponse) {
                        card2Progress.setVisibility(View.GONE);

                        leaderboardItems.clear();
                        leaderboardItems.addAll(leaderboardResponse.leaderboard);

                        leaderboardAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void parseNextBadges() {
        if (!NetUtils.isOnline(this)) {
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
                        Utils.handleException(e);

                        AuthHelper.checkIfUnauthorized(EcoPointsActivity.this, e);
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
        if (!NetUtils.isOnline(this)) {
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
                        Utils.handleException(e);

                        AuthHelper.checkIfUnauthorized(EcoPointsActivity.this, e);
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

    public static void showBadgeDialog(Activity activity, Badge badge) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.dialog_badge_details, null, false);

        TextView title = (TextView) view.findViewById(R.id.dialog_badge_title);
        TextView description = (TextView) view.findViewById(R.id.dialog_badge_description);
        TextView points = (TextView) view.findViewById(R.id.dialog_badge_points);
        TextView users = (TextView) view.findViewById(R.id.dialog_badge_users);
        TextView progressText = (TextView) view.findViewById(R.id.dialog_badge_progress_text);
        ProgressBar progress = (ProgressBar) view.findViewById(R.id.dialog_badge_progress);

        title.setText(badge.title);
        description.setText(badge.description);

        points.setText(activity.getResources().getQuantityString(
                R.plurals.eco_points_badge_dialog_points, badge.points, badge.points));

        users.setText(activity.getResources().getQuantityString(
                R.plurals.eco_points_badge_dialog_users, badge.users, badge.users));

        progressText.setText(activity.getString(R.string.eco_points_badge_dialog_progress,
                badge.progress));

        progress.setProgress(badge.progress <= 3 ? 3 : badge.progress);

        ImageView image = (ImageView) view.findViewById(R.id.dialog_badge_image);

        Glide.with(activity)
                .load(badge.iconUrl)
                .into(image);

        new AlertDialog.Builder(activity, R.style.DialogStyle)
                .setView(view)
                .create()
                .show();
    }
}
