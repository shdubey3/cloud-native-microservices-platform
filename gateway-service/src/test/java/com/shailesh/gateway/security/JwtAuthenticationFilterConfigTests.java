package com.shailesh.gateway.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused tests for {@link JwtAuthenticationFilter.Config} public path matching.
 * This ensures that security-sensitive paths are classified correctly as public or protected.
 */
class JwtAuthenticationFilterConfigTests {

    private final JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();

    @Test
    void isPublicPath_matchesAuthAndHealthEndpoints() {
        assertThat(config.isPublicPath("/auth/token")).isTrue();
        assertThat(config.isPublicPath("/auth/saml/acs")).isTrue();
        assertThat(config.isPublicPath("/health")).isTrue();
        assertThat(config.isPublicPath("/actuator/health")).isTrue();
    }

    @Test
    void isPublicPath_doesNotMatchProtectedApiRoutes() {
        assertThat(config.isPublicPath("/api/users/id/1")).isFalse();
        assertThat(config.isPublicPath("/api/orders/123")).isFalse();
        assertThat(config.isPublicPath("/api/catalog/products")).isFalse();
    }
}

