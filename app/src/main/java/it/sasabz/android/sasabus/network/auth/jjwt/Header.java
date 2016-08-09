package it.sasabz.android.sasabus.network.auth.jjwt;

import java.util.Map;

/**
 * A JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-5">JOSE header</a>.
 * <p>
 * <p>This is ultimately a JSON map and any values can be added to it, but JWT JOSE standard names are provided as
 * type-safe getters and setters for convenience.</p>
 * <p>
 * <p>Because this interface extends {@code Map&lt;String, Object&gt;}, if you would like to add your own properties,
 * you simply use map methods, for example:</p>
 * <p>
 * <pre>
 * header.{@link Map#put(Object, Object) put}("headerParamName", "headerParamValue");
 * </pre>
 * <p>
 * <h4>Creation</h4>
 * <p>
 * <p>It is easiest to create a {@code Header} instance by calling one of the
 * {@link Jwts#header() JWTs.header()} factory methods.</p>
 *
 * @since 0.1
 */
public interface Header<T extends Header<T>> extends Map<String, Object> {

    /**
     * JWT {@code Type} header parameter name: {@code "typ"}
     */
    String TYPE = "typ";

    /**
     * Returns the <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-5.1">
     * {@code typ}</a> (type) header value or {@code null} if not present.
     *
     * @return the {@code typ} header value or {@code null} if not present.
     */
    String getType();

    /**
     * Sets the JWT <a href="https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-5.1">
     * {@code typ}</a> (Type) header value.  A {@code null} value will remove the property from the JSON map.
     *
     * @param typ the JWT JOSE {@code typ} header value or {@code null} to remove the property from the JSON map.
     * @return the {@code Header} instance for method chaining.
     */
    T setType(String typ);
}
