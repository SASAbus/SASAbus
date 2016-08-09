package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * ClaimJwtException is a subclass of the {@link JwtException} that is thrown after a validation of an JTW claim failed.
 *
 * @since 0.5
 */
public abstract class ClaimJwtException extends JwtException {

    public static final String INCORRECT_EXPECTED_CLAIM_MESSAGE_TEMPLATE = "Expected %s claim to be: %s, but was: %s.";
    public static final String MISSING_EXPECTED_CLAIM_MESSAGE_TEMPLATE = "Expected %s claim to be: %s, but was not present in the JWT claims.";
    private static final long serialVersionUID = -7140271324575155881L;

    private final Header<?> header;

    private final Claims claims;

    ClaimJwtException(Header<?> header, Claims claims, String message) {
        super(message);
        this.header = header;
        this.claims = claims;
    }

    public Claims getClaims() {
        return claims;
    }

    public Header<?> getHeader() {
        return header;
    }
}
