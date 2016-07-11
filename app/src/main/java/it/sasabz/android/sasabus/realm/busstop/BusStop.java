package it.sasabz.android.sasabus.realm.busstop;

import android.content.Context;

import io.realm.RealmObject;

/**
 * Holds all the bus stops which the SASA Spa-AG operates. Most of the time the bus stops
 * will be grouped by their {@link #family}, which is a unique identifier for each bus stop
 * with the same name and municipality.
 *
 * @author Alex Lardschneider
 */
public class BusStop extends RealmObject {

    private int id;
    private int family;

    private String nameDe;
    private String nameIt;
    private String municDe;
    private String municIt;

    private float lat;
    private float lng;

    public BusStop() {
    }

    public BusStop(int id, String name, String munic, float lat, float lng, int family) {
        this.id = id;
        nameDe = name;
        nameIt = name;
        municDe = munic;
        municIt = munic;
        this.lat = lat;
        this.lng = lng;
        this.family = family;
    }

    public int getId() {
        return id;
    }

    public String getName(Context context) {
        String locale = context.getResources().getConfiguration().locale.toString();
        return locale.contains("de") ? nameDe : nameIt;
    }

    public String getNameDe() {
        return nameDe;
    }

    public String getNameIt() {
        return nameIt;
    }

    public CharSequence getMunic(Context context) {
        String locale = context.getResources().getConfiguration().locale.toString();
        return locale.contains("de") ? municDe : municIt;
    }

    public String getMunicDe() {
        return municDe;
    }

    public String getMunicIt() {
        return municIt;
    }

    public float getLat() {
        return lat;
    }

    public float getLng() {
        return lng;
    }

    public int getFamily() {
        return family;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public void setFamily(int family) {
        this.family = family;
    }

    public void setNameDe(String nameDe) {
        this.nameDe = nameDe;
    }

    public void setNameIt(String nameIt) {
        this.nameIt = nameIt;
    }

    public void setMunicDe(String municDe) {
        this.municDe = municDe;
    }

    public void setMunicIt(String municIt) {
        this.municIt = municIt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusStop busStop = (BusStop) o;

        return family == busStop.family;
    }

    @Override
    public int hashCode() {
        return family;
    }
}
