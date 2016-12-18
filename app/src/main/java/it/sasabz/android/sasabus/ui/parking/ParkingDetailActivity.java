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

package it.sasabz.android.sasabus.ui.parking;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.data.realm.busstop.BusStop;
import it.sasabz.android.sasabus.ui.departure.DepartureActivity;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Shows detailed information about a parking.
 *
 * @author Alex Lardschneider
 */
public class ParkingDetailActivity extends AppCompatActivity {

    private final Collection<String> stations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.changeLanguage(this);

        setContentView(R.layout.activity_parking_details);

        Intent intent = getIntent();
        String station = intent.getExtras().getString("name");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar cardToolbar = (Toolbar) findViewById(R.id.card_toolbar);
        cardToolbar.setTitle(station);
        cardToolbar.inflateMenu(R.menu.menu_parking);

        MenuItem call = cardToolbar.getMenu().findItem(R.id.action_parking_call);
        MenuItem navigate = cardToolbar.getMenu().findItem(R.id.action_parking_navigate);

        Drawable callIcon = call.getIcon();
        if (callIcon != null) {
            callIcon.mutate();
            callIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_primary),
                    PorterDuff.Mode.SRC_ATOP);
        }

        Drawable navigateIcon = navigate.getIcon();
        if (navigateIcon != null) {
            navigateIcon.mutate();
            navigateIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_primary),
                    PorterDuff.Mode.SRC_ATOP);
        }

        cardToolbar.setOnMenuItemClickListener(item -> {
            try {
                switch (item.getItemId()) {
                    case R.id.action_parking_navigate:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" +
                                intent.getExtras().getDouble("lat") + ',' + intent.getExtras().getDouble("lon"))));
                        return true;
                    case R.id.action_parking_call:
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" +
                                intent.getExtras().getString("phone", "").replace("/", ""))));
                        return true;
                }
            } catch (ActivityNotFoundException e) {
                Utils.logException(e);
            }

            return false;
        });

        TextView address = (TextView) findViewById(R.id.parking_details_location);
        TextView currentFree = (TextView) findViewById(R.id.parking_detail_current_free);
        TextView currentTotal = (TextView) findViewById(R.id.parking_detail_current_total);

        if (station != null) {
            switch (station.split(":")[0]) {
                case "P2":
                    stations.add("5124:Perathonerstraße:Via Perathoner:Bozen:Bolzano:46.49704:11.35588:73");
                    stations.add("5254:Loacker:Loacker:Bozen:Bolzano:46.49494:11.35784:204");
                    stations.add("5104:Bahnhof Bozen:Stazione Bolzano:Bozen:Bolzano:46.49713:11.35887:210");
                    break;
                case "P3":
                    stations.add("5028:Waltherplatz:Piazza Walther:Bozen:Bolzano:46.49786:11.35445:51");
                    stations.add("5124:Perathonerstraße:Via Perathoner:Bozen:Bolzano:46.49704:11.35588:103");
                    stations.add("5026:Dominikanerplatz:Piazza Domenicani:Bozen:Bolzano:46.49778:11.35192:244");
                    break;
                case "P4":
                    stations.add("5301:Cavourstraße:Via Cavour:Bozen:Bolzano:46.50159:11.35967:160");
                    stations.add("5122:De-Lai-Straße:Via De Lai:Bozen:Bolzano:46.49936:11.35951:165");
                    stations.add("5120:Zollstange:Piazza Dogana:Bozen:Bolzano:46.50021:11.3608:201");
                    break;
                case "P5":
                    stations.add("5124:Perathonerstraße:Via Perathoner:Bozen:Bolzano:46.49704:11.35588:164");
                    stations.add("5104:Bahnhof Bozen:Stazione Bolzano:Bozen:Bolzano:46.49713:11.35887:169");
                    stations.add("5122:De-Lai-Straße:Via De Lai:Bozen:Bolzano:46.49936:11.35951:217");
                    break;
                case "P6":
                    stations.add("5104:Bahnhof Bozen:Stazione Bolzano:Bozen:Bolzano:46.49713:11.35887:2");
                    stations.add("5124:Perathonerstraße:Via Perathoner:Bozen:Bolzano:46.49704:11.35588:231");
                    stations.add("5122:De-Lai-Straße:Via De Lai:Bozen:Bolzano:46.49936:11.35951:251");
                    break;
                case "P8":
                    stations.add("5253:Parkhaus Bozen:Parcheggio Bolzano:Bozen:Bolzano:46.49454:11.35598:58");
                    stations.add("5254:Loacker:Loacker:Bozen:Bolzano:46.49494:11.35784:104");
                    stations.add("5307:Verdiplatz:Piazza Verdi:Bozen:Bolzano:46.49541:11.35329:285");
                    break;
                case "P16":
                    stations.add("862:Braillestraße:Via Braille:Bozen:Bolzano:46.473:11.32824:122");
                    stations.add("5185:Messe:Fiera:Bozen:Bolzano:46.47358:11.32554:188");
                    stations.add("5195:Eiswelle:Palaonda:Bozen:Bolzano:46.47286:11.3313:331");
                    break;
            }
        }

        String free = String.valueOf(intent.getExtras().getInt("currentFree"));
        if (free.equals("-1")) {
            free = "---";
        }

        address.setText(intent.getExtras().getString("address", ""));
        currentFree.setText(free + ' ' + getString(R.string.parking_detail_current_free));
        currentTotal.setText(intent.getExtras().getInt("total") + " " + getString(R.string.parking_detail_slots_total));

        getNearestStation();
    }

    private void onClickStation(String value) {
        BusStop busStop = BusStopRealmHelper.getBusStop(Integer.parseInt(value.split(":")[0]));
        Intent intent = DepartureActivity.intent(this, busStop.getFamily());
        startActivity(intent);
    }

    private void getNearestStation() {
        int index = 0;

        for (String s : stations) {
            index++;

            String[] split = s.split(":");

            View view = findViewById(R.id.parking_detail_station_divider);
            view.setVisibility(View.VISIBLE);

            RelativeLayout relativeLayout;

            if (index == 1) {
                relativeLayout = (RelativeLayout) findViewById(R.id.parking_detail_station_1);
                relativeLayout.setVisibility(View.VISIBLE);

                TextView name = (TextView) findViewById(R.id.parking_detail_station_name_1);
                TextView distance = (TextView) findViewById(R.id.parking_detail_station_distance_1);

                name.setText(split[Utils.locale(this).contains("de") ? 1 : 2]);
                distance.setText(split[7] + getString(R.string.parking_detail_station_distance));

                relativeLayout.setOnClickListener(v -> onClickStation(s));
            } else if (index == 2) {
                relativeLayout = (RelativeLayout) findViewById(R.id.parking_detail_station_2);
                relativeLayout.setVisibility(View.VISIBLE);

                TextView name = (TextView) findViewById(R.id.parking_detail_station_name_2);
                TextView distance = (TextView) findViewById(R.id.parking_detail_station_distance_2);

                name.setText(split[Utils.locale(this).contains("de") ? 1 : 2]);
                distance.setText(split[7] + getString(R.string.parking_detail_station_distance));

                relativeLayout.setOnClickListener(v -> onClickStation(s));
            } else if (index == 3) {
                relativeLayout = (RelativeLayout) findViewById(R.id.parking_detail_station_3);
                relativeLayout.setVisibility(View.VISIBLE);

                TextView name = (TextView) findViewById(R.id.parking_detail_station_name_3);
                TextView distance = (TextView) findViewById(R.id.parking_detail_station_distance_3);

                name.setText(split[Utils.locale(this).contains("de") ? 1 : 2]);
                distance.setText(split[7] + getString(R.string.parking_detail_station_distance));

                relativeLayout.setOnClickListener(v -> onClickStation(s));
                break;
            }
        }
    }
}