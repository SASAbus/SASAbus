package it.sasabz.android.sasabus.ui.line;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.LinesApi;
import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.HydrogenAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Displays all currently driving hydrogen buses (428, 429, 430, 431, 432). If no hydrogen
 * buses are driving at the moment, it will display an empty state background like
 * {@link LinesFavoritesFragment}.
 *
 * @author Alex Lardschneider
 */
public class LinesHydrogenFragment extends RxFragment {

    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;
    @BindView(R.id.refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<RealtimeBus> mItems;
    private HydrogenAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lines_hydrogen, container, false);

        ButterKnife.bind(this, view);

        if (savedInstanceState == null) {
            mItems = new ArrayList<>();
        } else {
            mItems = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);
        }

        mAdapter = new HydrogenAdapter(getActivity(), mItems);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_amber, R.color.primary_red, R.color.primary_green, R.color.primary_indigo);
        mSwipeRefreshLayout.setOnRefreshListener(this::parseData);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            int errorWifiVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_WIFI);
            int errorGeneralVisibility = savedInstanceState.getInt(Config.BUNDLE_ERROR_GENERAL);

            //noinspection ResourceType
            mErrorGeneral.setVisibility(errorGeneralVisibility);

            //noinspection ResourceType
            mErrorWifi.setVisibility(errorWifiVisibility);

            return;
        }

        new Handler().postDelayed(this::parseData, Config.LINE_FRAGMENTS_POST_DELAY);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
        outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
    }

    private void parseData() {
        if (!NetUtils.isOnline(getActivity())) {
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

        LinesApi linesApi = RestClient.ADAPTER.create(LinesApi.class);
        linesApi.hydrogen()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .map(realtimeResponse -> {
                    for (RealtimeBus bus : realtimeResponse.buses) {
                        bus.currentStopName = bus.busStop == 0 ? getString(R.string.bus_depot) :
                                BusStopRealmHelper.getName(bus.busStop);
                    }
                    return realtimeResponse;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RealtimeResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        mErrorGeneral.setVisibility(View.VISIBLE);
                        mErrorWifi.setVisibility(View.GONE);

                        mItems.clear();
                        mAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }

                    @Override
                    public void onNext(RealtimeResponse realtimeResponse) {
                        if (realtimeResponse.buses.isEmpty()) {
                            mErrorGeneral.setVisibility(View.GONE);
                            mErrorWifi.setVisibility(View.GONE);

                            mItems.clear();
                            mAdapter.notifyDataSetChanged();

                            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));

                            return;
                        }

                        mItems.clear();
                        mItems.addAll(realtimeResponse.buses);

                        mAdapter.notifyDataSetChanged();

                        mErrorWifi.setVisibility(View.GONE);
                        mErrorGeneral.setVisibility(View.GONE);

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }
                });
    }
}