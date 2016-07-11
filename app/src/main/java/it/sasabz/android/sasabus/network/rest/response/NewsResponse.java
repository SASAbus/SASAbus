package it.sasabz.android.sasabus.network.rest.response;

import java.util.List;

import it.sasabz.android.sasabus.model.News;

public class NewsResponse {

    public List<News> news;

    @Override
    public String toString() {
        return "NewsResponse{" +
                "items=" + news +
                '}';
    }
}
