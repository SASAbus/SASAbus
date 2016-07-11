package it.sasabz.android.sasabus.network.rest.api;

import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.util.ReportHelper;

import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

public interface ReportApi {

    String TYPE_DEFAULT = "default";
    String TYPE_BUS = "bus";

    @Multipart
    @POST(Endpoint.REPORT)
    Observable<Void> send(@Path("type") String type, @Part("body") ReportHelper.ReportBody body,
                          @Part("image") RequestBody image);

    @Multipart
    @POST(Endpoint.REPORT)
    Observable<Void> sendNoImage(@Path("type") String type, @Part("body") ReportHelper.ReportBody body);
}