package it.sasabz.android.sasabus.beacon.ecopoints;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.sasabz.android.sasabus.beacon.Beacon;
import it.sasabz.android.sasabus.beacon.ecopoints.badge.Badge;
import it.sasabz.android.sasabus.beacon.ecopoints.badge.FirstStepBadge;
import it.sasabz.android.sasabus.beacon.ecopoints.badge.VeryBeginningBadge;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.NotificationUtils;

public final class BadgeHelper {

    private static final String TAG = "BadgeHelper";

    private static final List<Badge> BADGES;

    static {
        List<Badge> badges = new ArrayList<>();
        badges.add(new FirstStepBadge());
        badges.add(new VeryBeginningBadge());

        BADGES = Collections.unmodifiableList(badges);
    }

    private BadgeHelper() {
    }

    public static void evaluate(Context context, Beacon beacon) {
        for (Badge badge : BADGES) {
            if (!badge.completed() && badge.evaluate(beacon)) {
                LogUtils.e(TAG, "Completed badge " + badge.id());

                badge.complete();

                NotificationUtils.badge(context, badge);
            }
        }
    }
}
