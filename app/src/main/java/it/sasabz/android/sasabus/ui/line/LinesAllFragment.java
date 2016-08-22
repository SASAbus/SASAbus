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
import it.sasabz.android.sasabus.network.rest.model.Line;
import it.sasabz.android.sasabus.network.rest.response.LinesAllResponse;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.LinesAllAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Displays all lines, no matter if in service or not, in form of a list. Clicking on a line will
 * open {@link LineDetailActivity} to display more details about this line.
 *
 * @author Alex Lardschneider
 */
public class LinesAllFragment extends RxFragment {

    @BindView(R.id.refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;

    private final ArrayList<Line> mItems = new ArrayList<>();
    private LinesAllAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lines_all, container, false);

        ButterKnife.bind(this, view);

        mAdapter = new LinesAllAdapter(getActivity(), mItems);

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

            if (errorWifiVisibility != View.GONE || errorGeneralVisibility != View.GONE) {
                //noinspection ResourceType
                mErrorGeneral.setVisibility(errorGeneralVisibility);
                //noinspection ResourceType
                mErrorWifi.setVisibility(errorWifiVisibility);

                return;
            } else {
                ArrayList<Line> rowItemsTemp = savedInstanceState
                        .getParcelableArrayList(Config.BUNDLE_LIST);

                mItems.addAll(rowItemsTemp);
                mAdapter.notifyDataSetChanged();

                return;
            }
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

        String language = getResources().getConfiguration().locale.toString();

        LinesApi linesApi = RestClient.ADAPTER.create(LinesApi.class);
        linesApi.allLines(language)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LinesAllResponse>() {
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
                    public void onNext(LinesAllResponse linesAllResponse) {
                        mItems.clear();
                        mItems.addAll(linesAllResponse.lines);

                        mAdapter.notifyDataSetChanged();

                        mErrorWifi.setVisibility(View.GONE);
                        mErrorGeneral.setVisibility(View.GONE);

                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
                    }
                });
    }
}