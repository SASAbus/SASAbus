package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds the favorite lines by their id.
 *
 * @author Alex Lardschneider
 */
public class FavoriteLine extends RealmObject {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
