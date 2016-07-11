package it.sasabz.android.sasabus.fcm.command;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Interface for a command which can be executed by GCM.
 *
 * @author Alex Lardschneider
 */
public interface FcmCommand {
    void execute(Context context, @NonNull Map<String, String> data);
}
