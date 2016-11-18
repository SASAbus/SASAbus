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
 * A JwtHandler is invoked by a {@link JwtParser JwtParser} after parsing a JWT to indicate the exact
 * type of JWT or JWS parsed.
 *
 * @param <T> the type of object to return to the parser caller after handling the parsed JWT.
 * @since 0.2
 */
public interface JwtHandler<T> {

    /**
     * This method is invoked when a {@link JwtParser JwtParser} determines that the parsed JWT is
     * a plaintext JWT.  A plaintext JWT has a String (non-JSON) body payload and it is not cryptographically signed.
     *
     * @param jwt the parsed plaintext JWT
     * @return any object to be used after inspecting the JWT, or {@code null} if no return value is necessary.
     */
    T onPlaintextJwt(Jwt<Header, String> jwt);

    /**
     * This method is invoked when a {@link JwtParser JwtParser} determines that the parsed JWT is
     * a Claims JWT.  A Claims JWT has a {@link Claims} body and it is not cryptographically signed.
     *
     * @param jwt the parsed claims JWT
     * @return any object to be used after inspecting the JWT, or {@code null} if no return value is necessary.
     */
    T onClaimsJwt(Jwt<Header, Claims> jwt);

    /**
     * This method is invoked when a {@link JwtParser JwtParser} determines that the parsed JWT is
     * a plaintext JWS.  A plaintext JWS is a JWT with a String (non-JSON) body (payload) that has been
     * cryptographically signed.
     * <p>
     * <p>This method will only be invoked if the cryptographic signature can be successfully verified.</p>
     *
     * @param jws the parsed plaintext JWS
     * @return any object to be used after inspecting the JWS, or {@code null} if no return value is necessary.
     */
    T onPlaintextJws(Jws<String> jws);

    /**
     * This method is invoked when a {@link JwtParser JwtParser} determines that the parsed JWT is
     * a valid Claims JWS.  A Claims JWS is a JWT with a {@link Claims} body that has been cryptographically signed.
     * <p>
     * <p>This method will only be invoked if the cryptographic signature can be successfully verified.</p>
     *
     * @param jws the parsed claims JWS
     * @return any object to be used after inspecting the JWS, or {@code null} if no return value is necessary.
     */
    T onClaimsJws(Jws<Claims> jws);
}
