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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.BusStopDetail;
import it.sasabz.android.sasabus.ui.line.LineCourseActivity;
import it.sasabz.android.sasabus.util.Utils;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class BusStopDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<BusStopDetail> mItems;
    private final int mBusStopFamily;

    private static final int BUS_STOP_DETAIL_HEADER = 0;
    private static final int BUS_STOP_DETAIL_BUS = 1;
    private static final int BUS_STOP_DETAIL_DATA = 2;

    public BusStopDetailsAdapter(Context context, int family, List<BusStopDetail> items) {
        mItems = items;
        mBusStopFamily = family;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        BusStopDetail item = mItems.get(position);

        if (item.getAdditionalData() == null) {
            return BUS_STOP_DETAIL_BUS;
        }

        switch (item.getAdditionalData()) {
            case "data":
                return BUS_STOP_DETAIL_DATA;
            default:
                return BUS_STOP_DETAIL_HEADER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case BUS_STOP_DETAIL_DATA:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.include_error_data, viewGroup, false);

                viewHolder = new ViewHolderError(view);
                break;
            case BUS_STOP_DETAIL_BUS:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_item_bus_stop_details_bus, viewGroup, false);

                viewHolder = new ViewHolderBus(view);

                break;
            case BUS_STOP_DETAIL_HEADER:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_item_bus_stop_details_header, viewGroup, false);

                viewHolder = new ViewHolderHeader(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BusStopDetail item = mItems.get(position);

        int itemType = getItemViewType(position);

        if (itemType == BUS_STOP_DETAIL_HEADER) {
            ViewHolderHeader header = (ViewHolderHeader) holder;

            String[] split = item.getAdditionalData().split("#");

            header.overviewMunic.setText(split[0]);
            header.overviewLines.setText(split[1]);
        } else if (itemType == BUS_STOP_DETAIL_BUS) {
            ViewHolderBus bus = (ViewHolderBus) holder;

            bus.lineCard.setVisibility(View.VISIBLE);

            if (item.getDelay() == Config.BUS_STOP_DETAILS_OPERATION_RUNNING) {
                setVisibilityIfNeeded(bus.delayProgress, View.VISIBLE);
                setVisibilityIfNeeded(bus.delay, View.GONE);
            } else {
                setVisibilityIfNeeded(bus.delayProgress, View.GONE);
                setVisibilityIfNeeded(bus.delay, View.VISIBLE);
            }

            if (item.getDelay() > 3) {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.material_red_500));
            } else if (item.getDelay() > 0) {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.material_amber_700));
            } else {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.material_green_500));
            }

            if (item.getDelay() == Config.BUS_STOP_DETAILS_NO_DELAY) {
                bus.delay.setText("");
            } else {
                bus.delay.setText(item.getDelay() + "'");
            }

            bus.line.setText(item.getLine());
            bus.heading.setText(mContext.getString(R.string.station_heading, item.getLastStation()));
            bus.departureTime.setText(item.getTime());

            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(item.getTime().split(":")[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(item.getTime().split(":")[1]));

                long difference = calendar.getTime().getTime() - new Date().getTime();
                difference = difference / 1000 / 60;

                if (difference < -10) difference += 1440;
                else if (difference < 0) difference = 0;

                if (difference < 60) {
                    bus.departureMinutes.setText(String.format(Locale.ITALY, "%d'", difference));
                } else {
                    bus.departureMinutes.setText("");
                }
            } catch (Exception e) {
                Utils.logException(e);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private void setVisibilityIfNeeded(View view, int visibility) {
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }


    static final class ViewHolderHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.stations_detail_munic) TextView overviewMunic;
        @BindView(R.id.stations_detail_lines) TextView overviewLines;

        private ViewHolderHeader(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    final class ViewHolderBus extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_stations_detail_line) TextView line;
        @BindView(R.id.list_stations_detail_delay) TextView delay;
        @BindView(R.id.list_stations_detail_delay_progress) ProgressBar delayProgress;
        @BindView(R.id.list_stations_detail_heading) TextView heading;
        @BindView(R.id.list_stations_detail_departure_time) TextView departureTime;
        @BindView(R.id.list_stations_detail_departure_minutes) TextView departureMinutes;

        @BindView(R.id.list_stations_detail_card_line) CardView lineCard;

        private ViewHolderBus(View view) {
            super(view);

            ButterKnife.bind(this, view);

            lineCard.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            BusStopDetail item = mItems.get(position);

            mContext.startActivity(new Intent(mContext, LineCourseActivity.class)
                    .putExtra("time", item.getTime())
                    .putExtra(Config.EXTRA_LINE_ID, item.getLineId())
                    .putExtra(Config.EXTRA_STATION_ID, toIntArray(BusStopRealmHelper
                            .getBusStopIdsFromGroup(mBusStopFamily))));
        }
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    static final class ViewHolderError extends RecyclerView.ViewHolder {

        private ViewHolderError(View view) {
            super(view);
        }
    }
}