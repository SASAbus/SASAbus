package it.sasabz.android.sasabus.network.auth.jjwt.impl;

import java.nio.charset.Charset;

abstract class AbstractTextCodec implements TextCodec {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    static final Charset US_ASCII = Charset.forName("US-ASCII");

    @Override
    public String decodeToString(String encoded) {
        byte[] bytes = decode(encoded);
        return new String(bytes, UTF8);
    }
}
