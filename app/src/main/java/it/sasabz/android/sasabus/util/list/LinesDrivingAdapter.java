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
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.line.LineDriving;
import it.sasabz.android.sasabus.data.model.line.LineDrivingContent;

/**
 * @author Alex Lardschneider
 */
public class LinesDrivingAdapter extends ArrayAdapter<LineDriving> {

    private final Context context;

    public LinesDrivingAdapter(Context context, List<LineDriving> items) {
        super(context, R.layout.list_item_lines_driving, items);

        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        LineDriving item = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.list_item_lines_driving, parent, false);

        holder = new ViewHolder();
        holder.title = (TextView) convertView.findViewById(R.id.list_lines_driving_title);
        holder.location = (TextView) convertView.findViewById(R.id.list_lines_driving_location);
        holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.list_lines_driving_linear);

        holder.title.setText(context.getString(R.string.line_format, item.getName()));
        holder.location.setText(item.getMunic());

        for (LineDrivingContent content : item.getContentList()) {
            LinearLayout parentLayout = new LinearLayout(context);
            parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            parentLayout.setOrientation(LinearLayout.HORIZONTAL);
            parentLayout.setWeightSum(3.0f);

            TextView location = new TextView(context);
            location.setText(content.getBusStop());
            location.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            location.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            location.setTextSize(14);
            location.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));

            parentLayout.addView(location);

            TextView delay = new TextView(context);
            delay.setText(content.getDelay() + "'");
            delay.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
            delay.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            delay.setTextSize(14);

            if (content.getDelay() > 0) {
                delay.setTextColor(ContextCompat.getColor(context, R.color.material_red_500));
            } else {
                delay.setTextColor(ContextCompat.getColor(context, R.color.material_green_500));
            }

            parentLayout.addView(delay);

            holder.linearLayout.addView(parentLayout);
        }

        convertView.setTag(holder);

        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView location;
        LinearLayout linearLayout;
    }
}