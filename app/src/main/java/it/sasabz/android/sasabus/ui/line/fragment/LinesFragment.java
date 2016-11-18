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

package it.sasabz.android.sasabus.ui.line.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.rest.model.Line;
import it.sasabz.android.sasabus.ui.line.LinesActivity;
import it.sasabz.android.sasabus.ui.line.LinesAllAdapter;
import it.sasabz.android.sasabus.util.DeviceUtils;

/**
 * Displays all lines, no matter if in service or not, in form of a list. Clicking on a line will
 * open {@link it.sasabz.android.sasabus.ui.line.LineDetailsActivity} to display more details about this line.
 *
 * @author Alex Lardschneider
 */
public class LinesFragment extends RxFragment {

    @BindView(R.id.refresh) SwipeRefreshLayout mRefresh;
    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;

    private ArrayList<Line> mItems;
    private LinesAllAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lines, container, false);

        ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            mItems = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);
        } else {
            mItems = new ArrayList<>();
        }

        mAdapter = new LinesAllAdapter(getActivity(), mItems);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        GridLayoutManager gridLayoutManager;
        if (DeviceUtils.isTablet(getActivity())) {
            gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        } else {
            gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        }

        recyclerView.setLayoutManager(gridLayoutManager);

        mRefresh.setColorSchemeResources(Config.REFRESH_COLORS);
        mRefresh.setOnRefreshListener(() -> ((LinesActivity) getActivity()).parseData(true));

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

        mRefresh.setRefreshing(true);

        LinesActivity.loadedCount++;
        if (LinesActivity.loadedCount == 3) {
            ((LinesActivity) getActivity()).parseData(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Config.BUNDLE_LIST, mItems);
        outState.putInt(Config.BUNDLE_ERROR_WIFI, mErrorWifi.getVisibility());
        outState.putInt(Config.BUNDLE_ERROR_GENERAL, mErrorGeneral.getVisibility());
    }


    public void setLines(Collection<Line> lines, boolean manualRefresh) {
        mItems.clear();
        mItems.addAll(lines);

        if (manualRefresh) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemRangeInserted(0, mItems.size());
        }

        mErrorWifi.setVisibility(View.GONE);
        mErrorGeneral.setVisibility(View.GONE);

        mRefresh.setRefreshing(false);
    }

    public void showErrorGeneral() {
        mErrorGeneral.setVisibility(View.VISIBLE);
        mErrorWifi.setVisibility(View.GONE);

        mItems.clear();
        mAdapter.notifyDataSetChanged();

        mRefresh.setRefreshing(false);
    }

    public void showErrorWifi() {
        mErrorWifi.setVisibility(View.VISIBLE);
        mErrorGeneral.setVisibility(View.GONE);

        if (mAdapter != null) {
            mItems.clear();
            mAdapter.notifyDataSetChanged();
        }

        mRefresh.setRefreshing(false);
    }
}