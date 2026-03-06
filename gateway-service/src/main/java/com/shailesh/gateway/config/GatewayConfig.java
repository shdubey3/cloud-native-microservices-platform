package com.shailesh.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Spring Cloud Gateway Route Configuration
 *
 * Defines all routes and their corresponding downstream services.
 * Uses Service Discovery (Eureka) to route to actual service instances.
 *
 * Route format: lb://{service-name}
 * lb = Load Balance (discovers service from Eureka and load balances requests)
 *
 * Routes:
 * 1. /api/users/** → user-service (GET, POST, PUT, DELETE user operations)
 * 2. /api/orders/** → order-service (Order management)
 * 3. /api/catalog/** → catalog-service (Product catalog)
 * 4. /api/recommendations/** → recommendation-service (Personalized recommendations)
 * 5. /auth/** → auth-service (Authentication, SAML ACS, token generation) [no JWT required]
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ===== Public/Auth Routes (no JWT validation) =====
                .route("auth-service",
                        r -> r.path("/auth/**")
                                .uri("lb://auth-service"))

                // ===== User Service Routes (with JWT validation) =====
                .route("user-service",
                        r -> r.path("/api/users/**")
                                .filters(f -> f.rewritePath("/api/users/(?<segment>.*)", "/${segment}"))
                                .uri("lb://user-service"))

                // ===== Order Service Routes (with JWT validation) =====
                .route("order-service",
                        r -> r.path("/api/orders/**")
                                .filters(f -> f.rewritePath("/api/orders/(?<segment>.*)", "/${segment}"))
                                .uri("lb://order-service"))

                // ===== Catalog Service Routes (with JWT validation) =====
                .route("catalog-service",
                        r -> r.path("/api/catalog/**")
                                .filters(f -> f.rewritePath("/api/catalog/(?<segment>.*)", "/${segment}"))
                                .uri("lb://catalog-service"))

                // ===== Recommendation Service Routes (with JWT validation) =====
                .route("recommendation-service",
                        r -> r.path("/api/recommendations/**")
                                .filters(f -> f.rewritePath("/api/recommendations/(?<segment>.*)", "/${segment}"))
                                .uri("lb://recommendation-service"))

                .build();
    }
}
