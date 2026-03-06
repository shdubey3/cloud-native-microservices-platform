package com.shailesh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Service
 *
 * This service acts as a single entry point for all client requests.
 * It is responsible for:
 *
 * 1. Service Discovery: Uses Eureka to discover downstream service instances
 * 2. Routing: Routes requests to appropriate microservices based on URL patterns
 * 3. JWT Validation: Validates incoming JWT tokens from auth-service
 * 4. Rate Limiting: Applies rate limiting to prevent abuse
 * 5. Request/Response Logging: Logs all API requests and responses
 * 6. Cross-cutting Concerns: Handles authentication, CORS, etc.
 *
 * Routing Rules:
 * - /api/users/** → user-service (port 8082)
 * - /api/orders/** → order-service (port 8083)
 * - /api/catalog/** → catalog-service (port 8084)
 * - /api/recommendations/** → recommendation-service (port 8085)
 * - /auth/** → auth-service (port 8081) [no JWT required]
 *
 * JWT Validation Flow:
 * 1. Client sends JWT in Authorization header: "Bearer {token}"
 * 2. GatewayFilter extracts and validates JWT
 * 3. If valid, request proceeds to downstream service
 * 4. If invalid, returns 401 Unauthorized
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
