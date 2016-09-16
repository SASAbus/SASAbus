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

import java.util.List;

import it.sasabz.android.sasabus.network.rest.model.RealtimeBus;
import it.sasabz.android.sasabus.network.rest.response.RealtimeResponse;

public class RealtimeMapView {

    private final String TAG = "RealtimeMapView";

    private final WebView webView;

    private MapDownloadHelper downloadHelper;

    public RealtimeMapView(Context context, WebView webView) {
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
        new MapDownloadHelper(context, webView, this).checkForMap();

        this.webView.addJavascriptInterface(bridge, "Android");
        this.webView.loadUrl("file:///android_asset/map/realtime.html");
    }

    public void setMarkers(RealtimeResponse realtimeResponse, List<Integer> filterList) {
        StringBuilder data = new StringBuilder();

        for (RealtimeBus bus : realtimeResponse.buses) {
            data.append(bus.lineName).append('#')
                    .append(bus.trip).append('#')
                    .append(bus.lineId).append('#')
                    .append(bus.vehicle).append('#')
                    .append(bus.latitude).append('#')
                    .append(bus.longitude).append('#')
                    .append(bus.currentStopName).append('#')
                    .append(bus.lastStopName).append('#')
                    .append(bus.delayMin).append('#')
                    .append(bus.busStop).append('#')
                    .append(bus.colorHex).append('=');
        }

        if (data.length() > 0) {
            data.deleteCharAt(data.length() - 1);
        }

        StringBuilder sb = new StringBuilder();

        for (Integer line : filterList) {
            sb.append(line).append(", ");
        }

        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        webView.loadUrl("javascript:setMarkers(\"" + data.toString() + "\", [" + sb.toString() + "]);");
    }

    public void filter(List<Integer> filterList) {
        StringBuilder sb = new StringBuilder();

        for (Integer line : filterList) {
            sb.append(line).append(", ");
        }

        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        webView.loadUrl("javascript:filterMarkers([" + sb.toString() + "]);");
    }
}
