package it.sasabz.android.sasabus.util;

import android.content.Context;

import it.sasabz.android.sasabus.fcm.FcmUtils;
import it.sasabz.android.sasabus.realm.UserRealmHelper;

class SettingsMigration {

    private static final String TAG = "SettingsMigration";

    void migrate(Context context, long oldVersion, long newVersion) {
        LogUtils.e(TAG, "Migrating from " + oldVersion + " to " + newVersion);

        SettingsUtils.removeDeprecatedTags(context);
    }
}
