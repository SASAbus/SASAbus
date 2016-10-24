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

package it.sasabz.android.sasabus.data.network.auth.jjwt;

/**
 * A <a href="https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-31">JWS</a> header.
 *
 * @param <T> header type
 * @since 0.1
 */
public interface JwsHeader<T extends JwsHeader<T>> extends Header<T> {

    /**
     * JWS {@code Algorithm} header parameter name: {@code "alg"}
     */
    String ALGORITHM = "alg";

    /**
     * Returns the JWS <a href="https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-31#section-4.1.1">
     * {@code alg}</a> (algorithm) header value or {@code null} if not present.
     * <p>
     * <p>The algorithm header parameter identifies the cryptographic algorithm used to secure the JWS.  Consider
     * using {@link SignatureAlgorithm#forName(String) SignatureAlgorithm.forName} to convert this
     * string value to a type-safe enum instance.</p>
     *
     * @return the JWS {@code alg} header value or {@code null} if not present.  This will always be
     * {@code non-null} on validly constructed JWS instances, but could be {@code null} during construction.
     */
    String getAlgorithm();
}
