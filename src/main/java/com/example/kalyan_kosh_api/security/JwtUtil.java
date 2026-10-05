package com.example.kalyan_kosh_api.security;

import com.example.kalyan_kosh_api.portal.PortalCode;
import com.example.kalyan_kosh_api.portal.PortalContext;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private final Key key;
    private final long expirationMs;
    private final long mobileExpirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.mobile-expiration-ms:315360000000}") long mobileExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        this.mobileExpirationMs = mobileExpirationMs;
    }

    public String generateToken(UserDetails ud) {
        return generateTokenWithExpiry(ud, expirationMs, "web");
    }

    public String generateMobileToken(UserDetails ud) {
        return generateTokenWithExpiry(ud, mobileExpirationMs, "mobile");
    }

    private String generateTokenWithExpiry(UserDetails ud, long expiryMs, String client) {
        var roles = ud.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(ud.getUsername())
                .claim("roles", roles)
                .claim("client", client)
                .claim("portal", PortalContext.get().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validate(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String extractUsername(String token) {
        return validate(token).getBody().getSubject();
    }

    public Date extractIssuedAt(String token) {
        return validate(token).getBody().getIssuedAt();
    }

    /**
     * Returns null for old TAB1 tokens created before the portal claim was introduced.
     */
    public PortalCode extractPortal(String token) {
        String value = validate(token).getBody().get("portal", String.class);
        return PortalCode.fromValue(value);
    }
}
