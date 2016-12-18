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

package it.sasabz.android.sasabus.ui.ecopoints.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.rest.model.Event;
import it.sasabz.android.sasabus.data.network.rest.model.EventPoint;
import it.sasabz.android.sasabus.ui.widget.ObservableButton;
import it.sasabz.android.sasabus.util.CustomTabsHelper;
import it.sasabz.android.sasabus.util.map.EventMapView;
import it.sasabz.android.sasabus.util.recycler.EventDetailsPointsAdapter;
import timber.log.Timber;

public class EventDetailsActivity extends RxAppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_EVENT = "it.sasabz.android.sasabus.EXTRA_EVENT";
    public static final String EXTRA_EVENT_ID = "it.sasabz.android.sasabus.EXTRA_EVENT_ID";
    public static final String EXTRA_BEACON_POINT = "it.sasabz.android.sasabus.EXTRA_BEACON_POINT";
    public static final String EXTRA_QR_CODE = "it.sasabz.android.sasabus.EXTRA_QR_CODE";

    public static final String BROADCAST_BEACON_SEEN = "it.sasabz.android.sasabus.BROADCAST_BEACON_SEEN";
    public static final String BROADCAST_EVENT_COMPLETED = "it.sasabz.android.sasabus.BROADCAST_EVENT_COMPLETED";

    @BindView(R.id.event_details_title) TextView title;
    @BindView(R.id.event_details_subtitle) TextView subtitle;

    @BindView(R.id.event_details_description) TextView details;

    @BindView(R.id.event_details_description_header) TextView descriptionHeader;
    @BindView(R.id.event_details_points_header) TextView pointsHeader;

    @BindView(R.id.event_details_header) LinearLayout mHeader;
    @BindView(R.id.scroll_view) NestedScrollView mScrollView;
    @BindView(R.id.recycler) RecyclerView pointsRecycler;

    @BindView(R.id.event_details_claim_prize_button) ObservableButton claimPrize;

    private EventDetailsPointsAdapter mAdapter;

    private Event event;

    private CustomTabsHelper mTabsHelper;

    private EventMapView mapView;

    private final BroadcastReceiver pointReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.e("Got event point broadcast");

            int id = intent.getIntExtra(EXTRA_BEACON_POINT, -1);

            if (id == -1) {
                Timber.e("Missing intent extra " + EXTRA_BEACON_POINT);
                return;
            }

            List<EventPoint> points = event.points;
            for (int i = 0, pointsSize = points.size(); i < pointsSize; i++) {
                EventPoint point = points.get(i);
                if (point.id == id) {
                    point.scanned = true;
                    mAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    };
    private final BroadcastReceiver eventCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.e("Got event completed broadcast");

            String code = intent.getStringExtra(EXTRA_QR_CODE);

            if (TextUtils.isEmpty(code)) {
                Timber.e("Missing intent extra " + EXTRA_QR_CODE);
                return;
            }

            event.qrCode = code;
            event.completed = true;

            claimPrize.setEnabled(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                claimPrize.setBackgroundTintList(ColorStateList
                        .valueOf(Color.parseColor('#' + event.colorAccent)));
            }
        }
    };
    private final BroadcastReceiver qrCodeRedeemedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.e("Got QR code redeemed broadcast");

            event.completed = true;
            event.redeemed = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_EVENT)) {
            Timber.e("Missing intent extra " + EXTRA_EVENT);
            finish();
            return;
        }

        event = intent.getParcelableExtra(EXTRA_EVENT);

        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor('#' + event.colorPrimary));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        toolbar.setNavigationOnClickListener(view -> finish());

        WebView webView = (WebView) findViewById(R.id.webview);
        mapView = new EventMapView(this, webView);

        setupViews();
        setSubtitle();

        mTabsHelper = new CustomTabsHelper(this);
        mTabsHelper.start(Color.parseColor('#' + event.colorPrimary));

        mAdapter = new EventDetailsPointsAdapter(this, event.points);

        pointsRecycler.setAdapter(mAdapter);
        pointsRecycler.setNestedScrollingEnabled(false);
        pointsRecycler.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(pointReceiver,
                new IntentFilter(BROADCAST_BEACON_SEEN));

        LocalBroadcastManager.getInstance(this).registerReceiver(eventCompletedReceiver,
                new IntentFilter(BROADCAST_EVENT_COMPLETED));

        LocalBroadcastManager.getInstance(this).registerReceiver(qrCodeRedeemedReceiver,
                new IntentFilter(QrCodeActivity.BROADCAST_QR_CODE_REDEEMED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_details, menu);

        MenuItem map = menu.findItem(R.id.menu_event_details_website);

        int color;
        if (event.lightStatusBar) {
            color = ContextCompat.getColor(this, R.color.subtitle_on_primary);
        } else {
            color = ContextCompat.getColor(this, R.color.white);
        }

        Drawable mapDrawable = map.getIcon();
        mapDrawable.mutate();
        mapDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_event_details_website:
                mTabsHelper.launchUrl(Uri.parse(event.website));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mTabsHelper.stop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(pointReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(eventCompletedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(qrCodeRedeemedReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.event_details_claim_prize_button:
                if (event.completed) {
                    Intent intent = new Intent(this, QrCodeActivity.class);
                    intent.putExtra(EXTRA_EVENT, event);
                    startActivity(intent);
                } else {
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_content);

                    Snackbar.make(linearLayout,
                            R.string.snackbar_event_incomplete, Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setupViews() {
        int upColor = ContextCompat.getColor(this, event.lightStatusBar ?
                R.color.subtitle_on_primary : R.color.text_primary_light);

        Drawable up = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp).mutate();
        up.setColorFilter(upColor, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(up);

        int titleColor = ContextCompat.getColor(this, event.lightStatusBar ?
                R.color.text_on_primary : R.color.text_primary_light);

        title.setTextColor(titleColor);

        int subtitleColor = ContextCompat.getColor(this, event.lightStatusBar ?
                R.color.subtitle_on_primary : R.color.text_secondary_light);

        subtitle.setTextColor(subtitleColor);

        mHeader.setBackgroundColor(Color.parseColor('#' + event.colorPrimary));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && event.lightStatusBar) {
            mHeader.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor('#' + event.colorPrimaryDark));
        }

        descriptionHeader.setTextColor(Color.parseColor('#' + event.colorAccent));
        pointsHeader.setTextColor(Color.parseColor('#' + event.colorAccent));

        claimPrize.setOnClickListener(this);
        claimPrize.setEnabled(event.completed);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (event.completed) {
                claimPrize.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor('#' + event.colorAccent)));
            } else {
                claimPrize.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_disabled)));
            }
        }

        pointsRecycler.clearFocus();
        pointsRecycler.setFocusable(false);
        pointsRecycler.setFocusableInTouchMode(false);

        title.setText(event.title);
        details.setText(event.details);

        new Handler().postDelayed(() -> {
            mapView.setMarkers(event);
        }, 1000);
    }

    private void setSubtitle() {
        Calendar begin = Calendar.getInstance();
        begin.setTimeInMillis(event.begin * 1000);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(event.end * 1000);

        SimpleDateFormat beginFormat = new SimpleDateFormat("EEE dd, H:mm", Locale.getDefault());

        String subtitleText;

        if (begin.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat endFormat = new SimpleDateFormat("H:mm", Locale.getDefault());

            String beginText = beginFormat.format(begin.getTime());
            String endText = endFormat.format(end.getTime());

            subtitleText = beginText + " - " + endText;
        } else {
            String beginText = beginFormat.format(begin.getTime());
            String endText = beginFormat.format(end.getTime());

            subtitleText = beginText + " - " + endText;
        }

        subtitle.setText(getString(R.string.event_details_subtitle, subtitleText, event.location));
    }
}
