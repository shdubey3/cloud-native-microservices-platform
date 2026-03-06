package com.shailesh.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JWT Token Validator
 *
 * Validates JWT tokens issued by the auth-service.
 * Validates:
 * - Token signature (ensures it was issued by auth-service)
 * - Token expiration (ensures it hasn't expired)
 * - Token claims (extracts user info, roles, etc.)
 *
 * In production, use RS256 (asymmetric) with public key from auth-service's JWKS endpoint.
 * For demo purposes, we use HS256 (symmetric) with a shared secret.
 */
@Component
public class JwtTokenValidator {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    @Value("${jwt.secret:shailesh-secret-key-change-in-production}")
    private String jwtSecret;

    /**
     * Validates JWT token and returns claims if valid
     *
     * @param token The JWT token to validate
     * @return Claims from the token if valid
     * @throws JwtException if token is invalid or expired
     */
    public Claims validateToken(String token) throws JwtException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts token from Authorization header
     * Expected format: "Bearer {token}"
     *
     * @param authHeader The Authorization header value
     * @return The token without "Bearer " prefix
     */
    public String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
