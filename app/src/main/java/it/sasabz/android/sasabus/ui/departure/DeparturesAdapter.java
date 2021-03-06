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

package it.sasabz.android.sasabus.ui.departure;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.davale.sasabus.core.model.Departure;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.ui.line.LineCourseActivity;
import it.sasabz.android.sasabus.util.UIUtils;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
class DeparturesAdapter extends RecyclerView.Adapter<DeparturesAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Departure> mItems;

    DeparturesAdapter(Context context, List<Departure> items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public DeparturesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_departures, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeparturesAdapter.ViewHolder holder, int position) {
        Departure item = mItems.get(position);

        if (item.getDelay() > 3) {
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.material_red_500));
        } else if (item.getDelay() > 0) {
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.material_amber_700));
        } else {
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.material_green_500));
        }

        if (item.getDelay() == Departure.OPERATION_RUNNING) {
            holder.delay.setText(mContext.getString(R.string.departures_loading));
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.text_tertiary));
        } else if (item.getDelay() == Departure.NO_DELAY) {
            holder.delay.setText(mContext.getString(R.string.departures_no_data));
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.text_tertiary));
        } else {
            if (item.getDelay() > 0) {
                holder.delay.setText(mContext.getString(R.string.bottom_sheet_delayed, item.getDelay()));
            } else if (item.getDelay() < 0) {
                holder.delay.setText(mContext.getString(R.string.bottom_sheet_early, item.getDelay() * -1));
            } else {
                holder.delay.setText(mContext.getString(R.string.bottom_sheet_punctual));
            }

            holder.delay.setTextColor(UIUtils.getColorForDelay(mContext, item.getDelay()));
        }

        holder.line.setText(item.getLine());
        holder.destination.setText(item.getDestination());
        holder.time.setText(item.getTime());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_departures_line) TextView line;
        @BindView(R.id.list_item_departures_delay) TextView delay;
        @BindView(R.id.list_item_departures_destination) TextView destination;
        @BindView(R.id.list_item_departures_time) TextView time;

        @BindView(R.id.list_item_departures_layout) FrameLayout layout;

        private ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Departure item = mItems.get(position);

            Intent intent = LineCourseActivity.intent(v.getContext(), item.getTrip(),
                    item.getBusStopGroup(), item.getCurrentBusStop(), item.getVehicle());

            v.getContext().startActivity(intent);
        }
    }
}