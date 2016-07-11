package it.sasabz.android.sasabus.ui.trips;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.trip.Trip;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.CloudApi;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.user.TripToDelete;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.DeviceUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.TripAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Shows all the trips the user has made in a {@link RecyclerView} with {@link GridLayoutManager}
 * set to 2 columns. If the user has not made any trips it will display a background image and text
 * to inform the user.
 * <p>
 * Allows to delete planned trips by long pressing on them.
 * {@link #onDelete(String, int)} will be invoked when a trip should be deleted.
 *
 * @author Alex Lardschneider
 */
public class TripsActivity extends BaseActivity {

    private static final String TAG = "TripsActivity";
    private static final String SCREEN_LABEL = "Trips";

    private TripAdapter mAdapter;

    private final List<Trip> mTrips = new ArrayList<>();

    private final Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trips);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        mAdapter = new TripAdapter(this, mTrips);
        mAdapter.setActivity(this);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        if (DeviceUtils.isTablet(this)) {
            GridLayoutManager gridLayoutManager =
                    new GridLayoutManager(this, 2);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        parseData();
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_TRIPS;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realm.close();
    }


    public void onDelete(String hash, int position) {
        AnalyticsHelper.sendEvent(SCREEN_LABEL, "Deleted trip");

        realm.beginTransaction();
        realm.where(it.sasabz.android.sasabus.realm.user.Trip.class)
                .equalTo("hash", hash).findFirst().deleteFromRealm();
        realm.commitTransaction();

        mTrips.remove(position);
        mAdapter.notifyItemRemoved(position);

        // Send the request to delete the planned trip on the cloud.
        CloudApi cloudApi = RestClient.ADAPTER.create(CloudApi.class);
        cloudApi.deleteTripRx(hash)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        // Sending the request to delete the trip on the cloud failed so we
                        // need to store the request so we can retry next time a sync happens.

                        Realm realm1 = Realm.getDefaultInstance();
                        realm1.beginTransaction();

                        TripToDelete trip = realm1.createObject(TripToDelete.class);
                        trip.setType(TripToDelete.TYPE_TRIP);
                        trip.setHash(hash);

                        realm1.commitTransaction();
                        realm1.close();
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }

    private void parseData() {
        realm.where(it.sasabz.android.sasabus.realm.user.Trip.class).findAllAsync().asObservable()
                .filter(RealmResults::isLoaded)
                .first()
                .map(trips -> {
                    List<Trip> list = new ArrayList<>();

                    for (it.sasabz.android.sasabus.realm.user.Trip realmTrip : trips) {
                        Trip trip = new Trip(realmTrip);
                        trip.setOrigin(BusStopRealmHelper.getName(realmTrip.getOrigin()));
                        trip.setDestination(BusStopRealmHelper.getName(realmTrip.getDestination()));

                        list.add(trip);
                    }

                    return list;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trips -> {
                    mTrips.addAll(trips);
                    mAdapter.notifyDataSetChanged();
                });
    }
}
