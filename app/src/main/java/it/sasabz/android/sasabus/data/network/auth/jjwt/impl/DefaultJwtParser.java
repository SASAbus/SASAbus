/*
 * Copyright (C) 2016 David Dejori, Alex Lardschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.sasabz.android.sasabus.data.network.auth.jjwt.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import it.sasabz.android.sasabus.data.network.auth.jjwt.Claims;
import it.sasabz.android.sasabus.data.network.auth.jjwt.ExpiredJwtException;
import it.sasabz.android.sasabus.data.network.auth.jjwt.Header;
import it.sasabz.android.sasabus.data.network.auth.jjwt.Jws;
import it.sasabz.android.sasabus.data.network.auth.jjwt.JwsHeader;
import it.sasabz.android.sasabus.data.network.auth.jjwt.Jwt;
import it.sasabz.android.sasabus.data.network.auth.jjwt.JwtHandler;
import it.sasabz.android.sasabus.data.network.auth.jjwt.JwtHandlerAdapter;
import it.sasabz.android.sasabus.data.network.auth.jjwt.JwtParser;
import it.sasabz.android.sasabus.data.network.auth.jjwt.MalformedJwtException;
import it.sasabz.android.sasabus.data.network.auth.jjwt.SignatureAlgorithm;
import it.sasabz.android.sasabus.data.network.auth.jjwt.SignatureException;
import it.sasabz.android.sasabus.data.network.auth.jjwt.UnsupportedJwtException;
import it.sasabz.android.sasabus.data.network.auth.jjwt.impl.crypto.DefaultJwtSignatureValidator;
import it.sasabz.android.sasabus.data.network.auth.jjwt.impl.crypto.JwtSignatureValidator;
import it.sasabz.android.sasabus.data.network.auth.jjwt.lang.Assert;
import it.sasabz.android.sasabus.data.network.auth.jjwt.lang.Strings;

@SuppressWarnings("unchecked")
public class DefaultJwtParser implements JwtParser {

    //don't need millis since JWT date fields are only second granularity:
    private static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private static final Gson GSON = new Gson();
    private static final TypeToken<HashMap<String, Object>> TYPE_TOKEN =
            new TypeToken<HashMap<String, Object>>() {
            };

    private Key key;

    @Override
    public JwtParser setSigningKey(Key key) {
        Assert.notNull(key, "signing key cannot be null.");
        this.key = key;
        return this;
    }

    @Override
    public Jwt<?, ?> parse(String jwt) throws ExpiredJwtException, MalformedJwtException, SignatureException {
        Assert.hasText(jwt, "JWT String argument cannot be null or empty.");

        String base64UrlEncodedHeader = null;
        String base64UrlEncodedPayload = null;
        String base64UrlEncodedDigest = null;

        int delimiterCount = 0;

        StringBuilder sb = new StringBuilder(128);

        for (char c : jwt.toCharArray()) {

            if (c == SEPARATOR_CHAR) {
                String token = Strings.clean(sb.toString());

                if (delimiterCount == 0) {
                    base64UrlEncodedHeader = token;
                } else if (delimiterCount == 1) {
                    base64UrlEncodedPayload = token;
                }

                delimiterCount++;
                sb = new StringBuilder(128);
            } else {
                sb.append(c);
            }
        }

        if (delimiterCount != 2) {
            String msg = "JWT strings must contain exactly 2 period characters. Found: " + delimiterCount;
            throw new MalformedJwtException(msg);
        }
        if (sb.length() > 0) {
            base64UrlEncodedDigest = sb.toString();
        }

        if (base64UrlEncodedPayload == null) {
            throw new MalformedJwtException("JWT string '" + jwt + "' is missing a body/payload.");
        }

        // =============== Header =================
        Header header = null;

        if (base64UrlEncodedHeader != null) {
            String origValue = TextCodec.BASE64URL.decodeToString(base64UrlEncodedHeader);
            Map<String, Object> m = readValue(origValue);

            if (base64UrlEncodedDigest != null) {
                header = new DefaultJwsHeader(m);
            } else {
                header = new DefaultHeader(m);
            }
        }

        // =============== Body =================
        String payload;
        payload = TextCodec.BASE64URL.decodeToString(base64UrlEncodedPayload);

        Claims claims = null;

        if (payload.charAt(0) == '{' && payload.charAt(payload.length() - 1) == '}') { //likely to be json, parse it:
            Map<String, Object> claimsMap = readValue(payload);
            claims = new DefaultClaims(claimsMap);
        }

        // =============== Signature =================
        if (base64UrlEncodedDigest != null) { //it is signed - validate the signature

            JwsHeader jwsHeader = (JwsHeader) header;

            SignatureAlgorithm algorithm = null;

            if (header != null) {
                String alg = jwsHeader.getAlgorithm();
                if (Strings.hasText(alg)) {
                    algorithm = SignatureAlgorithm.forName(alg);
                }
            }

            if (algorithm == null) {
                //it is plaintext, but it has a signature.  This is invalid:
                String msg = "JWT string has a digest/signature, but the header does not reference a valid signature " +
                        "algorithm.";
                throw new MalformedJwtException(msg);
            }

            //digitally signed, let's assert the signature:
            Key key = this.key;

            Assert.notNull(key, "A signing key must be specified if the specified JWT is digitally signed.");

            //re-create the jwt part without the signature.  This is what needs to be signed for verification:
            String jwtWithoutSignature = base64UrlEncodedHeader + SEPARATOR_CHAR + base64UrlEncodedPayload;

            JwtSignatureValidator validator;
            try {
                validator = createSignatureValidator(algorithm, key);
            } catch (IllegalArgumentException e) {
                String algName = algorithm.getValue();
                String msg = "The parsed JWT indicates it was signed with the " + algName + " signature " +
                        "algorithm, but the specified signing key of type " + key.getClass().getName() +
                        " may not be used to validate " + algName + " signatures.  Because the specified " +
                        "signing key reflects a specific and expected algorithm, and the JWT does not reflect " +
                        "this algorithm, it is likely that the JWT was not expected and therefore should not be " +
                        "trusted.  Another possibility is that the parser was configured with the incorrect " +
                        "signing key, but this cannot be assumed for security reasons.";
                throw new UnsupportedJwtException(msg, e);
            }

            if (!validator.isValid(jwtWithoutSignature, base64UrlEncodedDigest)) {
                String msg = "JWT signature does not match locally computed signature. JWT validity cannot be " +
                        "asserted and should not be trusted.";
                throw new SignatureException(msg);
            }
        }

        Object body = claims != null ? claims : payload;

        if (base64UrlEncodedDigest != null) {
            return new DefaultJws<>((JwsHeader) header, body, base64UrlEncodedDigest);
        } else {
            return new DefaultJwt<>(header, body);
        }
    }

    /**
     * @since 0.5 mostly to allow testing overrides
     */
    private JwtSignatureValidator createSignatureValidator(SignatureAlgorithm alg, Key key) {
        return new DefaultJwtSignatureValidator(alg, key);
    }

    @Override
    public <T> T parse(String compact, JwtHandler<T> handler)
            throws ExpiredJwtException, MalformedJwtException, SignatureException {
        Assert.notNull(handler, "JwtHandler argument cannot be null.");
        Assert.hasText(compact, "JWT String argument cannot be null or empty.");

        Jwt<?, ?> jwt = parse(compact);

        if (jwt instanceof Jws) {
            Jws<?> jws = (Jws<?>) jwt;
            Object body = jws.getBody();
            if (body instanceof Claims) {
                return handler.onClaimsJws((Jws<Claims>) jws);
            } else {
                return handler.onPlaintextJws((Jws<String>) jws);
            }
        } else {
            Object body = jwt.getBody();
            if (body instanceof Claims) {
                return handler.onClaimsJwt((Jwt<Header, Claims>) jwt);
            } else {
                return handler.onPlaintextJwt((Jwt<Header, String>) jwt);
            }
        }
    }

    @Override
    public Jws<Claims> parseClaimsJws(String claimsJws) {
        return parse(claimsJws, new JwtHandlerAdapter<Jws<Claims>>() {
            @Override
            public Jws<Claims> onClaimsJws(Jws<Claims> jws) {
                return jws;
            }
        });
    }

    private Map<String, Object> readValue(String val) {
        return GSON.fromJson(val, TYPE_TOKEN.getType());
    }
}