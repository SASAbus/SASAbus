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
