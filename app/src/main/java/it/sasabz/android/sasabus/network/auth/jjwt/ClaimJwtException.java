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
 * ClaimJwtException is a subclass of the {@link JwtException} that is thrown after a validation of an JTW claim failed.
 *
 * @since 0.5
 */
public abstract class ClaimJwtException extends JwtException {

    public static final String INCORRECT_EXPECTED_CLAIM_MESSAGE_TEMPLATE = "Expected %s claim to be: %s, but was: %s.";
    public static final String MISSING_EXPECTED_CLAIM_MESSAGE_TEMPLATE = "Expected %s claim to be: %s, but was not present in the JWT claims.";
    private static final long serialVersionUID = -7140271324575155881L;

    private final Header<?> header;

    private final Claims claims;

    ClaimJwtException(Header<?> header, Claims claims, String message) {
        super(message);
        this.header = header;
        this.claims = claims;
    }

    public Claims getClaims() {
        return claims;
    }

    public Header<?> getHeader() {
        return header;
    }
}
