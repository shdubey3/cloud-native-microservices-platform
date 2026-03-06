package com.shailesh.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * User Service
 *
 * Manages user profiles and authentication data.
 *
 * Features:
 * 1. User CRUD operations via REST API
 * 2. MySQL backend with JPA/Hibernate
 * 3. Change Data Capture (CDC) via Debezium
 *    - All user modifications are published to Kafka topic: user-changes
 *    - Key: userId (ensures ordering per user)
 *    - Value: User change event (INSERT, UPDATE, DELETE)
 * 4. Flyway database migration
 *
 * Database Schema:
 * - users table with columns: id, username, email, firstName, lastName, roles, samlSubject, createdAt, updatedAt
 *
 * CDC Flow:
 * 1. Any INSERT/UPDATE/DELETE in users table is captured by Debezium
 * 2. Debezium publishes row change events to Kafka topic: user-changes
 * 3. Partition key is userId to maintain ordering
 * 4. Downstream services (recommendation-service) consume these events
 * 5. Services update their local read models based on user changes
 *
 * Benefits of CDC:
 * - No code changes needed to publish events
 * - Guaranteed event delivery (transactional log mining)
 * - Low latency (near real-time)
 * - Complete audit trail of all changes
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
