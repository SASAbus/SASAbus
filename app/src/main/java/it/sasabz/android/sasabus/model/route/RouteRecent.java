package it.sasabz.android.sasabus.model.route;

public class RouteRecent {

    private final int id;

    private String originName;
    private String originMunic;
    private final int originId;

    private String destinationName;
    private String destinationMunic;
    private final int destinationId;

    public RouteRecent(int id, int originId, String originMunic,
                       int destinationId, String destinationMunic) {

        this.id = id;

        this.originId = originId;
        this.originMunic = originMunic;
        this.destinationId = destinationId;
        this.destinationMunic = destinationMunic;
    }

    public int getId() {
        return id;
    }

    public String getOriginMunic() {
        return originMunic;
    }

    public String getDestinationMunic() {
        return destinationMunic;
    }

    public int getOriginId() {
        return originId;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public void setOriginMunic(String originMunic) {
        this.originMunic = originMunic;
    }

    public void setDestinationMunic(String destinationMunic) {
        this.destinationMunic = destinationMunic;
    }
}