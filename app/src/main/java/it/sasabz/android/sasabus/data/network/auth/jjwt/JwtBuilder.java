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

import java.util.Date;
import java.util.Map;

/**
 * A builder for constructing JWTs.
 *
 * @since 0.1
 */
public interface JwtBuilder extends ClaimsMutator<JwtBuilder> {

    //replaces any existing header with the specified header.

    JwtBuilder setHeader(Header header);

    JwtBuilder setHeader(Map<String, Object> header);

    JwtBuilder setClaims(Claims claims);

    JwtBuilder setClaims(Map<String, Object> claims);

    /**
     * Sets the JWT Claims <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.1">
     * {@code iss}</a> (issuer) value.  A {@code null} value will remove the property from the Claims.
     * <p>
     * <p>This is a convenience method.  It will first ensure a Claims instance exists as the JWT body and then set
     * the Claims {@link Claims#setIssuer(String) issuer} field with the specified value.  This allows you to write
     * code like this:</p>
     * <p>
     * <pre>
     * String jwt = Jwts.builder().setIssuer("Joe").compact();
     * </pre>
     * <p>
     * <p>instead of this:</p>
     * <pre>
     * Claims claims = Jwts.claims().setIssuer("Joe");
     * String jwt = Jwts.builder().setClaims(claims).compact();
     * </pre>
     * <p>if desired.</p>
     *
     * @param iss the JWT {@code iss} value or {@code null} to remove the property from the Claims map.
     * @return the builder instance for method chaining.
     * @since 0.2
     */
    @Override
    //only for better/targeted JavaDoc
    JwtBuilder setIssuer(String iss);

    /**
     * Sets the JWT Claims <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.2">
     * {@code sub}</a> (subject) value.  A {@code null} value will remove the property from the Claims.
     * <p>
     * <p>This is a convenience method.  It will first ensure a Claims instance exists as the JWT body and then set
     * the Claims {@link Claims#setSubject(String) subject} field with the specified value.  This allows you to write
     * code like this:</p>
     * <p>
     * <pre>
     * String jwt = Jwts.builder().setSubject("Me").compact();
     * </pre>
     * <p>
     * <p>instead of this:</p>
     * <pre>
     * Claims claims = Jwts.claims().setSubject("Me");
     * String jwt = Jwts.builder().setClaims(claims).compact();
     * </pre>
     * <p>if desired.</p>
     *
     * @param sub the JWT {@code sub} value or {@code null} to remove the property from the Claims map.
     * @return the builder instance for method chaining.
     * @since 0.2
     */
    @Override
    //only for better/targeted JavaDoc
    JwtBuilder setSubject(String sub);

    /**
     * Sets the JWT Claims <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.3">
     * {@code aud}</a> (audience) value.  A {@code null} value will remove the property from the Claims.
     * <p>
     * <p>This is a convenience method.  It will first ensure a Claims instance exists as the JWT body and then set
     * the Claims {@link Claims#setAudience(String) audience} field with the specified value.  This allows you to write
     * code like this:</p>
     * <p>
     * <pre>
     * String jwt = Jwts.builder().setAudience("You").compact();
     * </pre>
     * <p>
     * <p>instead of this:</p>
     * <pre>
     * Claims claims = Jwts.claims().setSubject("You");
     * String jwt = Jwts.builder().setClaims(claims).compact();
     * </pre>
     * <p>if desired.</p>
     *
     * @param aud the JWT {@code aud} value or {@code null} to remove the property from the Claims map.
     * @return the builder instance for method chaining.
     * @since 0.2
     */
    @Override
    //only for better/targeted JavaDoc
    JwtBuilder setAudience(String aud);

    /**
     * Sets the JWT Claims <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.4">
     * {@code exp}</a> (expiration) value.  A {@code null} value will remove the property from the Claims.
     * <p>
     * <p>A JWT obtained after this timestamp should not be used.</p>
     * <p>
     * <p>This is a convenience method.  It will first ensure a Claims instance exists as the JWT body and then set
     * the Claims {@link Claims#setExpiration(java.util.Date) expiration} field with the specified value.  This allows
     * you to write code like this:</p>
     * <p>
     * <pre>
     * String jwt = Jwts.builder().setExpiration(new Date(System.currentTimeMillis() + 3600000)).compact();
     * </pre>
     * <p>
     * <p>instead of this:</p>
     * <pre>
     * Claims claims = Jwts.claims().setExpiration(new Date(System.currentTimeMillis() + 3600000));
     * String jwt = Jwts.builder().setClaims(claims).compact();
     * </pre>
     * <p>if desired.</p>
     *
     * @param exp the JWT {@code exp} value or {@code null} to remove the property from the Claims map.
     * @return the builder instance for method chaining.
     * @since 0.2
     */
    @Override
    //only for better/targeted JavaDoc
    JwtBuilder setExpiration(Date exp);

