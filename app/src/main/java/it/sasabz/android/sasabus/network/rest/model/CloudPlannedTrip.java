package it.sasabz.android.sasabus.network.rest.model;

import it.sasabz.android.sasabus.realm.user.PlannedTrip;
import it.sasabz.android.sasabus.util.Utils;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CloudPlannedTrip {

    private final String hash;
    private final String title;

    @SerializedName("timestamp")
    private final long timeStamp;

    private final List<Integer> lines;
    private final List<Integer> notifications;

    @SerializedName("bus_stop")
    private final int busStop;

    @SerializedName("repeat_days")
    private final int repeatDays;

    @SerializedName("repeat_weeks")
    private final int repeatWeeks;

    public CloudPlannedTrip(PlannedTrip trip) {
        hash = trip.getHash();
        title = trip.getTitle();
        timeStamp = trip.getTimestamp();
        busStop = trip.getBusStop();
        lines = Utils.stringToList(trip.getLines(), ",");
        notifications = Utils.stringToList(trip.getNotifications(), ",");
        repeatDays = trip.getRepeatDays();
        repeatWeeks = trip.getRepeatWeeks();
    }

    public String getHash() {
        return hash;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getBusStop() {
        return busStop;
    }

    public List<Integer> getLines() {
        return lines;
    }

    public List<Integer> getNotifications() {
        return notifications;
    }

    public int getRepeatDays() {
        return repeatDays;
    }

    public int getRepeatWeeks() {
        return repeatWeeks;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "CloudPlannedTrip{" +
                "hash='" + hash + '\'' +
                ", title='" + title + '\'' +
                ", timeStamp=" + timeStamp +
                ", lines=" + lines +
                ", notifications=" + notifications +
                ", busStop=" + busStop +
                ", repeatDays=" + repeatDays +
                ", repeatWeeks=" + repeatWeeks +
                '}';
    }
}
