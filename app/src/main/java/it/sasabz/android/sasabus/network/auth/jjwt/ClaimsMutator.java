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

import java.util.Date;

/**
 * Mutation (modifications) to a {@link Claims Claims} instance.
 *
 * @param <T> the type of mutator
 * @see JwtBuilder
 * @see Claims
 * @since 0.2
 */
interface ClaimsMutator<T extends ClaimsMutator> {

    /**
     * Sets the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.1">
     * {@code iss}</a> (issuer) value.  A {@code null} value will remove the property from the JSON map.
     *
     * @param iss the JWT {@code iss} value or {@code null} to remove the property from the JSON map.
     * @return the {@code Claims} instance for method chaining.
     */
    T setIssuer(String iss);

    /**
     * Sets the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.2">
     * {@code sub}</a> (subject) value.  A {@code null} value will remove the property from the JSON map.
     *
     * @param sub the JWT {@code sub} value or {@code null} to remove the property from the JSON map.
     * @return the {@code Claims} instance for method chaining.
     */
    T setSubject(String sub);

    /**
     * Sets the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.3">
     * {@code aud}</a> (audience) value.  A {@code null} value will remove the property from the JSON map.
     *
     * @param aud the JWT {@code aud} value or {@code null} to remove the property from the JSON map.
     * @return the {@code Claims} instance for method chaining.
     */
    T setAudience(String aud);

    /**
     * Sets the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.4">
     * {@code exp}</a> (expiration) timestamp.  A {@code null} value will remove the property from the JSON map.
     * <p>
     * <p>A JWT obtained after this timestamp should not be used.</p>
     *
     * @param exp the JWT {@code exp} value or {@code null} to remove the property from the JSON map.
     * @return the {@code Claims} instance for method chaining.
     */
    T setExpiration(Date exp);

    /**
     * Sets the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.5">
     * {@code nbf}</a> (not before) timestamp.  A {@code null} value will remove the property from the JSON map.
     * <p>
     * <p>A JWT obtained before this timestamp should not be used.</p>
     *
     * @param nbf the JWT {@code nbf} value or {@code null} to remove the property from the JSON map.
     * @return the {@code Claims} instance for method chaining.
     */
    T setNotBefore(Date nbf);

    /**
     * Sets the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.6">
     * {@code iat}</a> (issued at) timestamp.  A {@code null} value will remove the property from the JSON map.
     * <p>
     * <p>The value is the timestamp when the JWT was created.</p>
     *
     * @param iat the JWT {@code iat} value or {@code null} to remove the property from the JSON map.
     * @return the {@code Claims} instance for method chaining.
     */
    T setIssuedAt(Date iat);

    /**
     * Sets the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.7">
     * {@code jti}</a> (JWT ID) value.  A {@code null} value will remove the property from the JSON map.
     * <p>
     * <p>This value is a CaSe-SenSiTiVe unique identifier for the JWT. If specified, this value MUST be assigned in a
     * manner that ensures that there is a negligible probability that the same value will be accidentally
     * assigned to a different data object.  The ID can be used to prevent the JWT from being replayed.</p>
     *
     * @param jti the JWT {@code jti} value or {@code null} to remove the property from the JSON map.
     * @return the {@code Claims} instance for method chaining.
     */
    T setId(String jti);
}