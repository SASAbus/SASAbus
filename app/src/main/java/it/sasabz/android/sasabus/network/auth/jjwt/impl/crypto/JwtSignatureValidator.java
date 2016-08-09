package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

public interface JwtSignatureValidator {

    boolean isValid(String jwtWithoutSignature, String base64UrlEncodedSignature);
}
