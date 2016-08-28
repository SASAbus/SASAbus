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

package it.sasabz.android.sasabus.ui.plannedtrip;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.trip.PlannedTrip;
import it.sasabz.android.sasabus.sync.SyncHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.widget.SimpleItemTouchHelperCallback;
import it.sasabz.android.sasabus.util.AlarmUtils;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.recycler.PlannedTripsAdapter;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Displays all the planned trips. Planned trips are a special type of trip which the user can
 * specify. It will remind the user in form of a notification to depart at a certain hour to catch
 * the trip which he selected.
 * <p>
 * The user can specify the bus stop at which it will take the bus and the line which the user wants
 * to take. It also allows to set notifications which will inform the user about the upcoming trip
 * either 10min, 30min or 1h before the bus departs.
 * <p>
 * The planned trips will be synced to the server together with the normal trips.
 *
 * @author Alex Lardschneider.
 * @see PlannedTripsAddActivity to add a planned trip
 * @see PlannedTripsViewActivity to view a planned trip
 * @see SyncHelper to sync planned trips
 */
public class PlannedTripsActivity extends BaseActivity {

    private static final String TAG = "PlannedTripsActivity";

    private List<PlannedTrip> items;
    private PlannedTripsAdapter adapter;

    private PlannedTrip removedItem;
    private int removedPosition;

    private final Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planned_trips);

        AnalyticsHelper.sendScreenView(TAG);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.planned_trips_add_new);
        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new Intent(this, PlannedTripsAddActivity.class)));
        }

        items = new ArrayList<>();
        adapter = new PlannedTripsAdapter(this, items);
        adapter.setActivity(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);
            recyclerView.getItemAnimator().setAddDuration(250);
            recyclerView.setAdapter(adapter);

            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        parseData();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (removedItem != null) {
            removeItem();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        adapter.setActivity(null);
        realm.close();
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_PLANNED_TRIPS;
    }

    public void onItemRemoved(PlannedTrip trip, int position) {
        if (removedItem != null) {
            removeItem();
        }

        Snackbar.make(getMainContent(), R.string.planned_trips_delete_confirmation, Snackbar.LENGTH_LONG)
                .setAction(R.string.planned_trips_delete_undo, v -> {
                    items.add(removedPosition, removedItem);
                    adapter.notifyItemInserted(position);

                    removedItem = null;
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                            if (removedItem != null) {
                                removeItem();
                            }
                        }
                    }
                })
                .setActionTextColor(ContextCompat.getColor(this, R.color.primary))
                .show();

        removedItem = trip;
        removedPosition = position;
    }

    private void parseData() {
        realm.where(it.sasabz.android.sasabus.realm.user.PlannedTrip.class).findAllAsync().asObservable()
                .filter(RealmResults::isLoaded)
                .first()
                .map(plannedTrips -> {
                    List<PlannedTrip> trips = new ArrayList<>();

                    for (it.sasabz.android.sasabus.realm.user.PlannedTrip trip : plannedTrips) {
                        trips.add(new PlannedTrip(trip));
                    }

                    return trips;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(plannedTrips -> {
                    items.clear();
                    items.addAll(plannedTrips);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void removeItem() {
        it.sasabz.android.sasabus.realm.user.PlannedTrip trip =
                realm.where(it.sasabz.android.sasabus.realm.user.PlannedTrip.class)
                        .equalTo("hash", removedItem.getHash()).findFirst();

        if (trip != null) {
            realm.beginTransaction();
            trip.deleteFromRealm();
            realm.commitTransaction();
        } else {
            LogUtils.e(TAG, "Planned trip " + removedItem.getHash() + " does not exists in db");
        }

        AlarmUtils.cancelTrip(this, removedItem);

        removedItem = null;
    }
}
