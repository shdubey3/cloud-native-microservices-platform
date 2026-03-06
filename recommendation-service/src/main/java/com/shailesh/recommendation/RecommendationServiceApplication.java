package com.shailesh.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Recommendation Service
 *
 * Builds user recommendation models by consuming events from:
 * 1. user-changes (Kafka topic from Debezium / user-service)
 *    - Tracks user profile changes
 *    - If user deletes account, clean up recommendations
 *
 * 2. order-events (Kafka topic from order-service)
 *    - Tracks purchasing behavior
 *    - Identifies product preferences per user
 *    - Builds collaborative filtering data
 *
 * MongoDB Collections:
 * - user_profiles: { userId, email, categories_interested, total_orders, last_order_time }
 * - recommendations: { userId, recommended_products, generated_at }
 *
 * Kafka Consumer Groups:
 * - recommendation-service: Consumes from both topics
 * - Partitions: 6 for both topics
 *    (ensures ordering per key, parallelism across partitions)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class RecommendationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendationServiceApplication.class, args);
    }
}
