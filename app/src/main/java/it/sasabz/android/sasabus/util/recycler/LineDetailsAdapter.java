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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.line.LineDetail;
import it.sasabz.android.sasabus.ui.bus.BusDetailActivity;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class LineDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<LineDetail> mItems;

    private static final int LINE_DETAIL_HEADER = 0;
    private static final int LINE_DETAIL_BUS = 1;
    private static final int LINE_DETAIL_ERROR = 2;
    private static final int LINE_DETAIL_TRACK = 3;
    private static final int LINE_DETAIL_INTERNET = 4;

    private final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm", Locale.US);

    public LineDetailsAdapter(Context context, List<LineDetail> items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        LineDetail item = mItems.get(position);

        if (item.getAdditionalData() == null) {
            return LINE_DETAIL_BUS;
        }

        switch (item.getAdditionalData()) {
            case "error":
                return LINE_DETAIL_ERROR;
            case "track":
                return LINE_DETAIL_TRACK;
            case "nointernet":
                return LINE_DETAIL_INTERNET;
            default:
                return LINE_DETAIL_HEADER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;

        switch (viewType) {
            case LINE_DETAIL_ERROR:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.include_error_general, viewGroup, false);

                return new ViewHolderError(view);
            case LINE_DETAIL_TRACK:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.include_error_not_tracked, viewGroup, false);

                return new ViewHolderError(view);
            case LINE_DETAIL_INTERNET:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.include_error_wifi, viewGroup, false);

                return new ViewHolderError(view);
            case LINE_DETAIL_BUS:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_item_line_details_bus, viewGroup, false);

                return new ViewHolderBus(view);
            case LINE_DETAIL_HEADER:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_item_line_details_header, viewGroup, false);

                return new ViewHolderHeader(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LineDetail item = mItems.get(position);

        int itemType = getItemViewType(position);

        if (itemType == LINE_DETAIL_HEADER) {
            ViewHolderHeader header = (ViewHolderHeader) holder;

            String[] split = item.getAdditionalData().split("#");

            header.overviewDeparture.setText(split[0]);
            header.overviewArrival.setText(split[1]);
            header.overviewLocation.setText(split[2]);
            header.overviewDate.setText(split[3]);
            header.overviewInfo.setText(split[4]);
        } else if (itemType == LINE_DETAIL_BUS) {
            ViewHolderBus bus = (ViewHolderBus) holder;

            bus.lineCard.setVisibility(View.VISIBLE);

            String date = TIME.format(new Date());

            bus.currentName.setText(mContext.getString(R.string.line_current_stop, item.getCurrentStation()));
            bus.currentTime.setText(date);
            bus.lastName.setText(mContext.getString(R.string.line_heading, item.getLastStation()));
            bus.lastTime.setText(item.getLastTime());
            bus.delay.setText(String.format(Locale.ITALY, "%s'", item.getDelay()));

            if (item.getDelay() > 3) {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_red));
            } else if (item.getDelay() > 0) {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_amber_dark));
            } else {
                bus.delay.setTextColor(ContextCompat.getColor(mContext, R.color.primary_green));
            }

            if (item.isColor()) {
                bus.lineCard.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.active_line_bg));
            } else {
                bus.lineCard.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.card_background));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static final class ViewHolderHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.lines_detail_departure) TextView overviewDeparture;
        @BindView(R.id.lines_detail_arrival) TextView overviewArrival;
        @BindView(R.id.lines_detail_location) TextView overviewLocation;
        @BindView(R.id.lines_detail_date) TextView overviewDate;
        @BindView(R.id.lines_detail_info) TextView overviewInfo;

        private ViewHolderHeader(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    final class ViewHolderBus extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_lines_detail_current_name) TextView currentName;
        @BindView(R.id.list_lines_detail_current_time) TextView currentTime;
        @BindView(R.id.list_lines_detail_last_name) TextView lastName;
        @BindView(R.id.list_lines_detail_last_time) TextView lastTime;
        @BindView(R.id.list_lines_detail_delay) TextView delay;

        @BindView(R.id.list_lines_detail_card_bus)
        CardView lineCard;

        private ViewHolderBus(View view) {
            super(view);

            ButterKnife.bind(this, view);

            lineCard.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            if (position == RecyclerView.NO_POSITION) return;

            LineDetail rowItem = mItems.get(position);

            if (rowItem.getAdditionalData() == null) {
                Intent intent = new Intent(mContext, BusDetailActivity.class);
                intent.putExtra(Config.EXTRA_VEHICLE, rowItem.getVehicle());
                mContext.startActivity(intent);
            }
        }
    }

    static final class ViewHolderError extends RecyclerView.ViewHolder {

        private ViewHolderError(View view) {
            super(view);
        }
    }
}