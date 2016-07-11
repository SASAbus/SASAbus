package it.sasabz.android.sasabus.util.recycler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.rest.model.Line;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.ui.line.LineDetailActivity;
import it.sasabz.android.sasabus.ui.line.LinesActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_lines_general, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Line item = mItems.get(position);

        viewHolder.title.setText(mContext.getString(R.string.line_format, item.getName()));
        viewHolder.location.setText(item.getCity());
        viewHolder.departure.setText(item.getOrigin());
        viewHolder.arrival.setText(item.getDestination());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        @BindView(R.id.list_item_lines_all_card) CardView cardView;
        @BindView(R.id.list_lines_all_title) TextView title;
        @BindView(R.id.list_lines_all_location) TextView location;
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

            Intent intent = new Intent(mContext, LineDetailActivity.class);
            intent.putExtra(Config.EXTRA_LINE_ID, item.getId());
            intent.putExtra(Config.EXTRA_LINE, item);

            ((Activity) mContext).startActivityForResult(intent, LinesActivity.INTENT_DISPLAY_FAVORITES);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return false;

            Line item = mItems.get(position);

            if (UserRealmHelper.hasFavoriteLine(item.getId())) {
                UserRealmHelper.removeFavoriteLine(item.getId());
                Snackbar.make(((BaseActivity) mContext).getMainContent(), mContext.getString(R.string.line_favorites_remove,
                        item.getName()), Snackbar.LENGTH_SHORT).show();
            } else {
                UserRealmHelper.addFavoriteLine(item.getId());
                Snackbar.make(((BaseActivity) mContext).getMainContent(), mContext.getString(R.string.line_favorites_add,
                        item.getName()), Snackbar.LENGTH_SHORT).show();
            }

            ((LinesActivity) mContext).invalidateFavorites();

            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);

            return true;
        }
    }
}