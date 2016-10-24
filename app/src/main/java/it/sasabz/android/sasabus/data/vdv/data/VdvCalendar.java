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

package it.sasabz.android.sasabus.data.vdv.data;

import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import it.sasabz.android.sasabus.data.vdv.model.VdvDate;

/**
 * Represents the calendar of day types of SASA SpA-AG. For example Saturdays, Sundays and holidays
 * have different timetables than the normal days (from Monday to Friday) and so on.
 *
 * @author David Dejori
 */
public final class VdvCalendar {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
    private static final Collection<VdvDate> calendar = new ArrayList<>();

    private VdvCalendar() {
    }

    /**
     * This method searches for the current date in the calendar. Note that if the app is being used
     * at midnight and the date suddenly changes, this method needs to be called again to correct
     * the date. As an alternative the app needs to be restarted.
     *
     * @param jCalendar the JSON data with the corresponding information
     * @throws VdvCalendarException
     */
    static void loadCalendar(JSONArray jCalendar) throws Exception {
        for (int i = 0; i < jCalendar.length(); i++) {
            JSONObject jDay = jCalendar.getJSONObject(i);

            calendar.add(new VdvDate(jDay.getInt("day_id"),
                    dateFormat.parse(jDay.getString("date"))
            ));
        }
    }

    public static VdvDate today() {
        if (calendar.isEmpty()) {
            throw new VdvCalendarException("The calendar must be initialized first, " +
                    "before a date is requested.");
        }

        for (VdvDate date : calendar) {
            if (DateUtils.isToday(date.getDate().getTime())) {
                return date;
            }
        }

        throw new VdvCalendarException(String.format("The requested day (%s) doesn't exist " +
                "in the calendar.", dateFormat.format(new Date())));
    }

    public static int date(Date date) {
        String dateString = dateFormat.format(date);

        if (calendar.isEmpty()) {
            throw new VdvCalendarException("The calendar must be initialized first, " +
                    "before a date (%s) is requested.");
        }

        for (VdvDate d : calendar) {
            if (dateFormat.format(d.getDate()).equals(dateString)) {
                return d.getId();
            }
        }

        throw new VdvCalendarException(String.format("The requested day (%s) doesn't exist " +
                "in the calendar.", dateString));
    }

    /**
     * This exception is used to indicate that an operation (like calculating trips) cannot be
     * completed because the information regarding the trip's date is not present in the calendar.
     *
     * @author David Dejori
     */
    public static class VdvCalendarException extends RuntimeException {

        private static final long serialVersionUID = -3543885957599889341L;

        public VdvCalendarException(String s) {
            super(s);
        }
    }
}
