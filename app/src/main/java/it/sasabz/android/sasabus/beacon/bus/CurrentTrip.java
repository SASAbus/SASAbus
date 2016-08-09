package it.sasabz.android.sasabus.beacon.bus;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.sasabz.android.sasabus.beacon.ecopoints.BadgeHelper;
import it.sasabz.android.sasabus.model.BusStop;
import it.sasabz.android.sasabus.model.JsonSerializable;
import it.sasabz.android.sasabus.provider.apis.Trips;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;

public class CurrentTrip implements JsonSerializable {

    public BusBeacon beacon;

    private transient Context mContext;

    public boolean isNotificationShown;

    private boolean updated;

    private final List<BusStop> path;
    private List<it.sasabz.android.sasabus.provider.model.BusStop> times;

    CurrentTrip(Context context, BusBeacon beacon) {
        this.mContext = context;
        this.beacon = beacon;

        // Check for badge
        BadgeHelper.evaluate(mContext, beacon);

        path = new ArrayList<>();

        times = Trips.getPath(mContext, beacon.trip);
        for (it.sasabz.android.sasabus.provider.model.BusStop busStop : times) {
            path.add(new BusStop(BusStopRealmHelper.getBusStop(busStop.getId())));
        }
    }

    public boolean checkUpdate() {
        boolean temp = updated;
        updated = false;

        return temp;
    }

    public int getId() {
        return beacon.id;
    }

    public int getDelay() {
        return beacon.delay;
    }

    public void setBeacon(BusBeacon beacon) {
        this.beacon = beacon;
    }

    public void update() {
        updated = true;

        if (beacon.isSuitableForTrip) {
            BusBeaconHandler.notificationAction.showNotification(this);
        }
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    void setNotificationShown(boolean notificationShown) {
        isNotificationShown = notificationShown;
    }

    public List<it.sasabz.android.sasabus.provider.model.BusStop> getTimes() {
        return times;
    }

    public CharSequence getTitle() {
        return beacon.title;
    }

    public List<BusStop> getPath() {
        return path;
    }
}
