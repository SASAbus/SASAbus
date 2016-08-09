package it.sasabz.android.sasabus.network.rest.response;

import java.util.List;

import it.sasabz.android.sasabus.network.rest.model.Badge;

public class BadgesResponse {

    public List<Badge> badges;

    @Override
    public String toString() {
        return "BadgesResponse{" +
                "badges=" + badges +
                '}';
    }
}
