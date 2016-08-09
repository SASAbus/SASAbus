package it.sasabz.android.sasabus.network.rest.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    private int status;

    public String error;

    @SerializedName("error_message")
    public String errorMessage;

    public String param;

    @SerializedName("access_token")
    public String token;

    public boolean success;

    @Override
    public String toString() {
        return "LoginResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", param='" + param + '\'' +
                ", token='" + token + '\'' +
                ", success=" + success +
                '}';
    }
}
