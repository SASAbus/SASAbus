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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.appwidget.ParkingWidgetProvider;
import it.sasabz.android.sasabus.model.Parking;
import it.sasabz.android.sasabus.util.Settings;

import static android.app.Activity.RESULT_OK;

/**
 * @author David Dejori
 */
public class ParkingConfigureAdapter extends RecyclerView.Adapter<ParkingConfigureAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Parking> mItems;
    private final int mAppWidgetId;

    public ParkingConfigureAdapter(Context context, List<Parking> items, int mAppWidgetId) {
        mContext = context;
        mItems = items;
        this.mAppWidgetId = mAppWidgetId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_parking_configure, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Parking item = mItems.get(position);

        viewHolder.name.setText(item.getName());
        viewHolder.address.setText(item.getAddress());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_parking_card_configure)
        CardView cardView;
        @BindView(R.id.parking_list_name)
        TextView name;
        @BindView(R.id.parking_list_address)
        TextView address;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            cardView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            Parking item = mItems.get(getAdapterPosition());

            Settings.setWidgetParking(mContext, item.getId());

            Intent intent = new Intent(mContext, ParkingWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = {mAppWidgetId};
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            mContext.sendBroadcast(intent);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            ((Activity) mContext).setResult(RESULT_OK, resultValue);
            ((Activity) mContext).finish();
        }
    }
}