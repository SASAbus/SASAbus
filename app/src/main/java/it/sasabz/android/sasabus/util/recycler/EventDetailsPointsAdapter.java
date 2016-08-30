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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.rest.model.EventPoint;

public class EventDetailsPointsAdapter extends RecyclerView.Adapter<EventDetailsPointsAdapter.ViewHolder> {

    private final List<EventPoint> mItems;
    private final Context mContext;

    public EventDetailsPointsAdapter(Context context, List<EventPoint> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_event_details_point, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EventPoint point = mItems.get(position);

        if (point.scanned) {
            holder.check.setColorFilter(ContextCompat.getColor(mContext, R.color.primary_green));
        } else {
            holder.check.setColorFilter(ContextCompat.getColor(mContext, R.color.black_400));
        }

        holder.title.setText(point.id + ". " + point.title);
        holder.description.setText(point.description);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_event_details_point_check) ImageView check;

        @BindView(R.id.list_event_details_point_title) TextView title;
        @BindView(R.id.list_event_details_point_description) TextView description;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}