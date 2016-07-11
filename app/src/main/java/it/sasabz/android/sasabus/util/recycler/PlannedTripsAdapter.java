package it.sasabz.android.sasabus.util.recycler;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.model.trip.PlannedTrip;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.ui.plannedtrip.PlannedTripsActivity;
import it.sasabz.android.sasabus.ui.plannedtrip.PlannedTripsViewActivity;
import it.sasabz.android.sasabus.ui.widget.ItemTouchHelperAdapter;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Alex Lardschneider
 */
public class PlannedTripsAdapter extends RecyclerView.Adapter<PlannedTripsAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private final List<PlannedTrip> mItems;
    private final Context mContext;

    private final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm", Locale.ITALY);

    private PlannedTripsActivity mActivity;

    public PlannedTripsAdapter(Context context, List<PlannedTrip> items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_planned_trips, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlannedTrip item = mItems.get(position);

        Date date = new Date(item.getTimestamp() * 1000);

        StringBuilder sb = new StringBuilder();
        for (int i : item.getLines()) {
            sb.append(Lines.lidToName(i)).append(", ");
        }

        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        if (item.getLines().size() == 1) {
            sb.insert(0, mContext.getString(R.string.line) + ' ');
        } else {
            sb.insert(0, mContext.getString(R.string.lines) + ' ');
        }

        holder.title.setText(item.getTitle());
        holder.time.setText(FORMAT.format(date));
        holder.line.setText(sb.toString());
        holder.stop.setText(BusStopRealmHelper.getName(item.getBusStop()));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onItemDismiss(int position) {
        PlannedTrip trip = mItems.get(position);

        mItems.remove(position);
        notifyItemRemoved(position);

        if (mActivity != null) {
            mActivity.onItemRemoved(trip, position);
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void setActivity(PlannedTripsActivity activity) {
        mActivity = activity;
    }

    public final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_planned_trips_card) CardView cardView;
        @BindView(R.id.list_item_planned_data_title) TextView title;
        @BindView(R.id.list_item_planned_data_time) TextView time;
        @BindView(R.id.list_item_planned_data_line) TextView line;
        @BindView(R.id.list_item_planned_data_stop) TextView stop;

        public ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Intent intent = new Intent(cardView.getContext(), PlannedTripsViewActivity.class);
            intent.putExtra(Config.EXTRA_PLANNED_TRIP_HASH, mItems.get(position).getHash());
            mContext.startActivity(intent);
        }
    }
}