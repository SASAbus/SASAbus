package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Holds all the trips which need to be deleted (or already are in the app), but their deletion
 * couldn't be sent to the server, most probably because of a connection issue.
 *
 * Trips and planned trips are collected together in the same table and can be identified by
 * their {@link #type}, which can be either {@link #TYPE_TRIP} or {@link #TYPE_PLANNED_TRIP}.
 *
 * The deletion request will be sent automatically the next time the app syncs and the
 * corresponding entry will be removed from the table. Ideally we want this table to be empty.
 *
 * @author Alex Lardschneider
 */
public class TripToDelete extends RealmObject {

    @Ignore
    public static final int TYPE_TRIP = 0;

    @Ignore
    public static final int TYPE_PLANNED_TRIP = 1;

    private String hash;
    private int type;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
