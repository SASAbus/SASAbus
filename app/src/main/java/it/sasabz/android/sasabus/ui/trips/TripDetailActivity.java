package it.sasabz.android.sasabus.ui.trips;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.Vehicle;
import it.sasabz.android.sasabus.model.Vehicles;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.busstop.BusStop;
import it.sasabz.android.sasabus.realm.user.Trip;
import it.sasabz.android.sasabus.ui.bus.BusDetailActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.map.TripDetailsMapView;
import it.sasabz.android.sasabus.util.recycler.TripAdapter;

/**
 * Shows detailed information about a trip like origin, destination, map which shows all the
 * bus stops the user passed by, and some info about fuel consumption.
 * <p>
 * As the {@link TripAdapter} starts this activity by using
 * a reveal animation it will slide up the toolbar and cards to display a nice animation
 * sequence.
 *
 * @author Alex Lardschneider
 */
public class TripDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TripDetailActivity";

    @BindView(R.id.trip_detail_start_station) TextView mStartStation;
    @BindView(R.id.trip_detail_start_time) TextView mStartTime;
    @BindView(R.id.trip_detail_stop_station) TextView mStopStation;
    @BindView(R.id.trip_detail_stop_time) TextView mStopTime;
    @BindView(R.id.trip_detail_duration) TextView mDuration;
    @BindView(R.id.trip_detail_line) TextView mLine;
    @BindView(R.id.trip_detail_distance) TextView mDistanceText;
    @BindView(R.id.trip_detail_vehicle_brand) TextView mVehicleBrand;
    @BindView(R.id.trip_detail_vehicle_name) TextView mVehicleFuel;
    @BindView(R.id.trip_detail_vehicle_info) LinearLayout mVehicleInfo;
    @BindView(R.id.trip_detail_vehicle_error) LinearLayout mVehicleError;
    @BindView(R.id.trip_detail_scrollview) ScrollView mScrollView;
    @BindView(R.id.trip_detail_vehicle_loading) ProgressBar mVehicleLoading;

    @BindView(R.id.trip_details_card_1) CardView cardView1;
    @BindView(R.id.trip_details_card_2) CardView cardView2;
    @BindView(R.id.trip_details_card_3) CardView cardView3;
    @BindView(R.id.trip_details_card_4) CardView cardView4;

    private String mTripHash;
    private Trip mTrip;

    private float mDistance;

    private final Realm realm = Realm.getDefaultInstance();

    private final short[] mEmissions = {
            0,   // ignore
            0,   // H2
            89,  // 2
            89,  // 3
            110, // 4
            89,  // 5
            110, // 6
            110, // 7
            89,  // 8
            89,  // 9
            89,  // 10
            110, // 11
            110, // 12
            110, // 13
            110, // 14
            160, // 15
            160, // 16
            160, // 17
            160, // 18
            160, // 19
            160  // 99
    };

    private TripDetailsMapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.changeLanguage(this);

        setContentView(R.layout.activity_trip_details);

        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mTripHash = intent.getStringExtra(Config.EXTRA_TRIP_HASH);

        cardView3.setOnClickListener(this);

        mVehicleLoading.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.primary_amber),
                PorterDuff.Mode.SRC_ATOP);

        WebView webView = (WebView) findViewById(R.id.googlemap);

        mapView = new TripDetailsMapView(this, webView);

        parseData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trip_details_card_3:
                Intent intent = new Intent(this, BusDetailActivity.class);
                intent.putExtra(Config.EXTRA_VEHICLE, mTrip.getVehicle());
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realm.close();
    }


    private void parseData() {
        mTrip = realm.where(Trip.class).equalTo("hash", mTripHash).findFirst();

        if (mTrip == null) {
            mScrollView.setVisibility(View.GONE);

            return;
        }

        parseVehicleData();

        Date startDate = new Date(mTrip.getDeparture() * 1000L);
        Date stopDate = new Date(mTrip.getArrival() * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ITALY);

        mStartStation.setText(BusStopRealmHelper
                .getName(mTrip.getOrigin()));
        mStopStation.setText(BusStopRealmHelper
                .getName(mTrip.getDestination()));

        mStartTime.setText(sdf.format(startDate));
        mStopTime.setText(sdf.format(stopDate));

        mLine.setText(Lines.lidToName(mTrip.getLine()));

        int timeDifference = (int) (stopDate.getTime() / 60000 - startDate.getTime() / 60000);

        if (timeDifference > 59) {
            int hours = timeDifference % 60;
            int minutes = timeDifference % 60;

            mDuration.setText(hours + "h " + minutes + '\'');
        } else {
            mDuration.setText(timeDifference + "'");
        }

        new Handler().postDelayed(() -> {
            mDistance = parseMapDataAndDistance(mTrip);
            parseVehicleData();
        }, 1000);
    }

    private float parseMapDataAndDistance(Trip trip) {
        String[] tripList = trip.getPath().split(",");

        List<BusStop> busStops = new ArrayList<>();

        float distance = 0F;
        for (int i = 0; i < tripList.length; i++) {
            BusStop busStop = BusStopRealmHelper.getBusStop(Integer.parseInt(tripList[i]));

            busStops.add(busStop);

            if (i >= 1) {
                BusStop station1 = BusStopRealmHelper.getBusStop(Integer.parseInt(tripList[i - 1]));

                distance += getDistance(busStop.getLat(), busStop.getLng(),
                        station1.getLat(), station1.getLng());
            }
        }

        mapView.setMarkers(busStops);

        if (distance < 1000) {
            mDistanceText.setText(Math.round(distance) + " m");
        } else {
            mDistanceText.setText(String.valueOf(round(distance / 1000)).replace(".", ",") + " km");
        }

        return distance;
    }

    private void parseVehicleData() {
        Vehicle vehicle = Vehicles.getBus(this, mTrip.getVehicle());

        if (vehicle != null) {
            loadBackdrop(vehicle.getGroup());

            mVehicleBrand.setText(vehicle.getVendor());
            mVehicleFuel.setText(vehicle.getFuel());

            mVehicleInfo.setVisibility(View.VISIBLE);
            mVehicleError.setVisibility(View.GONE);
            mVehicleLoading.setVisibility(View.GONE);

            TextView co2Emissions = (TextView) findViewById(R.id.trip_detail_co2_emission);
            TextView co2EmissionsCar = (TextView) findViewById(R.id.trip_detail_co2_emission_car);
            TextView fuelPrice = (TextView) findViewById(R.id.trip_detail_fuel_price);

            float co2 = mEmissions[vehicle.getGroup()] * mDistance / 1000;
            co2Emissions.setText(Math.round(co2) + " g");

            float co2Car = 120 * mDistance / 1000;
            co2EmissionsCar.setText(Math.round(co2Car) + " g");

            float fuelConsumption = 0.119F;
            float fuelPriceValue = fuelConsumption * mDistance / 1000F * mTrip.getFuelPrice();
            fuelPrice.setText(String.format(Locale.ITALY, "%.2f €", fuelPriceValue));
        } else {
            mVehicleError.setVisibility(View.VISIBLE);
            mVehicleInfo.setVisibility(View.GONE);
            mVehicleLoading.setVisibility(View.GONE);
        }
    }

    /**
     * Loads the background image into the {@link CollapsingToolbarLayout} and sets the content
     * scrim and the status bar scrim color. Also colors the card icons.
     *
     * @param id the vehicle class id.
     */
    private void loadBackdrop(int id) {
        ImageView imageView = (ImageView) findViewById(R.id.trip_detail_vehicle_image);

        if (imageView != null) {
            Glide.with(this).load(Uri.parse("file:///android_asset/images/bus_" + id + ".jpg"))
                    .animate(R.anim.fade_in_short)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
        }
    }

    private float getDistance(double lat1, double lng1, double lat2, double lng2) {
        Location l1 = new Location("start");
        l1.setLatitude(lat1);
        l1.setLongitude(lng1);

        Location l2 = new Location("stop");
        l2.setLatitude(lat2);
        l2.setLongitude(lng2);

        return l1.distanceTo(l2);
    }

    private double round(float number) {
        return Math.round(number * 100) / 100.0;
    }
}