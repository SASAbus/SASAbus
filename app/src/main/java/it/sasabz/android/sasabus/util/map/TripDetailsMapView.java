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

package it.sasabz.android.sasabus.util.map;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.List;

import it.sasabz.android.sasabus.realm.busstop.BusStop;

public class TripDetailsMapView {

    private final String TAG = "BusStopsMapView";

    private final WebView webView;

    private final Context context;

    public TripDetailsMapView(Context context, WebView webView) {
        this.context = context;

        this.webView = webView;
        this.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.e(TAG, String.format("%s @ %d: %s", cm.message(), cm.lineNumber(), cm.sourceId()));
                return true;
            }
        });

        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);

        JSInterface bridge = new JSInterface(context);

        this.webView.addJavascriptInterface(bridge, "Android");
        this.webView.loadUrl("file:///android_asset/map/trip_details.html");
    }

    public void setMarkers(List<BusStop> busStops) {
        StringBuilder data = new StringBuilder();

        for (BusStop busStop : busStops) {
            data.append(busStop.getId()).append("#")
                    .append(busStop.getName(context)).append("#")
                    .append(busStop.getMunic(context)).append("#")
                    .append(busStop.getLat()).append("#")
                    .append(busStop.getLng()).append("=");
        }

        if (data.length() > 0) {
            data.deleteCharAt(data.length() - 1);
        }

        // Need this to make sure the page has loaded otherwise WebView
        // will throw a Uncaught ReferenceError when calling JS.
        //noinspection CodeBlock2Expr
        new Handler().postDelayed(() -> {
            webView.loadUrl("javascript:setMarkers(\"" + data.toString() + "\");");
        }, 500);
    }
}
