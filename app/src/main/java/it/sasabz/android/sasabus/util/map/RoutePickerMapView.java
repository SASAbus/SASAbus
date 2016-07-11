package it.sasabz.android.sasabus.util.map;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.List;

import it.sasabz.android.sasabus.realm.busstop.SadBusStop;
import it.sasabz.android.sasabus.ui.route.RouteMapPickerActivity;

public class RoutePickerMapView {

    private final String TAG = "BusStopsMapView";

    private WebView webView;

    private RouteMapPickerActivity activity;

    public RoutePickerMapView(RouteMapPickerActivity activity, WebView webView) {
        this.activity = activity;

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

        JSInterface bridge = new JSInterface(activity);

        this.webView.addJavascriptInterface(bridge, "Android");
        this.webView.loadUrl("file:///android_asset/map/route_picker.html");
    }

    public void setMarkers(List<SadBusStop> busStops) {
        StringBuilder data = new StringBuilder();

        for (SadBusStop busStop : busStops) {
            data.append(busStop.getId()).append("#")
                    .append(busStop.getName(activity)).append("#")
                    .append(busStop.getMunic(activity)).append("#")
                    .append(busStop.getLat()).append("#")
                    .append(busStop.getLng()).append("=");
        }

        if (data.length() > 0) {
            data.deleteCharAt(data.length() - 1);
        }

        webView.loadUrl("javascript:setMarkers(\"" + data.toString() + "\");");
    }
}
