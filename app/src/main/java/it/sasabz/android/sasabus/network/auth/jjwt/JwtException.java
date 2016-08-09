package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Base class for JWT-related runtime exceptions.
 *
 * @since 0.1
 */
class JwtException extends RuntimeException {

    private static final long serialVersionUID = 6949451791970082995L;

    JwtException(String message) {
        super(message);
    }

    JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}