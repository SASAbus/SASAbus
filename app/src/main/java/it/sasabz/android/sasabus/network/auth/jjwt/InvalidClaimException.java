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
 * Exception indicating a parsed claim is invalid in some way.  Subclasses reflect the specific
 * reason the claim is invalid.
 *
 * @see IncorrectClaimException
 * @see MissingClaimException
 * @since 0.6
 */
class InvalidClaimException extends ClaimJwtException {

    private static final long serialVersionUID = 7125550855747734142L;

    InvalidClaimException(Header header, Claims claims, String message) {
        super(header, claims, message);
    }
}
