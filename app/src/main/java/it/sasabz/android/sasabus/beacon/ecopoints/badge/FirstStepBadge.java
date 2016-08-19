package it.sasabz.android.sasabus.beacon.ecopoints.badge;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.beacon.Beacon;
import it.sasabz.android.sasabus.beacon.bus.BusBeacon;

public class FirstStepBadge extends Badge {

    public FirstStepBadge() {
        super(120, R.string.badge_the_first_step_title, R.string.badge_the_first_step_subtitle,
                R.drawable.badge_blue_bus, 20);
    }

    @Override
    public boolean evaluate(Beacon beacon) {
        if (!super.evaluate(beacon)) {
            return false;
        }

        // Since we only want to know if the user scanned a bus beacon, we only have to
        // check if the passed beacon is a bus beacon, as the condition will be met then.
        return beacon instanceof BusBeacon;
    }
}
