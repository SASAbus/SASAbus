package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * An expanded (not compact/serialized) JSON Web Token.
 *
 * @param <B> the type of the JWT body contents, either a String or a {@link Claims} instance.
 * @since 0.1
 */
public interface Jwt<H extends Header, B> {

    /**
     * Returns the JWT {@link Header} or {@code null} if not present.
     *
     * @return the JWT {@link Header} or {@code null} if not present.
     */
    H getHeader();

    /**
     * Returns the JWT body, either a {@code String} or a {@code Claims} instance.
     *
     * @return the JWT body, either a {@code String} or a {@code Claims} instance.
     */
    B getBody();
}
