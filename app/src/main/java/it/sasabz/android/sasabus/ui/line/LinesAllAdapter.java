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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.network.rest.model.Line;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;

/**
 * @author Alex Lardschneider
 */
public class LinesAllAdapter extends RecyclerView.Adapter<LinesAllAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Line> mItems;

    public LinesAllAdapter(Context context, List<Line> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.list_item_lines_general, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Line item = mItems.get(position);

        holder.title.setText(item.name);
        holder.departure.setText(item.origin);
        holder.arrival.setText(item.destination);

        holder.title.setTextColor(Color.parseColor('#' + Lines.getColorForId(item.id)));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        @BindView(R.id.list_item_lines_all_card) CardView cardView;
        @BindView(R.id.list_lines_all_title) TextView title;
        @BindView(R.id.list_lines_all_departure) TextView departure;
        @BindView(R.id.list_lines_all_arrival) TextView arrival;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Line item = mItems.get(position);

            Intent intent = new Intent(mContext, LineDetailsActivity.class);
            intent.putExtra(Config.EXTRA_LINE_ID, item.id);
            intent.putExtra(Config.EXTRA_LINE, item);

            ((Activity) mContext).startActivityForResult(intent, LinesActivity.INTENT_DISPLAY_FAVORITES);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return false;

            Line item = mItems.get(position);

            if (UserRealmHelper.hasFavoriteLine(item.id)) {
                UserRealmHelper.removeFavoriteLine(item.id);
                Snackbar.make(((BaseActivity) mContext).getMainContent(), mContext.getString(R.string.line_favorites_remove,
                        item.name), Snackbar.LENGTH_SHORT).show();
            } else {
                UserRealmHelper.addFavoriteLine(item.id);
                Snackbar.make(((BaseActivity) mContext).getMainContent(), mContext.getString(R.string.line_favorites_add,
                        item.name), Snackbar.LENGTH_SHORT).show();
            }

            ((LinesActivity) mContext).invalidateFavorites();

            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);

            return true;
        }

        @Override
        public String toString() {
            return "LinesAllAdapter.ViewHolder{} " + super.toString();
        }
    }
}