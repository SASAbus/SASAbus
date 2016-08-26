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
import android.graphics.Color;
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
import it.sasabz.android.sasabus.model.route.RouteLegend;

/**
 * @author Alex Lardschneider
 */
public class RouteLegendAdapter extends RecyclerView.Adapter<RouteLegendAdapter.ViewHolder> {

    private final Context mContext;
    private final List<RouteLegend> mItems;

    private final int[] imageIds = {
            R.drawable.ic_bus,
            R.drawable.ic_directions_railway_white_48dp,
            R.drawable.ic_directions_subway_white_48dp,
            R.drawable.ic_pan_tool_white_48dp,
            R.drawable.ic_directions_walk_white_48dp
    };

    public RouteLegendAdapter(Context context, List<RouteLegend> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_route_legend, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        RouteLegend item = mItems.get(position);

        viewHolder.name.setText(item.getName());
        viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(mContext, imageIds[item.getType()]));
        viewHolder.color.setBackgroundColor(Color.parseColor(item.getColor()));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_route_legend_color_stripe) View color;
        @BindView(R.id.list_route_legend_icon) ImageView icon;
        @BindView(R.id.list_route_legend_name) TextView name;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}