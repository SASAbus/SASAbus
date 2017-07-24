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

import com.davale.sasabus.core.util.DeviceUtils;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.rest.model.Line;
import it.sasabz.android.sasabus.data.realm.user.FavoriteLine;
import it.sasabz.android.sasabus.ui.line.LinesActivity;
import it.sasabz.android.sasabus.ui.line.LinesAllAdapter;

/**
 * Displays all lines which the user added to the favorites. Identical to {@link LinesFragment},
 * except the empty state background which will be displayed if the user hasn't added any favorites.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class LinesFavoritesFragment extends RxFragment {

    @BindView(R.id.error_wifi) RelativeLayout mErrorWifi;
    @BindView(R.id.error_general) RelativeLayout mErrorGeneral;
    @BindView(R.id.refresh) SwipeRefreshLayout mRefresh;

    private ArrayList<Line> mItems;
    private LinesAllAdapter mAdapter;

    private Realm realm;

    private final Collection<FavoriteLine> favorites = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lines_favorites, container, false);
        ButterKnife.bind(this, view);

        if (savedInstanceState == null) {
            mItems = new ArrayList<>();
        } else {
            mItems = savedInstanceState.getParcelableArrayList(Config.BUNDLE_LIST);
        }

        realm = Realm.getDefaultInstance();

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

        mRefresh.setOnRefreshListener(() -> ((LinesActivity) getActivity()).parseData(true));
        mRefresh.setColorSchemeResources(Config.REFRESH_COLORS);
        mRefresh.setRefreshing(true);

        loadFavorites();

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


    public void loadFavorites() {
        realm.where(FavoriteLine.class).findAll().asObservable()
                .subscribe(lines -> {
                    favorites.clear();
                    favorites.addAll(lines);

                    mErrorGeneral.setVisibility(View.GONE);
                    mErrorWifi.setVisibility(View.GONE);

                    mRefresh.setRefreshing(false);
                });
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

    public void setLines(Iterable<Line> lines, boolean manualRefresh) {
        mItems.clear();

        loadFavorites();

        for (Line line : lines) {
            for (FavoriteLine favoriteLine : favorites) {
                if (line.id == favoriteLine.getId()) {
                    mItems.add(line);
                    break;
                }
            }
        }

        if (manualRefresh) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemRangeInserted(0, mItems.size());
        }

        mErrorWifi.setVisibility(View.GONE);
        mErrorGeneral.setVisibility(View.GONE);

        mRefresh.setRefreshing(false);
    }
}