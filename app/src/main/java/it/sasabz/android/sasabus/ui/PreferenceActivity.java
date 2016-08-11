package it.sasabz.android.sasabus.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import it.sasabz.android.sasabus.AppApplication;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.beacon.BeaconService;
import it.sasabz.android.sasabus.receiver.BluetoothReceiver;
import it.sasabz.android.sasabus.receiver.LocationReceiver;
import it.sasabz.android.sasabus.ui.widget.PreferenceFragment;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Activity which handles app preferences. For the sake of this application the preferences are
 * split into 5 fragments which each handle a functionality of the app, like beacons or
 * notifications.
 *
 * @author Alex Lardschneider
 */
public class PreferenceActivity extends AppCompatActivity {

    private static final String TAG = "PreferenceActivity";
    private static final String SCREEN_LABEL = "Preferences";

    private final String[] FRAGMENTS = {
            "PreferenceMenuFragment",
            "AppearanceFragment",
            "MapFragment",
            "BeaconsFragment",
            "NotificationsFragment",
            "AdvancedFragment"
    };

    private int position;

    private static boolean update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.changeLanguage(this);

        setContentView(R.layout.activity_preferences);

        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> navigateUpOrBack());

        Intent intent = getIntent();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.preferences_content, new PreferenceMenuFragment())
                .commit();

        if (intent.getStringExtra("FRAGMENT") != null) {
            setAppearanceFragment(false);
        } else {
            update = false;
        }

        if (savedInstanceState != null) {
            position = savedInstanceState.getInt("POSITION");

            Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENTS[position]);

            if (fragment == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.preferences_content, new PreferenceMenuFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.preferences_content, fragment, FRAGMENTS[position])
                        .setCustomAnimations(0, 0, 0, R.anim.anim_pop_exit)
                        .commit();
            }
        }

        intent.putExtra("FRAGMENT", (String) null);
    }

    @Override
    public void onBackPressed() {
        navigateUpOrBack();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("POSITION", position);
    }

    /**
     * Finishes this activity. If a configuration change happened, it will reload
     * the {@link MapActivity}. If a {@link PreferenceFragment} is in the backstack, it will first
     * pop that.
     */
    private void navigateUpOrBack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            position = 0;
        } else {
            if (update) {
                Intent intent = new Intent(this, MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                finish();
            }
        }
    }

    /**
     * Sets the currently displayed fragment to {@link AppearanceFragment}.
     *
     * @param anim a boolean value determining if the animation should be played when the fragment
     *             enters. This is useful because we don't want to animate the fragment on a
     *             configuration change as the user shouldn't notice it.
     */
    private void setAppearanceFragment(boolean anim) {
        position = 1;

        Fragment fragment = new AppearanceFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (anim) {
            transaction.setCustomAnimations(R.anim.anim_enter, 0, 0, R.anim.anim_pop_exit);
        } else {
            transaction.setCustomAnimations(0, 0, 0, R.anim.anim_pop_exit);
        }

        transaction.add(R.id.preferences_content, fragment, FRAGMENTS[1])
                .addToBackStack(FRAGMENTS[1])
                .commit();
    }

    /**
     * Sets the currently displayed fragment to {@link MapFragment}.
     */
    private void setMapFragment() {
        position = 2;

        Fragment fragment = new MapFragment();

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_enter, 0, 0, R.anim.anim_pop_exit)
                .add(R.id.preferences_content, fragment, FRAGMENTS[2])
                .addToBackStack(FRAGMENTS[2])
                .commit();
    }

    /**
     * Sets the currently displayed fragment to {@link BeaconsFragment}.
     */
    private void setBeaconsFragment() {
        position = 3;

        Fragment fragment = new BeaconsFragment();

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_enter, 0, 0, R.anim.anim_pop_exit)
                .add(R.id.preferences_content, fragment, FRAGMENTS[3])
                .addToBackStack(FRAGMENTS[3])
                .commit();
    }

    /**
     * Sets the currently displayed fragment to {@link NotificationsFragment}.
     */
    private void setNotificationsFragment() {
        position = 4;

        Fragment fragment = new NotificationsFragment();

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_enter, 0, 0, R.anim.anim_pop_exit)
                .add(R.id.preferences_content, fragment, FRAGMENTS[4])
                .addToBackStack(FRAGMENTS[4])
                .commit();
    }

    /**
     * Sets the currently displayed fragment to {@link AdvancedFragment}.
     */
    private void setAdvancedFragment() {
        position = 5;

        Fragment fragment = new AdvancedFragment();

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_enter, 0, 0, R.anim.anim_pop_exit)
                .add(R.id.preferences_content, fragment, FRAGMENTS[5])
                .addToBackStack(FRAGMENTS[5])
                .commit();
    }


    /**
     * The fragment the user sees first as it enters the settings. It holds all the categories
     * like appearance, map, beacons ecc.
     */
    public static class PreferenceMenuFragment extends PreferenceFragment implements View.OnClickListener {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_preferences, container, false);

            RelativeLayout appearance = (RelativeLayout) view.findViewById(R.id.preferences_appearance);
            RelativeLayout map = (RelativeLayout) view.findViewById(R.id.preferences_map);
            RelativeLayout beacons = (RelativeLayout) view.findViewById(R.id.preferences_beacons);
            RelativeLayout notifications = (RelativeLayout) view.findViewById(R.id.preferences_notifications);
            RelativeLayout advanced = (RelativeLayout) view.findViewById(R.id.preferences_advanced);

            appearance.setOnClickListener(this);
            map.setOnClickListener(this);
            beacons.setOnClickListener(this);
            notifications.setOnClickListener(this);
            advanced.setOnClickListener(this);

            return view;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.preferences_appearance:
                    ((PreferenceActivity) getActivity()).setAppearanceFragment(true);
                    break;
                case R.id.preferences_map:
                    ((PreferenceActivity) getActivity()).setMapFragment();
                    break;
                case R.id.preferences_beacons:
                    ((PreferenceActivity) getActivity()).setBeaconsFragment();
                    break;
                case R.id.preferences_notifications:
                    ((PreferenceActivity) getActivity()).setNotificationsFragment();
                    break;
            }
        }
    }

    /**
     * This fragment holds all settings regarding app appearance like theme or language.
     */
    public static class AppearanceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_appearance);

            ListPreference languageSelect = (ListPreference) findPreference("pref_language");
            languageSelect.setOnPreferenceChangeListener((preference, o) -> {
                update = true;
                Utils.changeLanguage(getActivity());

                Intent intent = getActivity().getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.putExtra("FRAGMENT", "AppearanceFragment");
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);

                return true;
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);

            assert view != null;
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_background));

            return view;
        }
    }

    /**
     * This fragment holds all settings regarding the map like the map style (i.e. terrain, hybrid)
     * or the auto refresh settings.
     */
    public static class MapFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_map);

            SwitchPreference mapUpdate = (SwitchPreference) findPreference(SettingsUtils.PREF_AUTO_UPDATE);
            mapUpdate.setOnPreferenceChangeListener((preference, newValue) -> {
                update = true;
                return true;
            });

            ListPreference mapInterval = (ListPreference) findPreference(SettingsUtils.PREF_AUTO_UPDATE_INTERVAL);
            mapInterval.setOnPreferenceChangeListener((preference, newValue) -> {
                update = true;
                return true;
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);

            assert view != null;
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_background));

            return view;
        }
    }

    /**
     * This fragment holds all settings regarding the beacons, like if they should be enabled.
     */
    public static class BeaconsFragment extends PreferenceFragment {

        private static final int REQUEST_ENABLE_BT = 1010;
        private static final int PERMISSIONS_ACCESS_LOCATION = 123;

        private SwitchPreference beaconsEnable;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_beacons);

            beaconsEnable = (SwitchPreference) findPreference(SettingsUtils.PREF_BEACONS_ENABLED);
            beaconsEnable.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = (boolean) newValue;

                if (value) {
                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName(getActivity(), BeaconService.class),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);

                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName(getActivity(), LocationReceiver.class),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);

                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName(getActivity(), BluetoothReceiver.class),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);

                    LogUtils.e(TAG, "Enabled beacon components");
                } else {
                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName(getActivity(), BeaconService.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName(getActivity(), LocationReceiver.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

                    getActivity().getPackageManager().setComponentEnabledSetting(
                            new ComponentName(getActivity(), BluetoothReceiver.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

                    LogUtils.e(TAG, "Disabled beacon components");
                }

                return !value || setBeaconScanner();
            });

            if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ||
                    ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                beaconsEnable.setChecked(false);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);

            assert view != null;
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_background));

            return view;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_ENABLE_BT) {
                if (resultCode == Activity.RESULT_OK) {
                    ((AppApplication) getActivity().getApplication()).startBeacon();
                } else {
                    beaconsEnable.setChecked(false);
                }
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            switch (requestCode) {
                case PERMISSIONS_ACCESS_LOCATION:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        beaconsEnable.setChecked(setBeaconScanner());
                    } else {
                        beaconsEnable.setChecked(false);
                    }
                    break;
            }
        }

        boolean setBeaconScanner() {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null || !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(getActivity(), R.string.toast_bluetooth_not_available, Toast.LENGTH_SHORT).show();

                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_LOCATION);
                return true;
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            } else {
                ((AppApplication) getActivity().getApplication()).startBeacon();
            }

            return true;
        }
    }

    /**
     * This fragment holds all settings regarding notifications regarding the notification, like
     * if they should be displayed or vibrate.
     */
    public static class NotificationsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_notifications);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);

            assert view != null;
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_background));

            return view;
        }
    }

    /**
     * This fragment holds all "advanced" settings like if/when bus stop scrim images should be
     * downloaded or if bus stop ids should be displayed.
     */
    public static class AdvancedFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_advanced);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);

            assert view != null;
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.card_background));

            return view;
        }
    }
}
