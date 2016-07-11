package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds the batched surveys. Batched surveys are surveys which couldn't be sent to the server
 * at the time the user filled them out.
 *
 * Those surveys will be sent automatically the next time the app performs a sync and will then
 * be removed from the database. Ideally we wan't this table to be empty all the time.
 *
 * @author Alex Lardschneider
 */
public class Survey extends RealmObject {

    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
