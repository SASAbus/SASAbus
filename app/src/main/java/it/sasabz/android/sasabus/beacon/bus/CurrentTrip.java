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
import it.sasabz.android.sasabus.beacon.notification.TripNotification;
import it.sasabz.android.sasabus.data.vdv.Api;
import it.sasabz.android.sasabus.data.vdv.model.VdvBusStop;
import it.sasabz.android.sasabus.model.BusStop;
import it.sasabz.android.sasabus.model.JsonSerializable;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.util.LogUtils;

public class CurrentTrip implements JsonSerializable {

    private static final String TAG = "CurrentTrip";

    public BusBeacon beacon;

    private transient Context mContext;

    boolean notificationVisible;

    private final List<BusStop> path;
    private List<VdvBusStop> times;

    CurrentTrip(Context context, BusBeacon beacon) {
        this.mContext = context;
        this.beacon = beacon;

        // Check for badge
        BadgeHelper.evaluate(mContext, beacon);

        path = new ArrayList<>();

        if (times != null) {
            times.clear();
        }

        List<VdvBusStop> newTimes = Api.getTrip(beacon.trip).calcTimedPath();

        if (newTimes != null) {
            times = new ArrayList<>(newTimes);

            for (VdvBusStop busStop : times) {
                path.add(new BusStop(BusStopRealmHelper.getBusStop(busStop.getId())));
            }
        } else {
            LogUtils.e(TAG, "Times for trip " + beacon.trip + " are null");
            beacon.setSuitableForTrip(context, false);
            notificationVisible = false;
        }
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
        if (beacon.isSuitableForTrip && notificationVisible) {
            TripNotification.showNotification(mContext, this);
        }
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    void setNotificationVisible(boolean visible) {
        notificationVisible = visible;
    }

    public List<VdvBusStop> getTimes() {
        return times;
    }

    public CharSequence getTitle() {
        return beacon.title;
    }

    public List<BusStop> getPath() {
        return path;
    }
}
