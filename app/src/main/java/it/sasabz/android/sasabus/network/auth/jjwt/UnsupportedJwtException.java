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

package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception thrown when receiving a JWT in a particular format/configuration that does not match the format expected
 * by the application.
 * <p>
 * <p>For example, this exception would be thrown if parsing an unsigned plaintext JWT when the application
 * requires a cryptographically signed Claims JWS instead.</p>
 *
 * @since 0.2
 */
public class UnsupportedJwtException extends JwtException {

    private static final long serialVersionUID = 774455941376493179L;

    UnsupportedJwtException(String message) {
        super(message);
    }

    public UnsupportedJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
