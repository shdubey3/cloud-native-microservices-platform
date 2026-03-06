package com.shailesh.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Gateway Filter
 *
 * This filter is applied to all routes that require authentication.
 * It intercepts requests, validates JWT tokens, and either:
 * - Allows the request to proceed if token is valid
 * - Returns 401 Unauthorized if token is missing or invalid
 *
 * The filter adds user information to the request headers so downstream
 * services can access user context without re-validating the token.
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenValidator tokenValidator;

    public JwtAuthenticationFilter(JwtTokenValidator tokenValidator) {
        super(Config.class);
        this.tokenValidator = tokenValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();

            // Skip authentication for public endpoints (auth, health checks)
            if (config.isPublicPath(path)) {
                log.debug("Skipping JWT validation for public path: {}", path);
                return chain.filter(exchange);
            }

            try {
                // Extract Authorization header
                String authHeader = exchange.getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || authHeader.isEmpty()) {
                    log.warn("Missing Authorization header for path: {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Extract and validate token
                String token = tokenValidator.extractToken(authHeader);
                if (token == null) {
                    log.warn("Invalid Authorization header format for path: {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                Claims claims = tokenValidator.validateToken(token);

                // Add user information to request headers for downstream services
                // These headers are passed to the backend service
                // Downstream services can use these to know authenticated user context
                String userId = (String) claims.get("sub");
                String email = (String) claims.get("email");
                String roles = (String) claims.get("roles");

                var wrappedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .header("X-User-Roles", roles != null ? roles : "")
                        .build();

                log.info("JWT validated for user: {} ({})", email, userId);
                return chain.filter(exchange.mutate().request(wrappedRequest).build());

            } catch (JwtException e) {
                log.error("JWT validation failed: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (Exception e) {
                log.error("Unexpected error during JWT validation: {}", e.getMessage(), e);
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        private static final String[] PUBLIC_PATHS = {"/auth/", "/health", "/actuator", "/swagger"};

        public boolean isPublicPath(String path) {
            for (String publicPath : PUBLIC_PATHS) {
                if (path.startsWith(publicPath)) {
                    return true;
                }
            }
            return false;
        }
    }
}
