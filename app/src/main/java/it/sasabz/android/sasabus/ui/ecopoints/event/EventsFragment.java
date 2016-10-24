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

package it.sasabz.android.sasabus.ui.ecopoints.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.NetUtils;
import it.sasabz.android.sasabus.data.network.auth.AuthHelper;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.EventsApi;
import it.sasabz.android.sasabus.data.network.rest.model.Event;
import it.sasabz.android.sasabus.data.network.rest.model.EventPoint;
import it.sasabz.android.sasabus.data.network.rest.response.EventResponse;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.EventsAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EventsFragment extends RxFragment {

    private static final String TAG = "EventsFragment";

    @BindView(R.id.recycler) RecyclerView recyclerView;
    @BindView(R.id.refresh) SwipeRefreshLayout mRefresh;

    private ArrayList<Event> mItems;
    private EventsAdapter mAdapter;

    private final BroadcastReceiver pointReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.e(TAG, "Got event point broadcast");

            int id = intent.getIntExtra(EventDetailsActivity.EXTRA_BEACON_POINT, -1);

            if (id == -1) {
                LogUtils.e(TAG, "Missing intent extra " + EventDetailsActivity.EXTRA_BEACON_POINT);
                return;
            }

            for (int i = 0; i < mItems.size(); i++) {
                Event event = mItems.get(i);

                for (EventPoint point : event.points) {
                    if (point.id == id) {
                        point.scanned = true;
                    }
                }

                int scannedCount = 0;
                for (EventPoint point : event.points) {
                    if (point.scanned) {
                        scannedCount++;
                    }
                }

                if (scannedCount == event.points.size()) {
                    event.completed = true;
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    };
    private final BroadcastReceiver eventCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.e(TAG, "Got QR code broadcast");

            String eventId = intent.getStringExtra(EventDetailsActivity.EXTRA_EVENT_ID);
            String code = intent.getStringExtra(EventDetailsActivity.EXTRA_QR_CODE);

            if (TextUtils.isEmpty(eventId)) {
                LogUtils.e(TAG, "Missing intent extra " + EventDetailsActivity.EXTRA_EVENT_ID);
                return;
            }

            if (TextUtils.isEmpty(code)) {
                LogUtils.e(TAG, "Missing intent extra " + EventDetailsActivity.EXTRA_QR_CODE);
                return;
            }

            for (Event event : mItems) {
                if (event.id.equals(eventId)) {
                    event.qrCode = code;
                    event.completed = true;
                }
            }
        }
    };
    private final BroadcastReceiver qrCodeRedeemedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.e(TAG, "Got QR code redeemed broadcast");

            String eventId = intent.getStringExtra(EventDetailsActivity.EXTRA_EVENT_ID);

            if (TextUtils.isEmpty(eventId)) {
                LogUtils.e(TAG, "Missing intent extra " + EventDetailsActivity.EXTRA_EVENT_ID);
                return;
            }

            for (Event event : mItems) {
                if (event.id.equals(eventId)) {
                    event.redeemed = true;
                    event.completed = true;
                    break;
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_eco_points_events, container, false);
        ButterKnife.bind(this, view);

        mItems = new ArrayList<>();
        mAdapter = new EventsAdapter(getActivity(), mItems);

        mRefresh.setOnRefreshListener(this::parseData);
        mRefresh.setColorSchemeResources(R.color.primary_amber,
                R.color.primary_red, R.color.primary_green, R.color.primary_indigo);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(pointReceiver,
                new IntentFilter(EventDetailsActivity.BROADCAST_BEACON_SEEN));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(eventCompletedReceiver,
                new IntentFilter(EventDetailsActivity.BROADCAST_EVENT_COMPLETED));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(qrCodeRedeemedReceiver,
                new IntentFilter(QrCodeActivity.BROADCAST_QR_CODE_REDEEMED));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        parseData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(pointReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(eventCompletedReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(qrCodeRedeemedReceiver);
    }

    private void parseData() {
        if (!NetUtils.isOnline(getActivity())) {
            return;
        }

        mRefresh.setRefreshing(true);

        EventsApi eventsApi = RestClient.ADAPTER.create(EventsApi.class);
        eventsApi.getEvents()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EventResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.logException(e);

                        AuthHelper.checkIfUnauthorized(getActivity(), e);
                    }

                    @Override
                    public void onNext(EventResponse response) {
                        mItems.clear();
                        mItems.addAll(response.events);

                        mAdapter.notifyDataSetChanged();

                        mRefresh.setRefreshing(false);
                    }
                });
    }
}
