package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import java.nio.charset.Charset;
import java.security.Key;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureAlgorithm;
import it.sasabz.android.sasabus.network.auth.jjwt.impl.TextCodec;
import it.sasabz.android.sasabus.network.auth.jjwt.lang.Assert;

public class DefaultJwtSignatureValidator implements JwtSignatureValidator {

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    private final SignatureValidator signatureValidator;

    public DefaultJwtSignatureValidator(SignatureAlgorithm alg, Key key) {
        this(DefaultSignatureValidatorFactory.INSTANCE, alg, key);
    }

    private DefaultJwtSignatureValidator(SignatureValidatorFactory factory, SignatureAlgorithm alg, Key key) {
        Assert.notNull(factory, "SignerFactory argument cannot be null.");
        signatureValidator = factory.createSignatureValidator(alg, key);
    }

    @Override
    public boolean isValid(String jwtWithoutSignature, String base64UrlEncodedSignature) {
        byte[] data = jwtWithoutSignature.getBytes(US_ASCII);

        byte[] signature = TextCodec.BASE64URL.decode(base64UrlEncodedSignature);

        return signatureValidator.isValid(data, signature);
    }
}
