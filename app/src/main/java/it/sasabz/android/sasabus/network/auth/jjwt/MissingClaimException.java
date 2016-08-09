package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception thrown when discovering that a required claim is not present, indicating the JWT is
 * invalid and may not be used.
 *
 * @since 0.6
 */
public class MissingClaimException extends InvalidClaimException {
    private static final long serialVersionUID = -6887653791246732790L;

    public MissingClaimException(Header header, Claims claims, String message) {
        super(header, claims, message);
    }
}
