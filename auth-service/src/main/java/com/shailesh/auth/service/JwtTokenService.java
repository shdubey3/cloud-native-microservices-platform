package com.shailesh.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Service
 *
 * Generates JWT tokens for authenticated users.
 *
 * Token Structure:
 * Header: { alg: HS256, typ: JWT }
 * Payload: {
 *   sub: userId,
 *   email: userEmail,
 *   roles: "ROLE_USER,ROLE_ADMIN",
 *   tenant: tenantId,
 *   iat: issuedAt,
 *   exp: expirationTime
 * }
 * Signature: HMAC-SHA256(secret)
 *
 * In production, use RS256 (RSA) with private key for signing and public key for verification.
 */
@Service
public class JwtTokenService {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    @Value("${jwt.secret:shailesh-secret-key-change-in-production}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:3600000}") // Default: 1 hour
    private long tokenExpirationMs;

    /**
     * Generates a JWT token for a user
     *
     * @param userId       The user's unique identifier
     * @param email        User's email address
     * @param roles        Comma-separated list of roles (e.g., "ROLE_USER,ROLE_ADMIN")
     * @param tenantId     Tenant ID (for multi-tenant systems)
     * @return The signed JWT token
     */
    public String generateToken(String userId, String email, String roles, String tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("roles", roles != null ? roles : "ROLE_USER");
        claims.put("tenant", tenantId != null ? tenantId : "default");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String token = Jwts.builder()
                .claims(claims)
                .subject(userId) // sub claim
                .issuedAt(now)    // iat claim
                .expiration(expiryDate) // exp claim
                .signWith(key)
                .compact();

        log.info("JWT token generated for user: {} ({})", email, userId);
        return token;
    }

    /**
     * Generates a test JWT token (for demo purposes)
     */
    public String generateTestToken(String userId, String email) {
        return generateToken(userId, email, "ROLE_USER", "default");
    }
}
