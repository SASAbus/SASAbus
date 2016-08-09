package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception indicating that either calculating a signature or verifying an existing signature of a JWT failed.
 *
 * @since 0.1
 */
public class SignatureException extends JwtException {

    private static final long serialVersionUID = -3601061268460083658L;

    public SignatureException(String message) {
        super(message);
    }

    public SignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
