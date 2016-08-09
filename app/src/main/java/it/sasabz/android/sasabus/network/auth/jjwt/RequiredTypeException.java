package it.sasabz.android.sasabus.network.auth.jjwt;

/**
 * Exception thrown when {@link Claims#get(String, Class)} is called and the value does not match the type of the
 * {@code Class} argument.
 *
 * @since 0.6
 */
public class RequiredTypeException extends JwtException {
    private static final long serialVersionUID = -5248183277332272778L;

    public RequiredTypeException(String message) {
        super(message);
    }
}
