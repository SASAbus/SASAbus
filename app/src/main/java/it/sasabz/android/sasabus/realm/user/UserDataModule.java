package it.sasabz.android.sasabus.realm.user;

import io.realm.annotations.RealmModule;

/**
 * Indicates which {@link io.realm.RealmObject} belong to the user data realm database.
 * This module itself has no use.
 *
 * @author Alex Lardschneider
 */
@RealmModule(classes = {
        Beacon.class,
        EarnedBadge.class,
        FavoriteBusStop.class,
        FavoriteLine.class,
        FilterLine.class,
        PlannedTrip.class,
        RecentRoute.class,
        Survey.class,
        Trip.class,
        TripToDelete.class
})
public class UserDataModule {
}