package it.sasabz.android.sasabus.network.rest.response;

import com.google.gson.annotations.SerializedName;

public class PasswordResponse {

    private int status;

    public String error;

    @SerializedName("error_message")
    public String errorMessage;

    public String param;

    public boolean success;

    @SerializedName("access_token")
    public String token;

    @Override
    public String toString() {
        return "PasswordResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", param='" + param + '\'' +
                ", success=" + success +
                '}';
    }
}
