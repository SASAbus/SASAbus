package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds the favorite bus stops by their group. As the bus stops get displayed grouped by their
 * family, we need to save the group of the bus stop instead of the individual id.
 *
 * @author Alex Lardschneider
 */
public class FavoriteBusStop extends RealmObject {

    private int group;

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
