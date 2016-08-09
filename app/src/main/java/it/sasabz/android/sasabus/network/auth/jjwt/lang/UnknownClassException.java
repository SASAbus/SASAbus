package it.sasabz.android.sasabus.network.auth.jjwt.lang;

/**
 * A {@code RuntimeException} equivalent of the JDK's
 * {@code ClassNotFoundException}, to maintain a RuntimeException paradigm.
 *
 * @since 0.1
 */
class UnknownClassException extends RuntimeException {

    private static final long serialVersionUID = 5981680767722677726L;

    /**
     * Constructs a new UnknownClassException.
     *
     * @param message the reason for the exception
     */
    UnknownClassException(String message) {
        super(message);
    }
}