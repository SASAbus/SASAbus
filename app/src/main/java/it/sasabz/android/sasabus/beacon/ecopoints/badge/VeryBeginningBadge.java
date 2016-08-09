package it.sasabz.android.sasabus.beacon.ecopoints.badge;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.beacon.Beacon;
import it.sasabz.android.sasabus.beacon.busstop.BusStopBeacon;

public class VeryBeginningBadge extends Badge {

    public VeryBeginningBadge() {
        super(110, R.string.badge_the_very_beginning_title, R.string.badge_the_very_beginning_subtitle,
                R.drawable.badge_red_stop, 10);
    }

    @Override
    public boolean evaluate(Beacon beacon) {
        if (!super.evaluate(beacon)) {
            return false;
        }

        // Since we only want to know if the user scanned a bus stop beacon, we only have to
        // check if the passed beacon is a bus stop beacon, as the condition will be met then.
        return beacon instanceof BusStopBeacon;
    }
}
