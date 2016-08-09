package it.sasabz.android.sasabus.network.auth.jjwt.impl;

import android.util.Base64;

class AndroidBase64Codec extends AbstractTextCodec {

    @Override
    public String encode(byte... data) {
        int flags = Base64.NO_PADDING | Base64.NO_WRAP;
        return Base64.encodeToString(data, flags);
    }

    @Override
    public byte[] decode(String encoded) {
        return Base64.decode(encoded, Base64.DEFAULT);
    }
}
