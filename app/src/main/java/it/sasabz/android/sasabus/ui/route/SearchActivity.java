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

package it.sasabz.android.sasabus.ui.route;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
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

import com.davale.sasabus.core.realm.BusStopRealmHelper;
import com.davale.sasabus.core.realm.model.SadBusStop;
import com.davale.sasabus.core.util.Strings;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.BusStop;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.list.BusStopPickerAdapter;
import rx.android.schedulers.AndroidSchedulers;

import static io.realm.Case.INSENSITIVE;

/**
 * Allows the user to pick a departure/arrival bus stop by searching it by either name or
 * municipality. When starting this activity it shows a nice reveal animation.
 *
 * @author Alex Lardschneider
 */
public class SearchActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "SearchActivity";
    private static final String SCREEN_LABEL = "Search";

    private SearchView mSearchView;
    private BusStopPickerAdapter mAdapter;
    private ArrayList<BusStop> mItems;

    private final int[] mStations = {
            66000468, // Stazione Bolzano
            66002162, // Via Perathoner
            66002132, // Casanova
            66002160, // Firmian
            66000640, // Bronzolo
            66000210, // Stazione Merano
            66002339, // Municipio Lana
            66000485, // Postal
            66000211, // Parcheggio Parcines
    };

    private int mSearchTop;

    private final Realm mRealm = Realm.getInstance(BusStopRealmHelper.CONFIG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        AnalyticsHelper.sendScreenView(TAG);

        Intent intent = getIntent();
        mSearchTop = intent.getIntExtra("mSearchTop", 0);

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
        toolbar.setNavigationIcon(up);
        toolbar.setNavigationOnClickListener(view -> navigateUpOrBack(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            doEnterAnim();
        }

        RealmQuery<SadBusStop> query = mRealm.where(SadBusStop.class);

        for (int id : mStations) {
            query = query.equalTo("id", id).or();
        }

        query.findAllAsync().asObservable()
                .map(sadBusStops -> {
                    List<BusStop> list = new ArrayList<>();

                    for (SadBusStop sadBusStop : sadBusStops) {
                        list.add(new BusStop(sadBusStop));

                    }
                    return list;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stations -> {
                    mItems.addAll(stations);
                    mAdapter.notifyDataSetChanged();
                });

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
        BusStop station = mItems.get(position);

        // "No results" item has a munic with value "null", check for that.
        if (station.getMunic() != null) {
            station = new BusStop(station);

            // ANALYTICS EVENT: Start a search on the Search activity
            AnalyticsHelper.sendEvent(SCREEN_LABEL, String.valueOf(station.getId()));

            Intent intent = new Intent();
            intent.putExtra(Config.EXTRA_STATION, station);
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

                    int top = mSearchTop == 0 ? searchPanel.getTop() : mSearchTop;

                    View parent = (View) searchPanel.getParent();

                    int revealRadius = (int) Math.sqrt(Math.pow(parent.getHeight(), 2) +
                            Math.pow(parent.getWidth(), 2));

                    // Center the animation on the top right of the panel i.e. near to the
                    // search button which launched this screen.
                    Animator show = ViewAnimationUtils.createCircularReveal(searchPanel,
                            (searchPanel.getLeft() + searchPanel.getRight()) / 4, top, 0f, revealRadius);
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
    @SuppressLint("WrongViewCast")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doExitAnim() {
        View searchPanel = findViewById(R.id.search_panel);
        // Center the animation on the top right of the panel i.e. near to the search button which
        // launched this screen. The starting radius therefore is the diagonal distance from the top
        // right to the bottom left

        int top = mSearchTop == 0 ? searchPanel.getTop() : mSearchTop;

        int revealRadius = (int) Math.sqrt(Math.pow(searchPanel.getWidth(), 2)
                + Math.pow(searchPanel.getHeight(), 2));

        // Animating the radius to 0 produces the contracting effect
        Animator shrink = ViewAnimationUtils.createCircularReveal(searchPanel,
                (searchPanel.getLeft() + searchPanel.getRight()) / 4, top, revealRadius, 0f);

        shrink.setDuration(400);
        shrink.setInterpolator(new FastOutSlowInInterpolator());
        shrink.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchPanel.setVisibility(View.INVISIBLE);
                ActivityCompat.finishAfterTransition(SearchActivity.this);
            }
        });
        shrink.start();

        // We also animate out the translucent background at the same time.
        findViewById(R.id.scrim)
                .animate()
                .alpha(0f)
                .setDuration(400)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }

    private void searchFor(String query) {
        String order = Utils.locale(this).contains("de") ? "nameDe" : "nameIt";

        mRealm.where(SadBusStop.class)
                .contains("nameDe", query, INSENSITIVE).or()
                .contains("nameIt", query, INSENSITIVE).or()
                .contains("municDe", query, INSENSITIVE).or()
                .contains("municIt", query, INSENSITIVE).or()
                .findAllSorted(order).asObservable()
                .map(sadBusStops -> {
                    List<BusStop> list = new ArrayList<>();

                    for (SadBusStop sadBusStop : sadBusStops) {
                        list.add(new BusStop(sadBusStop));

                    }
                    return list;
                })
                .map(busStops -> {
                    if (!query.isEmpty()) {
                        List<BusStop> list = new ArrayList<>();

                        for (BusStop busStop : busStops) {
                            String nameDe = Strings.formatQuery(busStop.getNameDe(), query, "{", "}");
                            String nameIt = Strings.formatQuery(busStop.getNameIt(), query, "{", "}");
                            String municDe = Strings.formatQuery(busStop.getMunicDe(), query, "{", "}");
                            String municIt = Strings.formatQuery(busStop.getMunicIt(), query, "{", "}");

                            list.add(new BusStop(busStop.getId(), nameDe, nameIt, municDe,
                                    municIt, busStop.getLat(), busStop.getLng(), 0));
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
                        mItems.add(new BusStop(0, getString(R.string.search_no_results), null, 0, 0, 0));
                    }

                    mAdapter.notifyDataSetChanged();
                });
    }

    private void setupSearchView() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            doExitAnim();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }
}
