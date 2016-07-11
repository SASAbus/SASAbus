package it.sasabz.android.sasabus.network.rest.response;

import java.util.List;

public class PathResponse {

    public List<Integer> path;

    @Override
    public String toString() {
        return "PathResponse{" +
                "path=" + path +
                '}';
    }
}
