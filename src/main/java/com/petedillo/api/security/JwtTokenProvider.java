package com.petedillo.api.security;

import com.petedillo.api.model.AdminUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 * Supports both access tokens (short-lived) and refresh tokens (long-lived).
 */
@Component
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_DURATION_MINUTES = 15;
    private static final long REFRESH_TOKEN_DURATION_DAYS = 7;
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${jwt.secret:my-secret-key-that-is-at-least-256-bits-long-for-security-purposes-1234567890}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a short-lived access token for authenticated API requests.
     *
     * @param adminUser the authenticated user
     * @return JWT access token
     */
    public String generateAccessToken(AdminUser adminUser) {
        Instant now = Instant.now();
        Instant expiryTime = now.plus(ACCESS_TOKEN_DURATION_MINUTES, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(String.valueOf(adminUser.getId()))
                .claim("username", adminUser.getUsername())
                .claim("email", adminUser.getEmail())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate a long-lived refresh token for obtaining new access tokens.
     *
     * @param adminUser the authenticated user
     * @return JWT refresh token
     */
    public String generateRefreshToken(AdminUser adminUser) {
        Instant now = Instant.now();
        Instant expiryTime = now.plus(REFRESH_TOKEN_DURATION_DAYS, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(String.valueOf(adminUser.getId()))
                .claim("username", adminUser.getUsername())
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extract all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims from the token
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        return (String) extractClaims(token).get("username");
    }

    /**
     * Extract the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }

    /**
     * Validate that a token is valid and matches the given username.
     *
     * @param token the JWT token
     * @param username the expected username
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = extractClaims(token);
            String tokenUsername = (String) claims.get("username");
            return username.equals(tokenUsername) && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a token has expired.
     *
     * @param claims the token claims
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Extract the expiration date from a token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }
}
