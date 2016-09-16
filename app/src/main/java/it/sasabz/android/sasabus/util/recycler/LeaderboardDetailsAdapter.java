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
import android.support.v7.widget.LinearLayoutManager;
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

public class LeaderboardDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_ITEM = 1;
    private static final int VIEW_PROGRESS = 0;

    private final Context mContext;

    private final List<LeaderboardPlayer> mItems;

    private static final int visibleThreshold = 5;
    private int lastVisibleItem;
    private int totalItemCount;

    private boolean loading;
    public final LoadMoreListener mListener;

    public LeaderboardDetailsAdapter(Context context, List<LeaderboardPlayer> items,
                                     RecyclerView recyclerView, LoadMoreListener listener) {
        mContext = context;
        mItems = items;
        mListener = listener;

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager)
                recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                totalItemCount = getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!loading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                    mListener.loadMore();
                    loading = true;
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item_leaderboard, parent, false);

            vh = new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item_leaderboard_progress, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;

            LeaderboardPlayer player = mItems.get(position);

            viewHolder.rank.setText(String.valueOf(position + 1));
            viewHolder.name.setText(player.username);
            viewHolder.points.setText(mContext.getResources().getQuantityString(R.plurals.eco_points_points,
                    player.points, player.points));

            Glide.with(mContext)
                    .load(Endpoint.API + Endpoint.ECO_POINTS_PROFILE_PICTURE_USER + player.profile)
                    .into(viewHolder.profilePicture);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_leaderboard_profile_picture) ImageView profilePicture;
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

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {

        public ProgressViewHolder(View v) {
            super(v);
        }
    }

    public interface LoadMoreListener {
        void loadMore();
    }
}