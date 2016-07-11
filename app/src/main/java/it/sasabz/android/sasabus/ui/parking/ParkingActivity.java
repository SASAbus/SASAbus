package it.sasabz.android.sasabus.ui.parking;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.Parking;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.ParkingApi;
import it.sasabz.android.sasabus.network.rest.response.ParkingResponse;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.ParkingAdapter;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Shows a list of available parking spots in bolzano with name, location, total spots and free
 * spots. More details are available by pressing on the parking to launch {@link ParkingDetailActivity}.
 *
 * @author Alex Lardschneider
 */
public class ParkingActivity extends BaseActivity {

    private static final String TAG = "ParkingActivity";

    private final ArrayList<Parking> mItems = new ArrayList<>();
    private ParkingAdapter mAdapter;

    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;
    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_parking);

        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        mAdapter = new ParkingAdapter(this, mItems);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(this::parseData);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_amber, R.color.primary_red,
                R.color.primary_green, R.color.primary_indigo);

        if (savedInstanceState != null) {
            int errorWifiVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_WIFI);
            int errorGeneralVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_GENERAL);

            if (errorWifiVisibility != View.GONE || errorGeneralVisibility != View.GONE) {
                // noinspection ResourceType
                mErrorGeneral.setVisibility(errorGeneralVisibility);
                // noinspection ResourceType
                mErrorWifi.setVisibility(errorWifiVisibility);

                return;
            } else {
                List<Parking> list = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);

                if (list != null && !list.isEmpty()) {
                    mItems.addAll(list);
                    mAdapter.notifyDataSetChanged();

                    return;
                }
            }
        }

        parseData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
        outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_PARKING;
    }

    private void parseData() {
        if (!NetUtils.isOnline(this)) {
            mErrorWifi.setVisibility(View.VISIBLE);
            mErrorGeneral.setVisibility(View.GONE);

            if (mAdapter != null) {
                mItems.clear();
                mAdapter.notifyDataSetChanged();
            }

            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
            return;
        }

        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));

        ParkingApi parkingApi = RestClient.ADAPTER.create(ParkingApi.class);
        parkingApi.getParking(locale())
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParkingResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        mErrorGeneral.setVisibility(View.VISIBLE);

                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }

                    @Override
                    public void onNext(ParkingResponse parkingResponse) {
                        mItems.clear();
                        mItems.addAll(parkingResponse.parking);

                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }

                        mErrorGeneral.setVisibility(View.GONE);
                        mErrorWifi.setVisibility(View.GONE);

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }
                });
    }
}