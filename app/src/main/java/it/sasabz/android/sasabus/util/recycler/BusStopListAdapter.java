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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.realm.busstop.BusStop;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.busstop.BusStopActivity;
import it.sasabz.android.sasabus.ui.busstop.BusStopDetailActivity;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class BusStopListAdapter extends RecyclerView.Adapter<BusStopListAdapter.ViewHolder> {

    private final Context mContext;
    private final List<BusStop> mItems;

    public BusStopListAdapter(Context context, List<BusStop> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_bus_stop, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BusStop station = mItems.get(position);

        if (station.getId() == -1) {
            holder.infoText.setVisibility(View.VISIBLE);

            if (holder.layout.getVisibility() != View.GONE) {
                holder.layout.setVisibility(View.GONE);
            }

            if (holder.divider.getVisibility() != View.GONE) {
                holder.divider.setVisibility(View.GONE);
            }
        } else if (station.getId() == -2) {
            holder.divider.setVisibility(View.VISIBLE);

            if (holder.layout.getVisibility() != View.GONE) {
                holder.layout.setVisibility(View.GONE);
            }

            if (holder.infoText.getVisibility() != View.GONE) {
                holder.infoText.setVisibility(View.GONE);
            }
        } else {
            if (holder.infoText.getVisibility() != View.GONE) {
                holder.infoText.setVisibility(View.GONE);
            }

            if (holder.divider.getVisibility() != View.GONE) {
                holder.divider.setVisibility(View.GONE);
            }

            if (holder.layout.getVisibility() != View.VISIBLE) {
                holder.layout.setVisibility(View.VISIBLE);
            }

            holder.name.setText(station.getName(mContext));

            holder.munic.setText(station.getMunic(mContext));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        @BindView(R.id.list_fragment_station_title) TextView name;
        @BindView(R.id.list_fragment_station_munic) TextView munic;
        @BindView(R.id.list_fragment_station_layout) CardView layout;

        @BindView(R.id.list_fragment_station_text) TextView infoText;
        @BindView(R.id.list_fragment_station_divider) View divider;

        private ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            layout.setOnClickListener(this);
            layout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Intent intent = new Intent(v.getContext(), BusStopDetailActivity.class);
            intent.putExtra(Config.EXTRA_STATION_ID, mItems.get(position).getId());
            ((Activity) mContext).startActivityForResult(intent, BusStopActivity.INTENT_DISPLAY_FAVORITES);
        }

        @Override
        public boolean onLongClick(View v) {
            BusStop station = mItems.get(getAdapterPosition());

            if (UserRealmHelper.hasFavoriteBusStop(station.getFamily())) {
                UserRealmHelper.removeFavoriteBusStop(station.getFamily());
                Snackbar.make(((BaseActivity) mContext).getMainContent(), mContext.getString(R.string.bus_stop_favorites_remove,
                        station.getName(mContext)), Snackbar.LENGTH_SHORT).show();
            } else {
                UserRealmHelper.addFavoriteBusStop(station.getFamily());
                Snackbar.make(((BaseActivity) mContext).getMainContent(), mContext.getString(R.string.bus_stop_favorites_add,
                        station.getName(mContext)), Snackbar.LENGTH_SHORT).show();
            }

            ((BusStopActivity) mContext).invalidateFavorites();

            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);

            return true;
        }
    }
}