package it.sasabz.android.sasabus.network.rest.response;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

    private int status;

    public String error;

    @SerializedName("error_message")
    public String errorMessage;

    public String param;

    public boolean success;

    @Override
    public String toString() {
        return "RegisterResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", param='" + param + '\'' +
                ", success=" + success +
                '}';
    }
}
