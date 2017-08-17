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

package it.sasabz.android.sasabus.ui.bus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.davale.sasabus.core.data.Buses;
import com.davale.sasabus.core.model.Bus;
import com.davale.sasabus.core.model.Vehicle;

import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Displays information about a bus/vehicle like license plate, fuel type ecc.
 * This activity consists of a {@link android.support.design.widget.AppBarLayout} with a picture of
 * the vehicle type as scrim, and a {@link CardView} below which holds the bus information.
 *
 * @author Alex Lardschneider
 */
public class BusDetailActivity extends AppCompatActivity {

    private static final String TAG = "BusDetailActivity";
    private static final String SCREEN_LABEL = "Bus details";

    private TextView mManufacturer;
    private TextView mModel;
    private TextView mFuel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.changeLanguage(this);

        setContentView(R.layout.activity_bus_details);

        Intent intent = getIntent();
        int vehicle = intent.getExtras().getInt(Config.EXTRA_VEHICLE);

        AnalyticsHelper.sendScreenView(TAG);
        AnalyticsHelper.sendEvent(SCREEN_LABEL, "Vehicle: " + vehicle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        CollapsingToolbarLayout mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbar.setTitle(getString(R.string.title_bus_details));

        mManufacturer = (TextView) findViewById(R.id.bus_details_manufacturer);
        mModel = (TextView) findViewById(R.id.bus_detail_model);
        mFuel = (TextView) findViewById(R.id.bus_detail_fuel);

        parseData(vehicle);
    }


    private void loadBackdrop(Vehicle vehicle) {
        ImageView imageView = (ImageView) findViewById(R.id.backdrop);

        Glide.with(this).load(Uri.parse("file:///android_asset/images/" + vehicle.getCode() + ".jpg"))
                .animate(R.anim.fade_in_short)
                .centerCrop()
                .crossFade()
                .into(imageView);
    }

    private void parseData(int vehicle) {
        Bus bus = Buses.getBus(vehicle);

        if (bus != null) {
            Vehicle v = bus.getVehicle();

            mManufacturer.setText(v.getManufacturer());
            mModel.setText(v.getModel());
            mFuel.setText(v.getFuelString(this));

            loadBackdrop(v);
        }
    }
}