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

    private WebView webView;

    private Context context;

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
        new Handler().postDelayed(() -> {
            webView.loadUrl("javascript:setMarkers(\"" + data.toString() + "\");");
        }, 500);
    }
}
