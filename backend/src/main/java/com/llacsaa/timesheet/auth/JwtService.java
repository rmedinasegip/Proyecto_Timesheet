package com.llacsaa.timesheet.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

/**
 * Firma/verifica el JWT (HS256) que viaja como cookie httpOnly
 * {@link #AUTH_COOKIE_NAME} — ver AuthController/AuthFilter. Sin refresh
 * tokens ni revocación: un token es válido hasta que expira o se hace
 * logout (mismo alcance que el proyecto Next.js hermano para este tipo de
 * piloto).
 */
@Component
public class JwtService {

    public static final String AUTH_COOKIE_NAME = "auth_token";

    private final long expiresInHours;
    private final Key key;

    public JwtService(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expires-in-hours}") long expiresInHours) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("jwt.secret debe estar configurado y tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.expiresInHours = expiresInHours;
    }

    public String sign(Long userCode, String email, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiresInHours * 3600_000L);
        return Jwts.builder()
                .setSubject(String.valueOf(userCode))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public long maxAgeSeconds() {
        return expiresInHours * 3600L;
    }

    public Optional<CurrentUser> verify(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            long code = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            return Optional.of(new CurrentUser(code, email, role));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
