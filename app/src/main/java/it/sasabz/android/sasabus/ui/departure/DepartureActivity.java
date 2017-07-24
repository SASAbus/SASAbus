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

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.davale.sasabus.core.model.Departure;
import com.davale.sasabus.core.realm.BusStopRealmHelper;
import com.davale.sasabus.core.realm.model.BusStop;
import com.davale.sasabus.core.vdv.DepartureMonitor;
import com.davale.sasabus.core.vdv.model.VdvDeparture;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.beacon.BeaconHandler;
import it.sasabz.android.sasabus.beacon.busstop.BusStopBeaconHandler;
import it.sasabz.android.sasabus.data.network.NetUtils;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.RealtimeApi;
import it.sasabz.android.sasabus.data.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.data.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.data.realm.UserRealmHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.widget.RecyclerItemDivider;
import it.sasabz.android.sasabus.util.AnimUtils;
import it.sasabz.android.sasabus.util.MapUtils;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class DepartureActivity extends BaseActivity implements View.OnClickListener,
        View.OnTouchListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private static final String BUNDLE_BUS_STOP = "com.davale.sasabus.BUNDLE_BUS_STOP";
    private static final String BUNDLE_BUS_STOP_TYPE = "com.davale.sasabus.BUNDLE_BUS_STOP_TYPE";

    private static final int ACTION_PICK_BUS_STOP = 101;

    private static final double LAT_LNG_BOUNDS = 0.0022495;

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_BEACON = 1;
    private static final int TYPE_GPS = 2;

    @BindView(R.id.departures_search_icon) ImageView mSearchIcon;
    @BindView(R.id.departures_search_text) TextView mSearchText;

    @BindView(R.id.departures_search_date) ImageView mDateButton;
    @BindView(R.id.departures_search_favorites) ImageView mFavoritesButton;

    @BindView(R.id.refresh) SwipeRefreshLayout mRefresh;
    @BindView(R.id.recycler) RecyclerView mRecyclerView;

    @BindView(R.id.empty_state) ScrollView mEmptyState;
    @BindView(R.id.empty_state_title) TextView mEmptyStateTitle;

    @BindView(R.id.departures_favorite_fab) FloatingActionButton mFavoritesFab;

    private ArrayList<Departure> mItems;
    private DeparturesAdapter mAdapter;

    @Nullable
    private BusStop busStop;
    private Calendar mCalendar;

    /**
     * Needed for the search activity circular reveal.
     */
    private float lastTouchX;
    private float lastTouchY;

    private boolean mIsInFavorites;
    private int busStopType = TYPE_NORMAL;

    private final long minDateMillis = System.currentTimeMillis();
    private final Realm realm = Realm.getInstance(BusStopRealmHelper.CONFIG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_departure);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(R.string.title_departure);

        mRefresh.setColorSchemeResources(Config.REFRESH_COLORS);
        mRefresh.setOnRefreshListener(() -> {
            if (busStop == null) {
                mRefresh.setRefreshing(false);
            } else {
                parseData(busStop.getFamily());
            }
        });

        if (savedInstanceState != null) {
            mItems = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);

            int busStopFamily = savedInstanceState.getInt(BUNDLE_BUS_STOP);
            busStopType = savedInstanceState.getInt(BUNDLE_BUS_STOP_TYPE);

            updateBusStopData(busStopFamily);
        } else {
            mItems = new ArrayList<>();
        }

        mAdapter = new DeparturesAdapter(this, mItems);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new RecyclerItemDivider(this));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mSearchText.setOnClickListener(this);
        mDateButton.setOnClickListener(this);
        mFavoritesButton.setOnClickListener(this);

        mSearchText.setOnTouchListener(this);
        mDateButton.setOnTouchListener(this);
        mFavoritesButton.setOnTouchListener(this);

        mFavoritesFab.setOnClickListener(this);

        mCalendar = Calendar.getInstance();

        if (savedInstanceState == null) {
            selectBusStop(getIntent());
        } else {
            int emptyState = savedInstanceState.getInt(Config.BUNDLE_EMPTY_STATE_VISIBILITY);
            if (emptyState != View.GONE) {
                showNoDeparturesEmptyState();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_PICK_BUS_STOP) {
            if (resultCode == RESULT_OK) {
                int group = data.getIntExtra(Config.EXTRA_BUS_STOP_GROUP, -1);
                busStop = BusStopRealmHelper.getBusStopOrNullFromGroup(group);

                Timber.i("Got bus stop group: %s", group);

                busStopType = TYPE_NORMAL;

                mItems.clear();
                mAdapter.notifyItemRangeRemoved(0, mItems.size());

                parseData(group);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected int getNavItem() {
        return NAVDRAWER_ITEM_DEPARTURES;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realm.close();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.departures_search_text:
                int[] location = new int[2];
                mSearchText.getLocationOnScreen(location);

                Intent intent = new Intent(this, DepartureSearchActivity.class);
                intent.putExtra(DepartureSearchActivity.EXTRA_X_POS, lastTouchX);
                intent.putExtra(DepartureSearchActivity.EXTRA_Y_POS, lastTouchY);
                startActivityForResult(intent, ACTION_PICK_BUS_STOP);
                break;
            case R.id.departures_search_favorites:
                location = new int[2];
                mSearchText.getLocationOnScreen(location);

                intent = new Intent(this, DepartureSearchActivity.class);
                intent.putExtra(DepartureSearchActivity.EXTRA_X_POS, lastTouchX);
                intent.putExtra(DepartureSearchActivity.EXTRA_Y_POS, lastTouchY);
                intent.putExtra(DepartureSearchActivity.EXTRA_SHOW_FAVORITES, true);
                startActivityForResult(intent, ACTION_PICK_BUS_STOP);
                break;
            case R.id.departures_search_date:
                Context context = this;
                if (Utils.isBrokenSamsungDevice()) {
                    context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog);
                }

                DatePickerDialog datePicker = new DatePickerDialog(context, R.style.DatePickerStyle, this, 0, 0, 0);

                datePicker.getDatePicker().setMinDate(minDateMillis);
                datePicker.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), datePicker);
                datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, null,
                        (DialogInterface.OnClickListener) null);

                datePicker.getDatePicker().init(mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), null);

                datePicker.show();
                break;
            case R.id.departures_favorite_fab:
                if (busStop == null) {
                    return;
                }

                if (mIsInFavorites) {
                    UserRealmHelper.removeFavoriteBusStop(busStop.getFamily());
                    Snackbar.make(getMainContent(), getString(R.string.bus_stop_favorites_remove,
                            busStop.getName(this)),
                            Snackbar.LENGTH_SHORT).show();

                    setFavoritesFabIcon(false);
                    mIsInFavorites = false;
                } else {
                    UserRealmHelper.addFavoriteBusStop(busStop.getFamily());
                    Snackbar.make(getMainContent(), getString(R.string.bus_stop_favorites_add,
                            busStop.getName(this)),
                            Snackbar.LENGTH_SHORT).show();

                    setFavoritesFabIcon(true);
                    mIsInFavorites = true;
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.getRawX();
            lastTouchY = event.getRawY();
        }

        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        selectBusStop(intent);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        Timber.e("Set date from picker: %s.%s.%s", dayOfMonth, month, year);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.DatePickerStyle, this,
                mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);

        timePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);

        Timber.e("Set time from picker: %s:%s", hourOfDay, minute);

        if (busStop != null) {
            parseData(busStop.getFamily());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
        outState.putInt(BUNDLE_BUS_STOP_TYPE, busStopType);
        outState.putInt(Config.BUNDLE_EMPTY_STATE_VISIBILITY, mEmptyState.getVisibility());

        if (busStop != null) {
            outState.putInt(BUNDLE_BUS_STOP, busStop.getFamily());
        }
    }


    private void selectBusStop(Intent intent) {
        // See if this activity was started by using {@link #intent(Context, int)}, so we can
        // use the passed bus stop group directly.
        if (intent.hasExtra(Config.EXTRA_BUS_STOP_GROUP)) {
            int group = intent.getIntExtra(Config.EXTRA_BUS_STOP_GROUP, 0);

            Timber.w("Got departure intent with bus stop: %s", group);

            parseData(group);
            return;
        }

        if (BeaconHandler.isListening) {
            // See if there are any beacons nearby. If there are, beacons should be prioritized
            // over normal gps location as beacons are generally more accurate.

            Timber.i("Beacons are enabled, trying to get nearby bus stop");

            Pair<Integer, it.sasabz.android.sasabus.data.model.BusStop> nearbyBusStop =
                    BusStopBeaconHandler.getInstance(this).getCurrentBusStop();
            if (nearbyBusStop != null) {
                Timber.i("Got beacon bus stop %s", nearbyBusStop.second.getId());

                busStopType = TYPE_BEACON;

                parseData(nearbyBusStop.second.getGroup());
                return;
            }
        }

        Location location = MapUtils.getLastKnownLocation(this);
        if (location != null) {
            // If we have an up-to-date location, find the nearest bus stop and display its departures.

            Timber.i("Got last known location: %s", location);

            long start = -System.currentTimeMillis();

            double maxLat = location.getLatitude() + LAT_LNG_BOUNDS;
            double maxLng = location.getLongitude() + LAT_LNG_BOUNDS;

            double minLat = location.getLatitude() - LAT_LNG_BOUNDS;
            double minLng = location.getLongitude() - LAT_LNG_BOUNDS;

            RealmResults<BusStop> results = realm.where(BusStop.class)
                    .between("lat", (float) minLat, (float) maxLat)
                    .between("lng", (float) minLng, (float) maxLng)
                    .findAll();

            if (!results.isEmpty()) {
                List<BusStop> list = new ArrayList<>(new LinkedHashSet<>(results));

                Collections.sort(list, (o1, o2) -> {
                    float distance1 = MapUtils.distance(o1.getLat(), o1.getLng(),
                            (float) location.getLatitude(), (float) location.getLongitude());

                    float distance2 = MapUtils.distance(o2.getLat(), o2.getLng(),
                            (float) location.getLatitude(), (float) location.getLongitude());

                    return (int) (distance1 - distance2);
                });

                Timber.i("Took %s millis", start + System.currentTimeMillis());
                Timber.i("Nearest: %s", list.get(0).getNameDe());

                busStopType = TYPE_GPS;

                parseData(list.get(0).getFamily());

                return;
            } else {
                Timber.w("No nearby bus stops found");
            }
        }

        mItems.clear();
        mAdapter.notifyDataSetChanged();

        showSelectEmptyState();
    }

    private void parseData(int group) {
        updateBusStopData(group);

        if (busStop == null) {
            return;
        }

        mRefresh.setRefreshing(true);

        AnimUtils.fadeOut(mEmptyState, AnimUtils.DURATION_MEDIUM);

        getDepartures(group)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Departure>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        mItems.clear();
                        mAdapter.notifyDataSetChanged();

                        mRefresh.setRefreshing(false);

                        showNoDeparturesEmptyState();
                    }

                    @Override
                    public void onNext(List<Departure> departures) {
                        mRefresh.setRefreshing(false);

                        mItems.clear();
                        mItems.addAll(departures);

                        mAdapter.notifyDataSetChanged();

                        if (departures.isEmpty()) {
                            showNoDeparturesEmptyState();
                            return;
                        }

                        if (NetUtils.isOnline(DepartureActivity.this) &&
                                DateUtils.isToday(mCalendar.getTimeInMillis())) {
                            loadDelays();
                        } else {
                            for (Departure item : mItems) {
                                if (item.getDelay() == Departure.OPERATION_RUNNING) {
                                    item.setDelay(Departure.NO_DELAY);
                                }
                            }
                        }
                    }
                });
    }

    private void updateBusStopData(int group) {
        busStop = BusStopRealmHelper.getBusStopOrNullFromGroup(group);
        if (busStop == null) {
            Timber.e("Bus stop with group %s doesn't exist", group);
        }

        switch (busStopType) {
            case TYPE_NORMAL:
                mSearchIcon.setImageDrawable(null);
                break;
            case TYPE_BEACON:
                mSearchIcon.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_bluetooth_white_48dp));
                break;
            case TYPE_GPS:
                mSearchIcon.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_gps_fixed_white_24dp));
                break;
            default:
                Timber.e("Unknown bus stop type: %s", busStopType);
        }

        mIsInFavorites = UserRealmHelper.hasFavoriteBusStop(group);
        setFavoritesFabIcon(mIsInFavorites);

        mFavoritesFab.setVisibility(View.VISIBLE);
        mFavoritesFab.animate()
                .alpha(1)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Set the bus stop text in the search field
        if (busStop != null) {
            mSearchText.setText('{' + busStop.getName(this) + "}, " + busStop.getMunic(this));
            mSearchText.setTextColor(ContextCompat.getColor(this, R.color.text_primary_light));
        }
    }

    private void loadDelays() {
        RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
        realtimeApi.delaysRx()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        for (Departure item : mItems) {
                            if (item.getDelay() == Departure.OPERATION_RUNNING) {
                                item.setDelay(Departure.NO_DELAY);
                            }
                        }

                        if (!mItems.isEmpty()) {
                            mAdapter.notifyItemRangeChanged(0, mItems.size());
                        }
                    }

                    @Override
                    public void onNext(RealtimeResponse response) {
                        for (RealtimeBus bus : response.buses) {
                            for (Departure item : mItems) {
                                if (item.getTrip() == bus.trip) {
                                    item.setDelay(bus.delayMin);
                                    item.setVehicle(bus.vehicle);
                                    item.setCurrentBusStop(bus.busStop);

                                    break;
                                }
                            }
                        }

                        for (Departure item : mItems) {
                            if (item.getDelay() == Departure.OPERATION_RUNNING) {
                                item.setDelay(Departure.NO_DELAY);
                            }
                        }

                        if (!mItems.isEmpty()) {
                            mAdapter.notifyItemRangeChanged(0, mItems.size());
                        }
                    }
                });
    }

    private void setFavoritesFabIcon(boolean favorite) {
        if (favorite) {
            mFavoritesFab.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.ic_star_white_48dp));
        } else {
            mFavoritesFab.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.ic_star_border_white_48dp));
        }
    }

    private void showSelectEmptyState() {
        mEmptyState.setVisibility(View.VISIBLE);
        mEmptyStateTitle.setText(R.string.empty_state_departures_select_title);
    }

    private void showNoDeparturesEmptyState() {
        AnimUtils.fadeIn(mEmptyState, AnimUtils.DURATION_MEDIUM, false);
        mEmptyStateTitle.setText(R.string.empty_state_departures_no_departures_title);
    }

    private Observable<List<Departure>> getDepartures(int family) {
        return Observable.fromCallable(() -> {
            List<Departure> items = new ArrayList<>();

            Collection<VdvDeparture> departures = new DepartureMonitor()
                    .atBusStopFamily(family)
                    .at(mCalendar.getTime())
                    .collect();

            for (VdvDeparture departure : departures) {
                items.add(departure.asDeparture(busStop.getId()));
            }

            return items;
        });
    }

    public static Intent intent(Context context, int group) {
        Intent intent = new Intent(context, DepartureActivity.class);
        intent.putExtra(Config.EXTRA_BUS_STOP_GROUP, group);
        return intent;
    }
}
