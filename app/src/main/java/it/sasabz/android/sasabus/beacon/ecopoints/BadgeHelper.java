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
