package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception indicating that a JWT was accepted before it is allowed to be accessed and must be rejected.
 *
 * @since 0.3
 */
public class PrematureJwtException extends ClaimJwtException {

    private static final long serialVersionUID = -1550517982052942761L;

    public PrematureJwtException(Header header, Claims claims, String message) {
        super(header, claims, message);
    }
}
