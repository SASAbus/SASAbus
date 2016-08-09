package it.sasabz.android.sasabus.util.recycler;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.rest.model.Badge;
import it.sasabz.android.sasabus.ui.ecopoints.EcoPointsActivity;

/**
 * @author Alex Lardschneider
 */
public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.ViewHolder> {

    private final List<Badge> mItems;
    private final Activity mActivity;

    public BadgeAdapter(Activity activity, List<Badge> items) {
        mItems = items;
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_badge, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Badge badge = mItems.get(position);

        Glide.with(mActivity)
                .load(badge.iconUrl)
                .into(holder.image);

        holder.name.setText(badge.title);
        holder.description.setText(badge.description);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_badge_layout) FrameLayout layout;
        @BindView(R.id.list_item_badge_image) ImageView image;
        @BindView(R.id.list_item_badge_name) TextView name;
        @BindView(R.id.list_item_badge_description) TextView description;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Badge badge = mItems.get(position);

            EcoPointsActivity.showBadgeDialog(mActivity, badge);
        }

        @Override
        public String toString() {
            return "BadgeAdapter.ViewHolder{} " + super.toString();
        }
    }
}