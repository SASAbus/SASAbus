package it.sasabz.android.sasabus.fcm.command;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.Map;

import it.sasabz.android.sasabus.util.LogUtils;

/**
 * Command which allows GCM to change app settings. To change app settings, a push notification
 * has to be sent to the {@code /topic/general} topic and include the type of the preference,
 * the key and a value. All preference types which are supported by {@link SharedPreferences.Editor}
 * are supported by this command.
 *
 * @author Alex Lardschneider
 */
public class ConfigCommand implements FcmCommand {

    private static final String TAG = "ConfigCommand";

    @Override
    public void execute(Context context, @NonNull Map<String, String> data) {
        LogUtils.e(TAG, "Received GCM test message: extraData=" + data);

        String type = data.get("type");
        String key = data.get("key");
        String value = data.get("value");

        LogUtils.e(TAG, "Setting key " + key + " of type " + type + " to value " + value);

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
