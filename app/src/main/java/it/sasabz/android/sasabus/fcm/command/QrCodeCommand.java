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

package it.sasabz.android.sasabus.fcm.command;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Map;

import it.sasabz.android.sasabus.ui.ecopoints.event.EventDetailsActivity;
import it.sasabz.android.sasabus.ui.ecopoints.event.QrCodeActivity;
import timber.log.Timber;

public class QrCodeCommand implements FcmCommand {

    @Override
    public void execute(Context context, @NonNull Map<String, String> data) {
        Timber.e("Received QR code command");

        String action = data.get("action");
        String event = data.get("event");

        switch (action) {
            case "destroy":
                Intent intent = new Intent(QrCodeActivity.BROADCAST_QR_CODE_REDEEMED);
                intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                break;
            default:
                Timber.e("Unknown action: " + action);
        }
    }
}
