package it.sasabz.android.sasabus.ui.busstop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.busstop.BusStop;
import it.sasabz.android.sasabus.realm.user.FavoriteBusStop;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.widget.NestedSwipeRefreshLayout;
import it.sasabz.android.sasabus.ui.widget.adapter.TabsAdapter;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.DeviceUtils;
import it.sasabz.android.sasabus.util.map.BusStopsMapView;
import it.sasabz.android.sasabus.util.recycler.BusStopListAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Holds the three bus stop fragments in a viewpager. These fragments display all the bus stops in
 * form of a list or on a map. A third fragment allows the user to select certain bus stops and
 * add them to the favorites, allowing easy and fast access to them.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class BusStopActivity extends BaseActivity {

    private static final String TAG = "BusStopActivity";

    static final int RESULT_DISPLAY_FAVORITES = 22341;
    public static final int INTENT_DISPLAY_FAVORITES = 22342;

    @BindView(R.id.viewpager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    @BindView(R.id.toolbar_search_text) EditText mSearchText;

    private TabsAdapter mAdapter;

    private FavoritesFragment mFavoritesFragment;
    private ListFragment mListFragment;
    private MapFragment mMapFragment;

    private static boolean isSearchBarShown;
    private static boolean isSearching;

    private final int[] tabIcons = {
            R.drawable.ic_star_white_48dp,
            R.drawable.ic_reorder_white_48dp,
            R.drawable.ic_map_white_48dp
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bus_stops);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        mAdapter = new TabsAdapter(getSupportFragmentManager(), true);

        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));

        mSearchText = (EditText) findViewById(R.id.toolbar_search_text);
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mListFragment != null) {
                    mListFragment.search(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFavoritesFragment = (FavoritesFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FavoritesFragment.class.getName());

            mListFragment = (ListFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, ListFragment.class.getName());

            mMapFragment = (MapFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, MapFragment.class.getName());
        }

        if (mFavoritesFragment == null) {
            mFavoritesFragment = new FavoritesFragment();
        }

        if (mListFragment == null) {
            mListFragment = new ListFragment();
        }

        if (mMapFragment == null) {
            mMapFragment = new MapFragment();
        }

        mAdapter.addFragment(mFavoritesFragment, getString(R.string.favorites));
        mAdapter.addFragment(mListFragment, getString(R.string.station_tab_list));
        mAdapter.addFragment(mMapFragment, getString(R.string.map));

        mViewPager.setAdapter(mAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            Drawable drawable = ContextCompat.getDrawable(this, tabIcons[i]);
            drawable = drawable.mutate();

            ColorStateList colours = ContextCompat.getColorStateList(this, R.color.selector_tab);

            DrawableCompat.setTintList(drawable, colours);

            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                tab.setIcon(drawable);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_DISPLAY_FAVORITES:
                if (resultCode == RESULT_DISPLAY_FAVORITES) {
                    mViewPager.post(() -> {
                        mViewPager.setCurrentItem(0);
                        mTabLayout.setupWithViewPager(mViewPager);
                    });
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_BUS_STOPS;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, FavoritesFragment.class.getName(), mFavoritesFragment);
        getSupportFragmentManager().putFragment(outState, ListFragment.class.getName(), mListFragment);
        getSupportFragmentManager().putFragment(outState, MapFragment.class.getName(), mMapFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateFavorites();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_bus_stops, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        toggleSearchBar();
        return true;
    }


    /**
     * Reloads the favorite bus stops after the user added/removed some by either long clicking
     * a bus stop on the list or adding it via {@link BusStopDetailActivity}.
     */
    public void invalidateFavorites() {
        if (mFavoritesFragment != null) {
            mFavoritesFragment.parseData();
        }
    }

    private void showSearchBar(boolean show) {
        if (show && !isSearchBarShown) {
            mSearchText.setVisibility(View.VISIBLE);
            mSearchText.requestFocus();

            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(mSearchText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            mSearchText.setVisibility(View.GONE);

            View focus = getCurrentFocus();

            if (focus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }

            if (mListFragment != null) {
                mListFragment.search("");
            }

            mSearchText.setText("");
        }

        isSearchBarShown = show;
    }

    private void toggleSearchBar() {
        showSearchBar(!isSearchBarShown);
    }


    /**
     * Holds all the favorite bus stops in a list.
     */
    public static class FavoritesFragment extends RxFragment {

        private RecyclerView mRecyclerView;
        private BusStopListAdapter mAdapter;

        private List<BusStop> mItems;

        final Realm userRealm = Realm.getDefaultInstance();
        final Realm busStopRealm = Realm.getInstance(BusStopRealmHelper.CONFIG);

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_bus_stop_favorites, container, false);

            mItems = new ArrayList<>();
            mAdapter = new BusStopListAdapter(getActivity(), mItems);

            mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
            mRecyclerView.setHasFixedSize(true);

            if (DeviceUtils.isTablet(getActivity())) {
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            } else {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            }

            mRecyclerView.setAdapter(mAdapter);

            new Handler().postDelayed(this::parseData, Config.BUS_STOP_FRAGMENTS_POST_DELAY);

            return view;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            userRealm.close();
            busStopRealm.close();
        }

        public void parseData() {
            if (getActivity() == null) return;

            mItems.clear();
            mAdapter.notifyDataSetChanged();

            userRealm.where(FavoriteBusStop.class).findAllAsync().asObservable()
                    .compose(bindToLifecycle())
                    .filter(RealmResults::isLoaded)
                    .map(favoriteBusStops -> {
                        List<BusStop> busStops = new ArrayList<>();

                        for (FavoriteBusStop busStop : favoriteBusStops) {
                            busStops.add(busStopRealm.where(BusStop.class).equalTo("family",
                                    busStop.getGroup()).findFirst());
                        }

                        return busStops;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<BusStop>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(List<BusStop> busStops) {
                            mItems.addAll(busStops);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    /**
     * Holds all bus stops in a list. Allows the user to search through the list by using
     * the header search bar.
     */
    public static class ListFragment extends Fragment {

        private RecyclerView mRecyclerView;
        private BusStopListAdapter mAdapter;

        private NestedSwipeRefreshLayout mSwipeRefreshLayout;

        private List<BusStop> mSearchItems;
        private List<BusStop> mItems;

        final Realm realm = Realm.getInstance(BusStopRealmHelper.CONFIG);

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_bus_stop_list, container, false);

            mItems = new ArrayList<>(900);
            mSearchItems = new ArrayList<>(900);

            mAdapter = new BusStopListAdapter(getActivity(), mSearchItems);

            mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
            mRecyclerView.setHasFixedSize(true);

            if (DeviceUtils.isTablet(getActivity())) {
                GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);

                mRecyclerView.setLayoutManager(layoutManager);
            } else {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            }

            mRecyclerView.setAdapter(mAdapter);

            mSwipeRefreshLayout = (NestedSwipeRefreshLayout) view.findViewById(R.id.refresh);
            mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));

            new Handler().postDelayed(this::parseData, Config.BUS_STOP_FRAGMENTS_POST_DELAY);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            realm.close();
        }

        public void parseData() {
            mItems.clear();

            String locale = getResources().getConfiguration().locale.toString();
            String sort = locale.contains("de") ? "nameDe" : "nameIt";

            realm.where(BusStop.class).findAllSortedAsync(sort).asObservable()
                    .map((Func1<RealmResults<BusStop>, List<BusStop>>) busStops -> new ArrayList<>(
                            new LinkedHashSet<>(busStops)))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stations -> {
                        mItems.addAll(stations);
                        mSearchItems.addAll(mItems);

                        mAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    });
        }

        private void search(String string) {
            string = string.toLowerCase().replace(" ", "");

            mSearchItems.clear();

            if (string.isEmpty()) {
                isSearching = false;
                mSearchItems.addAll(mItems);
            } else {
                for (BusStop station : mItems) {
                    if (station.getNameDe() == null && station.getMunicDe() == null) continue;

                    String nameDe = station.getNameDe().toLowerCase().replace(" ", "");
                    String municDe = station.getMunicDe().toLowerCase().replace(" ", "");

                    String nameIt = station.getNameIt().toLowerCase().replace(" ", "");
                    String municIt = station.getMunicIt().toLowerCase().replace(" ", "");

                    if (nameDe.contains(string) ||
                            municDe.contains(string) ||
                            nameIt.contains(string) ||
                            municIt.contains(string) ||
                            String.valueOf(station.getId()).contains(string)) {
                        mSearchItems.add(station);
                    }
                }

                isSearching = true;
            }

            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Displays all the bus stops on map
     */
    public static class MapFragment extends RxFragment {

        private View view;

        private BusStopsMapView mapView;

        final Realm realm = Realm.getInstance(BusStopRealmHelper.CONFIG);

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.fragment_bus_stop_map, container, false);

            WebView webView = (WebView) view.findViewById(R.id.googlemap);

            mapView = new BusStopsMapView(getActivity(), webView);

            return view;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            parseData();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            realm.close();
        }

        private void parseData() {
            realm.where(BusStop.class).findAllAsync().asObservable()
                    .compose(bindToLifecycle())
                    .filter(RealmResults::isLoaded)
                    .delay(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(busStops -> {
                        List<BusStop> list = new ArrayList<>();

                        for (BusStop busStop : busStops) {
                            if (busStop.getNameDe() == null && busStop.getMunicDe() == null)
                                continue;

                            list.add(busStop);
                        }

                        mapView.setMarkers(list);
                    });
        }
    }
}