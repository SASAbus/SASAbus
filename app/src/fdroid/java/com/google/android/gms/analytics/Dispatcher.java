package com.google.android.gms.analytics;

interface Dispatcher {

    interface Callbacks {
    }

    void init(Callbacks callbacks);
    void stop();
}