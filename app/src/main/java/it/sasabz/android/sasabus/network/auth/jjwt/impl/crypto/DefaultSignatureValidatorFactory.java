package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import java.security.Key;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureAlgorithm;
import it.sasabz.android.sasabus.network.auth.jjwt.lang.Assert;

class DefaultSignatureValidatorFactory implements SignatureValidatorFactory {

    static final SignatureValidatorFactory INSTANCE = new DefaultSignatureValidatorFactory();

    @Override
    public SignatureValidator createSignatureValidator(SignatureAlgorithm alg, Key key) {
        Assert.notNull(alg, "SignatureAlgorithm cannot be null.");
        Assert.notNull(key, "Signing Key cannot be null.");

        switch (alg) {
            case RS256:
            case RS384:
            case RS512:
            case PS256:
            case PS384:
            case PS512:
                return new RsaSignatureValidator(alg, key);
            default:
                throw new IllegalArgumentException("The '" + alg.name() + "' algorithm cannot be used for signing.");
        }
    }
}
