package com.shop.auth.service;

import com.shop.common.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class JwtService {
    private final AppProperties props;

    public String generateAccessToken(UUID userId, String email) {
        long expMs = props.getJwt().getAccessTokenExpiration() * 1000;
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expMs))
            .signWith(key())
            .compact();
    }

    public String generateRefreshToken() { return UUID.randomUUID().toString(); }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public boolean isValid(String token) {
        try { parseClaims(token); return true; }
        catch (Exception e) { return false; }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
