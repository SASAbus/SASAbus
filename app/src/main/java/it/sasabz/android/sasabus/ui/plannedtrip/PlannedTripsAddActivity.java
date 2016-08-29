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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.realm.Realm;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.CircleLine;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.model.trip.PlannedTrip;
import it.sasabz.android.sasabus.model.trip.PlannedTripNotification;
import it.sasabz.android.sasabus.provider.API;
import it.sasabz.android.sasabus.provider.apis.Handler;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.ui.widget.NestedListView;
import it.sasabz.android.sasabus.ui.widget.RecyclerItemClickListener;
import it.sasabz.android.sasabus.util.AlarmUtils;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.HashUtils;
import it.sasabz.android.sasabus.util.Strings;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.list.PlannedTripsNotificationAdapter;
import it.sasabz.android.sasabus.util.recycler.CircleLinesAdapter;

/**
 * Allows to add a planned trip by specifying bus stop, line, notification interval, departure
 * time and date and repeat interval.
 *
 * @author Alex Lardschneider
 * @see PlannedTripsActivity to view all planned trips.
 * @see PlannedTripsViewActivity to view a single planned trip.
 */
public class PlannedTripsAddActivity extends AppCompatActivity implements View.OnClickListener,
        TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener,
        AdapterView.OnItemClickListener, RecyclerItemClickListener.OnItemClickListener {

    private static final String TAG = "PlannedTripsActivity";

    private static final String BUNDLE_TITLE = "BUNDLE_TITLE";
    private static final String BUNDLE_BUS_STOP = "BUNDLE_BUS_STOP";
    private static final String BUNDLE_CALENDAR_DATE = "BUNDLE_CALENDAR_DATE";
    private static final String BUNDLE_CALENDAR_TIME = "BUNDLE_CALENDAR_TIME";
    private static final String BUNDLE_LIST_LINES = "BUNDLE_LIST_LINES";
    private static final String BUNDLE_LIST_NOTIFICATIONS = "BUNDLE_LIST_NOTIFICATIONS";
    private static final String BUNDLE_LIST_REPEAT_DAYS = "BUNDLE_LIST_REPEAT_DAYS";
    private static final String BUNDLE_LIST_REPEAT_WEEKS = "BUNDLE_LIST_REPEAT_WEEKS";
    private static final String BUNDLE_LIST_REPEAT_TEXT = "BUNDLE_LIST_REPEAT_TEXT";
    private static final String BUNDLE_LIST_DIALOG_LINES = "BUNDLE_LIST_DIALOG_LINES";

    private static final int REPEAT_EVERY_DAY = -1;
    private static final int REPEAT_EVERY_WEEK = -2;

    private DatePickerDialog mDatePickerDialog;

    private Calendar mDateCalendar;
    private Calendar mTimeCalendar;

    private EditText mTitle;

    private TextView mDateText;
    private TextView mTimeText;
    private TextView mSelectLineText;
    private TextView mSelectBusStopText;
    private TextView mRepeatText;

    private long mMinDateMillis;

    private int mBusStopId;

    private PlannedTripsNotificationAdapter mAdapter;
    private ArrayList<PlannedTripNotification> mNotifications;

    private CircleLinesAdapter mDialogLineAdapter;
    private ArrayList<CircleLine> mDialogLineItems;

    private ArrayList<Integer> mRepeatDays;
    private ArrayList<Integer> mRepeatWeeks;
    private ArrayList<Integer> mLines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planned_trips_add);

        AnalyticsHelper.sendScreenView(TAG);

        mTitle = (EditText) findViewById(R.id.planned_trips_add_title);

        ImageButton exit = (ImageButton) findViewById(R.id.planned_trips_add_exit);
        if (exit != null) {
            exit.setOnClickListener(this);
        }

        TextView save = (TextView) findViewById(R.id.planned_trips_add_save);
        if (save != null) {
            save.setOnClickListener(this);
        }

        mDateText = (TextView) findViewById(R.id.planned_trips_add_date_picker);
        if (mDateText != null) {
            mDateText.setOnClickListener(this);
        }

        mTimeText = (TextView) findViewById(R.id.planned_trips_add_time_picker);
        if (mTimeText != null) {
            mTimeText.setOnClickListener(this);
        }

        mSelectLineText = (TextView) findViewById(R.id.planned_trips_add_select_line);
        if (mSelectLineText != null) {
            mSelectLineText.setOnClickListener(this);
        }

        mSelectBusStopText = (TextView) findViewById(R.id.planned_trips_add_select_stop);
        if (mSelectBusStopText != null) {
            mSelectBusStopText.setOnClickListener(this);
        }

        mRepeatText = (TextView) findViewById(R.id.planned_trips_add_repeat);
        if (mRepeatText != null) {
            mRepeatText.setOnClickListener(this);
        }

        if (savedInstanceState != null) {
            mTitle.setText(savedInstanceState.getString(BUNDLE_TITLE));

            mDateCalendar = (Calendar) savedInstanceState.getSerializable(BUNDLE_CALENDAR_DATE);
            mTimeCalendar = (Calendar) savedInstanceState.getSerializable(BUNDLE_CALENDAR_TIME);

            mLines = savedInstanceState.getIntegerArrayList(BUNDLE_LIST_LINES);

            StringBuilder sb = new StringBuilder();
            for (Integer i : mLines) {
                sb.append(Lines.lidToName(i)).append(", ");
            }

            if (!mLines.isEmpty()) {
                if (mLines.size() == 1) {
                    sb.insert(0, getString(R.string.line) + ' ');
                } else {
                    sb.insert(0, getString(R.string.lines) + ' ');
                }

                if (sb.length() > 1) {
                    sb.setLength(sb.length() - 2);

                }

                mSelectLineText.setText(sb.toString());
                mSelectLineText.setTextColor(ContextCompat.getColor(this, R.color.text_default));
            }

            mBusStopId = savedInstanceState.getInt(BUNDLE_BUS_STOP);

            mSelectBusStopText.setText(BusStopRealmHelper.getName(mBusStopId) +
                    " (" + BusStopRealmHelper.getMunic(mBusStopId) + ')');
            mSelectBusStopText.setTextColor(ContextCompat.getColor(this, R.color.text_default));

            mNotifications = savedInstanceState.getParcelableArrayList(BUNDLE_LIST_NOTIFICATIONS);

            mRepeatDays = savedInstanceState.getIntegerArrayList(BUNDLE_LIST_REPEAT_DAYS);
            mRepeatWeeks = savedInstanceState.getIntegerArrayList(BUNDLE_LIST_REPEAT_WEEKS);

            mRepeatText.setText(savedInstanceState.getString(BUNDLE_LIST_REPEAT_TEXT));

            mDialogLineItems = savedInstanceState.getParcelableArrayList(BUNDLE_LIST_DIALOG_LINES);
        } else {
            mDateCalendar = Calendar.getInstance();
            mTimeCalendar = Calendar.getInstance();

            mLines = new ArrayList<>();
            mNotifications = new ArrayList<>();

            mNotifications.add(new PlannedTripNotification(getString(
                    R.string.planned_trips_add_notification_30), 30, true, false));
            mNotifications.add(new PlannedTripNotification(getString(
                    R.string.planned_trips_add_notification), 0, false, true));

            mRepeatDays = new ArrayList<>();
            mRepeatWeeks = new ArrayList<>();

            mRepeatDays.add(REPEAT_EVERY_DAY);

            mDialogLineItems = new ArrayList<>();
            for (int id : Lines.lineIds) {
                mDialogLineItems.add(new CircleLine(id));
            }
        }

        mDialogLineAdapter = new CircleLinesAdapter(this, mDialogLineItems);
        mAdapter = new PlannedTripsNotificationAdapter(this, mNotifications);

        mMinDateMillis = mTimeCalendar.getTimeInMillis();

        NestedListView listView = (NestedListView) findViewById(R.id.planned_trips_add_notifications);
        if (listView != null) {
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(this);
        }

        Context context = this;

        // Fix for broken Samsung devices on 4.X.X which crash for some reason.
        // Can be fixed by using the Holo Theme for the calendar. Not fancy but it works.
        if (isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog);
        }

        mDatePickerDialog = new DatePickerDialog(context, R.style.DatePicker, this, mDateCalendar.get(Calendar.YEAR),
                mDateCalendar.get(Calendar.MONTH), mDateCalendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMinDate(mMinDateMillis);

        updateDate();
        updateTime();

        // Load the plan data in background thread if needed, so when the user
        // presses the bus stop picker it is responsive because it doesn't
        // have to load the data first, which takes a couple of seconds.
        new Thread(() -> {
            Handler.load(this);
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.planned_trips_add_exit:
                showExitDialog();
                break;
            case R.id.planned_trips_add_save:
                savePlannedTrip();
                break;
            case R.id.planned_trips_add_date_picker:
                mDatePickerDialog.getDatePicker().init(mDateCalendar.get(Calendar.YEAR),
                        mDateCalendar.get(Calendar.MONTH), mDateCalendar.get(Calendar.DAY_OF_MONTH), null);

                mDatePickerDialog.show();
                break;
            case R.id.planned_trips_add_time_picker:
                new TimePickerDialog(this, R.style.DatePicker, this, mTimeCalendar.get(Calendar.HOUR_OF_DAY),
                        mTimeCalendar.get(Calendar.MINUTE), true).show();
                break;
            case R.id.planned_trips_add_select_line:
                showLinePicker();
                break;
            case R.id.planned_trips_add_select_stop:
                showBusStopPicker(view);
                break;
            case R.id.planned_trips_add_repeat:
                showRepeatPicker();
                break;
            default:
                throw new IllegalStateException("Invalid view " + view);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(BUNDLE_TITLE, mTitle.getText().toString());
        outState.putSerializable(BUNDLE_CALENDAR_DATE, mDateCalendar);
        outState.putSerializable(BUNDLE_CALENDAR_TIME, mTimeCalendar);

        outState.putIntegerArrayList(BUNDLE_LIST_LINES, mLines);

        outState.putParcelableArrayList(BUNDLE_LIST_DIALOG_LINES, mDialogLineItems);

        outState.putInt(BUNDLE_BUS_STOP, mBusStopId);

        outState.putParcelableArrayList(BUNDLE_LIST_NOTIFICATIONS, mNotifications);

        outState.putIntegerArrayList(BUNDLE_LIST_REPEAT_DAYS, mRepeatDays);
        outState.putIntegerArrayList(BUNDLE_LIST_REPEAT_WEEKS, mRepeatWeeks);

        outState.putString(BUNDLE_LIST_REPEAT_TEXT, mRepeatText.getText().toString());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mTimeCalendar.set(Calendar.MINUTE, minute);

        updateTime();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        Date minDate = new Date(mMinDateMillis);

        if (calendar.getTime().before(minDate)) {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(minDate);

            mDateCalendar.set(Calendar.YEAR, calendar1.get(Calendar.YEAR));
            mDateCalendar.set(Calendar.MONTH, calendar1.get(Calendar.MONTH));
            mDateCalendar.set(Calendar.DAY_OF_MONTH, calendar1.get(Calendar.DAY_OF_MONTH));
        } else {
            mDateCalendar.set(Calendar.YEAR, year);
            mDateCalendar.set(Calendar.MONTH, monthOfYear);
            mDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }

        updateDate();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showNotificationPicker(position);
    }

    @Override
    public void onItemClick(View childView, int position) {
        mDialogLineItems.get(position).setSelected(!mDialogLineItems.get(position).isSelected());
        mDialogLineAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemLongPress(View childView, int position) {
        // Long press is not needed.
    }


    private void updateDate() {
        SimpleDateFormat sdf;

        switch (Utils.locale(this)) {
            case "it":
                sdf = new SimpleDateFormat("EEE dd MMM yyyy", Locale.ITALY);
                break;
            case "de":
                sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.GERMAN);
                break;
            default:
                sdf = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
                break;
        }

        mDateText.setText(sdf.format(mDateCalendar.getTime()));
    }

    private void updateTime() {
        mTimeText.setText(new SimpleDateFormat("HH:mm", Locale.US).format(mTimeCalendar.getTime()));
    }

    private void showLinePicker() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_planned_trips_lines, null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        recyclerView.setAdapter(mDialogLineAdapter);

        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(getString(R.string.planned_trips_add_dialog_line))
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();

                    mLines.clear();
                    int count = 0;
                    for (CircleLine item : mDialogLineItems) {
                        if (item.isSelected()) {
                            mLines.add(item.getId());

                            sb.append(Lines.lidToName(item.getId())).append(", ");
                            count++;
                        }
                    }

                    if (count == 0) {
                        mSelectLineText.setText(R.string.planned_trips_add_line);
                        mSelectLineText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

                        dialog.dismiss();
                        return;
                    } else if (count == 1) {
                        sb.insert(0, getString(R.string.line) + ' ');
                    } else {
                        sb.insert(0, getString(R.string.lines) + ' ');
                    }

                    if (sb.length() > 1) {
                        sb.setLength(sb.length() - 2);
                    }

                    mSelectLineText.setText(sb.toString());
                    mSelectLineText.setTextColor(ContextCompat.getColor(this, R.color.text_default));

                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    for (CircleLine item : mDialogLineItems) {
                        item.setSelected(false);
                    }

                    dialog.dismiss();
                })
                .create()
                .show();

    }

    private void showBusStopPicker(View view) {
        if (!mLines.isEmpty()) {
            Collection<Integer> stops = new ArrayList<>();
            for (int i = 0; i < mDialogLineItems.size(); i++) {
                if (mDialogLineItems.get(i).isSelected()) {
                    stops.add(mDialogLineItems.get(i).getId());
                }
            }

            List<SimpleBusStop> busStops = new ArrayList<>();
            for (Integer busStop : API.getBusStopsOfLines(this, stops)) {
                busStops.add(new SimpleBusStop(busStop, BusStopRealmHelper.getName(busStop) +
                        " (" + BusStopRealmHelper.getMunic(busStop) + ')'));
            }

            Collections.sort(busStops, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));

            CharSequence[] busStopNames = new CharSequence[busStops.size()];
            for (int i = 0; i < busStops.size(); i++) {
                busStopNames[i] = busStops.get(i).getName();
            }

            new AlertDialog.Builder(this, R.style.DialogStyle)
                    .setTitle(getString(R.string.planned_trips_add_dialog_add_bus_stop))
                    .setItems(busStopNames, (dialog, which) -> {
                        mBusStopId = busStops.get(which).getId();

                        mSelectBusStopText.setText(BusStopRealmHelper.getName(mBusStopId) +
                                " (" + BusStopRealmHelper.getMunic(mBusStopId) + ')');
                        mSelectBusStopText.setTextColor(ContextCompat.getColor(this, R.color.text_default));
                    })
                    .create()
                    .show();
        } else {
            Snackbar.make(view, R.string.planned_trips_add_no_lines, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showNotificationPicker(int position) {
        String[] times = {
                getString(R.string.planned_trips_add_notification_none),
                getString(R.string.planned_trips_add_notification_10),
                getString(R.string.planned_trips_add_notification_30),
                getString(R.string.planned_trips_add_notification_60),
        };

        Dialog d = new AlertDialog.Builder(this, R.style.DialogStyle)
                .setItems(times, (dialog, which) -> {
                    int minutes = 0;
                    switch (which) {
                        case 1:
                            minutes = 10;
                            break;
                        case 2:
                            minutes = 30;
                            break;
                        case 3:
                            minutes = 60;
                            break;
                    }

                    if (which == 0) {
                        if (position < mNotifications.size() - 1) {
                            mNotifications.remove(position);
                            mNotifications.get(position).setLight(false);
                        }
                    } else {
                        if (position > 0 && position < mNotifications.size() - 1) {
                            mNotifications.get(position).setImage(false);
                            mNotifications.get(position).setLight(false);
                        }

                        mNotifications.get(position).setMinutes(minutes);

                        if (mNotifications.get(position).getText().equals(times[0]) ||
                                mNotifications.get(position).getText().equals(getString(R.string.planned_trips_add_notification))) {
                            mNotifications.add(position, new PlannedTripNotification(times[which], minutes, false, false));
                        } else {
                            mNotifications.get(position).setText(times[which]);
                        }
                    }

                    for (int i = 0; i < mNotifications.size(); i++) {
                        if (i == 0) {
                            mNotifications.get(0).setImage(true);
                        } else {
                            mNotifications.get(i).setImage(false);
                        }
                    }

                    if (!mNotifications.isEmpty()) {
                        mNotifications.get(mNotifications.size() - 1).setLight(true);
                        mNotifications.get(mNotifications.size() - 1).setText(getString(R.string.planned_trips_add_notification));
                    }

                    mAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
                .create();

        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.show();
    }

    private void showRepeatPicker() {
        String[] times = {
                getString(R.string.planned_trips_add_repeat_never),
                getString(R.string.planned_trips_add_repeat_day_short),
                getString(R.string.planned_trips_add_repeat_week),
        };

        Dialog d = new AlertDialog.Builder(this, R.style.DialogStyle)
                .setItems(times, (dialog, which) -> {
                    mRepeatText.setText(times[which]);

                    mRepeatDays.clear();
                    mRepeatWeeks.clear();

                    if (which == 1) {
                        mRepeatDays.add(REPEAT_EVERY_DAY);
                    } else if (which == 2) {
                        mRepeatWeeks.add(REPEAT_EVERY_WEEK);
                    }

                    dialog.dismiss();
                })
                .create();

        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.show();
    }

    private void showExitDialog() {
        View focus = getCurrentFocus();
        if (focus != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }

        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setMessage(getString(R.string.planned_trips_add_confirm_discard))
                .setPositiveButton(getString(R.string.planned_trips_add_keep_editing), (dialog, which) -> dialog.dismiss())
                .setNegativeButton(getString(R.string.planned_trips_add_discard), (dialog, which) -> finish())
                .create()
                .show();
    }

    private void savePlannedTrip() {
        if (mTitle.getText().length() == 0 || mLines.isEmpty() || mBusStopId == 0) {
            View focus = getCurrentFocus();
            if (focus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }

            new AlertDialog.Builder(this, R.style.DialogStyle)
                    .setMessage(getString(R.string.planned_trips_add_empty_fields))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();

            return;
        }
        PlannedTrip trip = new PlannedTrip();

        Calendar time = Calendar.getInstance();

        /*
         * Sets the unique trip hash consisting of the android device id and
         * the start date of this trip. The hash consists of the 8 first chars of the md5
         * hash.
         */
        trip.setHash(HashUtils.getRandomString(16));

        time.set(Calendar.DAY_OF_MONTH, mDateCalendar.get(Calendar.DAY_OF_MONTH));
        time.set(Calendar.MONTH, mDateCalendar.get(Calendar.MONTH));
        time.set(Calendar.YEAR, mDateCalendar.get(Calendar.YEAR));

        time.set(Calendar.HOUR_OF_DAY, mTimeCalendar.get(Calendar.HOUR_OF_DAY));
        time.set(Calendar.MINUTE, mTimeCalendar.get(Calendar.MINUTE));

        Set<Integer> notifications = new LinkedHashSet<>();

        for (PlannedTripNotification item : mNotifications) {
            notifications.add(item.getMinutes());
        }
        notifications.remove(0);

        List<Integer> list = new ArrayList<>(notifications);
        Collections.sort(list);

        trip.setTimestamp(time.getTimeInMillis() / 1000);
        trip.setBusStop(mBusStopId);
        trip.setLines(mLines);
        trip.setTitle(mTitle.getText().toString());

        if (!mRepeatDays.isEmpty() && mRepeatDays.get(0) == REPEAT_EVERY_DAY) {
            trip.setRepeatDays(PlannedTrip.FLAG_MONDAY);
        }

        if (!mRepeatWeeks.isEmpty() && mRepeatWeeks.get(0) == REPEAT_EVERY_WEEK) {
            trip.setRepeatWeeks(Integer.MAX_VALUE);
        }

        trip.setNotifications(list);

        // Insert planned trip into db
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        it.sasabz.android.sasabus.realm.user.PlannedTrip plannedTrip =
                realm.createObject(it.sasabz.android.sasabus.realm.user.PlannedTrip.class);

        plannedTrip.setHash(trip.getHash());
        plannedTrip.setTitle(trip.getTitle());
        plannedTrip.setBusStop(trip.getBusStop());
        plannedTrip.setTimeStamp(trip.getTimestamp());
        plannedTrip.setRepeatDays(trip.getRepeatDays());
        plannedTrip.setRepeatWeeks(trip.getRepeatWeeks());
        plannedTrip.setLines(Strings.listToString(trip.getLines(), Strings.DEFAULT_DELIMITER));
        plannedTrip.setNotifications(Strings.listToString(trip.getNotifications(),
                Strings.DEFAULT_DELIMITER));

        realm.commitTransaction();
        realm.close();

        AlarmUtils.scheduleTrips(this);
        finish();
    }

    private static boolean isBrokenSamsungDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && isBetweenAndroidVersions(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }

    /**
     * Simple class representing a bus stop which only holds the id and a language
     * dependent name. Used in the bus stop picker to display all the bus stops from the
     * selected lines.
     *
     * @author Alex Lardschneider
     */
    private static class SimpleBusStop {

        private final int id;
        private final String name;

        public SimpleBusStop(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
