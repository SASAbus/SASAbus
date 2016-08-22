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

import java.security.Key;

/**
 * A {@code SigningKeyResolver} can be used by a {@link JwtParser JwtParser} to find a signing key that
 * should be used to verify a JWS signature.
 * <p>
 * <p>A {@code SigningKeyResolver} is necessary when the signing key is not already known before parsing the JWT and the
 * JWT header or payload (plaintext body or Claims) must be inspected first to determine how to look up the signing key.
 * Once returned by the resolver, the JwtParser will then verify the JWS signature with the returned key.  For
 * example:</p>
 * <p>
 * <pre>
 * Jws&lt;Claims&gt; jws = Jwts.parser().setSigningKeyResolver(new SigningKeyResolverAdapter() {
 *         &#64;Override
 *         public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
 *             //inspect the header or claims, lookup and return the signing key
 *             return getSigningKeyBytes(header, claims); //implement me
 *         }})
 *     .parseClaimsJws(compact);
 * </pre>
 * <p>
 * <p>A {@code SigningKeyResolver} is invoked once during parsing before the signature is verified.</p>
 * <p>
 * <h4>SigningKeyResolverAdapter</h4>
 * <p>
 * <p>If you only need to resolve a signing key for a particular JWS (either a plaintext or Claims JWS), consider using
 * the {@link SigningKeyResolverAdapter} and overriding only the method you need to support instead of
 * implementing this interface directly.</p>
 *
 * @see SigningKeyResolverAdapter
 * @since 0.4
 */
public interface SigningKeyResolver {

    /**
     * Returns the signing key that should be used to validate a digital signature for the Claims JWS with the specified
     * header and claims.
     *
     * @param header the header of the JWS to validate
     * @param claims the claims (body) of the JWS to validate
     * @return the signing key that should be used to validate a digital signature for the Claims JWS with the specified
     * header and claims.
     */
    Key resolveSigningKey(JwsHeader<?> header, Claims claims);

    /**
     * Returns the signing key that should be used to validate a digital signature for the Plaintext JWS with the
     * specified header and plaintext payload.
     *
     * @param header    the header of the JWS to validate
     * @param plaintext the plaintext body of the JWS to validate
     * @return the signing key that should be used to validate a digital signature for the Plaintext JWS with the
     * specified header and plaintext payload.
     */
    Key resolveSigningKey(JwsHeader<?> header, String plaintext);
}
