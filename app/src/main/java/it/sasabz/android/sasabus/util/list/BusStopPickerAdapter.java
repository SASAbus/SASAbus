package it.sasabz.android.sasabus.util.list;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.BusStop;

/**
 * @author Alex Lardschneider
 */
public class BusStopPickerAdapter extends ArrayAdapter<BusStop> {

    private final Context context;

    public BusStopPickerAdapter(Context context, List<BusStop> items) {
        super(context, R.layout.list_item_search_result, items);

        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        BusStop item = getItem(position);

        TextView textView;

        if (view == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.list_item_search_result, parent, false);

            textView = (TextView) view.findViewById(R.id.search_result);

            view.setTag(textView);
        } else {
            textView = (TextView) view.getTag();
        }

        String name = item.getName(context);
        String munic = item.getMunic(context);

        if (munic != null) {
            textView.setText(name + ", " + munic, TextView.BufferType.SPANNABLE);

            Spannable span = (Spannable) textView.getText();
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_primary)),
                    name.replace("{", "").replace("}", "").length() + 1,
                    span.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            textView.setText(name);
        }

        return view;
    }
}