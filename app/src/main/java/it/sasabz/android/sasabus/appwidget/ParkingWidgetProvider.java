package it.sasabz.android.sasabus.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.widget.RemoteViews;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.News;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.NewsApi;
import it.sasabz.android.sasabus.network.rest.api.ParkingApi;
import it.sasabz.android.sasabus.network.rest.response.NewsResponse;
import it.sasabz.android.sasabus.network.rest.response.ParkingResponse;
import it.sasabz.android.sasabus.util.SettingsUtils;
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

            int id = SettingsUtils.getWidgetParking(context);

            if (id == 0) return;

            ParkingApi parkingApi = RestClient.ADAPTER.create(ParkingApi.class);
            parkingApi.getParking(context.getResources().getConfiguration().locale.toString(), id)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ParkingResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.handleException(e);
                        }

                        @Override
                        public void onNext(ParkingResponse parkingResponse) {
                            if (parkingResponse.parking.isEmpty()) return;

                            views.setTextViewText(R.id.widget_parking_name, parkingResponse.parking.get(0).getName());
                            views.setTextViewText(R.id.widget_parking_location, parkingResponse.parking.get(0).getAddress());
                            views.setTextViewText(R.id.widget_parking_phone_number, parkingResponse.parking.get(0).getPhone());
                            views.setTextViewText(R.id.widget_parking_slots, parkingResponse.parking.get(0).getFreeSlots() + "/" + parkingResponse.parking.get(0).getTotalSlots() + " " + context.getResources().getString(R.string.parking_detail_current_free));

                            Intent intent = new Intent(context, ParkingWidgetProvider.class);
                            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

                            views.setOnClickPendingIntent(R.id.widget_parking_refresh, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                            appWidgetManager.updateAppWidget(widgetId, views);
                        }
                    });
        }
    }
}