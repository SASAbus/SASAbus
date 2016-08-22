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
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;

/**
 * @author Alex Lardschneider
 */
public class CircleImageAdapter extends RecyclerView.Adapter<CircleImageAdapter.ViewHolder> {

    private final Context mContext;
    private final List<String> mItems;
    private OnImageClickedListener mListener;

    public CircleImageAdapter(Context context, List<String> items,
                              OnImageClickedListener listener) {

        mContext = context;
        mItems = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_profile_picture, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        String item = mItems.get(position);

        Glide.with(mContext)
                .load(item)
                .into(viewHolder.image);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public final class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.list_item_profile_picture_layout) FrameLayout frameLayout;
        @BindView(R.id.list_item_profile_picture_image) ImageView image;

        private ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            frameLayout.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            mListener.onImageClick(position);
        }
    }

    public interface OnImageClickedListener {
        void onImageClick(int position);
    }
}