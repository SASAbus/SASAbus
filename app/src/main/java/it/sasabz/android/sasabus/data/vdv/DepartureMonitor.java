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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import it.sasabz.android.sasabus.data.vdv.data.VdvCalendar;
import it.sasabz.android.sasabus.data.vdv.data.VdvTrips;
import it.sasabz.android.sasabus.data.vdv.model.VdvBusStop;
import it.sasabz.android.sasabus.data.vdv.model.VdvDeparture;
import it.sasabz.android.sasabus.data.vdv.model.VdvTrip;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Calculates the departures for any combination of bus stops and lines, at any point of time in the
 * company calendar's range.
 *
 * @author David Dejori
 */
public class DepartureMonitor {

    private final Collection<VdvBusStop> busStops = new ArrayList<>();
    private final Collection<Integer> lineFilter = new ArrayList<>();

    private long time = Api.Time.now();
    private int past = 600;
    private int maxElements = Integer.MAX_VALUE;

    public DepartureMonitor atBusStop(int busStop) {
        busStops.add(new VdvBusStop(busStop));

        return this;
    }

    public DepartureMonitor atBusStops(int... busStops) {
        for (int busStop : busStops) {
            this.busStops.add(new VdvBusStop(busStop));
        }

        return this;
    }

    public DepartureMonitor atBusStops(Iterable<Integer> busStops) {
        for (int busStop : busStops) {
            this.busStops.add(new VdvBusStop(busStop));
        }

        return this;
    }

    public DepartureMonitor atBusStopFamily(int family) {
        busStops.addAll(BusStopRealmHelper.getBusStopsFromFamily(family));

        return this;
    }

    public DepartureMonitor atBusStopFamilies(int... families) {
        for (int busStopFamily : families) {
            busStops.addAll(BusStopRealmHelper.getBusStopsFromFamily(busStopFamily));
        }

        return this;
    }

    public DepartureMonitor atBusStopFamilies(Iterable<Integer> families) {
        for (int busStopFamily : families) {
            busStops.addAll(BusStopRealmHelper.getBusStopsFromFamily(busStopFamily));
        }

        return this;
    }

    public DepartureMonitor at(Date date) {
        return at(date.getTime());
    }

    private DepartureMonitor at(long millis) {
        time = Api.Time.addOffset(millis);

        return this;
    }

    public DepartureMonitor filterLine(int line) {
        lineFilter.add(line);

        return this;
    }

    public DepartureMonitor filterLines(int... lines) {
        for (int line : lines) {
            lineFilter.add(line);
        }

        return this;
    }

    public DepartureMonitor filterLines(Collection<Integer> lines) {
        lineFilter.addAll(lines);

        return this;
    }

    public DepartureMonitor maxElements(int maxElements) {
        this.maxElements = maxElements;
        return this;
    }

    public DepartureMonitor includePastDepartures(int seconds) {
        past = seconds;

        return this;
    }

    public Collection<VdvDeparture> collect() {
        Date date = new Date();
        date.setTime(time);

        try {
            VdvTrips.loadTrips(null, VdvCalendar.date(date));
        } catch (Exception e) {
            Utils.logException(e);
            return Collections.emptyList();
        }

        List<VdvDeparture> departures = new ArrayList<>();
        Collection<Integer> lines = new ArrayList<>();
        Collection<Integer> busStopFamilies = new HashSet<>();

        for (VdvBusStop busStop : busStops) {
            busStopFamilies.add(BusStopRealmHelper.getBusStopGroup(busStop.getId()));
        }

        for (int busStopFamily : busStopFamilies) {
            lines.addAll(Api.getPassingLines(busStopFamily));
        }

        if (!lineFilter.isEmpty()) {
            lines.retainAll(lineFilter);
        }

        time /= 1000;
        time %= 86400;

        for (VdvTrip trip : VdvTrips.ofSelectedDay()) {
            if (lines.contains(trip.getLineId())) {
                List<VdvBusStop> path = trip.calcPath();

                if (!Collections.disjoint(path, busStops)) {
                    for (VdvBusStop busStop : busStops) {
                        int index = path.indexOf(busStop);

                        if (index != -1 && index != path.size() - 1) {
                            path = trip.calcTimedPath();

                            if (path.get(index).getDeparture() > time - past) {
                                departures.add(new VdvDeparture(
                                        trip.getLineId(),
                                        path.get(index).getDeparture(),
                                        path.get(path.size() - 1),
                                        trip.getTripId()
                                ));
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(departures, VdvDeparture::compareTo);

        if (maxElements != 0) {
            departures = departures.subList(0, maxElements > departures.size()
                    ? departures.size() : maxElements);
        }

        return departures;
    }
}
