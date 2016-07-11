package it.sasabz.android.sasabus.beacon;

class CurrentTrip {

    private BusBeacon beacon;
    private boolean notificationShown;

    CurrentTrip(BusBeacon beacon) {
        this.beacon = beacon;
    }

    BusBeacon getBeacon() {
        return beacon;
    }

    void setBeacon(BusBeacon beacon) {
        this.beacon = beacon;
    }

    boolean checkUpdate() {
        return true;
    }

    int getId() {
        return beacon.getId();
    }

    boolean isNotificationShown() {
        return notificationShown;
    }

    void setNotificationShown(boolean notificationShown) {
        this.notificationShown = notificationShown;
    }

    public CharSequence getTitle() {
        return beacon.getTitle();
    }
}
