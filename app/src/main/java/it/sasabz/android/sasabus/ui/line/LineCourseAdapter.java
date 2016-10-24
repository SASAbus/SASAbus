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

package it.sasabz.android.sasabus.ui.line;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.line.LineCourse;
import it.sasabz.android.sasabus.realm.busstop.BusStop;
import it.sasabz.android.sasabus.ui.departure.DepartureActivity;

/**
 * @author Alex Lardschneider
 */
class LineCourseAdapter extends RecyclerView.Adapter<LineCourseAdapter.ViewHolder> {

    private final Context mContext;
    private final List<LineCourse> mItems;

    LineCourseAdapter(Context context, List<LineCourse> items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_line_course, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LineCourse item = mItems.get(position);

        int padding = (int) mContext.getResources().getDimension(
                R.dimen.line_course_big_circle_padding);

        if (position == 0 || position == mItems.size() - 1) {
            holder.image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.dot));
            holder.image.setPadding(padding, padding, padding, padding);
        } else {
            holder.image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_dots_5));
            holder.image.setPadding(0, 0, 0, 0);
        }

        if (item.dot) {
            holder.image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.dot));
            holder.image.setPadding(padding, padding, padding, padding);
        } else if (item.bus) {
            holder.image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bus));
            holder.image.setPadding(padding, padding, padding, padding);
        }

        if (item.active) {
            holder.text.setTextColor(ContextCompat.getColor(mContext, R.color.text_primary));
            holder.image.setColorFilter(ContextCompat.getColor(mContext, R.color.text_primary));
        } else {
            holder.text.setTextColor(ContextCompat.getColor(mContext, R.color.text_tertiary));
            holder.image.setColorFilter(ContextCompat.getColor(mContext, R.color.text_tertiary), PorterDuff.Mode.SRC_IN);
        }

        BusStop busStop = item.busStop;
        holder.text.setText(item.time + " - " + busStop.getName(mContext) + " (" + busStop.getMunic(mContext) + ')');
        holder.lines.setText(Html.fromHtml(item.lineText));
        holder.lines.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_line_course_layout) LinearLayout layout;
        @BindView(R.id.list_line_course_lines) TextView lines;
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

            Intent intent = DepartureActivity.intent(v.getContext(), item.busStop.getFamily());
            v.getContext().startActivity(intent);
        }

        @Override
        public String toString() {
            return "LineCourseAdapter.ViewHolder{} " + super.toString();
        }
    }
}