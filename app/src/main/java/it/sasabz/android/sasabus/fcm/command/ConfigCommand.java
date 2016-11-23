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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.Map;

import timber.log.Timber;

/**
 * Command which allows GCM to change app settings. To change app settings, a push notification
 * has to be sent to the {@code /topic/general} topic and include the type of the preference,
 * the key and a value. All preference types which are supported by {@link SharedPreferences.Editor}
 * are supported by this command.
 *
 * @author Alex Lardschneider
 */
public class ConfigCommand implements FcmCommand {

    @Override
    public void execute(Context context, @NonNull Map<String, String> data) {
        Timber.e("Received GCM test message: extraData=" + data);

        String type = data.get("type");
        String key = data.get("key");
        String value = data.get("value");

        Timber.e("Setting key " + key + " of type " + type + " to value " + value);

        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();

        try {
            switch (type) {
                case "String":
                    editor.putString(key, value);
                    break;
                case "int":
                    editor.putInt(key, Integer.parseInt(value));
                    break;
                case "boolean":
                    editor.putBoolean(key, Boolean.parseBoolean(value));
                    break;
                case "float":
                    editor.putFloat(key, Float.parseFloat(value));
                    break;
                case "long":
                    editor.putLong(key, Long.parseLong(value));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        editor.apply();
    }
}
