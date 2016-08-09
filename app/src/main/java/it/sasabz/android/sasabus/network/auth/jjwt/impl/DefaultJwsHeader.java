package it.sasabz.android.sasabus.network.auth.jjwt.impl;

import java.util.Map;

import it.sasabz.android.sasabus.network.auth.jjwt.JwsHeader;

class DefaultJwsHeader extends DefaultHeader implements JwsHeader {

    DefaultJwsHeader(Map<String, Object> map) {
        super(map);
    }

    @Override
    public String getAlgorithm() {
        return getString(ALGORITHM);
    }
}
