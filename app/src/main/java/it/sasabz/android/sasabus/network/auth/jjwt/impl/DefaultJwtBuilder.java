package it.sasabz.android.sasabus.network.auth.jjwt.impl;

import java.util.Date;
import java.util.Map;

import it.sasabz.android.sasabus.network.auth.jjwt.Claims;
import it.sasabz.android.sasabus.network.auth.jjwt.Header;
import it.sasabz.android.sasabus.network.auth.jjwt.JwtBuilder;
import it.sasabz.android.sasabus.network.auth.jjwt.Jwts;
import it.sasabz.android.sasabus.network.auth.jjwt.lang.Strings;

public class DefaultJwtBuilder implements JwtBuilder {

    private Claims claims;

    @Override
    public JwtBuilder setHeader(Header header) {
        return this;
    }

    @Override
    public JwtBuilder setHeader(Map<String, Object> header) {
        return this;
    }

    private Claims ensureClaims() {
        if (claims == null) {
            claims = new DefaultClaims();
        }
        return claims;
    }

    @Override
    public JwtBuilder setClaims(Claims claims) {
        this.claims = claims;
        return this;
    }

    @Override
    public JwtBuilder setClaims(Map<String, Object> claims) {
        this.claims = Jwts.claims(claims);
        return this;
    }

    @Override
    public JwtBuilder setIssuer(String iss) {
        if (Strings.hasText(iss)) {
            ensureClaims().setIssuer(iss);
        } else {
            if (claims != null) {
                claims.setIssuer(iss);
            }
        }
        return this;
    }

    @Override
    public JwtBuilder setSubject(String sub) {
        if (Strings.hasText(sub)) {
            ensureClaims().setSubject(sub);
        } else {
            if (claims != null) {
                claims.setSubject(sub);
            }
        }
        return this;
    }

    @Override
    public JwtBuilder setAudience(String aud) {
        if (Strings.hasText(aud)) {
            ensureClaims().setAudience(aud);
        } else {
            if (claims != null) {
                claims.setAudience(aud);
            }
        }
        return this;
    }

    @Override
    public JwtBuilder setExpiration(Date exp) {
        if (exp != null) {
            ensureClaims().setExpiration(exp);
        } else {
            if (claims != null) {
                //noinspection ConstantConditions
                claims.setExpiration(exp);
            }
        }
        return this;
    }

    @Override
    public JwtBuilder setNotBefore(Date nbf) {
        if (nbf != null) {
            ensureClaims().setNotBefore(nbf);
        } else {
            if (claims != null) {
                //noinspection ConstantConditions
                claims.setNotBefore(nbf);
            }
        }
        return this;
    }

    @Override
    public JwtBuilder setIssuedAt(Date iat) {
        if (iat != null) {
            ensureClaims().setIssuedAt(iat);
        } else {
            if (claims != null) {
                //noinspection ConstantConditions
                claims.setIssuedAt(iat);
            }
        }
        return this;
    }

    @Override
    public JwtBuilder setId(String jti) {
        if (Strings.hasText(jti)) {
            ensureClaims().setId(jti);
        } else {
            if (claims != null) {
                claims.setId(jti);
            }
        }
        return this;
    }
}
