package it.sasabz.android.sasabus.util.recycler;

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
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.Vehicle;
import it.sasabz.android.sasabus.model.Vehicles;
import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.ui.MapActivity;
import it.sasabz.android.sasabus.util.Preconditions;

/**
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class HydrogenAdapter extends RecyclerView.Adapter<HydrogenAdapter.ViewHolder> {

    private final Context mContext;
    private final List<RealtimeBus> mItems;

    public HydrogenAdapter(Context context, List<RealtimeBus> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_lines_hydrogen, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RealtimeBus bus = mItems.get(position);

        Vehicle vehicle = Vehicles.getBus(mContext, bus.vehicle);
        Preconditions.checkNotNull(vehicle, "vehicle == null");

        holder.id.setText(String.valueOf(bus.vehicle));

        holder.line.setText(bus.lineName.equals("null") ? mContext.getString(R.string.bus_not_in_service_short) :
                mContext.getString(R.string.line) + ' ' + bus.lineName);

        holder.busStop.setText(bus.currentStopName);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item_lines_all_card) CardView cardView;
        @BindView(R.id.list_lines_hydrogen_id) TextView id;
        @BindView(R.id.list_lines_hydrogen_line) TextView line;
        @BindView(R.id.list_lines_hydrogen_bus_stop) TextView busStop;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            RealtimeBus item = mItems.get(position);
            Intent intent = new Intent(mContext, MapActivity.class);
            intent.putExtra(Config.EXTRA_VEHICLE, item.vehicle);
            mContext.startActivity(intent);
        }
    }
}