package it.sasabz.android.sasabus.network.auth.jjwt;

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
