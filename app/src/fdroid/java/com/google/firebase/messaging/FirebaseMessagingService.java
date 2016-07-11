package com.google.firebase.messaging;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class FirebaseMessagingService extends Service {

    public FirebaseMessagingService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @android.support.annotation.WorkerThread
    public void onMessageReceived(RemoteMessage remoteMessage) {
    }
}