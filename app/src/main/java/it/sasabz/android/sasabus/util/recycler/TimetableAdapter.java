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
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.util.IOUtils;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private final Context mContext;
    private final List<String> mItems;

    public TimetableAdapter(Context context, List<String> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_timetable, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (mItems.get(position).equals("BZ_MAP")) {
            viewHolder.line.setText(R.string.timetable_map_bolzano);
            viewHolder.munic.setText("");
        } else if (mItems.get(position).equals("ME_MAP")) {
            viewHolder.line.setText(R.string.timetable_map_merano);
            viewHolder.munic.setText("");
        } else {
            String[] array = mItems.get(position).split("_");

            if (array.length != 2) {
                viewHolder.line.setText("");
                viewHolder.munic.setText("");
                return;
            }

            String munic = array[0];
            munic = munic.equals("BZ") ? mContext.getString(R.string.bolzano) : mContext.getString(R.string.merano);

            viewHolder.line.setText(mContext.getString(R.string.line_format, array[1]));
            viewHolder.munic.setText(munic);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_timetable_line)
        TextView line;
        @BindView(R.id.list_timetable_munic)
        TextView munic;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            String tripId = String.valueOf(mItems.get(position));

            if (tripId.equals("-1")) {
                tripId = "map_bz";
            } else if (tripId.equals("-2")) {
                tripId = "map_me";
            }

            File file = new File(IOUtils.getTimetablesDir(mContext), tripId + ".pdf");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent chooser = Intent.createChooser(intent, mContext.getString(R.string.timetable_open));

            mContext.startActivity(chooser);
        }
    }
}