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

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.BusStop;
import it.sasabz.android.sasabus.model.route.RouteRecent;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.realm.user.RecentRoute;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.widget.RecyclerItemClickListener;
import it.sasabz.android.sasabus.ui.widget.RecyclerItemDivider;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.RecentAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Allows the user to select departure/arrival, time, date and many more things to calculate
 * a route between the departure bus stop and arrival bus stop.
 *
 * @author Alex Lardschneider
 */
public class RouteActivity extends BaseActivity implements View.OnClickListener,
        RecyclerItemClickListener.OnItemClickListener {

    private static final String TAG = "RouteActivity";

    private static final int ACTION_PICK_DEPARTURE = 1;
    private static final int ACTION_PICK_ARRIVAL = 2;

    private static final int DEFAULT_WALK_TIME = 5;
    private static final int DEFAULT_RESULTS_COUNT = 5;

    @BindView(R.id.route_text_departure) TextView departure;
    @BindView(R.id.route_text_arrival) TextView arrival;
    @BindView(R.id.route_time_picker) TextView timePicker;
    @BindView(R.id.route_date_picker) TextView datePicker;
    @BindView(R.id.route_text_recent_title) TextView recentText;
    @BindView(R.id.route_text_results) TextView results;
    @BindView(R.id.route_text_walk) TextView walk;
    @BindView(R.id.route_text_recent_card) CardView recentCard;

    private BusStop mDepartureBusStop;
    private BusStop mArrivalBusStop;

    private Calendar mTimeCalendar;
    private Calendar mDateCalendar;

    private List<RouteRecent> mItems;
    private RecentAdapter mAdapter;

    private ActionMode mActionMode;

    private int mWalkTime = DEFAULT_WALK_TIME;
    private int mResultsCount = DEFAULT_RESULTS_COUNT;

    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route);

        AnalyticsHelper.sendScreenView(TAG);

        ButterKnife.bind(this);

        mRealm = Realm.getDefaultInstance();

        mTimeCalendar = Calendar.getInstance();
        mDateCalendar = Calendar.getInstance();

        departure.setOnClickListener(this);
        arrival.setOnClickListener(this);
        results.setOnClickListener(this);
        walk.setOnClickListener(this);

        ImageView departureMap = (ImageView) findViewById(R.id.route_departure_map);
        ImageView arrivalMap = (ImageView) findViewById(R.id.route_arrival_map);
        ImageView imageButton = (ImageView) findViewById(R.id.route_swap);
        ImageView timeSet = (ImageView) findViewById(R.id.route_time_set);

        ImageView arrivalArrowImage = (ImageView) findViewById(R.id.route_arrival_image_arrow);
        arrivalArrowImage.setRotation(90);

        FloatingActionButton search = (FloatingActionButton) findViewById(R.id.route_search);

        departureMap.setOnClickListener(this);
        arrivalMap.setOnClickListener(this);
        timeSet.setOnClickListener(this);
        imageButton.setOnClickListener(this);
        search.setOnClickListener(this);

        mItems = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        recyclerView.addItemDecoration(new RecyclerItemDivider(this));

        mAdapter = new RecentAdapter(this, recyclerView, mItems);
        recyclerView.setAdapter(mAdapter);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            search.setTranslationY(getResources().getDimension(R.dimen.fab_margin_bottom));
        }

        if (savedInstanceState != null) {
            departure.setText(savedInstanceState.getString("DEPARTURE"));
            arrival.setText(savedInstanceState.getString("ARRIVAL"));

            if (!departure.getText().equals(getString(R.string.route_departure))) {
                departure.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }

            if (!arrival.getText().equals(getString(R.string.route_arrival))) {
                arrival.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }

            timePicker.setText(savedInstanceState.getString("TIME"));
            datePicker.setText(savedInstanceState.getString("DATE"));

            loadRecent();
        } else {
            ViewCompat.setAlpha(search, 0);
            ViewCompat.setScaleX(search, 0);
            ViewCompat.setScaleY(search, 0);

            search.animate().setInterpolator(new OvershootInterpolator())
                    .setStartDelay(500)
                    .scaleX(1)
                    .alpha(1)
                    .scaleY(1)
                    .setDuration(300)
                    .start();

            updateDate();
            updateTime();
        }

        long minDateMillis = new GregorianCalendar().getTimeInMillis();

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            Date minDate = new Date(minDateMillis);

            if (calendar.getTime().before(minDate)) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(minDate);

                mDateCalendar.set(Calendar.YEAR, calendar1.get(Calendar.YEAR));
                mDateCalendar.set(Calendar.MONTH, calendar1.get(Calendar.MONTH));
                mDateCalendar.set(Calendar.DAY_OF_MONTH, calendar1.get(Calendar.DAY_OF_MONTH));
            } else {
                mDateCalendar.set(Calendar.YEAR, year);
                mDateCalendar.set(Calendar.MONTH, monthOfYear);
                mDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }

            updateDateFormPicker();
        };

        Context context = this;
        if (isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.DatePicker, date, mDateCalendar.get(Calendar.YEAR),
                mDateCalendar.get(Calendar.MONTH), mDateCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(minDateMillis);

        datePicker.setOnClickListener(v -> {
            datePickerDialog.getDatePicker().init(mDateCalendar.get(Calendar.YEAR),
                    mDateCalendar.get(Calendar.MONTH), mDateCalendar.get(Calendar.DAY_OF_MONTH), null);

            datePickerDialog.show();
        });

        TimePickerDialog.OnTimeSetListener time = (timePicker1, hour, minute) -> {
            mTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
            mTimeCalendar.set(Calendar.MINUTE, minute);

            updateTimeFormPicker();
        };

        timePicker.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.DatePicker, time,
                    mTimeCalendar.get(Calendar.HOUR_OF_DAY), mTimeCalendar.get(Calendar.MINUTE), true);

            timePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            timePickerDialog.show();
        });
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_ROUTE;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadRecent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_PICK_DEPARTURE:
                if (resultCode == Activity.RESULT_OK) {
                    mDepartureBusStop = data.getExtras().getParcelable(Config.EXTRA_STATION);

                    if (mDepartureBusStop != null) {
                        departure.setText(mDepartureBusStop.getName(this));
                        departure.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    }
                }
                break;
            case ACTION_PICK_ARRIVAL:
                if (resultCode == Activity.RESULT_OK) {
                    mArrivalBusStop = data.getExtras().getParcelable(Config.EXTRA_STATION);

                    if (mArrivalBusStop != null) {
                        arrival.setText(mArrivalBusStop.getName(this));
                        arrival.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    }
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("DEPARTURE", String.valueOf(departure.getText()));
        outState.putString("ARRIVAL", String.valueOf(arrival.getText()));

        outState.putString("DATE", String.valueOf(datePicker.getText()));
        outState.putString("TIME", String.valueOf(timePicker.getText()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.route_text_departure:
                int[] location = new int[2];
                departure.getLocationOnScreen(location);

                startActivityForResult(new Intent(this, SearchActivity.class)
                        .putExtra("mSearchTop", location[1] - departure.getHeight() / 2), ACTION_PICK_DEPARTURE);
                break;
            case R.id.route_text_arrival:
                location = new int[2];
                arrival.getLocationOnScreen(location);

                startActivityForResult(new Intent(this, SearchActivity.class)
                        .putExtra("mSearchTop", location[1] - arrival.getHeight() / 2), ACTION_PICK_ARRIVAL);
                break;
            case R.id.route_departure_map:
                startActivityForResult(new Intent(this, RouteMapPickerActivity.class), ACTION_PICK_DEPARTURE);
                break;
            case R.id.route_arrival_map:
                startActivityForResult(new Intent(this, RouteMapPickerActivity.class), ACTION_PICK_ARRIVAL);
                break;
            case R.id.route_time_set:
                updateTime();
                updateDate();
                break;
            case R.id.route_text_results:
                showResultsDialog();
                break;
            case R.id.route_text_walk:
                showWalkDialog();
                break;
            case R.id.route_swap:
                CharSequence dep = departure.getText();
                CharSequence arr = arrival.getText();

                departure.setText(arr);
                arrival.setText(dep);

                BusStop tempDepartureStation = mDepartureBusStop;
                mDepartureBusStop = mArrivalBusStop;
                mArrivalBusStop = tempDepartureStation;

                if (departure.getText().equals(getString(R.string.route_arrival))) {
                    departure.setText(R.string.route_departure);
                }

                if (arrival.getText().equals(getString(R.string.route_departure))) {
                    arrival.setText(R.string.route_arrival);
                }

                if (departure.getText().equals(getString(R.string.route_departure))) {
                    departure.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                } else {
                    departure.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }

                if (arrival.getText().equals(getString(R.string.route_arrival))) {
                    arrival.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                } else {
                    arrival.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }

                break;
            case R.id.route_search:
                if (departure.getText().equals(getString(R.string.route_departure))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_route_enter_departure), Toast.LENGTH_SHORT).show();
                } else if (arrival.getText().equals(getString(R.string.route_arrival))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_route_enter_arrival), Toast.LENGTH_SHORT).show();
                } else if (mDepartureBusStop != null && mArrivalBusStop != null && mDepartureBusStop.getId() == mArrivalBusStop.getId()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_route_station_match), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, RouteResultActivity.class);
                    intent.putExtra("date", mDateCalendar.getTime());
                    intent.putExtra("time", timePicker.getText());
                    intent.putExtra("results", mResultsCount);
                    intent.putExtra("walk", mWalkTime);

                    intent.putExtra(Config.EXTRA_DEPARTURE_ID, mDepartureBusStop != null ? mDepartureBusStop.getId() : null);
                    intent.putExtra(Config.EXTRA_ARRIVAL_ID, mArrivalBusStop != null ? mArrivalBusStop.getId() : null);

                    if (mDepartureBusStop != null && mArrivalBusStop != null) {
                        UserRealmHelper.insertRecent(mDepartureBusStop.getId(),
                                mArrivalBusStop.getId());
                    }

                    startActivity(intent);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown id " + v.getId());
        }
    }

    @Override
    public void onItemClick(View childView, int position) {
        if (mActionMode == null) {
            RouteRecent item = mItems.get(position);

            mDepartureBusStop = new BusStop(BusStopRealmHelper.getSadBusStop(item.getOriginId()));
            mArrivalBusStop = new BusStop(BusStopRealmHelper.getSadBusStop(item.getDestinationId()));

            departure.setText(mDepartureBusStop.getName());
            departure.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

            arrival.setText(mArrivalBusStop.getName());
            arrival.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        } else {
            onListItemSelect(childView, position);
        }
    }

    @Override
    public void onItemLongPress(View childView, int position) {
        onListItemSelect(childView, position);
    }


    private void loadRecent() {
        mRealm.where(RecentRoute.class).findAllAsync().asObservable()
                .filter(RealmResults::isLoaded)
                .map((Func1<RealmResults<RecentRoute>, Collection<RecentRoute>>)
                        recentRoutes -> mRealm.copyFromRealm(recentRoutes))
                .observeOn(Schedulers.newThread())
                .map(routeRecents -> {
                    List<RouteRecent> route = new ArrayList<>();

                    for (RecentRoute recent : routeRecents) {
                        RouteRecent routeRecent = new RouteRecent(recent.getId(),
                                recent.getDepartureId(), null, recent.getArrivalId(), null);

                        routeRecent.setOriginName(BusStopRealmHelper
                                .getSadName(recent.getDepartureId()));

                        routeRecent.setOriginMunic(BusStopRealmHelper
                                .getSadMunic(recent.getDepartureId()));

                        routeRecent.setDestinationName(BusStopRealmHelper
                                .getSadName(recent.getArrivalId()));

                        routeRecent.setDestinationMunic(BusStopRealmHelper
                                .getSadMunic(recent.getArrivalId()));

                        route.add(routeRecent);
                    }

                    return route;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recentItems -> {
                    mItems.clear();

                    if (recentItems.isEmpty()) {
                        recentText.setVisibility(View.GONE);
                        recentCard.setVisibility(View.INVISIBLE);
                    } else {
                        mItems.addAll(recentItems);

                        recentText.setVisibility(View.VISIBLE);
                        recentCard.setVisibility(View.VISIBLE);
                    }

                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void onListItemSelect(View view, int position) {
        mAdapter.toggleSelection(view, position);

        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            mActionMode = startActionMode(new ActionModeCallback());
        } else if (!hasCheckedItems && mActionMode != null) {
            mActionMode.finish();
        }

        if (mActionMode != null) {
            mActionMode.setTitle(mAdapter.getSelectedCount() + " selected");
        }
    }

    private void deleteRecent(RouteRecent item) {
        UserRealmHelper.deleteRecent(item.getId());

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        if (mItems.isEmpty()) {
            recentText.setVisibility(View.GONE);
        }
    }

    private void updateDateFormPicker() {
        SimpleDateFormat sdf;
        switch (Utils.locale(this)) {
            case "it":
                sdf = new SimpleDateFormat("dd MMM", Locale.ITALY);
                break;
            case "de":
                sdf = new SimpleDateFormat("dd MMM", Locale.GERMAN);
                break;
            default:
                sdf = new SimpleDateFormat("dd MMM", Locale.US);
                break;
        }

        datePicker.setText(sdf.format(mDateCalendar.getTime()));
    }

    private void updateTimeFormPicker() {
        timePicker.setText(new SimpleDateFormat("HH:mm", Locale.US).format(mTimeCalendar.getTime()));
    }

    private void updateDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf;

        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

        switch (Utils.locale(this)) {
            case "it":
                sdf = new SimpleDateFormat("dd MMM", Locale.ITALY);
                break;
            case "de":
                sdf = new SimpleDateFormat("dd MMM", Locale.GERMAN);
                break;
            default:
                sdf = new SimpleDateFormat("dd MMM", Locale.US);
                break;
        }

        datePicker.setText(sdf.format(calendar.getTime()));
    }

    private void updateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);

        mTimeCalendar.setTime(calendar.getTime());
        timePicker.setText(sdf.format(calendar.getTime()));
    }

    private void showResultsDialog() {
        RelativeLayout linearLayout = new RelativeLayout(this);
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMaxValue(20);
        numberPicker.setMinValue(1);
        numberPicker.setValue(mResultsCount);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(numberPicker, numPickerParams);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(R.string.dialog_route_number_title)
                .setView(linearLayout)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    mResultsCount = numberPicker.getValue();
                    results.setText(getResources().getQuantityString(R.plurals.route_result_count, numberPicker.getValue(), numberPicker.getValue()));

                })
                .setNegativeButton(R.string.dialog_route_number_negative, (dialog, id) -> dialog.cancel());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showWalkDialog() {
        RelativeLayout linearLayout = new RelativeLayout(this);
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMaxValue(30);
        numberPicker.setMinValue(1);
        numberPicker.setValue(mWalkTime);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(numberPicker, numPickerParams);

        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(R.string.dialog_route_number_title)
                .setView(linearLayout)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    mWalkTime = numberPicker.getValue();
                    walk.setText(getResources().getQuantityString(R.plurals.route_walk_count, mWalkTime, mWalkTime));

                })
                .setNegativeButton(R.string.dialog_route_number_negative, (dialog, id) -> dialog.cancel()).create()
                .show();
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_context_route, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.context_delete:
                    SparseBooleanArray selected = mAdapter.getSelectedIds();

                    for (int i = selected.size() - 1; i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            RouteRecent item1 = mItems.get(selected.keyAt(i));

                            mItems.remove(item1);
                            deleteRecent(item1);
                        }
                    }

                    mode.finish();
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.removeSelection();
            mActionMode = null;
        }
    }

    private static boolean isBrokenSamsungDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && isBetweenAndroidVersions(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }
}