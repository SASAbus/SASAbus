package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception indicating that a JWT was accepted after it expired and must be rejected.
 *
 * @since 0.3
 */
public class ExpiredJwtException extends ClaimJwtException {

    private static final long serialVersionUID = -4600140824067812241L;

    public ExpiredJwtException(Header header, Claims claims, String message) {
        super(header, claims, message);
    }
}
