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
 * Exception thrown when discovering that a required claim is not present, indicating the JWT is
 * invalid and may not be used.
 *
 * @since 0.6
 */
public class MissingClaimException extends InvalidClaimException {
    private static final long serialVersionUID = -6887653791246732790L;

    public MissingClaimException(Header header, Claims claims, String message) {
        super(header, claims, message);
    }
}
