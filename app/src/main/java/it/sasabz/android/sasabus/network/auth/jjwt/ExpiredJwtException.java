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
 * Exception indicating that a JWT was accepted after it expired and must be rejected.
 *
 * @since 0.3
 */
public class ExpiredJwtException extends ClaimJwtException {

    private static final long serialVersionUID = -4600140824067812241L;

    public ExpiredJwtException(Header header, Claims claims, String message) {
        super(header, claims, message);
    }
}
