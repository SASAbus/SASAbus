package it.sasabz.android.sasabus.realm.busstop;

import android.content.Context;

import io.realm.RealmObject;

/**
 * Holds all the bus stops which the SAD and the SASA Spa-AG operates. Those bus stops are used
 * to calculate the route, as the route planned only works with SAD bus stops and their ids.
 *
 * @author Alex Lardschneider
 */
public class SadBusStop extends RealmObject {

    private int id;

    private String nameDe;
    private String nameIt;
    private String municDe;
    private String municIt;

    private float lat;
    private float lng;

    public SadBusStop() {
    }

    public SadBusStop(int id, String name, String munic, float lat, float lng) {
        this.id = id;
        nameDe = name;
        nameIt = name;
        municDe = munic;
        municIt = munic;
        this.lat = lat;
        this.lng = lng;
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

    public int getId() {
        return id;
    }

    public CharSequence getName() {
        return nameDe;
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

    public String getMunic() {
        return municDe;
    }

    public String getMunic(Context context) {
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

    public void setId(int id) {
        this.id = id;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }
}
