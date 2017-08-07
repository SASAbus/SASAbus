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
import android.text.Html;
import android.widget.RemoteViews;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.model.News;
import it.sasabz.android.sasabus.data.network.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.NewsApi;
import it.sasabz.android.sasabus.data.network.rest.response.NewsResponse;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * An app widget provider (widgets on the home screen pages) for the news delivered from Sasa SpA-AG.
 *
 * @author David Dejori
 */
public class NewsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_news);

            NewsApi newsApi = RestClient.INSTANCE.getADAPTER().create(NewsApi.class);
            newsApi.getNews()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<NewsResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.logException(e);
                        }

                        @Override
                        public void onNext(NewsResponse newsResponse) {
                            String list = "";

                            for (News news : newsResponse.news) {
                                list += String.format("<b>%s</b>: %s<br>", news.getZone(), news.getTitle());
                            }

                            views.setTextViewText(R.id.widget_news_list, Html.fromHtml(list));

                            appWidgetManager.updateAppWidget(widgetId, views);
                        }
                    });
        }
    }
}