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

package it.sasabz.android.sasabus.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.sasabz.android.sasabus.fcm.command.ConfigCommand;
import it.sasabz.android.sasabus.fcm.command.FcmCommand;
import it.sasabz.android.sasabus.fcm.command.LogoutCommand;
import it.sasabz.android.sasabus.fcm.command.NewsCommand;
import it.sasabz.android.sasabus.fcm.command.NotificationCommand;
import it.sasabz.android.sasabus.fcm.command.QrCodeCommand;
import it.sasabz.android.sasabus.fcm.command.SyncCommand;
import it.sasabz.android.sasabus.fcm.command.TestCommand;
import it.sasabz.android.sasabus.fcm.command.TrafficLightCommand;
import it.sasabz.android.sasabus.util.LogUtils;

/**
 * This {@link android.app.Service} is launched when a new GCM message arrives. It will then select
 * the appropriate command from a defined list of commands. The command will then handle the
 * GCM message and process it further.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class FcmService extends FirebaseMessagingService {

    private static final String TAG = "FcmService";

    private static final Map<String, FcmCommand> MESSAGE_RECEIVERS;

    static {
        Map<String, FcmCommand> receivers = new HashMap<>();
        receivers.put("test", new TestCommand());
        receivers.put("sync", new SyncCommand());
        receivers.put("notification", new NotificationCommand());
        receivers.put("news", new NewsCommand());
        receivers.put("traffic_light", new TrafficLightCommand());
        receivers.put("config", new ConfigCommand());
        receivers.put("logout", new LogoutCommand());
        receivers.put("qr_code", new QrCodeCommand());

        MESSAGE_RECEIVERS = Collections.unmodifiableMap(receivers);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        LogUtils.e(TAG, "onMessageReceived()");

        String receiver = message.getData().get("receiver");

        FcmCommand command = MESSAGE_RECEIVERS.get(receiver);
        if (command == null) {
            LogUtils.e(TAG, "Unknown command received: " + receiver);
        } else {
            command.execute(this, message.getData());
        }
    }
}