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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.rest.model.Event;
import it.sasabz.android.sasabus.ui.ecopoints.event.EventDetailsActivity;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private final List<Event> mItems;
    private final Context mContext;

    private final SimpleDateFormat BEGIN_FORMAT = new SimpleDateFormat("EEE dd MMM, H:mm", Locale.getDefault());
    private final SimpleDateFormat END_FORMAT = new SimpleDateFormat("H:mm", Locale.getDefault());

    public EventsAdapter(Context context, List<Event> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_event, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = mItems.get(position);

        holder.title.setText(event.title);
        holder.description.setText(event.description);

        Calendar begin = Calendar.getInstance();
        begin.setTimeInMillis(event.begin * 1000);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(event.end * 1000);

        String subtitleText;

        if (begin.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)) {
            String beginText = BEGIN_FORMAT.format(begin.getTime());
            String endText = END_FORMAT.format(end.getTime());

            subtitleText = beginText + " - " + endText;
        } else {
            String beginText = BEGIN_FORMAT.format(begin.getTime());
            String endText = BEGIN_FORMAT.format(end.getTime());

            subtitleText = beginText + " - " + endText;
        }

        holder.date.setText(subtitleText);

        if (event.completed) {
            holder.completeBadge.setVisibility(View.VISIBLE);
        } else {
            holder.completeBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_event_card) CardView layout;
        @BindView(R.id.list_item_event_complete_badge) FrameLayout completeBadge;

        @BindView(R.id.list_item_event_title) TextView title;
        @BindView(R.id.list_item_event_subtitle) TextView description;
        @BindView(R.id.list_item_event_date) TextView date;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Event event = mItems.get(position);

            Intent intent = new Intent(mContext, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT, event);

            mContext.startActivity(intent);
        }
    }
}