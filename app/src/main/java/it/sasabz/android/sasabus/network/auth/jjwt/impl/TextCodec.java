package it.sasabz.android.sasabus.network.auth.jjwt.impl;

public interface TextCodec {

    TextCodec BASE64 = new DefaultTextCodecFactory().getTextCodec();
    TextCodec BASE64URL = new Base64UrlCodec();

    String encode(byte... data);

    byte[] decode(String encoded);

    String decodeToString(String encoded);
}
