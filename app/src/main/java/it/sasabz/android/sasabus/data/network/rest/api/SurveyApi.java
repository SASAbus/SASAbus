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

package it.sasabz.android.sasabus.data.network.rest.api;

import android.content.Context;

import it.sasabz.android.sasabus.data.model.trip.Trip;
import it.sasabz.android.sasabus.data.network.rest.Endpoint;
import it.sasabz.android.sasabus.data.network.rest.model.CloudTrip;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface SurveyApi {

    @POST(Endpoint.SURVEY)
    Observable<Void> send(@Body ReportBody body);

    class ReportBody extends ReportApi.ReportBody {

        private final Trip trip;
        private final int rating;
        private int id;

        public ReportBody(Context context, String email, String message, int vehicle,
                          int rating, CloudTrip trip) {
            super(context, email, message, vehicle);

            this.trip = new Trip(trip);
            this.rating = rating;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}