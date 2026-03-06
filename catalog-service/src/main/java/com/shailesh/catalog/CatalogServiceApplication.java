package com.shailesh.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Catalog Service
 *
 * Product catalog management.
 *
 * Features:
 * 1. MongoDB backend for flexible product schema
 * 2. Redis caching for read-heavy product lookups
 * 3. Cache invalidation on product updates
 * 4. Demonstrates cache-aside pattern
 *
 * Caching Strategy:
 * - Cache Key: product:{productId}
 * - Cache TTL: 1 hour (configurable)
 * - Hit/Miss Logging: Helps observe cache effectiveness
 * - Invalidation: On product update or delete
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CatalogServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
