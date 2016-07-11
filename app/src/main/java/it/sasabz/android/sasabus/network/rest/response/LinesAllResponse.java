package it.sasabz.android.sasabus.network.rest.response;

import java.util.List;

import it.sasabz.android.sasabus.network.rest.model.Line;

public class LinesAllResponse {

    public List<Line> lines;

    @Override
    public String toString() {
        return "LinesAllResponse{" +
                "lines=" + lines +
                '}';
    }
}
