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

import javax.crypto.spec.SecretKeySpec;

import it.sasabz.android.sasabus.network.auth.jjwt.lang.Assert;

/**
 * An <a href="http://en.wikipedia.org/wiki/Adapter_pattern">Adapter</a> implementation of the
 * {@link SigningKeyResolver} interface that allows subclasses to process only the type of JWS body that
 * is known/expected for a particular case.
 * <p>
 * <p>The {@link #resolveSigningKey(JwsHeader, Claims)} and {@link #resolveSigningKey(JwsHeader, String)} method
 * implementations delegate to the
 * {@link #resolveSigningKeyBytes(JwsHeader, Claims)} and {@link #resolveSigningKeyBytes(JwsHeader, String)} methods
 * respectively.  The latter two methods simply throw exceptions:  they represent scenarios expected by
 * calling code in known situations, and it is expected that you override the implementation in those known situations;
 * non-overridden *KeyBytes methods indicates that the JWS input was unexpected.</p>
 * <p>
 * <p>If either {@link #resolveSigningKey(JwsHeader, String)} or {@link #resolveSigningKey(JwsHeader, Claims)}
 * are not overridden, one (or both) of the *KeyBytes variants must be overridden depending on your expected
 * use case.  You do not have to override any method that does not represent an expected condition.</p>
 *
 * @since 0.4
 */
class SigningKeyResolverAdapter implements SigningKeyResolver {

    @Override
    public Key resolveSigningKey(JwsHeader<?> header, Claims claims) {
        SignatureAlgorithm alg = SignatureAlgorithm.forName(header.getAlgorithm());
        Assert.isTrue(false, "The default resolveSigningKey(JwsHeader, Claims) implementation cannot be " +
                "used for asymmetric key algorithms (RSA, Elliptic Curve).  " +
                "Override the resolveSigningKey(JwsHeader, Claims) method instead and return a " +
                "Key instance appropriate for the " + alg.name() + " algorithm.");
        byte[] keyBytes = resolveSigningKeyBytes(header, claims);
        return new SecretKeySpec(keyBytes, alg.getJcaName());
    }

    @Override
    public Key resolveSigningKey(JwsHeader<?> header, String plaintext) {
        SignatureAlgorithm alg = SignatureAlgorithm.forName(header.getAlgorithm());
        Assert.isTrue(false, "The default resolveSigningKey(JwsHeader, String) implementation cannot be " +
                "used for asymmetric key algorithms (RSA, Elliptic Curve).  " +
                "Override the resolveSigningKey(JwsHeader, String) method instead and return a " +
                "Key instance appropriate for the " + alg.name() + " algorithm.");
        byte[] keyBytes = resolveSigningKeyBytes(header, plaintext);
        return new SecretKeySpec(keyBytes, alg.getJcaName());
    }

    /**
     * Convenience method invoked by {@link #resolveSigningKey(JwsHeader, Claims)} that obtains the necessary signing
     * key bytes.  This implementation simply throws an exception: if the JWS parsed is a Claims JWS, you must
     * override this method or the {@link #resolveSigningKey(JwsHeader, Claims)} method instead.
     * <p>
     * <p><b>NOTE:</b> You cannot override this method when validating RSA signatures.  If you expect RSA signatures,
     * you must override the {@link #resolveSigningKey(JwsHeader, Claims)} method instead.</p>
     *
     * @param header the parsed {@link JwsHeader}
     * @param claims the parsed {@link Claims}
     * @return the signing key bytes to use to verify the JWS signature.
     */
    private byte[] resolveSigningKeyBytes(JwsHeader<?> header, Claims claims) {
        throw new UnsupportedJwtException("The specified SigningKeyResolver implementation does not support " +
                "Claims JWS signing key resolution.  Consider overriding either the " +
                "resolveSigningKey(JwsHeader, Claims) method or, for HMAC algorithms, the " +
                "resolveSigningKeyBytes(JwsHeader, Claims) method.");
    }

    /**
     * Convenience method invoked by {@link #resolveSigningKey(JwsHeader, String)} that obtains the necessary signing
     * key bytes.  This implementation simply throws an exception: if the JWS parsed is a plaintext JWS, you must
     * override this method or the {@link #resolveSigningKey(JwsHeader, String)} method instead.
     *
     * @param header  the parsed {@link JwsHeader}
     * @param payload the parsed String plaintext payload
     * @return the signing key bytes to use to verify the JWS signature.
     */
    private byte[] resolveSigningKeyBytes(JwsHeader<?> header, String payload) {
        throw new UnsupportedJwtException("The specified SigningKeyResolver implementation does not support " +
                "plaintext JWS signing key resolution.  Consider overriding either the " +
                "resolveSigningKey(JwsHeader, String) method or, for HMAC algorithms, the " +
                "resolveSigningKeyBytes(JwsHeader, String) method.");
    }
}
