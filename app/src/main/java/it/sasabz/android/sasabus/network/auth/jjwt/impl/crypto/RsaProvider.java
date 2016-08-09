package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.HashMap;
import java.util.Map;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureAlgorithm;
import it.sasabz.android.sasabus.network.auth.jjwt.SignatureException;
import it.sasabz.android.sasabus.network.auth.jjwt.lang.Assert;

abstract class RsaProvider extends SignatureProvider {

    private static final Map<SignatureAlgorithm, PSSParameterSpec> PSS_PARAMETER_SPECS = createPssParameterSpecs();

    private static Map<SignatureAlgorithm, PSSParameterSpec> createPssParameterSpecs() {

        Map<SignatureAlgorithm, PSSParameterSpec> m = new HashMap<>();

        MGF1ParameterSpec ps = MGF1ParameterSpec.SHA256;
        PSSParameterSpec spec = new PSSParameterSpec(ps.getDigestAlgorithm(), "MGF1", ps, 32, 1);
        m.put(SignatureAlgorithm.PS256, spec);

        ps = MGF1ParameterSpec.SHA384;
        spec = new PSSParameterSpec(ps.getDigestAlgorithm(), "MGF1", ps, 48, 1);
        m.put(SignatureAlgorithm.PS384, spec);

        ps = MGF1ParameterSpec.SHA512;
        spec = new PSSParameterSpec(ps.getDigestAlgorithm(), "MGF1", ps, 64, 1);
        m.put(SignatureAlgorithm.PS512, spec);

        return m;
    }

    RsaProvider(SignatureAlgorithm alg, Key key) {
        super(alg, key);
        Assert.isTrue(alg.isRsa(), "SignatureAlgorithm must be an RSASSA or RSASSA-PSS algorithm.");
    }

    @Override
    Signature createSignatureInstance() {

        Signature sig = super.createSignatureInstance();

        PSSParameterSpec spec = PSS_PARAMETER_SPECS.get(alg);
        if (spec != null) {
            setParameter(sig, spec);
        }
        return sig;
    }

    private void setParameter(Signature sig, AlgorithmParameterSpec spec) {
        try {
            doSetParameter(sig, spec);
        } catch (InvalidAlgorithmParameterException e) {
            String msg = "Unsupported RSASSA-PSS parameter '" + spec + "': " + e.getMessage();
            throw new SignatureException(msg, e);
        }
    }

    private void doSetParameter(Signature sig, AlgorithmParameterSpec spec) throws InvalidAlgorithmParameterException {
        sig.setParameter(spec);
    }

    /**
     * Generates a new RSA secure-randomly key pair of the specified size using JJWT's default {@link
     * SignatureProvider#DEFAULT_SECURE_RANDOM SecureRandom instance}.  This is a convenience method that immediately
     * delegates to {@link #generateKeyPair(int, SecureRandom)}.
     *
     * @param keySizeInBits the key size in bits (<em>NOT bytes</em>).
     * @return a new RSA secure-random key pair of the specified size.
     * @see #generateKeyPair(int, SecureRandom)
     * @see #generateKeyPair(String, int, SecureRandom)
     * @since 0.5
     */
    private static KeyPair generateKeyPair(int keySizeInBits) {
        return generateKeyPair(keySizeInBits, SignatureProvider.DEFAULT_SECURE_RANDOM);
    }

    /**
     * Generates a new RSA secure-random key pair of the specified size using the given SecureRandom number generator.
     * This is a convenience method that immediately delegates to {@link #generateKeyPair(String, int, SecureRandom)}
     * using {@code RSA} as the {@code jcaAlgorithmName} argument.
     *
     * @param keySizeInBits the key size in bits (<em>NOT bytes</em>)
     * @param random        the secure random number generator to use during key generation.
     * @return a new RSA secure-random key pair of the specified size using the given SecureRandom number generator.
     * @see #generateKeyPair(int)
     * @see #generateKeyPair(String, int, SecureRandom)
     * @since 0.5
     */
    private static KeyPair generateKeyPair(int keySizeInBits, SecureRandom random) {
        return generateKeyPair("RSA", keySizeInBits, random);
    }

    /**
     * Generates a new secure-random key pair of the specified size using the specified SecureRandom according to the
     * specified {@code jcaAlgorithmName}.
     *
     * @param jcaAlgorithmName the name of the JCA algorithm to use for key pair generation, for example, {@code RSA}.
     * @param keySizeInBits    the key size in bits (<em>NOT bytes</em>)
     * @param random           the SecureRandom generator to use during key generation.
     * @return a new secure-randomly generated key pair of the specified size using the specified SecureRandom according
     * to the specified {@code jcaAlgorithmName}.
     * @see #generateKeyPair(int)
     * @see #generateKeyPair(int, SecureRandom)
     * @since 0.5
     */
    private static KeyPair generateKeyPair(String jcaAlgorithmName, int keySizeInBits, SecureRandom random) {
        KeyPairGenerator keyGenerator;
        try {
            keyGenerator = KeyPairGenerator.getInstance(jcaAlgorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to obtain an RSA KeyPairGenerator: " + e.getMessage(), e);
        }

        keyGenerator.initialize(keySizeInBits, random);

        return keyGenerator.genKeyPair();
    }

}