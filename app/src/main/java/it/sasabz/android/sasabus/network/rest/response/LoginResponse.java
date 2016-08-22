/*
 * Copyright (C) 2016 David Dejori, Alex Lardschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
