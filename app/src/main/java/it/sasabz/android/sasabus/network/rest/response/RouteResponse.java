package it.sasabz.android.sasabus.network.rest.response;

import it.sasabz.android.sasabus.network.rest.model.Route;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RouteResponse {

    @SerializedName("status")
    public int status;

    @SerializedName("routes")
    public List<Route> routes;
}