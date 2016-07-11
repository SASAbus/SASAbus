package it.sasabz.android.sasabus.util.list;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.trip.PlannedTripNotification;

import java.util.List;

/**
 * @author Alex Lardschneider
 */
public class PlannedTripsNotificationAdapter extends ArrayAdapter<PlannedTripNotification> {

    private final Context context;

    public PlannedTripsNotificationAdapter(Context context, List<PlannedTripNotification> items) {
        super(context, R.layout.list_item_lines_driving, items);

        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        PlannedTripNotification item = getItem(position);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.list_item_planned_trips_notification, parent, false);

        FrameLayout layoutImageLight = (FrameLayout) convertView.findViewById(R.id.list_item_planned_trips_notification_light_image);
        FrameLayout layoutImage = (FrameLayout) convertView.findViewById(R.id.list_item_planned_trips_notification_image);
        FrameLayout layoutLight = (FrameLayout) convertView.findViewById(R.id.list_item_planned_trips_notification_light);
        FrameLayout layout = (FrameLayout) convertView.findViewById(R.id.list_item_planned_trips_add_notification);

        holder = new ViewHolder();

        if (item.isImage() && item.isLight()) {
            layoutImage.setVisibility(View.GONE);
            layoutLight.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);

            layoutImageLight.setVisibility(View.VISIBLE);
            holder.text = (TextView) convertView.findViewById(R.id.planned_trips_notification_text_light_image);
        } else if (item.isLight()) {
            layoutImageLight.setVisibility(View.GONE);
            layoutImage.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);

            layoutLight.setVisibility(View.VISIBLE);
            holder.text = (TextView) convertView.findViewById(R.id.planned_trips_notification_text_light);
        } else if (item.isImage()) {
            layoutImageLight.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
            layoutLight.setVisibility(View.GONE);

            layoutImage.setVisibility(View.VISIBLE);
            holder.text = (TextView) convertView.findViewById(R.id.planned_trips_notification_text_image);
        } else {
            layoutImageLight.setVisibility(View.GONE);
            layoutImage.setVisibility(View.GONE);
            layoutLight.setVisibility(View.GONE);

            layout.setVisibility(View.VISIBLE);
            holder.text = (TextView) convertView.findViewById(R.id.planned_trips_notification_text);
        }

        holder.text.setText(item.getText());

        return convertView;
    }

    private static class ViewHolder {
        TextView text;
    }
}