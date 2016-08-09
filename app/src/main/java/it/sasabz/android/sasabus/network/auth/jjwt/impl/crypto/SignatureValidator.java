package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

interface SignatureValidator {

    boolean isValid(byte[] data, byte... signature);
}