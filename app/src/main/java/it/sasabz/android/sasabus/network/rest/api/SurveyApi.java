package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.beacon.survey.SurveyActivity;
import it.sasabz.android.sasabus.network.rest.Endpoint;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface SurveyApi {

    @POST(Endpoint.SURVEY)
    Observable<Void> send(@Body SurveyActivity.ReportBody body);
}