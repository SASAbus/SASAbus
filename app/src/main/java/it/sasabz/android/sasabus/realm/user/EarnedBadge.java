package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * A badge object which indicates which badge has been earned.
 * <p>
 * Only the earned badges which are hardcoded in the app are saved here, as the server already
 * knows if the user earned any of the other badges and can supply information about them.
 *
 * @author Alex Lardschneider
 */
public class EarnedBadge extends RealmObject {

    private int id;
    private boolean sent;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
