package it.sasabz.android.sasabus.util.map;

import android.content.Context;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import it.sasabz.android.sasabus.network.rest.response.PathResponse;
import it.sasabz.android.sasabus.realm.BusStopRealmHelper;
import it.sasabz.android.sasabus.realm.busstop.BusStop;

public class LinePathMapView {

    private final String TAG = "LinePathMapView";

    private WebView webView;

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
