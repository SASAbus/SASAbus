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

package it.sasabz.android.sasabus.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.provider.PlanData;
import it.sasabz.android.sasabus.ui.about.AboutActivity;
import it.sasabz.android.sasabus.ui.busstop.BusStopActivity;
import it.sasabz.android.sasabus.ui.ecopoints.EcoPointsActivity;
import it.sasabz.android.sasabus.ui.intro.Intro;
import it.sasabz.android.sasabus.ui.intro.data.IntroData;
import it.sasabz.android.sasabus.ui.line.LinesActivity;
import it.sasabz.android.sasabus.ui.parking.ParkingActivity;
import it.sasabz.android.sasabus.ui.route.RouteActivity;
import it.sasabz.android.sasabus.util.Changelog;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;

/**
 * A base activity that handles common functionality in the app. This includes the
 * navigation drawer, handling of app upgrades, Action Bar tweaks, amongst others.
 * <p>
 * All activities which are present in the navigation drawer have to subclass this class to
 * show the navigation drawer with all drawer items.
 *
 * @author Alex Lardschneider
 */
public abstract class BaseActivity extends RxAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "BaseActivity";

    /**
     * Navigation drawer
     */
    private DrawerLayout mDrawerLayout;

    /**
     * Handler to help with fading the main content
     */
    private final Handler mHandler = new Handler();

    /**
     * Use this action if you want to fade the activity content, i.e. after you click
     * on a navigation drawer item.
     */
    private static final String ACTION_FADE_CONTENT = "it.sasabz.android.sasabus.ACTION_FADE_CONTENT";

    public static final String ACTION_NO_CHANGELOG = "it.sasabz.android.sasabus.ACTION_NO_CHANGELOG";

    /**
     * The menu item ids for the navigation drawer.
     */
    static final int NAVDRAWER_ITEM_MAP = R.id.nav_map;
    protected static final int NAVDRAWER_ITEM_LINES = R.id.nav_lines;
    protected static final int NAVDRAWER_ITEM_BUS_STOPS = R.id.nav_stations;
    protected static final int NAVDRAWER_ITEM_ROUTE = R.id.nav_route;
    protected static final int NAVDRAWER_ITEM_ECO_POINTS = R.id.nav_eco_points;
    static final int NAVDRAWER_ITEM_TIMETABLES = R.id.nav_timetables;
    static final int NAVDRAWER_ITEM_NEWS = R.id.nav_news;
    protected static final int NAVDRAWER_ITEM_PARKING = R.id.nav_parking;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;

    /**
     * Delay to launch nav drawer item, to allow close animation to play
     */
    private static final int NAVDRAWER_LAUNCH_DELAY = 300;

    /**
     * Fade in and fade out durations for the main content when switching between
     * different Activities of the app through the navigation drawer
     */
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 300;

    /**
     * Toolbar
     */
    private Toolbar mToolbar;

    /**
     * The main content, consisting of a {@link android.support.design.widget.CoordinatorLayout}
     * to allow {@link android.support.design.widget.Snackbar} animations to play.
     */
    private View mMainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the Intro screen has been showed; if not, show it.
        if (SettingsUtils.shouldShowIntro(this)) {
            Intent intent = new Intent(this, Intro.class);
            startActivity(intent);

            finish();

            return;
        } else if (SettingsUtils.isDataUpdateAvailable(this) || !PlanData.planDataExists(this)) {
            Intent intent = new Intent(this, IntroData.class);
            startActivity(intent);

            finish();

            return;
        } else if (!ACTION_NO_CHANGELOG.equals(getIntent().getAction()) &&
                SettingsUtils.shouldShowChangelog(this)) {
            Changelog.showDialog(this);
        }

        overridePendingTransition(0, 0);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        setUpToolbar();
        setUpNavDrawer();

        mMainContent = findViewById(R.id.main_content);
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Intent intent = getIntent();

        /*
         * Fade the main content if we transition from a navigation drawer activity
         */
        if (savedInstanceState == null && intent.getAction() != null &&
                intent.getAction().equals(ACTION_FADE_CONTENT)) {
            if (mMainContent != null) {
                mMainContent.setAlpha(0);
                mMainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
            } else {
                LogUtils.e(TAG, "No view with ID main_content to fade in.");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, PreferenceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getNavItem() == NAVDRAWER_ITEM_INVALID ||
                getNavItem() == NAVDRAWER_ITEM_BUS_STOPS ||
                getNavItem() == NAVDRAWER_ITEM_MAP ||
                getNavItem() == NAVDRAWER_ITEM_ECO_POINTS) {
            return false;
        }

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();

        onNavDrawerItemClicked(menuItem.getItemId());
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.getMenu().findItem(getNavItem()).setChecked(true);
        }
    }


    /**
     * Override this method in Activities which have a entry in the navigation
     * drawer. The implemented method should return the menu id corresponding to its drawer
     * entry.
     *
     * @return the menu id of the current activity, or {@link #NAVDRAWER_ITEM_INVALID}
     * if the activity is not present in the navigation drawer
     */
    protected abstract int getNavItem();

    /**
     * Set up the Toolbar
     */
    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    /**
     * Set up the navigation drawer and the click listeners for the drawer.
     * Also hides the scrollbar from the navigation drawer and sets up the action bar
     * drawer toggle animation.
     */
    private void setUpNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (mDrawerLayout == null || mToolbar == null) {
            return;
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);

            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }

            navigationView.getMenu().findItem(getNavItem()).setChecked(true);
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    /**
     * Handles the navigation from the navigation drawer.
     *
     * @param item the menu item id where the click was performed.
     */
    private void goToNavDrawerItem(int item) {
        switch (item) {
            case NAVDRAWER_ITEM_MAP:
                createBackStack(new Intent(this, MapActivity.class));
                break;
            case NAVDRAWER_ITEM_LINES:
                createBackStack(new Intent(this, LinesActivity.class));
                break;
            case NAVDRAWER_ITEM_BUS_STOPS:
                createBackStack(new Intent(this, BusStopActivity.class));
                break;
            case NAVDRAWER_ITEM_ROUTE:
                createBackStack(new Intent(this, RouteActivity.class));
                break;
            case NAVDRAWER_ITEM_ECO_POINTS:
                createBackStack(new Intent(this, EcoPointsActivity.class));
                break;
            case NAVDRAWER_ITEM_TIMETABLES:
                createBackStack(new Intent(this, TimetableActivity.class));
                break;
            case NAVDRAWER_ITEM_NEWS:
                createBackStack(new Intent(this, NewsActivity.class));
                break;
            case NAVDRAWER_ITEM_PARKING:
                createBackStack(new Intent(this, ParkingActivity.class));
                break;
            default:
                throw new IllegalStateException("Unknown nav drawer item id " + item);
        }
    }

    /**
     * Executed when a navigation item is clicked.
     *
     * @param itemId the clicked on item id.
     */
    private void onNavDrawerItemClicked(int itemId) {
        if (itemId == getNavItem()) {
            closeNavDrawer();
            return;
        }

        // launch the target Activity after a short delay, to allow the close animation to play
        mHandler.postDelayed(() -> goToNavDrawerItem(itemId), NAVDRAWER_LAUNCH_DELAY);

        // fade out the main content
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }

        closeNavDrawer();
    }

    /**
     * Checks if the navigation drawer is currently open.
     *
     * @return {@code true} if open, {@code false} otherwise.
     */
    private boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    /**
     * Close the navigation drawer
     */
    private void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * Enables back navigation for activities that are launched from the NavBar. See
     * {@code AndroidManifest.xml} to find out the parent activity names for each activity.
     *
     * @param intent the intent of the activity
     */
    private void createBackStack(Intent intent) {
        intent.setAction(ACTION_FADE_CONTENT);

        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    /**
     * This utility method handles Up navigation intents by searching for a parent activity and
     * navigating there if defined. When using this for an activity make sure to define both the
     * native parentActivity as well as the AppCompat one when supporting API levels less than 16.
     * when the activity has a single parent activity. If the activity doesn't have a single parent
     * activity then don't define one and this method will use back button functionality. If "Up"
     * functionality is still desired for activities without parents then use
     * {@code syntheticParentActivity} to define one dynamically.
     * <p>
     * Note: Up navigation intents are represented by a back arrow in the top left of the Toolbar
     * in Material Design guidelines.
     *
     * @param currentActivity Activity in use when navigate Up action occurred.
     */
    protected static void navigateUpOrBack(Activity currentActivity) {
        // Retrieve parent activity from AndroidManifest.
        Intent intent = NavUtils.getParentActivityIntent(currentActivity);

        if (intent == null) {
            // No parent defined in manifest. This indicates the activity may be used by
            // in multiple flows throughout the app and doesn't have a strict parent. In
            // this case the navigation up button should act in the same manner as the
            // back button. This will result in users being forwarded back to other
            // applications if currentActivity was invoked from another application.
            currentActivity.onBackPressed();
        } else {
            if (NavUtils.shouldUpRecreateTask(currentActivity, intent)) {
                // Need to synthesize a backstack since currentActivity was probably invoked by a
                // different app. The preserves the "Up" functionality within the app according to
                // the activity hierarchy defined in AndroidManifest.xml via parentActivity
                // attributes.
                TaskStackBuilder builder = TaskStackBuilder.create(currentActivity);
                builder.addNextIntentWithParentStack(intent);
                builder.startActivities();
            } else {
                // Navigate normally to the manifest defined "Up" activity.
                NavUtils.navigateUpTo(currentActivity, intent);
            }
        }
    }

    /**
     * Returns the mToolbar currently in use
     *
     * @return {@link #mToolbar}
     */
    @Nullable
    protected Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * Returns the main content layout in use,
     * mostly {@link android.support.design.widget.CoordinatorLayout}
     *
     * @return {@link #mMainContent}
     */
    @Nullable
    public View getMainContent() {
        return mMainContent;
    }

    protected String locale() {
        return Utils.locale(this);
    }
}
