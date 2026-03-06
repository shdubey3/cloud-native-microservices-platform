package com.shailesh.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server for Service Discovery and Registration
 *
 * This is the central service registry where all microservices register themselves.
 * Other services use Eureka to discover available instances of their dependencies.
 *
 * Key concepts:
 * - Services register with Eureka (eureka.client.register-with-eureka=true)
 * - Services discover other services via Eureka (eureka.client.fetch-registry=true)
 * - Eureka maintains a registry of available service instances
 * - Clients use service names (e.g., "user-service") to discover instances
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
