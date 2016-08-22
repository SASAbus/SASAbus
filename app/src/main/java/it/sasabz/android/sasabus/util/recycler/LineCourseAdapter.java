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
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.line.LineCourse;
import it.sasabz.android.sasabus.ui.busstop.BusStopDetailActivity;

/**
 * @author Alex Lardschneider
 */
public class LineCourseAdapter extends RecyclerView.Adapter<LineCourseAdapter.ViewHolder> {

    private final Context mContext;
    private final List<LineCourse> mItems;

    public LineCourseAdapter(Context context, List<LineCourse> items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_line_course, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        LineCourse item = mItems.get(i);

        if (i == 0 || i == mItems.size() - 1) {
            holder.image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.dot));
        } else {
            holder.image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.dots));
        }

        if (i >= 1 && !mItems.get(i - 1).isActive() && item.isActive() || item.isDot()) {
            holder.image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.dot));
        }

        if (item.isActive()) {
            holder.text.setTextColor(ContextCompat.getColor(mContext, R.color.text_default));
            holder.image.setColorFilter(ContextCompat.getColor(mContext, R.color.text_default));
        } else {
            holder.text.setTextColor(ContextCompat.getColor(mContext, R.color.text_secondary));
            holder.image.setColorFilter(ContextCompat.getColor(mContext, R.color.text_secondary), PorterDuff.Mode.SRC_IN);
        }

        holder.text.setText(item.getTime() + " - " + item.getBusStop() + " (" + item.getMunic() + ')');
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_lines_course_layout) LinearLayout layout;
        @BindView(R.id.list_line_course_image) ImageView image;
        @BindView(R.id.list_line_course_text) TextView text;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            if (position == RecyclerView.NO_POSITION) return;

            LineCourse item = mItems.get(position);

            Intent intent = new Intent(v.getContext(), BusStopDetailActivity.class);
            intent.putExtra(Config.EXTRA_STATION_ID, item.getId());
            v.getContext().startActivity(intent);
        }
    }
}