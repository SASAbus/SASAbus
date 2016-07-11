package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds the planned trips.
 *
 * @author Alex Lardschneider
 */
public class PlannedTrip extends RealmObject {

    private int busStop;
    private long timeStamp;

    private String title;
    private String hash;
    private String lines;
    private String notifications;

    private int repeatDays;
    private int repeatWeeks;

    public int getBusStop() {
        return busStop;
    }

    public void setBusStop(int busStop) {
        this.busStop = busStop;
    }

    public long getTimestamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLines() {
        return lines;
    }

    public void setLines(String lines) {
        this.lines = lines;
    }

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    public int getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(int repeatDays) {
        this.repeatDays = repeatDays;
    }

    public int getRepeatWeeks() {
        return repeatWeeks;
    }

    public void setRepeatWeeks(int repeatWeeks) {
        this.repeatWeeks = repeatWeeks;
    }

    @Override
    public String toString() {
        return "PlannedTrip{" +
                "busStop=" + busStop +
                ", timeStamp=" + timeStamp +
                ", title='" + title + '\'' +
                ", hash='" + hash + '\'' +
                ", lines='" + lines + '\'' +
                ", notifications='" + notifications + '\'' +
                ", repeatDays=" + repeatDays +
                ", repeatWeeks=" + repeatWeeks +
                "} " + super.toString();
    }
}
