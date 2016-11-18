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

package it.sasabz.android.sasabus.beacon.ecopoints.badge;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import io.realm.Realm;
import it.sasabz.android.sasabus.beacon.Beacon;
import it.sasabz.android.sasabus.data.network.auth.AuthHelper;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.data.realm.UserRealmHelper;
import it.sasabz.android.sasabus.data.realm.user.EarnedBadge;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.rx.NextObserver;
import rx.schedulers.Schedulers;

public abstract class InAppBadge {

    private static final String TAG = "Badge";

    private final int id;
    private final int title;
    private final int summary;
    private final int icon;
    private final int points;

    InAppBadge(int id, int title, int summary, int icon, int points) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.icon = icon;
        this.points = points;
    }

    public boolean completed() {
        return UserRealmHelper.hasEarnedBadge(id);
    }

    public void complete() {
        UserRealmHelper.setEarnedBadge(id);

        EcoPointsApi api = RestClient.ADAPTER.create(EcoPointsApi.class);
        api.sendBadge(id)
                .subscribeOn(Schedulers.io())
                .subscribe(new NextObserver<Void>() {
                    @Override
                    public void onNext(Void aVoid) {
                        Realm realm = Realm.getDefaultInstance();

                        EarnedBadge badge = realm.where(EarnedBadge.class)
                                .equalTo("id", id).findFirst();

                        if (badge == null) {
                            LogUtils.e(TAG, "Badge with id " + id + " has been inserted into database, " +
                                    "but cannot be queried.");
                            return;
                        }

                        realm.beginTransaction();
                        badge.setSent(true);
                        realm.commitTransaction();

                        realm.close();

                        LogUtils.e(TAG, "Uploaded badge " + id);
                    }
                });
    }

    /**
     * Evaluates if the badge is completed by checking  different statements match in each badge.
     * Those statements vary by badge, and can be true e.g. if the user scans the first beacon.
     *
     * @param beacon the beacon to evaluate the expression with.
     * @return {@code true} if the condition has been met, {@code false} if it hasn't
     */
    public boolean evaluate(Beacon beacon) {
        // You can only earn the badge if you are logged in, so check for that first.
        return AuthHelper.getTokenIfValid() != null;
    }

    /**
     * The id of the badge which will be used to interact with the api and to check which badge
     * has already been completed.
     *
     * @return the badge id.
     */
    public int id() {
        return id;
    }

    /**
     * The title which will be used in the notification.
     *
     * @return the badge title in form of a string resource.
     */
    @StringRes
    public int title() {
        return title;
    }

    /**
     * The summary which will be used in the notification.
     *
     * @return the badge summary in form of a string resource.
     */
    @StringRes
    public int summary() {
        return summary;
    }

    /**
     * The icon which will be used in the notification.
     *
     * @return the icon in form of a drawable res.
     */
    @DrawableRes
    public int icon() {
        return icon;
    }

    /**
     * Indicates how many eco points you earn for completing this badge.
     *
     * @return the earned eco points.
     */
    public int points() {
        return points;
    }
}
