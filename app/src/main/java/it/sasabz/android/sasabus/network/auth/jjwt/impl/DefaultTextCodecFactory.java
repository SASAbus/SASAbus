package it.sasabz.android.sasabus.network.auth.jjwt.impl;

class DefaultTextCodecFactory implements TextCodecFactory {

    @Override
    public TextCodec getTextCodec() {
        return new AndroidBase64Codec();
    }
}
