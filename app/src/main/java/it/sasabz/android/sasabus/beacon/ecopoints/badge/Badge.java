package it.sasabz.android.sasabus.beacon.ecopoints.badge;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import it.sasabz.android.sasabus.beacon.Beacon;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.realm.UserRealmHelper;

public abstract class Badge {

    private final int id;
    private final int title;
    private final int summary;
    private final int icon;
    private final int points;

    Badge(int id, int title, int summary, int icon, int points) {
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
