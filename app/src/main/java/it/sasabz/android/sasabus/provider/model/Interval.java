package it.sasabz.android.sasabus.provider.model;

/**
 * Represents a time interval between two bus stops.
 *
 * @author David Dejori
 */
public class Interval {

    private final int fgr;
    private final int busStop1;
    private final int busStop2;

    public Interval(int fgr, int busStop1, int busStop2) {
        this.fgr = fgr;
        this.busStop1 = busStop1;
        this.busStop2 = busStop2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interval interval = (Interval) o;
        return fgr == interval.fgr && busStop1 == interval.busStop1 && busStop2 == interval.busStop2;
    }

    @Override
    public int hashCode() {
        int result = fgr;
        result = 31 * result + busStop1;
        result = 31 * result + busStop2;
        return result;
    }
}
