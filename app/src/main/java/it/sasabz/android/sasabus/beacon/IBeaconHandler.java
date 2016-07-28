package it.sasabz.android.sasabus.beacon;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

public interface IBeaconHandler {

    void updateBeacons(Collection<Beacon> beacons);
    void validateBeacon(Beacon beacon, int major);
}
