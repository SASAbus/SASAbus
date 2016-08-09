package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception thrown when discovering that a required claim does not equal the required value, indicating the JWT is
 * invalid and may not be used.
 *
 * @since 0.6
 */
public class IncorrectClaimException extends InvalidClaimException {

    private static final long serialVersionUID = 1108704169682900519L;

    public IncorrectClaimException(Header header, Claims claims, String message) {
        super(header, claims, message);
    }
}
