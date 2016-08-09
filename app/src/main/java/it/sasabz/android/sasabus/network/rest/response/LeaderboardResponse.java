package it.sasabz.android.sasabus.network.rest.response;

import java.util.List;

import it.sasabz.android.sasabus.network.rest.model.LeaderboardPlayer;

public class LeaderboardResponse {

    public List<LeaderboardPlayer> leaderboard;

    @Override
    public String toString() {
        return "LeaderboardPlayer{" +
                "leaderboard=" + leaderboard +
                '}';
    }
}
