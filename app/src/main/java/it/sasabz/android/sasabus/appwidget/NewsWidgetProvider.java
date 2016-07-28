package it.sasabz.android.sasabus.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.text.Html;
import android.widget.RemoteViews;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.News;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.NewsApi;
import it.sasabz.android.sasabus.network.rest.response.NewsResponse;
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

            NewsApi newsApi = RestClient.ADAPTER.create(NewsApi.class);
            newsApi.getNews(context.getResources().getConfiguration().locale.toString())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<NewsResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.handleException(e);
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