package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Beacon extends RealmObject {

    @Ignore public static final String TYPE_BUS = "bus";
    @Ignore public static final String TYPE_BUS_STOP = "stop";

    private String type;
    private int major;
    private int minor;
    private int timeStamp;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }
}
