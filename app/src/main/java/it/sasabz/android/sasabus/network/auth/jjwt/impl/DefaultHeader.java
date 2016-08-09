package it.sasabz.android.sasabus.network.auth.jjwt.impl;

import java.util.Map;

import it.sasabz.android.sasabus.network.auth.jjwt.Header;

@SuppressWarnings("unchecked")
public class DefaultHeader<T extends Header<T>> extends JwtMap implements Header<T> {

    public DefaultHeader() {
    }

    public DefaultHeader(Map<String, Object> map) {
        super(map);
    }

    @Override
    public String getType() {
        return getString(TYPE);
    }

    @Override
    public T setType(String typ) {
        setValue(TYPE, typ);
        return (T) this;
    }
}
