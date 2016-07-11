package it.sasabz.android.sasabus.fcm.command;

import android.content.Context;
import android.support.annotation.NonNull;

import it.sasabz.android.sasabus.util.LogUtils;

import java.util.Map;

/**
 * Test command because debugging is fun ;-)
 *
 * @author Alex Lardschneider
 */
public class TestCommand implements FcmCommand {

    private static final String TAG = "TestCommand";

    @Override
    public void execute(Context context, @NonNull Map<String, String> data) {
        LogUtils.e(TAG, "Received GCM test message: extraData=" + data);
    }
}
