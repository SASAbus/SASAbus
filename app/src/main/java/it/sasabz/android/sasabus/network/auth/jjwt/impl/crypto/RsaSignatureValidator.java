package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureAlgorithm;
import it.sasabz.android.sasabus.network.auth.jjwt.SignatureException;
import it.sasabz.android.sasabus.network.auth.jjwt.lang.Assert;

public class RsaSignatureValidator extends RsaProvider implements SignatureValidator {

    private final RsaSigner SIGNER;

    public RsaSignatureValidator(SignatureAlgorithm alg, Key key) {
        super(alg, key);
        Assert.isTrue(key instanceof RSAPrivateKey || key instanceof RSAPublicKey,
                "RSA Signature validation requires either a RSAPublicKey or RSAPrivateKey instance.");
        SIGNER = key instanceof RSAPrivateKey ? new RsaSigner(alg, key) : null;
    }

    @Override
    public boolean isValid(byte[] data, byte... signature) {
        if (key instanceof PublicKey) {
            Signature sig = createSignatureInstance();
            PublicKey publicKey = (PublicKey) key;
            try {
                return doVerify(sig, publicKey, data, signature);
            } catch (Exception e) {
                String msg = "Unable to verify RSA signature using configured PublicKey. " + e.getMessage();
                throw new SignatureException(msg, e);
            }
        } else {
            Assert.notNull(SIGNER, "RSA Signer instance cannot be null.  This is a bug.  Please report it.");
            byte[] computed = SIGNER.sign(data);
            return Arrays.equals(computed, signature);
        }
    }

    private boolean doVerify(Signature sig, PublicKey publicKey, byte[] data, byte... signature)
            throws InvalidKeyException, java.security.SignatureException {
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }

}
