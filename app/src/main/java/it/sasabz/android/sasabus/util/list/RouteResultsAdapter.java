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

package it.sasabz.android.sasabus.util.list;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.route.RouteLeg;
import it.sasabz.android.sasabus.data.model.route.RouteResult;

/**
 * @author Alex Lardschneider
 */
public class RouteResultsAdapter extends ArrayAdapter<RouteResult> {

    private final Context context;
    private final List<View> viewList;
    private final List<RouteResult> items;

    private final int[] imageIds = {
            R.drawable.ic_bus,
            R.drawable.ic_directions_railway_white_48dp,
            R.drawable.ic_directions_subway_white_48dp,
            R.drawable.ic_pan_tool_white_48dp,
            R.drawable.ic_directions_walk_white_48dp
    };

    public RouteResultsAdapter(Context context, List<RouteResult> items) {
        super(context, R.layout.list_item_route_results, items);

        this.context = context;
        this.items = items;

        viewList = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            viewList.add(null);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        viewList.clear();

        for (int i = 0; i < items.size(); i++) {
            viewList.add(null);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        RouteResult item = getItem(position);

        if (viewList.get(position) != null) {
            return viewList.get(position);
        }

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.list_item_route_results, parent, false);
        holder = new ViewHolder(convertView);

        holder.title.setText("#" + (position + 1));

        holder.dateStart.setText(item.getDepartureTime());
        holder.dateInterval.setText(item.getDuration() < 60 ? item.getDuration() + "'" : item.getDuration() / 60 + "h " + item.getDuration() % 60 + '\'');
        holder.dateEnd.setText(item.getArrivalTime());

        for (int i = 0; i < item.getLegs().size(); i++) {
            RouteLeg leg = item.getLegs().get(i);

            LinearLayout.LayoutParams relativeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            if (i >= 1) {
                relativeParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.dimen_24), 0, 0);
            } else {
                relativeParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.dimen_10), 0, 0);
            }

            LinearLayout parentLayout = new LinearLayout(context);
            parentLayout.setOrientation(LinearLayout.VERTICAL);
            parentLayout.setLayoutParams(relativeParams);

            RelativeLayout header = (RelativeLayout) mInflater.inflate(R.layout.partial_route_results_header, parentLayout, false);

            ImageView imageView = (ImageView) header.findViewById(R.id.partial_route_results_image_1);
            imageView.setImageResource(imageIds[leg.getId()]);

            TextView type = (TextView) header.findViewById(R.id.partial_route_results_header_type);
            type.setText(leg.getVehicle());

            TextView duration = (TextView) header.findViewById(R.id.partial_route_results_duration);
            TextView lineName = (TextView) header.findViewById(R.id.partial_route_results_line_name);
            LinearLayout header2 = (LinearLayout) header.findViewById(R.id.partial_route_results_header_2);
            LinearLayout header1 = (LinearLayout) header.findViewById(R.id.partial_route_results_header_1);

            if (leg.getId() == 3) {
                header2.setVisibility(View.GONE);
                duration.setVisibility(View.GONE);
            } else {
                lineName.setText(leg.getLine());
                duration.setText(leg.getDuration() + "'");
            }

            if (leg.getId() == 4) {
                header2.setVisibility(View.GONE);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) header1.getLayoutParams();
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                header1.setLayoutParams(params);
            }

            parentLayout.addView(header);

            if (leg.getId() != 3) {
                RelativeLayout contentDeparture = (RelativeLayout) mInflater.inflate(R.layout.partial_route_results_content, parentLayout, false);

                TextView departureStation = (TextView) contentDeparture.findViewById(R.id.partial_route_results_content_station);
                departureStation.setText(leg.getDeparture().getMunic() + ", " + leg.getDeparture().getName());

                TextView departureTime = (TextView) contentDeparture.findViewById(R.id.partial_route_results_content_time);
                departureTime.setText(leg.getDepartureTime());

                parentLayout.addView(contentDeparture);


                RelativeLayout contentArrival = (RelativeLayout) mInflater.inflate(R.layout.partial_route_results_content, parentLayout, false);

                TextView arrivalStation = (TextView) contentArrival.findViewById(R.id.partial_route_results_content_station);
                arrivalStation.setText(leg.getArrival().getMunic() + ", " + leg.getArrival().getName());

                TextView arrivalTime = (TextView) contentArrival.findViewById(R.id.partial_route_results_content_time);
                arrivalTime.setText(leg.getArrivalTime());

                parentLayout.addView(contentArrival);
            }

            holder.linearLayout.addView(parentLayout);
        }

        convertView.setTag(holder);

        viewList.set(position, convertView);

        return convertView;
    }

    static class ViewHolder {

        @BindView(R.id.route_list_title) TextView title;
        @BindView(R.id.route_list_time_start) TextView dateStart;
        @BindView(R.id.route_list_time_interval) TextView dateInterval;
        @BindView(R.id.route_list_time_end) TextView dateEnd;
        @BindView(R.id.route_list_linear) LinearLayout linearLayout;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}