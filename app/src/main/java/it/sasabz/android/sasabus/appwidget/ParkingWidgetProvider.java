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

package it.sasabz.android.sasabus.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.Parking;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.ParkingApi;
import it.sasabz.android.sasabus.network.rest.response.ParkingResponse;
import it.sasabz.android.sasabus.util.Settings;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * An app widget provider (widgets on the home screen pages) for a parking house/area.
 *
 * @author David Dejori
 */
public class ParkingWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_parking);

            int id = Settings.getWidgetParking(context);

            if (id == 0) return;

            ParkingApi parkingApi = RestClient.ADAPTER.create(ParkingApi.class);
            parkingApi.getParking(Utils.locale(context), id)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ParkingResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.logException(e);
                        }

                        @Override
                        public void onNext(ParkingResponse parkingResponse) {
                            if (parkingResponse.parking.isEmpty()) return;

                            Parking parking = parkingResponse.parking.get(0);

                            views.setTextViewText(R.id.widget_parking_name, parking.getName());
                            views.setTextViewText(R.id.widget_parking_location, parking.getAddress());
                            views.setTextViewText(R.id.widget_parking_phone_number, parking.getPhone());
                            views.setTextViewText(R.id.widget_parking_slots, parking.getFreeSlots() + "/" + parking.getTotalSlots() + " " + context.getResources().getString(R.string.parking_detail_current_free));

                            appWidgetManager.updateAppWidget(widgetId, views);
                        }
                    });
        }
    }
}