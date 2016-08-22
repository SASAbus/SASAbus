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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.model.trip.Trip;
import it.sasabz.android.sasabus.ui.trips.TripDetailActivity;
import it.sasabz.android.sasabus.ui.trips.TripsActivity;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private final List<Trip> items;
    private final Context context;
    private TripsActivity activity;

    private final SimpleDateFormat DATE = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public TripAdapter(Context context, List<Trip> items) {
        this.items = items;
        this.context = context;
    }

    public void setActivity(TripsActivity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_trips, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Trip trip = items.get(position);

        String startDate = DATE.format(new Date(trip.getStartTime() * 1000));

        viewHolder.date.setText(startDate);
        viewHolder.line.setText(context.getString(R.string.line_format, Lines.lidToName(trip.getLine())));
        viewHolder.from.setText(trip.getOrigin());
        viewHolder.to.setText(trip.getDestination());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        @BindView(R.id.list_trips_layout) CardView layout;
        @BindView(R.id.list_trips_date) TextView date;
        @BindView(R.id.list_trips_line) TextView line;
        @BindView(R.id.list_trips_from) TextView from;
        @BindView(R.id.list_trips_to) TextView to;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            layout.setOnClickListener(this);
            layout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Trip trip = items.get(position);

            Intent intent = new Intent(context, TripDetailActivity.class);
            intent.putExtra(Config.EXTRA_TRIP_HASH, trip.getHash());

            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return false;

            new AlertDialog.Builder(context, R.style.DialogStyle)
                    .setMessage(R.string.dialog_trips_delete_message)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        activity.onDelete(items.get(position).getHash(), position);
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();

            return true;
        }
    }
}