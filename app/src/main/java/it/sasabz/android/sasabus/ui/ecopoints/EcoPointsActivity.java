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
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.network.rest.model.Badge;
import it.sasabz.android.sasabus.network.rest.model.LeaderboardPlayer;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.ecopoints.detail.ProfileActivity;
import it.sasabz.android.sasabus.ui.ecopoints.event.EventsFragment;
import it.sasabz.android.sasabus.ui.widget.adapter.TabsAdapter;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.LeaderboardAdapter;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class EcoPointsActivity extends BaseActivity {

    private static final String TAG = "EcoPointsActivity";

    public static final String EXTRA_SHOW_EVENTS = "it.sasabz.android.sasabus.EXTRA_SHOW_EVENTS";
    public static final String EXTRA_SHOW_BADGES = "it.sasabz.android.sasabus.EXTRA_SHOW_BADGES";

    private static final String FRAGMENT_PROFILE = "PROFILE";
    private static final String FRAGMENT_BADGES = "BADGES";
    private static final String FRAGMENT_EVENTS = "EVENTS";

    public static final int ECO_POINTS_PROFILE_RESULT = 1001;

    private ProfileFragment mProfileFragment;
    private BadgesFragment mBadgesFragment;
    private EventsFragment mEventsFragment;

    @BindView(R.id.viewpager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;

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

        TabsAdapter mAdapter = new TabsAdapter(getSupportFragmentManager(), false);

        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));

        if (savedInstanceState != null) {
            mProfileFragment = (ProfileFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_PROFILE);

            mBadgesFragment = (BadgesFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_BADGES);

            mEventsFragment = (EventsFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_EVENTS);
        }

        if (mProfileFragment == null) {
            mProfileFragment = new ProfileFragment();
        }

        if (mBadgesFragment == null) {
            mBadgesFragment = new BadgesFragment();
        }

        if (mEventsFragment == null) {
            mEventsFragment = new EventsFragment();
        }

        mAdapter.addFragment(mProfileFragment, getString(R.string.fragment_eco_points_profile));
        mAdapter.addFragment(mBadgesFragment, getString(R.string.fragment_eco_points_badges));
        mAdapter.addFragment(mEventsFragment, getString(R.string.fragment_eco_points_events));

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        Intent intent = getIntent();
        if (intent.hasExtra(Config.EXTRA_BADGE)) {
            Badge badge = intent.getParcelableExtra(Config.EXTRA_BADGE);
            showBadgeDialog(this, badge);
        }

        if (intent.hasExtra(EXTRA_SHOW_BADGES)) {
            mViewPager.setCurrentItem(1);
        }

        if (intent.hasExtra(EXTRA_SHOW_EVENTS)) {
            mViewPager.setCurrentItem(2);
        }
    }

    @Override
    protected int getNavItem() {
        return NAVDRAWER_ITEM_ECO_POINTS;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_PROFILE, mProfileFragment);
            getSupportFragmentManager().putFragment(outState, FRAGMENT_BADGES, mBadgesFragment);
        } catch (IllegalStateException e) {
            Utils.logException(e);
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
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_PROFILE, mProfileFragment.profile);

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
            File profileFile = (File) data.getSerializableExtra(ProfileActivity.EXTRA_PROFILE_FILE);
            String profileUrl = data.getStringExtra(ProfileActivity.EXTRA_PROFILE_URL);

            LeaderboardAdapter.ViewHolder viewHolder = findViewHolderForProfile();

            if (profileFile != null) {
                Glide.with(this)
                        .load(profileFile)
                        .into(mProfileFragment.profilePicture);

                if (viewHolder != null) {
                    Glide.with(this)
                            .load(profileFile)
                            .into(viewHolder.profilePicture);
                }
            } else if (profileUrl != null) {
                Glide.with(this)
                        .load(profileUrl)
                        .into(mProfileFragment.profilePicture);

                if (viewHolder != null) {
                    Glide.with(this)
                            .load(profileUrl)
                            .into(viewHolder.profilePicture);
                }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(Config.EXTRA_BADGE)) {
            Badge badge = intent.getParcelableExtra(Config.EXTRA_BADGE);
            showBadgeDialog(this, badge);
        }
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

        progress.setProgress(badge.progress);

        ImageView image = (ImageView) view.findViewById(R.id.dialog_badge_image);

        Glide.with(activity)
                .load(badge.iconUrl)
                .into(image);

        new AlertDialog.Builder(activity, R.style.DialogStyle)
                .setView(view)
                .create()
                .show();
    }

    private LeaderboardAdapter.ViewHolder findViewHolderForProfile() {
        if (mProfileFragment == null || mProfileFragment.mItems == null ||
                mProfileFragment.recyclerView == null || mProfileFragment.profile == null) {
            return null;
        }

        ArrayList<LeaderboardPlayer> list = mProfileFragment.mItems;
        for (int i = 0; i < list.size(); i++) {
            LeaderboardPlayer player = list.get(i);

            if (player.id.equals(mProfileFragment.profile.id)) {
                LogUtils.e(TAG, "Found view holder for profile");

                return (LeaderboardAdapter.ViewHolder) mProfileFragment.recyclerView
                        .findViewHolderForLayoutPosition(i);
            }
        }

        return null;
    }
}
