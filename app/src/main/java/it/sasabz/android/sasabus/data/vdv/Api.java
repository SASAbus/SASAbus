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

package it.sasabz.android.sasabus.data.vdv;

import android.content.Context;

import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.sasabz.android.sasabus.data.model.line.Lines;
import it.sasabz.android.sasabus.data.vdv.data.VdvCalendar;
import it.sasabz.android.sasabus.data.vdv.data.VdvHandler;
import it.sasabz.android.sasabus.data.vdv.data.VdvPaths;
import it.sasabz.android.sasabus.data.vdv.data.VdvTrips;
import it.sasabz.android.sasabus.data.vdv.model.VdvBusStop;
import it.sasabz.android.sasabus.data.vdv.model.VdvTrip;
import rx.Observable;
import timber.log.Timber;

/**
 * This is the main offline API where the app gets data from. This API tells us specific information
 * about departures and trips. It uses the SASA SpA-AG offline stored open data.
 *
 * @author David Dejori
 */
public final class Api {

    private Api() {
    }

    public static VdvTrip getTrip(int tripId) {
        return getTrip(tripId, true);
    }

    public static VdvTrip getTrip(int tripId, boolean verifyUiThread) {
        VdvHandler.blockTillLoaded(verifyUiThread);

        Collection<VdvTrip> trips = VdvTrips.ofSelectedDay();

        for (VdvTrip trip : trips) {
            if (trip.getTripId() == tripId) {
                return trip;
            }
        }

        Timber.e("Trip %s not found", tripId);

        return VdvTrip.empty;
    }

    static List<Integer> getPassingLines(int group) {
        VdvHandler.blockTillLoaded();

        Collection<VdvBusStop> busStops = BusStopRealmHelper.getBusStopsFromFamily(group);

        List<Integer> lines = new ArrayList<>();
        for (Map.Entry<Integer, List<List<VdvBusStop>>> line : VdvPaths.getPaths().entrySet()) {
            for (List<VdvBusStop> variant : line.getValue()) {
                if (!Collections.disjoint(variant, busStops)) {
                    lines.add(line.getKey());
                    break;
                }
            }
        }

        Collections.sort(lines, (o1, o2) -> Lines.ORDER.indexOf(o1) - Lines.ORDER.indexOf(o2));

        return lines;
    }

    public static boolean todayExists(Context context) {
        VdvHandler.blockTillLoaded();

        try {
            VdvCalendar.today(context);
        } catch (VdvCalendar.VdvCalendarException e) {
            return false;
        }

        return VdvHandler.isValid();
    }

    public static Observable<Boolean> todayExistsRx(Context context) {
        return Observable.fromCallable(() -> Api.todayExists(context));
    }

    public static final class Time {

        private Time() {
        }

        static long addOffset(long seconds) {
            seconds += DateTimeZone.forID("Europe/Rome").getOffset(seconds);
            return seconds;
        }

        static long now() {
            long now = System.currentTimeMillis();
            return now + DateTimeZone.forID("Europe/Rome").getOffset(now);
        }

        public static String toTime(long seconds) {
            return String.format(Locale.ROOT, "%02d:%02d", seconds / 3600 % 24, seconds % 3600 / 60);
        }
    }
}