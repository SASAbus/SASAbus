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
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.davale.sasabus.core.realm.BusStopRealmHelper;
import com.davale.sasabus.core.realm.model.BusStop;

import it.sasabz.android.sasabus.data.network.rest.response.PathResponse;

public class LinePathMapView {

    private final String TAG = "LinePathMapView";

    private final WebView webView;

    public LinePathMapView(Context context, WebView webView) {
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
        this.webView.loadUrl("file:///android_asset/map/path.html");
    }

    public void setMarkers(PathResponse pathResponse) {
        StringBuilder data = new StringBuilder();

        for (int stop : pathResponse.path) {
            BusStop busStop = BusStopRealmHelper.getBusStop(stop);
            data.append(busStop.getId()).append('#')
                    .append(busStop.getFamily()).append('#')
                    .append(busStop.getLat()).append('#')
                    .append(busStop.getLng()).append('#')
                    .append(busStop.getName(webView.getContext())).append('#')
                    .append(busStop.getMunic(webView.getContext())).append('=');
        }

        if (data.length() > 0) {
            data.deleteCharAt(data.length() - 1);
        }

        webView.loadUrl("javascript:setMarkers(\"" + data.toString() + "\");");
    }
}