    /**
     * Sets the JWT Claims <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.5">
     * {@code nbf}</a> (not before) value.  A {@code null} value will remove the property from the Claims.
     * <p>
     * <p>A JWT obtained before this timestamp should not be used.</p>
     * <p>
     * <p>This is a convenience method.  It will first ensure a Claims instance exists as the JWT body and then set
     * the Claims {@link Claims#setNotBefore(java.util.Date) notBefore} field with the specified value.  This allows
     * you to write code like this:</p>
     * <p>
     * <pre>
     * String jwt = Jwts.builder().setNotBefore(new Date()).compact();
     * </pre>
     * <p>
     * <p>instead of this:</p>
     * <pre>
     * Claims claims = Jwts.claims().setNotBefore(new Date());
     * String jwt = Jwts.builder().setClaims(claims).compact();
     * </pre>
     * <p>if desired.</p>
     *
     * @param nbf the JWT {@code nbf} value or {@code null} to remove the property from the Claims map.
     * @return the builder instance for method chaining.
     * @since 0.2
     */
    @Override
    //only for better/targeted JavaDoc
    JwtBuilder setNotBefore(Date nbf);

    /**
     * Sets the JWT Claims <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.6">
     * {@code iat}</a> (issued at) value.  A {@code null} value will remove the property from the Claims.
     * <p>
     * <p>The value is the timestamp when the JWT was created.</p>
     * <p>
     * <p>This is a convenience method.  It will first ensure a Claims instance exists as the JWT body and then set
     * the Claims {@link Claims#setIssuedAt(java.util.Date) issuedAt} field with the specified value.  This allows
     * you to write code like this:</p>
     * <p>
     * <pre>
     * String jwt = Jwts.builder().setIssuedAt(new Date()).compact();
     * </pre>
     * <p>
     * <p>instead of this:</p>
     * <pre>
     * Claims claims = Jwts.claims().setIssuedAt(new Date());
     * String jwt = Jwts.builder().setClaims(claims).compact();
     * </pre>
     * <p>if desired.</p>
     *
     * @param iat the JWT {@code iat} value or {@code null} to remove the property from the Claims map.
     * @return the builder instance for method chaining.
     * @since 0.2
     */
    @Override
    //only for better/targeted JavaDoc
    JwtBuilder setIssuedAt(Date iat);

    /**
     * Sets the JWT Claims <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.7">
     * {@code jti}</a> (JWT ID) value.  A {@code null} value will remove the property from the Claims.
     * <p>
     * <p>The value is a CaSe-SenSiTiVe unique identifier for the JWT. If specified, this value MUST be assigned in a
     * manner that ensures that there is a negligible probability that the same value will be accidentally
     * assigned to a different data object.  The ID can be used to prevent the JWT from being replayed.</p>
     * <p>
     * <p>This is a convenience method.  It will first ensure a Claims instance exists as the JWT body and then set
     * the Claims {@link Claims#setId(String) id} field with the specified value.  This allows
     * you to write code like this:</p>
     * <p>
     * <pre>
     * String jwt = Jwts.builder().setId(UUID.randomUUID().toString()).compact();
     * </pre>
     * <p>
     * <p>instead of this:</p>
     * <pre>
     * Claims claims = Jwts.claims().setIssuedAt(UUID.randomUUID().toString());
     * String jwt = Jwts.builder().setClaims(claims).compact();
     * </pre>
     * <p>if desired.</p>
     *
     * @param jti the JWT {@code jti} (id) value or {@code null} to remove the property from the Claims map.
     * @return the builder instance for method chaining.
     * @since 0.2
     */
    @Override
    //only for better/targeted JavaDoc
    JwtBuilder setId(String jti);
}
