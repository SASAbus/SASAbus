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
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.CloudApi;
import it.sasabz.android.sasabus.realm.user.TripToDelete;
import it.sasabz.android.sasabus.sync.SyncHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.widget.SimpleItemTouchHelperCallback;
import it.sasabz.android.sasabus.util.AlarmUtils;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.PlannedTripsAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

        // Send the request to delete the planned trip on the cloud.
        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        cloudApi.deletePlannedTripRx(removedItem.getHash())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        // Sending the request to delete the planned trip on the cloud failed so we
                        // need to store the request so we can retry next time a sync happens.

                        // Start a new realm instance as the activity-wide one might have been
                        // closed already and this request continues to run even if the activity
                        // has been destroyed.
                        Realm realm1 = Realm.getDefaultInstance();
                        realm1.beginTransaction();

                        TripToDelete tripToDelete = realm1.createObject(TripToDelete.class);
                        tripToDelete.setType(TripToDelete.TYPE_PLANNED_TRIP);
                        tripToDelete.setHash(removedItem.getHash());

                        realm1.commitTransaction();
                        realm1.close();
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });

        removedItem = null;
    }
}
