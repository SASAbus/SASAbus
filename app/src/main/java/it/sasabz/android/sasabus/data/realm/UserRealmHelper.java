/*
 * Copyright (C) 2016 David Dejori, Alex Lardschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.sasabz.android.sasabus.data.realm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;
import it.sasabz.android.sasabus.beacon.bus.BusBeacon;
import it.sasabz.android.sasabus.data.model.line.Lines;
import it.sasabz.android.sasabus.data.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.data.realm.user.Beacon;
import it.sasabz.android.sasabus.data.realm.user.EarnedBadge;
import it.sasabz.android.sasabus.data.realm.user.FavoriteBusStop;
import it.sasabz.android.sasabus.data.realm.user.FavoriteLine;
import it.sasabz.android.sasabus.data.realm.user.FilterLine;
import it.sasabz.android.sasabus.data.realm.user.RecentRoute;
import it.sasabz.android.sasabus.data.realm.user.UserDataModule;
import it.sasabz.android.sasabus.sync.TripSyncHelper;
import it.sasabz.android.sasabus.util.Utils;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public final class UserRealmHelper {

    private static final String TAG = "UserRealmHelper";

    /**
     * Version should not be in YY MM DD Rev. format as it makes upgrading harder.
     */
    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "default.realm";

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    private UserRealmHelper() {
    }

    /**
     * Initializes the default realm instance, being the user database. This database holds all
     * user specific data, e.g. favorite lines/bus stops or trips.
     *
     * @param context Context needed to build the {@link RealmConfiguration}.
     */
    public static void init(Context context) {
        sContext = context;

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(DB_NAME)
                .schemaVersion(DB_VERSION)
                .modules(new UserDataModule())
                .migration(new Migration())
                .build();

        Realm.setDefaultConfiguration(config);
    }

    private static class Migration implements RealmMigration {

        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            Log.e(TAG, "Upgrading realm from " + oldVersion + " to " + newVersion);

            RealmSchema schema = realm.getSchema();

            if (oldVersion == 1) {
                schema.remove("Trip");
                schema.remove("TripToDelete");
                schema.remove("Survey");

                oldVersion++;
            }

            if (oldVersion < newVersion) {
                throw new IllegalStateException(String.format("Missing upgrade from %s to %s",
                        oldVersion, newVersion));
            }
        }
    }


    // ======================================= RECENTS =============================================

    public static void insertRecent(int departureId, int arrivalId) {
        if (!recentExists(departureId, arrivalId)) {
            Realm realm = Realm.getDefaultInstance();

            int maxId = 0;
            Number max = realm.where(RecentRoute.class).max("id");
            if (max != null) {
                maxId = max.intValue() + 1;
            }

            realm.beginTransaction();

            RecentRoute recentRoute = realm.createObject(RecentRoute.class);
            recentRoute.setId(maxId);
            recentRoute.setDepartureId(departureId);
            recentRoute.setArrivalId(arrivalId);

            realm.commitTransaction();
            realm.close();
        }
    }

    public static void deleteRecent(int id) {
        Realm realm = Realm.getDefaultInstance();

        RecentRoute recentRoute = realm.where(RecentRoute.class).equalTo("id", id).findFirst();

        realm.beginTransaction();
        recentRoute.deleteFromRealm();
        realm.commitTransaction();

        realm.close();
    }

    private static boolean recentExists(int departureId, int arrivalId) {
        Realm realm = Realm.getDefaultInstance();

        int count = realm.where(RecentRoute.class).equalTo("departureId", departureId).or()
                .equalTo("arrivalId", arrivalId).findAll().size();

        realm.close();

        return count > 0;
    }


    // ====================================== FAVORITES ============================================

    public static void addFavoriteLine(int lineId) {
        Realm realm = Realm.getDefaultInstance();

        FavoriteLine line = realm.where(FavoriteLine.class).equalTo("id", lineId).findFirst();
        if (line != null) {
            // Line already exists in database, skip it.
            return;
        }

        realm.beginTransaction();

        FavoriteLine favoriteLine = realm.createObject(FavoriteLine.class);
        favoriteLine.setId(lineId);

        realm.commitTransaction();
        realm.close();

        Timber.e("Added favorite line " + lineId);
    }

    public static void addFavoriteBusStop(int busStopGroup) {
        Realm realm = Realm.getDefaultInstance();

        FavoriteBusStop busStop = realm.where(FavoriteBusStop.class)
                .equalTo("group", busStopGroup).findFirst();
        if (busStop != null) {
            // Bus stop group already exists in database, skip it.
            return;
        }

        realm.beginTransaction();

        FavoriteBusStop favoriteLine = realm.createObject(FavoriteBusStop.class);
        favoriteLine.setGroup(busStopGroup);

        realm.commitTransaction();
        realm.close();

        Timber.e("Added favorite bus stop group " + busStopGroup);
    }

    public static void removeFavoriteLine(int lineId) {
        Realm realm = Realm.getDefaultInstance();

        FavoriteLine line = realm.where(FavoriteLine.class).equalTo("id", lineId).findFirst();
        if (line != null) {
            realm.beginTransaction();
            line.deleteFromRealm();
            realm.commitTransaction();
        }

        realm.close();

        Timber.e("Removed favorite line " + lineId);
    }

    public static void removeFavoriteBusStop(int busStopGroup) {
        Realm realm = Realm.getDefaultInstance();

        FavoriteBusStop busStop = realm.where(FavoriteBusStop.class)
                .equalTo("group", busStopGroup).findFirst();
        if (busStop != null) {
            realm.beginTransaction();
            busStop.deleteFromRealm();
            realm.commitTransaction();
        }

        realm.close();

        Timber.e("Removed favorite bus stop group " + busStopGroup);
    }

    public static boolean hasFavoriteLine(int lineId) {
        Realm realm = Realm.getDefaultInstance();
        boolean result = realm.where(FavoriteLine.class).equalTo("id", lineId).count() > 0;
        realm.close();

        return result;
    }

    public static boolean hasFavoriteBusStop(int busStopGroup) {
        Realm realm = Realm.getDefaultInstance();
        boolean result = realm.where(FavoriteBusStop.class).equalTo("group", busStopGroup).count() > 0;
        realm.close();

        return result;
    }

    public static boolean hasFavoriteLines() {
        Realm realm = Realm.getDefaultInstance();
        boolean result = realm.where(FavoriteLine.class).count() > 0;
        realm.close();

        return result;
    }


    // ======================================= TRIPS ===============================================

    public static CloudTrip insertTrip(Context context, BusBeacon beacon) {
        int startIndex = beacon.busStops.indexOf(beacon.origin);

        if (startIndex == -1) {
            Utils.throwTripError(sContext, "Trip " + beacon.id + " startIndex == -1");
            return null;
        }

        // Save the beacon trip list to a temporary list.
        List<Integer> stops = new ArrayList<>(beacon.busStops);
        beacon.busStops.clear();

        // Check if the start index is not bigger that the size of the list, so we can sub-list
        // it without crash.
        if (startIndex > stops.size()) {
            Utils.throwTripError(sContext, "Trip " + beacon.id + " startIndex > stops.size");
            return null;
        }

        // Get the stops from the start index till the end of the list.
        stops = stops.subList(startIndex, stops.size());

        int stopIndex = stops.indexOf(beacon.destination);

        // Check if the end index is bigger than 0, thus it exists in the list.
        if (stopIndex < 0) {
            Utils.throwTripError(sContext, "Trip " + beacon.id + " stopIndex < 0");
            return null;
        }

        // Get the stops from the start index till the end index.
        int endIndex = stopIndex + 1 > stops.size() ? stops.size() : stopIndex + 1;
        List<Integer> stopList = stops.subList(0, endIndex);

        if (stopList.isEmpty()) {
            Utils.throwTripError(sContext, "Trip " + beacon.id + " invalid -> sb.length() == 0\n\n" +
                    "list: " + Arrays.toString(beacon.busStops.toArray()) + "\n\n" +
                    "start: " + beacon.origin + '\n' +
                    "stop: " + beacon.destination);

            return null;
        }

        Timber.e("Inserted trip " + beacon.getHash());

        CloudTrip cloudTrip = new CloudTrip(
                beacon.getHash(),
                beacon.lineId,
                beacon.variant,
                beacon.trip,
                beacon.id,
                beacon.origin,
                beacon.destination,
                (int) beacon.getStartDate().getTime() / 1000,
                (int) beacon.lastSeen / 1000, stopList
        );

        TripSyncHelper.upload(context, Collections.singletonList(cloudTrip), Schedulers.io());

        return cloudTrip;
    }


    // ===================================== DISRUPTIONS ===========================================

    public static void setFilter(Iterable<Integer> lines) {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        realm.where(FilterLine.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();

        for (Integer line : lines) {
            realm.beginTransaction();

            FilterLine filterLine = realm.createObject(FilterLine.class);
            filterLine.setLine(line);

            realm.commitTransaction();
        }

        realm.close();
    }

    public static Collection<Integer> getFilter() {
        Realm realm = Realm.getDefaultInstance();

        Collection<FilterLine> result = realm.copyFromRealm(realm.where(FilterLine.class).findAll());
        Collection<Integer> lines = new ArrayList<>();

        for (FilterLine line : result) {
            lines.add(line.getLine());
        }

        realm.close();

        if (lines.isEmpty()) {
            lines.add(100001);

            for (int i = 2; i < Lines.checkBoxesId.length; i++) {
                lines.add(Lines.checkBoxesId[i]);
            }
        }

        return lines;
    }


    // ======================================= BEACONS =============================================

    public static void addBeacon(org.altbeacon.beacon.Beacon beacon, String type) {
        Realm realm = Realm.getDefaultInstance();

        int major = beacon.getId2().toInt();
        int minor = beacon.getId3().toInt();

        if (major == 1 && minor != 1) {
            major = beacon.getId3().toInt();
            minor = beacon.getId2().toInt();
        }

        realm.beginTransaction();

        Beacon realmObject = realm.createObject(Beacon.class);
        realmObject.setType(type);
        realmObject.setMajor(major);
        realmObject.setMinor(minor);
        realmObject.setTimeStamp((int) (System.currentTimeMillis() / 1000));

        realm.commitTransaction();
        realm.close();

        Timber.w("Added beacon " + major + " to realm");
    }


    // ======================================= BADGES ==============================================

    public static boolean hasEarnedBadge(int badgeId) {
        Realm realm = Realm.getDefaultInstance();

        EarnedBadge badge = realm.where(EarnedBadge.class).equalTo("id", badgeId).findFirst();

        boolean result = badge != null;

        realm.close();

        return result;
    }

    public static void setEarnedBadge(int badgeId) {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();

        EarnedBadge badge = realm.createObject(EarnedBadge.class);
        badge.setId(badgeId);

        realm.commitTransaction();

        realm.close();
    }
}
