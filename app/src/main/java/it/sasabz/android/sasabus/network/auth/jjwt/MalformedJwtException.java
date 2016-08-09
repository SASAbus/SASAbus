package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception indicating that a JWT was not correctly constructed and should be rejected.
 *
 * @since 0.2
 */
public class MalformedJwtException extends JwtException {

    private static final long serialVersionUID = -7660779959294065799L;

    public MalformedJwtException(String message) {
        super(message);
    }
}
