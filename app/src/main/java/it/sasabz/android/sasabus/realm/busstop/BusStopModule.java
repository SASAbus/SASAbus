package it.sasabz.android.sasabus.realm.busstop;

import io.realm.annotations.RealmModule;

/**
 * Indicates which {@link io.realm.RealmObject} belongs to the bus stop realm database.
 * This module itself has no use.
 *
 * @author Alex Lardschneider
 */
@RealmModule(classes = {
        BusStop.class,
        SadBusStop.class
})
public class BusStopModule {
}