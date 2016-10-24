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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.Departure;
import it.sasabz.android.sasabus.ui.line.LineCourseActivity;
import it.sasabz.android.sasabus.util.UIUtils;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class DeparturesAdapter extends RecyclerView.Adapter<DeparturesAdapter.ViewHolder> {

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

        if (item.delay > 3) {
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_red));
        } else if (item.delay > 0) {
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_amber_dark));
        } else {
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_green));
        }

        if (item.delay == Departure.OPERATION_RUNNING) {
            holder.delay.setText(mContext.getString(R.string.departures_loading));
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.text_tertiary));
        } else if (item.delay == Departure.NO_DELAY) {
            holder.delay.setText(mContext.getString(R.string.departures_no_data));
            holder.delay.setTextColor(ContextCompat.getColor(mContext, R.color.text_tertiary));
        } else {
            if (item.delay > 0) {
                holder.delay.setText(mContext.getString(R.string.bottom_sheet_delayed, item.delay));
            } else if (item.delay < 0) {
                holder.delay.setText(mContext.getString(R.string.bottom_sheet_early, item.delay * -1));
            } else {
                holder.delay.setText(mContext.getString(R.string.bottom_sheet_punctual));
            }

            holder.delay.setTextColor(UIUtils.getColorForDelay(mContext, item.delay));
        }

        holder.line.setText(item.line);
        holder.destination.setText(item.destination);
        holder.time.setText(item.time);
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

            Intent intent = LineCourseActivity.intent(v.getContext(), item.lineId, item.trip,
                    item.busStopGroup, item.currentBusStop, item.vehicle);

            v.getContext().startActivity(intent);
        }
    }
}