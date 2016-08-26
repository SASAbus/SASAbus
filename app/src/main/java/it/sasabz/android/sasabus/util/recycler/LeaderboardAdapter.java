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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.model.LeaderboardPlayer;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardPlayer> mItems;
    private final Context mContext;

    public LeaderboardAdapter(Context context, List<LeaderboardPlayer> items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_leaderboard, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LeaderboardPlayer player = mItems.get(position);

        holder.rank.setText(String.valueOf(position + 1));
        holder.name.setText(player.username);
        holder.points.setText(mContext.getResources().getQuantityString(R.plurals.eco_points_points,
                player.points, player.points));

        Glide.with(mContext)
                .load(Endpoint.API + Endpoint.ECO_POINTS_PROFILE_PICTURE_USER + player.profile)
                .into(holder.profilePicture);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_leaderboard_profile_picture) public ImageView profilePicture;
        @BindView(R.id.list_item_leaderboard_rank) TextView rank;
        @BindView(R.id.list_item_leaderboard_name) TextView name;
        @BindView(R.id.list_item_leaderboard_points) TextView points;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
        }
    }
}