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

package it.sasabz.android.sasabus.ui.plannedtrip;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.model.trip.PlannedTripNotification;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.list.PlannedTripsNotificationAdapter;

/**
 * Displays detailed information about a planned trip. By pressing the
 * {@link android.support.design.widget.FloatingActionButton} in the left corner the user can edit
 * the planned trip.
 *
 * @author Alex Lardschneider
 */
public class PlannedTripsViewActivity extends AppCompatActivity {

    private static final String TAG = "PlannedTripsView";

    private final Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planned_trips_view);

        if (!getIntent().hasExtra(Config.EXTRA_PLANNED_TRIP_HASH)) {
            LogUtils.e(TAG, "Missing extra Config.EXTRA_PLANNED_TRIP_ID");

            finish();
            return;
        }

        String hash = getIntent().getStringExtra(Config.EXTRA_PLANNED_TRIP_HASH);
        it.sasabz.android.sasabus.realm.user.PlannedTrip trip = realm.where(it.sasabz.android.sasabus.realm.user.PlannedTrip.class)
                .equalTo("hash", hash).findFirst();

        if (trip == null) {
            LogUtils.e(TAG, "Planned trip with hash " + hash + " does not exist");

            finish();
            return;
        }

        it.sasabz.android.sasabus.model.trip.PlannedTrip plannedTrip = new it.sasabz.android.sasabus.model.trip.PlannedTrip(trip);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_clear_white_24dp));

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(plannedTrip.getTitle());

        TextView dateTime = (TextView) findViewById(R.id.planned_trips_view_date_time);
        dateTime.setText(getFormattedDateTime(plannedTrip.getTimestamp()));

        String[] lines = new String[plannedTrip.getLines().size()];
        List<Integer> lines1 = plannedTrip.getLines();
        for (int i = 0; i < lines1.size(); i++) {
            int line = lines1.get(i);
            lines[i] = Lines.lidToName(line);
        }

        TextView line = (TextView) findViewById(R.id.planned_trips_view_line);
        line.setText(getString(R.string.line_format, Utils.arrayToString(lines, ", ")));

        TextView stop = (TextView) findViewById(R.id.planned_trips_view_stop);
        stop.setText(BusStopRealmHelper.getName(plannedTrip.getBusStop()));

        List<PlannedTripNotification> items = new ArrayList<>();
        ListAdapter adapter = new PlannedTripsNotificationAdapter(this, items);

        for (int i = 0; i < plannedTrip.getNotifications().size(); i++) {
            int minutes = plannedTrip.getNotifications().get(i);

            int text;
            switch (minutes) {
                case 10:
                    text = R.string.planned_trips_add_notification_10;
                    break;
                case 30:
                    text = R.string.planned_trips_add_notification_30;
                    break;
                case 60:
                    text = R.string.planned_trips_add_notification_60;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown notification minute " + minutes);
            }

            items.add(new PlannedTripNotification(getString(text), minutes, i == 0, false));
        }

        ListView listView = (ListView) findViewById(R.id.planned_trips_view_notifications);
        listView.setAdapter(adapter);

        AnalyticsHelper.sendScreenView(TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_planned_trips_view, menu);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realm.close();
    }

    private CharSequence getFormattedDateTime(long timeStamp) {
        String locale = getResources().getConfiguration().locale.toString();

        String formatString;
        switch (locale) {
            case "de":
                formatString = "EEEE, MMMM dd, HH:mm";
                break;
            case "en":
                formatString = "EEEE, MMMM dd, HH:mm";
                break;
            default:
                formatString = "EEEE, MMMM dd, HH:mm";
                break;
        }

        return new SimpleDateFormat(formatString, getResources().getConfiguration().locale)
                .format(new Date(timeStamp * 1000));
    }
}
