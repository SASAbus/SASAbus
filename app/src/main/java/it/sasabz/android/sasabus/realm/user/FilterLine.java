package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds the lines which the user selected as "active" in the filter.
 *
 * @author Alex Lardschneider
 */
public class FilterLine extends RealmObject {

    private int line;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}
