package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception thrown when receiving a JWT in a particular format/configuration that does not match the format expected
 * by the application.
 * <p>
 * <p>For example, this exception would be thrown if parsing an unsigned plaintext JWT when the application
 * requires a cryptographically signed Claims JWS instead.</p>
 *
 * @since 0.2
 */
public class UnsupportedJwtException extends JwtException {

    private static final long serialVersionUID = 774455941376493179L;

    UnsupportedJwtException(String message) {
        super(message);
    }

    public UnsupportedJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
