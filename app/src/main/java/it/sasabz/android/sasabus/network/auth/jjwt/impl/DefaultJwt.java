package it.sasabz.android.sasabus.network.auth.jjwt.impl;

import it.sasabz.android.sasabus.network.auth.jjwt.Header;
import it.sasabz.android.sasabus.network.auth.jjwt.Jwt;

class DefaultJwt<B> implements Jwt<Header, B> {

    private final Header header;
    private final B body;

    DefaultJwt(Header header, B body) {
        this.header = header;
        this.body = body;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public B getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "header=" + header + ",body=" + body;
    }
}