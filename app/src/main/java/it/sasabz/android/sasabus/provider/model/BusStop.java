package it.sasabz.android.sasabus.provider.model;

import it.sasabz.android.sasabus.provider.ApiUtils;

/**
 * Represents a bus stop.
 *
 * @author David Dejori
 */
public class BusStop {

    private final int id;
    private int seconds;
    private String time;

    public BusStop(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
        time = ApiUtils.getTime(seconds);
    }

    public String getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && id == ((BusStop) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
