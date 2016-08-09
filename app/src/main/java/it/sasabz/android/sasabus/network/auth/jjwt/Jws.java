package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * An expanded (not compact/serialized) Signed JSON Web Token.
 *
 * @param <B> the type of the JWS body contents, either a String or a {@link Claims} instance.
 * @since 0.1
 */
public interface Jws<B> extends Jwt<JwsHeader, B> {

    String getSignature();
}
