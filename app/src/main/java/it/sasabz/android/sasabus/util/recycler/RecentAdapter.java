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

package it.sasabz.android.sasabus.util.recycler;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.route.RouteRecent;
import it.sasabz.android.sasabus.data.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.data.realm.busstop.BusStop;
import it.sasabz.android.sasabus.ui.departure.DepartureActivity;

/**
 * @author Alex Lardschneider
 */
public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    private final List<RouteRecent> mItems;
    private final RecyclerView mRecyclerView;
    private final Context mContext;
    private final SparseBooleanArray mSelectedItemsIds;

    public RecentAdapter(Context context, RecyclerView recyclerView, List<RouteRecent> items) {
        mItems = items;
        mRecyclerView = recyclerView;
        mContext = context;

        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_route_recent, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        RouteRecent item = mItems.get(i);

        holder.departure.setText(item.getOriginName() + " (" + item.getOriginMunic() + ')');
        holder.arrival.setText(item.getDestinationName() + " (" + item.getDestinationMunic() + ')');
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void toggleSelection(View view, int position) {
        selectView(view, position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds.clear();

        for (int i = 0; i < mItems.size(); i++) {
            View view = mRecyclerView.getLayoutManager().findViewByPosition(i);

            if (view != null) {
                view.setSelected(false);
                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_background));
            }
        }

        notifyDataSetChanged();
    }

    private void selectView(View view, int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, true);
            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.black_400));
        } else {
            mSelectedItemsIds.delete(position);
            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_background));
        }
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_route_recent_departure) TextView departure;
        @BindView(R.id.list_route_recent_arrival) TextView arrival;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            if (position == RecyclerView.NO_POSITION) return;

            RouteRecent item = mItems.get(position);

            BusStop busStop = BusStopRealmHelper.getBusStop(item.getId());
            Intent intent = DepartureActivity.intent(v.getContext(), busStop.getFamily());

            v.getContext().startActivity(intent);
        }
    }
}