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

package it.sasabz.android.sasabus.ui.departure;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.realm.busstop.BusStop;
import it.sasabz.android.sasabus.data.realm.user.FavoriteBusStop;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Strings;
import it.sasabz.android.sasabus.util.UIUtils;
import it.sasabz.android.sasabus.util.Utils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

import static io.realm.Case.INSENSITIVE;

/**
 * Allows the user to pick a departure/arrival bus stop by searching it by either title or
 * municipality. When starting this activity it shows a nice reveal animation.
 *
 * @author Alex Lardschneider
 */
public class DepartureSearchActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "SearchActivity";
    private static final String SCREEN_LABEL = "SearchDepartures";

    public static final String EXTRA_X_POS = "com.davale.sasabus.EXTRA_X_POS";
    public static final String EXTRA_Y_POS = "com.davale.sasabus.EXTRA_Y_POS";

    public static final String EXTRA_SHOW_FAVORITES = "com.davale.sasabus.EXTRA_SHOW_FAVORITES";

    private SearchView mSearchView;
    private BusStopPickerAdapter mAdapter;
    private ArrayList<it.sasabz.android.sasabus.data.model.BusStop> mItems;

    private final int[] mDefaultBusStops = {
            61,  // Casanova
            102, // Fiera
            105, // Firmian
            175, // Ospedale
            209, // Piazza Domenicani
            227, // Piazza Vittoria (Via Cesare Battisti)
            544, // Piazza Vittoria (Corso Libertà)
            229, // Piazza Walther
            449, // Via Perathoner
            461, // Via Resia S. Pio X
    };

    private int mSearchX;
    private int mSearchY;

    private boolean showFavorites;

    private final Realm mRealm = Realm.getInstance(BusStopRealmHelper.CONFIG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED, getIntent());

        setContentView(R.layout.activity_search);

        AnalyticsHelper.sendScreenView(TAG);

        Intent intent = getIntent();
        mSearchX = (int) intent.getFloatExtra(EXTRA_X_POS, 0);
        mSearchY = (int) intent.getFloatExtra(EXTRA_Y_POS, 0);

        if (intent.hasExtra(EXTRA_SHOW_FAVORITES)) {
            showFavorites = true;
        }

        mSearchView = (SearchView) findViewById(R.id.search_view);
        setupSearchView();

        mItems = new ArrayList<>();
        mAdapter = new BusStopPickerAdapter(this, mItems);

        ListView listView = (ListView) findViewById(R.id.search_results);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        Drawable up = DrawableCompat.wrap(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp));
        DrawableCompat.setTint(up, ContextCompat.getColor(this, R.color.text_hint));

        Toolbar toolbar = getToolbar();
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.text_hint));
        toolbar.setNavigationIcon(up);
        toolbar.setNavigationOnClickListener(view -> navigateUpOrBack(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            doEnterAnim();
        }

        loadDefaultBusStops();

        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected int getNavItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        it.sasabz.android.sasabus.data.model.BusStop busStop = mItems.get(position);

        // "No results" item has a munic with value "null", check for that.
        if (busStop.getMunic() != null) {

            // ANALYTICS EVENT: Start a search on the Search activity
            AnalyticsHelper.sendEvent(SCREEN_LABEL, String.valueOf(busStop.getGroup()));
            //AnswersHelper.logSearch(AnswersHelper.CATEGORY_DEPARTURES, String.valueOf(busStop.getGroup()));

            Intent intent = new Intent();
            intent.putExtra(Config.EXTRA_BUS_STOP_GROUP, busStop.getGroup());
            setResult(RESULT_OK, intent);

            dismiss(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    /**
     * On Lollipop+ perform a circular reveal animation (an expanding circular mask) when showing
     * the search panel.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doEnterAnim() {
        // Fade in a background scrim as this is a floating window. We could have used a
        // translucent window background but this approach allows us to turn off window animation &
        // overlap the fade with the reveal animation – making it feel snappier.
        View scrim = findViewById(R.id.scrim);
        scrim.animate()
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();

        // Next perform the circular reveal on the search panel
        View searchPanel = findViewById(R.id.search_panel);
        if (searchPanel != null) {
            // We use a view tree observer to set this up once the view is measured & laid out
            searchPanel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    searchPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                    // As the height will change once the initial suggestions are delivered by the
                    // loader, we can't use the search panels height to calculate the final radius
                    // so we fall back to it's parent to be safe

                    int[] positions = new int[2];
                    searchPanel.getLocationOnScreen(positions);

                    int x = mSearchX == 0 ? searchPanel.getTop() : mSearchX;
                    int y = mSearchY == 0 ? (searchPanel.getLeft() + searchPanel.getRight()) / 2 : mSearchY;

                    x -= positions[0];
                    y -= positions[1];

                    View parent = (View) searchPanel.getParent();

                    int radius = (int) Math.sqrt(Math.pow(parent.getHeight(), 2) +
                            Math.pow(parent.getWidth(), 2));

                    // Center the animation on the top right of the panel i.e. near to the
                    // search button which launched this screen.
                    Animator show = ViewAnimationUtils.createCircularReveal(searchPanel,
                            x, y, 0f, radius);

                    show.setDuration(750);
                    show.setInterpolator(new FastOutSlowInInterpolator());
                    show.start();

                    return false;
                }
            });
        }
    }

    /**
     * On Lollipop+ perform a circular animation (a contracting circular mask) when hiding the
     * search panel.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doExitAnim() {
        View searchPanel = findViewById(R.id.search_panel);
        // Center the animation on the top right of the panel i.e. near to the search button which
        // launched this screen. The starting radius therefore is the diagonal distance from the top
        // right to the bottom left

        int[] positions = new int[2];
        searchPanel.getLocationOnScreen(positions);

        int x = mSearchX == 0 ? searchPanel.getTop() : mSearchX;
        int y = mSearchY == 0 ? (searchPanel.getLeft() + searchPanel.getRight()) / 2 : mSearchY;

        x -= positions[0];
        y -= positions[1];

        int radius = (int) Math.sqrt(Math.pow(searchPanel.getWidth(), 2)
                + Math.pow(searchPanel.getHeight(), 2));

        // Animating the radius to 0 produces the contracting effect
        Animator shrink = ViewAnimationUtils.createCircularReveal(searchPanel, x, y, radius, 0f);

        shrink.setDuration(400);
        shrink.setInterpolator(new FastOutSlowInInterpolator());
        shrink.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchPanel.setVisibility(View.INVISIBLE);
                ActivityCompat.finishAfterTransition(DepartureSearchActivity.this);
            }
        });
        shrink.start();

        // We also animate out the translucent background at the same time.
        findViewById(R.id.scrim).animate()
                .alpha(0f)
                .setDuration(400)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }


    private void loadDefaultBusStops() {
        if (showFavorites) {
            Realm realm = Realm.getDefaultInstance();

            realm.where(FavoriteBusStop.class).findAllAsync().asObservable()
                    .compose(bindToLifecycle())
                    .filter(RealmResults::isLoaded)
                    .map(favoriteBusStops -> {
                        List<it.sasabz.android.sasabus.data.model.BusStop> busStops = new ArrayList<>();

                        if (favoriteBusStops.isEmpty()) {
                            return busStops;
                        }

                        for (FavoriteBusStop busStop : favoriteBusStops) {
                            BusStop stop = mRealm.where(BusStop.class)
                                    .equalTo("family", busStop.getGroup()).findFirst();

                            if (stop != null) {
                                busStops.add(new it.sasabz.android.sasabus.data.model.BusStop(stop));
                            }
                        }

                        realm.close();

                        return busStops;
                    })
                    .subscribe(stops -> {
                        mItems.addAll(stops);

                        if (mItems.isEmpty()) {
                            mItems.add(new it.sasabz.android.sasabus.data.model.BusStop(0,
                                    getString(R.string.empty_state_bus_stop_favorites_title), null, 0, 0, 0));
                        }

                        mAdapter.notifyDataSetChanged();
                    });
        } else {
            RealmQuery<BusStop> query = mRealm.where(BusStop.class);

            for (int group : mDefaultBusStops) {
                query = query.equalTo("family", group).or();
            }

            query.findAllAsync().asObservable()
                    .compose(bindToLifecycle())
                    .filter(RealmResults::isLoaded)
                    .map((Func1<RealmResults<BusStop>, List<BusStop>>) busStops -> new ArrayList<>(
                            new LinkedHashSet<>(busStops)))
                    .map(busStops -> {
                        List<it.sasabz.android.sasabus.data.model.BusStop> list = new ArrayList<>();

                        for (BusStop busStop : busStops) {
                            list.add(new it.sasabz.android.sasabus.data.model.BusStop(busStop));

                        }
                        return list;
                    })
                    .subscribe(stops -> {
                        mItems.addAll(stops);
                        mAdapter.notifyDataSetChanged();
                    });
        }
    }

    private void searchFor(String query) {
        String locale = Utils.locale(this);
        String sort = locale.contains("de") ? "nameDe" : "nameIt";

        mRealm.where(BusStop.class)
                .contains("nameDe", query, INSENSITIVE).or()
                .contains("nameIt", query, INSENSITIVE).or()
                .contains("municDe", query, INSENSITIVE).or()
                .contains("municIt", query, INSENSITIVE)
                .findAllSorted(sort)
                .asObservable()
                .filter(RealmResults::isLoaded)
                .map((Func1<RealmResults<BusStop>, List<BusStop>>) busStops -> new ArrayList<>(
                        new LinkedHashSet<>(busStops)))
                .map(stops -> {
                    List<it.sasabz.android.sasabus.data.model.BusStop> list = new ArrayList<>();

                    for (BusStop busStop : stops) {
                        list.add(new it.sasabz.android.sasabus.data.model.BusStop(busStop));

                    }
                    return list;
                })
                .map(busStops -> {
                    if (!query.isEmpty()) {
                        List<it.sasabz.android.sasabus.data.model.BusStop> list = new ArrayList<>();

                        for (it.sasabz.android.sasabus.data.model.BusStop busStop : busStops) {
                            String nameDe = Strings.formatQuery(busStop.getNameDe(), query, "{", "}");
                            String nameIt = Strings.formatQuery(busStop.getNameIt(), query, "{", "}");
                            String municDe = Strings.formatQuery(busStop.getMunicDe(), query, "{", "}");
                            String municIt = Strings.formatQuery(busStop.getMunicIt(), query, "{", "}");

                            list.add(new it.sasabz.android.sasabus.data.model.BusStop(busStop.getId(), nameDe,
                                    nameIt, municDe, municIt, busStop.getLat(), busStop.getLng(),
                                    busStop.getGroup()));
                        }

                        return list;
                    }

                    return busStops;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stations -> {
                    mItems.clear();
                    mItems.addAll(stations);

                    if (mItems.isEmpty()) {
                        mItems.add(new it.sasabz.android.sasabus.data.model.BusStop(0,
                                getString(R.string.search_no_results), null, 0, 0, 0));
                    }

                    mAdapter.notifyDataSetChanged();
                });
    }

    private void setupSearchView() {
        if (showFavorites) {
            mSearchView.setVisibility(View.GONE);
            getSupportActionBar().setTitle(R.string.favorites);
            return;
        }

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(false);

        // Set the query hint.
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchFor(s);
                return true;
            }
        });

        mSearchView.setOnCloseListener(() -> {
            dismiss(null);

            return false;
        });
    }

    public void dismiss(View view) {
        UIUtils.hideKeyboard(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            doExitAnim();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }
}
