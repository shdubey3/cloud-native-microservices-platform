package com.shailesh.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Spring Cloud Config Server for Centralized Configuration Management
 *
 * This server provides externalized configuration for all microservices.
 * Configuration can be stored in:
 * - Local git repository
 * - Remote git repository
 * - File system
 * - Database
 *
 * All services fetch their configuration from this server at startup and periodically refresh.
 * Configuration hierarchy: application.yml < application-{profile}.yml < application-{service}.yml
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
