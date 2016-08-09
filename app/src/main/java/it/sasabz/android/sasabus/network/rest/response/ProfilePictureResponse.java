package it.sasabz.android.sasabus.network.rest.response;

import java.util.List;

public class ProfilePictureResponse {

    public String directory;

    public List<String> pictures;

    @Override
    public String toString() {
        return "ProfilePictureResponse{" +
                "directory='" + directory + '\'' +
                ", pictures=" + pictures +
                '}';
    }
}
