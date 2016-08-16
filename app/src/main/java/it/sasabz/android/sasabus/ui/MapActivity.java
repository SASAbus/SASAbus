package it.sasabz.android.sasabus.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.fcm.FcmService;
import it.sasabz.android.sasabus.model.Buses;
import it.sasabz.android.sasabus.model.Vehicle;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.RealtimeApi;
import it.sasabz.android.sasabus.network.rest.api.TrafficLightApi;
import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;
import it.sasabz.android.sasabus.network.rest.response.TrafficLightResponse;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.UserRealmHelper;
import it.sasabz.android.sasabus.ui.widget.OffsetNestedSwipeRefreshLayout;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.AnimUtils;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Preconditions;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.map.MapDownloadHelper;
import it.sasabz.android.sasabus.util.map.RealtimeMapView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Activity which will displayed first when the user launches the app. It shows a map with all
 * the positions of the buses in form of a marker.
 * <p>
 * By clicking the marker the user can see more useful information about this bus which will
 * be displayed in form of a {@link BottomSheetBehavior bottom sheet}. An image of this vehicle
 * pops up behind the sheet as the user drags it towards the top.
 * <p>
 * This activity also handles notifications from the api which will be displayed in form of a
 * {@link Snackbar} if they are send via the realtime api or a {@link AlertDialog} when send
 * through gcm. The most common notifications are problems about the realtime api or obsolete
 * app versions.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class MapActivity extends BaseActivity implements View.OnClickListener,
        Observer<RealtimeResponse> {

    private static final String TAG = "MapActivity";

    public static final String EXTRA_DIALOG_TITLE = "it.sasabz.android.sasabus.EXTRA_DIALOG_TITLE";

    public static final String EXTRA_DIALOG_MESSAGE = "it.sasabz.android.sasabus.EXTRA_DIALOG_MESSAGE";

    public static final String EXTRA_DIALOG_YES = "it.sasabz.android.sasabus.EXTRA_DIALOG_YES";

    public static final String EXTRA_DIALOG_NO = "it.sasabz.android.sasabus.EXTRA_DIALOG_NO";

    public static final String EXTRA_DIALOG_URL = "it.sasabz.android.sasabus.EXTRA_DIALOG_URL";

    /**
     * Various maps and lists to hold markers, bus data and filter.
     */
    private final ArrayList<RealtimeBus> mBusData = new ArrayList<>();
    private ArrayList<Integer> mFilter = new ArrayList<>();

    /**
     * Special type of swipe refresh layout which does not refresh when scrolled, as scrolling the
     * map will trigger the refresh.
     */
    @BindView(R.id.refresh)
    OffsetNestedSwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Various views for the filter.
     */
    private FloatingActionButton mFabFilterTop;
    private FloatingActionButton mFabFilterBottom;
    private FloatingActionButton mFabFilterBg;
    private ScrollView mFilterScrollView;
    private RelativeLayout mFilterBackground;

    /**
     * Snackbars to show updates like missing internet connection or general error
     */
    private Snackbar mInfoSnackbar;
    private Snackbar mStationSnackbar;
    private Snackbar mErrorSnackbar;
    private Snackbar mInternetSnackbar;

    /**
     * Indicates if a data refresh is currently running.
     */
    private boolean mIsRefreshing;

    /**
     * Indicates which button the user clicked on the rating card.
     */
    private boolean mPositiveClick;
    private boolean mNegativeClick;

    /**
     * Indicates if the info snackbar can be shown. Will change to false once it has been showed
     * so the user doesn't get annoyed by the continuous snackbar changes.
     */
    private boolean mShowInfoSnackBar = true;

    /**
     * Indicates if auto refresh has been enabled in the settings.
     */
    private boolean mAutoRefresh;

    /**
     * Holds the bus id when a user clicked on the notification.
     */
    private int mBusBeaconId;

    /**
     * The interval to use when auto refreshing the map data.
     */
    private int mRefreshInterval;

    /**
     * Handler used to post delayed actions when auto refresh is enabled.
     */
    private final Handler HANDLER = new Handler();

    /**
     * The runnable which refreshes the data.
     */
    private final Runnable REFRESH_RUNNABLE = this::parseData;

    /**
     * Determines if the announcement dialog has been shown.
     */
    private boolean mShowedAnnouncementDialog;

    /**
     * Indicates if the filter is currently open so different actions can be started on back press,
     * like close the filter when it is open or exit/navigate up when it is closed.
     */
    private boolean mFilterOpen;

    private RealtimeMapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        getSupportActionBar().setTitle(R.string.map);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(Config.EXTRA_VEHICLE)) {
            mBusBeaconId = intent.getIntExtra(Config.EXTRA_VEHICLE, 0);
        }

        AnalyticsHelper.sendScreenView(TAG);

        if (!BuildConfig.DEBUG) {
            setupRating();
        }

        setupFilter();

        mAutoRefresh = SettingsUtils.isMapAutoEnabled(this);
        mRefreshInterval = SettingsUtils.getMapAutoInterval(this);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_amber, R.color.primary_red,
                R.color.primary_green, R.color.primary_indigo);

        if (savedInstanceState != null) {

            //noinspection ResourceType
            mFilterBackground.setVisibility(savedInstanceState.getInt("FILTER_BACKGROUND"));
            //noinspection ResourceType
            mFilterScrollView.setVisibility(savedInstanceState.getInt("FILTER_SCROLL"));
            //noinspection ResourceType
            mFabFilterTop.setVisibility(savedInstanceState.getInt("FAB_TOP"));

            mBusBeaconId = savedInstanceState.getInt("MARKER");

            ArrayList<RealtimeBus> temp = savedInstanceState.getParcelableArrayList("BUS_DATA");

            if (temp != null) {
                mBusData.addAll(temp);
                temp.clear();
            }
        }

        new MapDownloadHelper(this).checkMapFirstTime();

        WebView webView = (WebView) findViewById(R.id.webview);

        mapView = new RealtimeMapView(this, webView);

        parseData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        HANDLER.removeCallbacks(REFRESH_RUNNABLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        HANDLER.removeCallbacks(REFRESH_RUNNABLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        showAnnouncementDialogIfNeeded(getIntent());

        if (SettingsUtils.isMapAutoEnabled(this)) {
            parseData();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!SettingsUtils.isMapAutoEnabled(this)) {
                    parseData();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_map, menu);

        if (mAutoRefresh) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            FrameLayout view = (FrameLayout) inflater.inflate(R.layout.include_map_refresh, null);

            ((ImageView) view.findViewById(R.id.map_refresh_icon))
                    .setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_autorenew_white_24dp));

            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            rotation.setRepeatCount(Animation.INFINITE);
            view.startAnimation(rotation);

            menu.findItem(R.id.action_refresh).setActionView(view);
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.map_filter_fab_2:
            case R.id.filter_background:
                closeFilter();
                break;
            case R.id.map_filter_fab_1:
                openFilter();
                break;
            default:
                filterItemClick(v);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(Config.EXTRA_VEHICLE)) {
            mBusBeaconId = intent.getIntExtra(Config.EXTRA_VEHICLE, 0);

            parseData();
            showBusInfo(mBusBeaconId);
        }

        LogUtils.i(TAG, "onNewIntent, extras " + intent.getExtras());

        if (intent.hasExtra(EXTRA_DIALOG_MESSAGE)) {
            mShowedAnnouncementDialog = false;
            showAnnouncementDialogIfNeeded(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mFilterBackground != null) {
            outState.putInt("FILTER_BACKGROUND", mFilterBackground.getVisibility());
        }

        if (mFilterScrollView != null) {
            outState.putInt("FILTER_SCROLL", mFilterScrollView.getVisibility());
        }

        if (mFabFilterTop != null) {
            outState.putInt("FAB_TOP", mFabFilterTop.getVisibility());
        }

        outState.putParcelableArrayList("BUS_DATA", mBusData);

        UserRealmHelper.setFilter(mFilter);
        hideSnackbar();
    }

    @Override
    public int getNavItem() {
        return NAVDRAWER_ITEM_MAP;
    }


    @Override
    public void onNext(RealtimeResponse realtimeResponse) {
        showStatusSnackbar(realtimeResponse);

        mBusData.clear();
        mBusData.addAll(realtimeResponse.buses);

        hideSnackbar();

        if (mBusBeaconId != 0) {
            showBusInfo(mBusBeaconId);
        }

        if (mAutoRefresh) {
            HANDLER.postDelayed(REFRESH_RUNNABLE, mRefreshInterval);
        }

        mIsRefreshing = false;

        mapView.setMarkers(realtimeResponse, mFilter);

        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        Utils.handleException(e);

        mIsRefreshing = false;
        showErrorSnackbar(R.string.error_general);

        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));

        if (mAutoRefresh) {
            HANDLER.postDelayed(REFRESH_RUNNABLE, mRefreshInterval);
        }
    }


    /**
     * This method makes the network request to the api using
     * OkHttp's async networking.
     */
    private void parseData() {
        if (mIsRefreshing) return;

        if (!NetUtils.isOnline(this)) {
            showInternetSnackbar();

            if (mAutoRefresh) {
                HANDLER.postDelayed(REFRESH_RUNNABLE, mRefreshInterval);
            }
        } else {
            mIsRefreshing = true;
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));

            RealtimeApi realtimeApi = RestClient.ADAPTER.create(RealtimeApi.class);
            realtimeApi.get(locale())
                    .compose(bindToLifecycle())
                    .subscribeOn(Schedulers.newThread())
                    .map(realtimeResponse -> {
                        for (RealtimeBus bus : realtimeResponse.buses) {
                            String bus1 = null;

                            if (Buses.getBus(bus.vehicle) != null) {
                                bus1 = Buses.getBus(bus.vehicle).getVehicle().getCode();
                            }

                            String currentStopName = BusStopRealmHelper.getName(bus.busStop);
                            String lastStopName = BusStopRealmHelper.getName(bus.destination);

                            bus.group = bus1;
                            bus.currentStopName = currentStopName;
                            bus.lastStopName = lastStopName;
                        }

                        return realtimeResponse;
                    })
                    .delay(1, TimeUnit.SECONDS) // Delay is needed to make sure map is loaded.
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this);

            TrafficLightApi trafficLightApi = RestClient.ADAPTER.create(TrafficLightApi.class);
            trafficLightApi.trafficLight(locale(), SettingsUtils.getTrafficLightCity(MapActivity.this))
                    .compose(bindToLifecycle())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<TrafficLightResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.handleException(e);
                        }

                        @Override
                        public void onNext(TrafficLightResponse trafficLightResponse) {
                            mFabFilterTop.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + trafficLightResponse.color)));
                            mFabFilterBottom.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + trafficLightResponse.color)));
                            mFabFilterBg.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + trafficLightResponse.color)));
                        }
                    });
        }
    }

    /**
     * Updates the marker visibility according to the filter settings
     */
    private void updateFilterMarkers() {
        mapView.filter(mFilter);
    }

    /**
     * Opens the marker info window of a certain bus.
     *
     * @param vehicle the vehicle id to open.
     */
    private void showBusInfo(int vehicle) {
        if (vehicle == 0) {
            LogUtils.e(TAG, "vehicle == null");
            return;
        }

        LogUtils.e(TAG, "Vehicle " + vehicle + " not on map");

        mBusBeaconId = 0;
        showErrorSnackbar(R.string.snackbar_bus_not_driving);
    }

    /**
     * Shows a error snackbar when something happened. Also used to display the "BusMarker not driving"
     * snackbar.
     *
     * @param message the message string resource to display as text.
     */
    private void showErrorSnackbar(@StringRes int message) {
        if (mErrorSnackbar != null && mErrorSnackbar.isShown()) return;

        mErrorSnackbar = Snackbar.make(getMainContent(), message, Snackbar.LENGTH_INDEFINITE);

        View snackbarView = mErrorSnackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));

        runOnUiThread(() -> {
            if (mInternetSnackbar != null) {
                mInternetSnackbar.dismiss();
            }
            mErrorSnackbar.show();
        });
    }

    /**
     * Shows a snackbar when no internet access is available and the network data fetch
     * failed.
     */
    private void showInternetSnackbar() {
        if (mInternetSnackbar != null && mInternetSnackbar.isShown()) return;

        mInternetSnackbar = Snackbar.make(getMainContent(), R.string.error_wifi, Snackbar.LENGTH_INDEFINITE);

        View snackbarView = mInternetSnackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));

        runOnUiThread(() -> {
            if (mErrorSnackbar != null) {
                mErrorSnackbar.dismiss();
            }
            mInternetSnackbar.show();
        });
    }

    /**
     * Shows a status snackbar used to display information like available app updates or server
     * problems. If {@link RealtimeResponse#status} is {@code link}and {@link RealtimeResponse#link}
     * is not empty the app displays a snackbar with a clickable link which opens in the preferred
     * browser.
     *
     * @param response the {@link RealtimeResponse} which contains the status message and
     *                 snackbar duration as well as an optional link.
     */
    private void showStatusSnackbar(RealtimeResponse response) {
        Preconditions.checkNotNull(response, "response == null");

        if (TextUtils.isEmpty(response.status)) return;
        LogUtils.e(TAG, "Got status message: " + response.message);

        if (!mShowInfoSnackBar) return;


        //noinspection ResourceType
        mInfoSnackbar = Snackbar.make(getMainContent(), response.message,
                response.duration);

        mInfoSnackbar.setActionTextColor(ContextCompat.getColor(this, R.color.snackbar_action_text));

        switch (response.status) {
            case "update":
                mInfoSnackbar.setAction(R.string.map_snackbar_update, v -> {
                    try {
                        mInfoSnackbar.dismiss();
                        mFabFilterBg.setTranslationY(0);
                        mFabFilterBottom.setTranslationY(0);
                        mFabFilterTop.setTranslationY(0);

                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=it.sasabz.android.sasabus")));
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=it.sasabz.android.sasabus")));
                    }
                });
                break;
            case "link":
                if (TextUtils.isEmpty(response.link)) {
                    LogUtils.e(TAG, "Got link status but link is null or empty");
                    return;
                }

                mInfoSnackbar.setAction(R.string.map_snackbar_view, v -> {
                    try {
                        mInfoSnackbar.dismiss();
                        mFabFilterBg.setTranslationY(0);
                        mFabFilterBottom.setTranslationY(0);
                        mFabFilterTop.setTranslationY(0);

                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(response.link)));
                    } catch (ActivityNotFoundException e) {
                        Utils.handleException(e);
                    }
                });
                break;
        }

        View snackbarView = mInfoSnackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));

        new Handler().postDelayed(() -> mInfoSnackbar.show(), 500);

        mShowInfoSnackBar = false;
    }

    /**
     * Call this method to hide all {@link Snackbar}s currently being displayed.
     */
    private void hideSnackbar() {
        if (mInfoSnackbar != null) {
            mInfoSnackbar.dismiss();
        }

        if (mErrorSnackbar != null) {
            mErrorSnackbar.dismiss();
        }

        if (mInternetSnackbar != null) {
            mInternetSnackbar.dismiss();
        }

        if (mStationSnackbar != null) {
            mStationSnackbar.dismiss();
        }
    }


    /**
     * Sets up the filter. As we don't know how many lines to add to the filter in advance,
     * we cannot generate this layout by using XML, but we have to create all views programmatically
     * depending on the amount of lines.
     */
    private void setupFilter() {
        mFilter.addAll(UserRealmHelper.getFilter());

        mFabFilterTop = (FloatingActionButton) findViewById(R.id.map_filter_fab_2);
        mFabFilterBottom = (FloatingActionButton) findViewById(R.id.map_filter_fab_1);
        mFabFilterBg = (FloatingActionButton) findViewById(R.id.map_filter_fab_bg);

        mFilterBackground = (RelativeLayout) findViewById(R.id.filter_background);
        mFilterScrollView = (ScrollView) findViewById(R.id.filter_scroll);

        mFabFilterTop.setOnClickListener(this);
        mFabFilterBottom.setOnClickListener(this);
        mFilterBackground.setOnClickListener(this);

        ScrollView.LayoutParams
                layoutParams = new ScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.filter_margin_28);
        } else {
            layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.filter_margin_20);
        }

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < Lines.checkBoxesId.length; i++) {
            RelativeLayout rl = new RelativeLayout(this);
            rl.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            RelativeLayout.LayoutParams
                    radioParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            radioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            radioParams.addRule(RelativeLayout.CENTER_VERTICAL);

            CheckBox cb = new CheckBox(this);
            cb.setLayoutParams(radioParams);
            cb.setGravity(Gravity.CENTER);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cb.setButtonTintList(ColorStateList.valueOf(Color.parseColor('#' + Lines.lineColors[i])));
            }

            if (mFilter.contains(Lines.checkBoxesId[i])) {
                cb.setChecked(true);
            }

            cb.setId(Lines.checkBoxesId[i] * 100);
            cb.setOnClickListener(this);

            RelativeLayout.LayoutParams
                    textParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.LEFT_OF, Lines.checkBoxesId[i] * 100);
            textParams.addRule(RelativeLayout.CENTER_VERTICAL);
            textParams.rightMargin = (int) getResources().getDimension(R.dimen.dimen_10);

            TextView tv = new TextView(this);
            tv.setLayoutParams(textParams);
            tv.setText(Lines.getNames(this)[i]);
            tv.setTextColor(ContextCompat.getColor(this, R.color.marker_popup_text_primary));

            rl.addView(cb);
            rl.addView(tv);

            linearLayout.addView(rl);
        }

        mFilterScrollView.addView(linearLayout);

    }

    /**
     * Opens the filter. This method animates the fab by rotating
     * the two buttons and fading them. A third fab is used to prevent alpha errors.
     * Also fades in the white content scrim.
     */
    private void openFilter() {
        hideSnackbar();

        mFilterBackground.setVisibility(View.VISIBLE);
        mFilterScrollView.setVisibility(View.VISIBLE);

        Animator
                backgroundAnimator = ObjectAnimator.ofPropertyValuesHolder(mFilterBackground,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f)
        );

        Animator scrollAnimator = ObjectAnimator.ofPropertyValuesHolder(mFilterScrollView,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f)
        );

        AnimatorSet hideUI = new AnimatorSet();
        hideUI.setInterpolator(new AccelerateDecelerateInterpolator());
        hideUI.playTogether(backgroundAnimator, scrollAnimator);

        hideUI.start();

        mFabFilterTop.setVisibility(View.VISIBLE);

        Animator showTopFabAnimation = ObjectAnimator.ofPropertyValuesHolder(mFabFilterTop,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0.0f, 180.0f)
        );

        Animator
                hideBottomFabAnimation = ObjectAnimator.ofPropertyValuesHolder(mFabFilterBottom,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0.0f, 180.0f)
        );

        AnimatorSet revealAnimation = new AnimatorSet();
        revealAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnimation.playTogether(showTopFabAnimation, hideBottomFabAnimation);
        revealAnimation.start();

        mFilterOpen = true;
    }

    /**
     * Hides the filter. This method animates the fab by rotating
     * the two buttons and fading them. A third fab is used to prevent alpha errors.
     * Also fades out the white content scrim.
     */
    private void closeFilter() {
        if (!mFilterOpen) return;

        Animator
                backgroundAnimator = ObjectAnimator.ofPropertyValuesHolder(mFilterBackground,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f)
        );

        Animator scrollAnimator = ObjectAnimator.ofPropertyValuesHolder(mFilterScrollView,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f)
        );

        AnimatorSet hideUI = new AnimatorSet();
        hideUI.setInterpolator(new AccelerateDecelerateInterpolator());
        hideUI.playTogether(backgroundAnimator, scrollAnimator);
        hideUI.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mFilterBackground.setVisibility(View.GONE);
                mFilterScrollView.setVisibility(View.GONE);
            }
        });
        hideUI.start();

        Animator hideTopFabAnimation = ObjectAnimator.ofPropertyValuesHolder(mFabFilterTop,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 180.0f, 360.0f)
        );

        Animator
                revealBottomFabAnimation = ObjectAnimator.ofPropertyValuesHolder(mFabFilterBottom,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 180.0f, 360.0f)
        );

        AnimatorSet revealAnimation = new AnimatorSet();
        revealAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnimation.playTogether(hideTopFabAnimation, revealBottomFabAnimation);
        revealAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mFabFilterTop.setVisibility(View.GONE);
            }
        });
        revealAnimation.start();

        mFilterOpen = false;
    }

    /**
     * Executed when a filter checkbox has been clicked.
     *
     * @param v the {@link CheckBox} which has been clicked.
     */
    private void filterItemClick(View v) {
        if (v instanceof CheckBox) {
            for (int i = 0; i < Lines.checkBoxesId.length; i++) {
                if (Lines.checkBoxesId[i] * 100 == v.getId()) {
                    /*
                     * User clicked on the all checkbox, set the all checkbox to checked and disable
                     * if so users cannot uncheck it, add all the buses to the active filter
                     */
                    if (i == 0) {
                        if (((Checkable) v).isChecked()) {
                            v.setClickable(false);
                        }

                        findViewById(Lines.checkBoxesId[1] * 100).setClickable(true);

                        ((Checkable) findViewById(Lines.checkBoxesId[1] * 100)).setChecked(false);

                        mFilter.clear();
                        for (int j = 2; j < Lines.checkBoxesId.length; j++) {
                            mFilter.add(Lines.checkBoxesId[j]);
                            ((Checkable) findViewById(Lines.checkBoxesId[j] * 100)).setChecked(true);
                        }

                        mFilter.add(Lines.checkBoxesId[0]);
                    } else if (i == 1) {
                        /*
                         * User clicked on the none checkbox, set it to active and disable it, set
                         * the all checkbox to clickable again and remove all the buses from the
                         * active filter
                         */
                        if (((Checkable) v).isChecked()) {
                            v.setClickable(false);
                        }

                        findViewById(Lines.checkBoxesId[0] * 100).setClickable(true);

                        ((Checkable) findViewById(Lines.checkBoxesId[0] * 100)).setChecked(false);

                        mFilter.clear();
                        for (int j = 2; j < Lines.checkBoxesId.length; j++) {
                            ((Checkable) findViewById(Lines.checkBoxesId[j] * 100)).setChecked(false);
                        }

                        mFilter.add(Lines.checkBoxesId[1]);
                    } else {
                        ((Checkable) findViewById(Lines.checkBoxesId[1] * 100)).setChecked(false);

                        if (((Checkable) v).isChecked()) {
                            mFilter.add(Lines.checkBoxesId[i]);

                            findViewById(Lines.checkBoxesId[1] * 100).setClickable(true);
                            ((Checkable) findViewById(Lines.checkBoxesId[1] * 100)).setChecked(false);

                            for (int j = 0; j < mFilter.size(); j++) {
                                if (mFilter.get(j) == Lines.checkBoxesId[1]) {
                                    mFilter.remove(j);
                                    break;
                                }
                            }

                            boolean allChecked = true;
                            for (int j = 4; j < Lines.checkBoxesId.length; j++) {
                                if (!((Checkable) findViewById(Lines.checkBoxesId[j] * 100)).isChecked()) {
                                    allChecked = false;
                                    break;
                                }
                            }

                            if (allChecked) {
                                ((Checkable) findViewById(Lines.checkBoxesId[0] * 100)).setChecked(true);
                                mFilter.add(Lines.checkBoxesId[0]);
                            }
                        } else {
                            findViewById(Lines.checkBoxesId[0] * 100).setClickable(true);
                            ((Checkable) findViewById(Lines.checkBoxesId[0] * 100)).setChecked(false);

                            boolean noneChecked = true;
                            for (int j = 2; j < Lines.checkBoxesId.length; j++) {
                                if (((Checkable) findViewById(Lines.checkBoxesId[j] * 100)).isChecked()) {
                                    noneChecked = false;
                                    break;
                                }
                            }

                            if (noneChecked) {
                                ((Checkable) findViewById(Lines.checkBoxesId[1] * 100)).setChecked(true);
                                mFilter.add(Lines.checkBoxesId[1]);
                            }

                            for (int j = mFilter.size() - 1; j >= 0; j--) {
                                if (mFilter.get(j) == Lines.checkBoxesId[i]) {
                                    mFilter.remove(j);
                                    break;
                                }
                            }
                        }
                    }

                    break;
                }
            }

            updateFilterMarkers();
        }
    }

    /**
     * Sets up the rating {@link android.support.v7.widget.CardView}. If the user has started
     * the app for more than 20 times it displays the rating card.
     */
    private void setupRating() {
        SettingsUtils.incrementStartupCount(this);

        if (SettingsUtils.canAskForRating(this) && SettingsUtils.getStartupCount(this) >= 20) {
            FrameLayout rating = (FrameLayout) findViewById(R.id.rating_popup);
            TextView title = (TextView) findViewById(R.id.rating_title);
            Button positive = (Button) findViewById(R.id.rating_positive);
            Button negative = (Button) findViewById(R.id.rating_negative);

            ViewCompat.setAlpha(rating, 0);
            rating.setVisibility(View.VISIBLE);

            AnimUtils.fadeIn(rating, AnimUtils.DURATION_MEDIUM);

            AnalyticsHelper.sendEvent("Rating", "Showing popup");

            positive.setOnClickListener(v -> {
                if (mNegativeClick) {
                    AnimUtils.fadeOut(rating, AnimUtils.DURATION_MEDIUM);

                    SettingsUtils.setRatingDisabled(this);

                    startActivity(new Intent(this, AboutActivity.class).putExtra("dialog_report", true));

                    AnalyticsHelper.sendEvent("Rating", "Feedback click");
                } else if (mPositiveClick) {
                    try {
                        Uri uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                    }

                    AnimUtils.fadeOut(rating, AnimUtils.DURATION_MEDIUM);

                    SettingsUtils.setRatingDisabled(this);
                    SettingsUtils.markAsRated(this);

                    AnalyticsHelper.sendEvent("Rating", "Rate click");
                } else {
                    title.setText(R.string.rating_title2);
                    positive.setText(R.string.rating_positive1);
                    negative.setText(R.string.rating_negative2);

                    mPositiveClick = true;
                }
            });

            negative.setOnClickListener(v -> {
                if (mPositiveClick || mNegativeClick) {
                    SettingsUtils.setRatingDisabled(this);

                    AnimUtils.fadeOut(rating, AnimUtils.DURATION_MEDIUM);

                    if (mPositiveClick) {
                        AnalyticsHelper.sendEvent("Rating", "Don't ask again positive");
                    } else {
                        AnalyticsHelper.sendEvent("Rating", "Don't ask again negative");
                    }
                } else {
                    title.setText(R.string.rating_title3);
                    positive.setText(R.string.rating_positive1);
                    negative.setText(R.string.rating_negative2);
                    mNegativeClick = true;
                }
            });
        }
    }

    /**
     * Shows a dialog which was send through {@link FcmService GCM}.
     *
     * @param intent the intent with which this activity was started.
     */
    private void showAnnouncementDialogIfNeeded(Intent intent) {
        String title = intent.getStringExtra(EXTRA_DIALOG_TITLE);
        String message = intent.getStringExtra(EXTRA_DIALOG_MESSAGE);

        if (!mShowedAnnouncementDialog && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(message)) {
            LogUtils.i(TAG, "showAnnouncementDialogIfNeeded, title: " + title);
            LogUtils.i(TAG, "showAnnouncementDialogIfNeeded, message: " + message);

            String yes = intent.getStringExtra(EXTRA_DIALOG_YES);
            LogUtils.i(TAG, "showAnnouncementDialogIfNeeded, yes: " + yes);
            String no = intent.getStringExtra(EXTRA_DIALOG_NO);
            LogUtils.i(TAG, "showAnnouncementDialogIfNeeded, no: " + no);
            String url = intent.getStringExtra(EXTRA_DIALOG_URL);
            LogUtils.i(TAG, "showAnnouncementDialogIfNeeded, url: " + url);

            Spannable spannable = new SpannableString(message);
            Linkify.addLinks(spannable, Linkify.WEB_URLS);

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);

            if (!TextUtils.isEmpty(title)) {
                builder.setTitle(title);
            }

            builder.setMessage(spannable);

            if (!TextUtils.isEmpty(no)) {
                builder.setNegativeButton(no, (dialog, which) -> dialog.cancel());
            }

            if (!TextUtils.isEmpty(yes) && !TextUtils.isEmpty(url)) {
                builder.setPositiveButton(yes, (dialog, which) -> {
                    Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(urlIntent);
                });
            }

            AlertDialog dialog = builder.create();
            dialog.show();

            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                // makes the embedded links in the text clickable, if there are any
                messageView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            mShowedAnnouncementDialog = true;
        }
    }
}
