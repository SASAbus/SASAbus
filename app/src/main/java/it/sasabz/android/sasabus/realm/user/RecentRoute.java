package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds all the recent routes which the user searched for.
 * This does not include any query made with a google place instead of a normal bus stops,
 * as the ids of them are not interchangeable.
 *
 * @author Alex Lardschneider
 */
public class RecentRoute extends RealmObject {

    private int id;
    private int departureId;
    private int arrivalId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDepartureId() {
        return departureId;
    }

    public void setDepartureId(int departureId) {
        this.departureId = departureId;
    }

    public int getArrivalId() {
        return arrivalId;
    }

    public void setArrivalId(int arrivalId) {
        this.arrivalId = arrivalId;
    }
}
