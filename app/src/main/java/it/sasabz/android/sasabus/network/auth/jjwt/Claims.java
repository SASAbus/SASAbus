package it.sasabz.android.sasabus.network.auth.jjwt;

import java.util.Date;
import java.util.Map;

/**
 * A JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4">Claims set</a>.
 * <p>
 * <p>This is ultimately a JSON map and any values can be added to it, but JWT standard names are provided as
 * type-safe getters and setters for convenience.</p>
 * <p>
 * <p>Because this interface extends {@code Map&lt;String, Object&gt;}, if you would like to add your own properties,
 * you simply use map methods, for example:</p>
 * <p>
 * <pre>
 * claims.{@link Map#put(Object, Object) put}("someKey", "someValue");
 * </pre>
 * <p>
 * <h4>Creation</h4>
 * <p>
 * <p>It is easiest to create a {@code Claims} instance by calling one of the
 * {@link Jwts#claims() JWTs.claims()} factory methods.</p>
 *
 * @since 0.1
 */
public interface Claims extends Map<String, Object>, ClaimsMutator<Claims> {

    /**
     * JWT {@code Issuer} claims parameter name: {@code "iss"}
     */
    String ISSUER = "iss";

    /**
     * JWT {@code Subject} claims parameter name: {@code "sub"}
     */
    String SUBJECT = "sub";

    /**
     * JWT {@code Audience} claims parameter name: {@code "aud"}
     */
    String AUDIENCE = "aud";

    /**
     * JWT {@code Expiration} claims parameter name: {@code "exp"}
     */
    String EXPIRATION = "exp";

    /**
     * JWT {@code Not Before} claims parameter name: {@code "nbf"}
     */
    String NOT_BEFORE = "nbf";

    /**
     * JWT {@code Issued At} claims parameter name: {@code "iat"}
     */
    String ISSUED_AT = "iat";

    /**
     * JWT {@code JWT ID} claims parameter name: {@code "jti"}
     */
    String ID = "jti";

    @Override
        //only for better/targeted JavaDoc
    Claims setIssuer(String iss);

    /**
     * Returns the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.2">
     * {@code sub}</a> (subject) value or {@code null} if not present.
     *
     * @return the JWT {@code sub} value or {@code null} if not present.
     */
    String getSubject();

    @Override
        //only for better/targeted JavaDoc
    Claims setSubject(String sub);

    @Override
        //only for better/targeted JavaDoc
    Claims setAudience(String aud);

    /**
     * Returns the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.4">
     * {@code exp}</a> (expiration) timestamp or {@code null} if not present.
     * <p>
     * <p>A JWT obtained after this timestamp should not be used.</p>
     *
     * @return the JWT {@code exp} value or {@code null} if not present.
     */
    Date getExpiration();

    @Override
        //only for better/targeted JavaDoc
    Claims setExpiration(Date exp);

    /**
     * Returns the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.5">
     * {@code nbf}</a> (not before) timestamp or {@code null} if not present.
     * <p>
     * <p>A JWT obtained before this timestamp should not be used.</p>
     *
     * @return the JWT {@code nbf} value or {@code null} if not present.
     */
    Date getNotBefore();

    @Override
        //only for better/targeted JavaDoc
    Claims setNotBefore(Date nbf);

    @Override
        //only for better/targeted JavaDoc
    Claims setIssuedAt(Date iat);

    /**
     * Returns the JWTs <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.7">
     * {@code jti}</a> (JWT ID) value or {@code null} if not present.
     * <p>
     * <p>This value is a CaSe-SenSiTiVe unique identifier for the JWT. If available, this value is expected to be
     * assigned in a manner that ensures that there is a negligible probability that the same value will be
     * accidentally
     * assigned to a different data object.  The ID can be used to prevent the JWT from being replayed.</p>
     *
     * @return the JWT {@code jti} value or {@code null} if not present.
     */
    String getId();

    @Override
        //only for better/targeted JavaDoc
    Claims setId(String jti);

    <T> T get(String claimName, Class<T> requiredType);
}
