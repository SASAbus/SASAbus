package it.sasabz.android.sasabus.network.auth.jjwt.impl;

import it.sasabz.android.sasabus.network.auth.jjwt.Jws;
import it.sasabz.android.sasabus.network.auth.jjwt.JwsHeader;

class DefaultJws<B> implements Jws<B> {

    private final JwsHeader<?> header;
    private final B body;
    private final String signature;

    DefaultJws(JwsHeader<?> header, B body, String signature) {
        this.header = header;
        this.body = body;
        this.signature = signature;
    }

    @Override
    public JwsHeader<?> getHeader() {
        return header;
    }

    @Override
    public B getBody() {
        return body;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "header=" + header + ",body=" + body + ",signature=" + signature;
    }
}