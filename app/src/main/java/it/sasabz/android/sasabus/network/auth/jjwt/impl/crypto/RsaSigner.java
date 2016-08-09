package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureAlgorithm;
import it.sasabz.android.sasabus.network.auth.jjwt.SignatureException;

class RsaSigner extends RsaProvider implements Signer {

    RsaSigner(SignatureAlgorithm alg, Key key) {
        super(alg, key);
        if (!(key instanceof RSAPrivateKey)) {
            String msg = "RSA signatures must be computed using an RSAPrivateKey.  The specified key of type " +
                    key.getClass().getName() + " is not an RSAPrivateKey.";
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public byte[] sign(byte... data) {
        try {
            return doSign(data);
        } catch (InvalidKeyException e) {
            throw new SignatureException("Invalid RSA PrivateKey. " + e.getMessage(), e);
        } catch (java.security.SignatureException e) {
            throw new SignatureException("Unable to calculate signature using RSA PrivateKey. " + e.getMessage(), e);
        }
    }

    private byte[] doSign(byte... data) throws InvalidKeyException, java.security.SignatureException {
        PrivateKey privateKey = (PrivateKey) key;
        Signature sig = createSignatureInstance();
        sig.initSign(privateKey);
        sig.update(data);
        return sig.sign();
    }
}
