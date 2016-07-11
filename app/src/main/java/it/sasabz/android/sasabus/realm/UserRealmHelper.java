package it.sasabz.android.sasabus.realm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import it.sasabz.android.sasabus.beacon.BusBeacon;
import it.sasabz.android.sasabus.model.line.Lines;
import it.sasabz.android.sasabus.network.rest.model.CloudTrip;
import it.sasabz.android.sasabus.realm.user.Beacon;
import it.sasabz.android.sasabus.realm.user.FavoriteBusStop;
import it.sasabz.android.sasabus.realm.user.FavoriteLine;
import it.sasabz.android.sasabus.realm.user.FilterLine;
import it.sasabz.android.sasabus.realm.user.RecentRoute;
import it.sasabz.android.sasabus.realm.user.Trip;
import it.sasabz.android.sasabus.realm.user.UserDataModule;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.SettingsUtils;
import it.sasabz.android.sasabus.util.Utils;

public final class UserRealmHelper {

    private static final String TAG = "UserRealmHelper";

    /**
     * Version should not be in YY MM DD Rev. format as it makes upgrading harder.
     */
    private static final int DB_VERSION = 1;
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

        RealmConfiguration config = new RealmConfiguration.Builder(context)
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

    public static void migrateFavorites() {
        String favoriteLines = SettingsUtils.getFavoriteLines(sContext);
        if (favoriteLines != null) {
            String[] favoriteLinesSplit = favoriteLines.split(",");

            for (String s : favoriteLinesSplit) {
                addFavoriteLine(Integer.parseInt(s));
            }
        }

        String favoriteBusStops = SettingsUtils.getFavoriteBusStops(sContext);
        if (favoriteBusStops != null) {
            String[] favoriteBusStopsSplit = favoriteBusStops.split(",");

            for (String s : favoriteBusStopsSplit) {
                addFavoriteBusStop(Integer.parseInt(s));
            }
        }
    }

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

        LogUtils.e(TAG, "Added favorite line " + lineId);
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

        LogUtils.e(TAG, "Added favorite bus stop group " + busStopGroup);
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

        LogUtils.e(TAG, "Removed favorite line " + lineId);
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

        LogUtils.e(TAG, "Removed favorite bus stop group " + busStopGroup);
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


    // ======================================= TRIPS ===============================================

    public static boolean insertTrip(BusBeacon beacon) {
        int startIndex = beacon.getBusStops().indexOf(beacon.getOrigin());

        if (startIndex == -1) {
            Utils.throwTripError(sContext, "Trip " + beacon.getId() + " startIndex == -1");
            return false;
        }

        // Save the beacon trip list to a temporary list.
        List<Integer> stops = new ArrayList<>(beacon.getBusStops());
        beacon.getBusStops().clear();

        // Check if the start index is not bigger that the size of the list, so we can sub-list
        // it without crash.
        if (startIndex > stops.size()) {
            Utils.throwTripError(sContext, "Trip " + beacon.getId() + " startIndex > stops.size");
            return false;
        }

        // Get the stops from the start index till the end of the list.
        stops = stops.subList(startIndex, stops.size());

        int stopIndex = stops.indexOf(beacon.getDestination());

        // Check if the end index is bigger than 0, thus it exists in the list.
        if (stopIndex < 0) {
            Utils.throwTripError(sContext, "Trip " + beacon.getId() + " stopIndex < 0");
            return false;
        }

        // Get the stops from the start index till the end index.
        int endIndex = stopIndex + 1 > stops.size() ? stops.size() : stopIndex + 1;
        List<Integer> stopList = stops.subList(0, endIndex);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < stopList.size(); i++) {
            sb.append(stopList.get(i)).append(',');
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        } else {
            Utils.throwTripError(sContext, "Trip " + beacon.getId() + " invalid -> sb.length() == 0\n\n" +
                    "list: " + Arrays.toString(beacon.getBusStops().toArray()) + "\n\n" +
                    "start: " + beacon.getOrigin() + '\n' +
                    "stop: " + beacon.getDestination());

            return false;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Trip trip = realm.createObject(Trip.class);
        trip.setHash(beacon.getHash());
        trip.setLine(beacon.getLineId());
        trip.setVehicle(beacon.getId());
        trip.setVariant(beacon.getVariant());
        trip.setTrip(beacon.getTripId());
        trip.setOrigin(beacon.getOrigin());
        trip.setDestination(beacon.getDestination());
        trip.setDeparture(beacon.getStartDate().getTime() / 1000);
        trip.setArrival(beacon.getLastSeen() / 1000);
        trip.setPath(sb.toString());
        trip.setFuelPrice(beacon.getFuelPrice());

        realm.commitTransaction();
        realm.close();

        LogUtils.e(TAG, "Inserted trip " + beacon.getHash());

        return true;
    }

    public static void insertTrip(CloudTrip cloudTrip) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Trip trip = realm.createObject(Trip.class);
        trip.setHash(cloudTrip.getHash());
        trip.setLine(cloudTrip.getLine());
        trip.setVehicle(cloudTrip.getVehicle());
        trip.setVariant(cloudTrip.getVariant());
        trip.setTrip(cloudTrip.getTrip());
        trip.setOrigin(cloudTrip.getOrigin());
        trip.setDestination(cloudTrip.getDestination());
        trip.setDeparture(cloudTrip.getDeparture());
        trip.setArrival(cloudTrip.getArrival());
        trip.setPath(Utils.listToString(cloudTrip.getPath(), ","));
        trip.setFuelPrice(cloudTrip.getDieselPrice());

        realm.commitTransaction();
        realm.close();

        LogUtils.e(TAG, "Inserted trip " + cloudTrip.getHash());
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

        LogUtils.w(TAG, "Added beacon " + major + " to realm");
    }
}
