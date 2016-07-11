package it.sasabz.android.sasabus.provider.model;

/**
 * Represents the stop time of a bus at a specific stop.
 *
 * @author David Dejori
 */
public class StopTime {

    private final int id;
    private final int stop;

    public StopTime(int id, int stop) {
        this.id = id;
        this.stop = stop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StopTime stopTime = (StopTime) o;
        return id == stopTime.id && stop == stopTime.stop;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + stop;
        return result;
    }
}
