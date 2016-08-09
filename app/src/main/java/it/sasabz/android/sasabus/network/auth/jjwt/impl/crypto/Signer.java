package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureException;

interface Signer {

    byte[] sign(byte... data) throws SignatureException;
}
