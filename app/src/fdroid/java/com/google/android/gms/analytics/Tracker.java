package com.google.android.gms.analytics;

import android.content.Context;

public class Tracker {

    public static final String VERSION = "1.4.2";

    private static Tracker instance = new Tracker();

    private boolean debug = false;

    public static Tracker getInstance() {
        return instance;
    }

    public void start(String s, int i, Context context) {
    }

    public void start(String s, Context context) {
    }

    public void stop() {
    }

    public void setDebug(boolean flag) {
        debug = flag;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setScreenName(String s) {
    }

    public void send(HitBuilders.HitBuilder hb) {
    }

    public void enableAdvertisingIdCollection(boolean value) {
    }

    public void enableAutoActivityTracking(boolean value) {
    }

    public void enableExceptionReporting(boolean value) {
    }

    public void setAppName(String name) {
    }

    public void setAppVersion(String version) {
    }

    public void setSessionTimeout(int timeout) {
    }
}