package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import java.security.Key;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureAlgorithm;

interface SignatureValidatorFactory {

    SignatureValidator createSignatureValidator(SignatureAlgorithm alg, Key key);
}
