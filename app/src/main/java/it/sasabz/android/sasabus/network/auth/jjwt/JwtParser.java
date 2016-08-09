package it.sasabz.android.sasabus.network.auth.jjwt;

import java.security.Key;

/**
 * A parser for reading JWT strings, used to convert them into a {@link Jwt} object representing the expanded JWT.
 *
 * @since 0.1
 */
public interface JwtParser {

    char SEPARATOR_CHAR = '.';

    /**
     * Sets the signing key used to verify any discovered JWS digital signature.  If the specified JWT string is not
     * a JWS (no signature), this key is not used.
     * <p>
     * <p>Note that this key <em>MUST</em> be a valid key for the signature algorithm found in the JWT header
     * (as the {@code alg} header parameter).</p>
     * <p>
     * <p>This method overwrites any previously set key.</p>
     *
     * @param key the algorithm-specific signature verification key to use to validate any discovered JWS digital
     *            signature.
     * @return the parser for method chaining.
     */
    JwtParser setSigningKey(Key key);

    /**
     * Parses the specified compact serialized JWT string based on the builder's current configuration state and
     * returns the resulting JWT or JWS instance.
     * <p>
     * <p>This method returns a JWT or JWS based on the parsed string.  Because it may be cumbersome to determine if it
     * is a JWT or JWS, or if the body/payload is a Claims or String with {@code instanceof} checks, the
     * {@link #parse(String, JwtHandler) parse(String,JwtHandler)} method allows for a type-safe callback approach that
     * may help reduce code or instanceof checks.</p>
     *
     * @param jwt the compact serialized JWT to parse
     * @return the specified compact serialized JWT string based on the builder's current configuration state.
     * @throws MalformedJwtException    if the specified JWT was incorrectly constructed (and therefore invalid).
     *                                  Invalid
     *                                  JWTs should not be trusted and should be discarded.
     * @throws SignatureException       if a JWS signature was discovered, but could not be verified.  JWTs that fail
     *                                  signature validation should not be trusted and should be discarded.
     * @throws ExpiredJwtException      if the specified JWT is a Claims JWT and the Claims has an expiration time
     *                                  before the time this method is invoked.
     * @throws IllegalArgumentException if the specified string is {@code null} or empty or only whitespace.
     * @see #parse(String, JwtHandler)
     * @see #parseClaimsJws(String)
     */
    Jwt parse(String jwt) throws ExpiredJwtException, MalformedJwtException, SignatureException, IllegalArgumentException;

    /**
     * Parses the specified compact serialized JWT string based on the builder's current configuration state and
     * invokes the specified {@code handler} with the resulting JWT or JWS instance.
     * <p>
     * <p>If you are confident of the format of the JWT before parsing, you can create an anonymous subclass using the
     * {@link JwtHandlerAdapter JwtHandlerAdapter} and override only the methods you know are relevant
     * for your use case(s), for example:</p>
     * <p>
     * <pre>
     * String compactJwt = request.getParameter("jwt"); //we are confident this is a signed JWS
     *
     * String subject = Jwts.parser().setSigningKey(key).parse(compactJwt, new JwtHandlerAdapter&lt;String&gt;() {
     *     &#64;Override
     *     public String onClaimsJws(Jws&lt;Claims&gt; jws) {
     *         return jws.getBody().getSubject();
     *     }
     * });
     * </pre>
     * <p>
     * <p>If you know the JWT string can be only one type of JWT, then it is even easier to invoke one of the
     * following convenience methods instead of this one:</p>
     * <p>
     * <ul>
     * <li>{@link #parseClaimsJws(String)}</li>
     * </ul>
     *
     * @param jwt the compact serialized JWT to parse
     * @return the result returned by the {@code JwtHandler}
     * @throws MalformedJwtException    if the specified JWT was incorrectly constructed (and therefore invalid).
     *                                  Invalid JWTs should not be trusted and should be discarded.
     * @throws SignatureException       if a JWS signature was discovered, but could not be verified.  JWTs that fail
     *                                  signature validation should not be trusted and should be discarded.
     * @throws ExpiredJwtException      if the specified JWT is a Claims JWT and the Claims has an expiration time
     *                                  before the time this method is invoked.
     * @throws IllegalArgumentException if the specified string is {@code null} or empty or only whitespace, or if the
     *                                  {@code handler} is {@code null}.
     * @see #parseClaimsJws(String)
     * @see #parse(String)
     * @since 0.2
     */
    <T> T parse(String jwt, JwtHandler<T> handler)
            throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException;

    /**
     * Parses the specified compact serialized JWS string based on the builder's current configuration state and
     * returns
     * the resulting Claims JWS instance.
     * <p>
     * <p>This is a convenience method that is usable if you are confident that the compact string argument reflects a
     * Claims JWS. A Claims JWS is a JWT with a {@link Claims} body that has been cryptographically signed.</p>
     * <p>
     * <p><b>If the compact string presented does not reflect a Claims JWS, an {@link UnsupportedJwtException} will be
     * thrown.</b></p>
     *
     * @param claimsJws a compact serialized Claims JWS string.
     * @return the {@link Jws Jws} instance that reflects the specified compact Claims JWS string.
     * @throws UnsupportedJwtException  if the {@code claimsJws} argument does not represent an Claims JWS
     * @throws MalformedJwtException    if the {@code claimsJws} string is not a valid JWS
     * @throws SignatureException       if the {@code claimsJws} JWS signature validation fails
     * @throws ExpiredJwtException      if the specified JWT is a Claims JWT and the Claims has an expiration time
     *                                  before the time this method is invoked.
     * @throws IllegalArgumentException if the {@code claimsJws} string is {@code null} or empty or only whitespace
     * @see #parse(String, JwtHandler)
     * @see #parse(String)
     * @since 0.2
     */
    Jws<Claims> parseClaimsJws(String claimsJws)
            throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException;
}
