package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureAlgorithm;
import it.sasabz.android.sasabus.network.auth.jjwt.SignatureException;
import it.sasabz.android.sasabus.network.auth.jjwt.lang.Assert;
import it.sasabz.android.sasabus.network.auth.jjwt.lang.RuntimeEnvironment;

abstract class SignatureProvider {

    /**
     * JJWT's default SecureRandom number generator.  This RNG is initialized using the JVM default as follows:
     * <p>
     * <pre>{@code
     * static {
     *     DEFAULT_SECURE_RANDOM = new SecureRandom();
     *     DEFAULT_SECURE_RANDOM.nextBytes(new byte[64]);
     * }
     * }</pre>
     * <p>
     * <p>{@code nextBytes} is called to force the RNG to initialize itself if not already initialized.  The
     * byte array is not used and discarded immediately for garbage collection.</p>
     */
    static final SecureRandom DEFAULT_SECURE_RANDOM;

    static {
        DEFAULT_SECURE_RANDOM = new SecureRandom();
        DEFAULT_SECURE_RANDOM.nextBytes(new byte[64]);
    }

    final SignatureAlgorithm alg;
    final Key key;

    SignatureProvider(SignatureAlgorithm alg, Key key) {
        Assert.notNull(alg, "SignatureAlgorithm cannot be null.");
        Assert.notNull(key, "Key cannot be null.");
        this.alg = alg;
        this.key = key;
    }

    Signature createSignatureInstance() {
        try {
            return getSignatureInstance();
        } catch (NoSuchAlgorithmException e) {
            String msg = "Unavailable " + alg.getFamilyName() + " Signature algorithm '" + alg.getJcaName() + "'.";
            if (!alg.isJdkStandard() && !isBouncyCastleAvailable()) {
                msg += " This is not a standard JDK algorithm. Try including BouncyCastle in the runtime classpath.";
            }
            throw new SignatureException(msg, e);
        }
    }

    private Signature getSignatureInstance() throws NoSuchAlgorithmException {
        return Signature.getInstance(alg.getJcaName());
    }

    private boolean isBouncyCastleAvailable() {
        return RuntimeEnvironment.BOUNCY_CASTLE_AVAILABLE;
    }
}