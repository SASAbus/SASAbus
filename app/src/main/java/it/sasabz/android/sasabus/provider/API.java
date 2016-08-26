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

package it.sasabz.android.sasabus.provider;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.provider.apis.CompanyCalendar;
import it.sasabz.android.sasabus.provider.apis.Departures;
import it.sasabz.android.sasabus.provider.apis.Handler;
import it.sasabz.android.sasabus.provider.apis.Paths;
import it.sasabz.android.sasabus.provider.apis.Trips;
import it.sasabz.android.sasabus.provider.model.BusStop;
import it.sasabz.android.sasabus.provider.model.PlannedDeparture;
import it.sasabz.android.sasabus.provider.model.Trip;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.Preconditions;

/**
 * This is the main offline API where the app gets data from. This API tells us specific information
 * about departures and trips. It uses the SASA SpA-AG offline stored open data.
 *
 * @author David Dejori
 */
public final class API {

    public static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm", Locale.ITALY);

    private static final Pattern A = Pattern.compile("A", Pattern.LITERAL);
    private static final Pattern B = Pattern.compile("B", Pattern.LITERAL);

    private API() {
    }

    @NonNull
    public static List<BusStop> getPath(Context context, long time, int line) {
        Preconditions.checkNotNull(context, "getPath() context == null");

        Handler.load(context);

        Calendar calendar = Calendar.getInstance();

        time %= 86400;
        if (time < 5400) {
            time += 86400;
            calendar.add(Calendar.DATE, -1);
        }

        List<Trip> trips = Trips.getTrips(CompanyCalendar.getDayType(DATE.format(calendar.getTime())), line);

        for (Trip trip : trips) {
            if (trip.getSecondsAtUserStop() == time) {
                return trip.getPath();
            }
        }

        return new ArrayList<>();
    }

    @NonNull
    private static List<Trip> getDepartures(Context context, String date, String time, int stop) {
        Preconditions.checkNotNull(context, "getDepartures() context == null");

        Handler.load(context);

        return Departures.getDepartures(date, time, stop);
    }

    @NonNull
    private static List<Trip> getDepartures(Context context, String date, String time, Iterable<BusStop> stops) {
        Preconditions.checkNotNull(context, "getDepartures() context == null");

        Handler.load(context);

        return Departures.getDepartures(date, time, stops);
    }

    @NonNull
    public static List<Trip> getDepartures(Context context, int stop) {
        Preconditions.checkNotNull(context, "getDepartures() context == null");

        Date now = new Date();
        return getDepartures(context, DATE.format(now), TIME.format(now), stop);
    }

    @NonNull
    public static List<Trip> getMergedDepartures(Context context, int group) {
        Preconditions.checkNotNull(context, "getMergedDepartures() context == null");

        Collection<BusStop> stops = new ArrayList<>();
        Iterable<it.sasabz.android.sasabus.realm.busstop.BusStop> busStops =
                new ArrayList<>(BusStopRealmHelper.getBusStopsFromGroup(group));

        for (it.sasabz.android.sasabus.realm.busstop.BusStop busStop : busStops) {
            stops.add(new BusStop(busStop.getId()));
        }

        Date now = new Date();
        String date = DATE.format(now);
        String time = TIME.format(now);

        return getDepartures(context, date, time, stops);
    }

    @NonNull
    public static List<String> getPassingLines(Context context, int family) {
        Preconditions.checkNotNull(context, "getPassingLines() context == null");

        Handler.load(context);

        Collection<BusStop> busStops = BusStopRealmHelper.getBusStopsFromFamily(family);

        List<String> lines = new ArrayList<>();
        for (Map.Entry<Integer, HashMap<Integer, List<BusStop>>> line : Paths.getPaths().entrySet()) {
            for (Map.Entry<Integer, List<BusStop>> variant : line.getValue().entrySet()) {
                if (!Collections.disjoint(variant.getValue(), busStops)) {
                    lines.add(Lines.lidToName(line.getKey()));
                    break;
                }
            }
        }

        Collections.sort(lines, (lhs, rhs) -> Integer.parseInt(B.matcher(A.matcher(lhs).replaceAll(Matcher.quoteReplacement(""))).replaceAll(Matcher.quoteReplacement(""))) -
                Integer.parseInt(B.matcher(A.matcher(rhs).replaceAll(Matcher.quoteReplacement(""))).replaceAll(Matcher.quoteReplacement(""))));

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("A")) lines.set(i, line.replace("A", "B"));
            if (line.contains("B")) lines.set(i, line.replace("B", "A"));
        }

        return lines;
    }

    @NonNull
    public static Iterable<Integer> getBusStopsOfLines(Context context, Iterable<Integer> lines) {
        Preconditions.checkNotNull(context, "getBusStopsOfLines() context == null");

        Handler.load(context);

        Collection<Integer> busStops = new HashSet<>();
        for (Integer line : lines) {
            for (int i = 0; i < Paths.VARIANTS.get(line); i++) {
                List<BusStop> path = Paths.getPath(line, i + 1);
                for (int j = 0; j < path.size(); j++) {
                    busStops.add(path.get(j).getId());
                }
            }
        }

        return busStops;
    }

    @Nullable
    public static PlannedDeparture getNextTrip(Context context, Collection<Integer> lines, int stop, int time) {
        Preconditions.checkNotNull(context, "getNextTrip() context == null");

        Handler.load(context);

        Date now = new Date();

        LogUtils.e("PROVIDER", "DATE: " + DATE.format(now));
        LogUtils.e("PROVIDER", "TIME: " + ApiUtils.getTime(time));

        List<Trip> departures = Departures.getDepartures(DATE.format(now), ApiUtils.getTime(time), stop);

        for (Trip trip : departures) {
            if (lines.contains(trip.getLine())) {
                return new PlannedDeparture(trip.getLine(), trip.getSecondsAtStation(stop), trip.getTrip());
            }
        }

        return null;
    }

    public static boolean todayExists(Context context) {
        Handler.load(context);
        return CompanyCalendar.getDayType(DATE.format(Calendar.getInstance().getTime())) != -1;
    }
}
