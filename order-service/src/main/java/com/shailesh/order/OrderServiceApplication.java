package com.shailesh.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Order Service
 *
 * Manages customer orders.
 * Produces order-events to Kafka topic for downstream services (notification, analytics, etc.).
 *
 * Features:
 * 1. Order CRUD operations
 * 2. MySQL backend for order persistence
 * 3. Kafka producer for order events (CREATE, UPDATE, COMPLETE, CANCEL)
 * 4. Event partitioning key: userId (ensures all user orders are processed in order)
 *
 * Kafka Topic: order-events
 * - Partitions: 6 (example)
 * - Key: userId (to maintain ordering per user)
 * - Value: OrderEvent (orderId, userId, status, totalAmount, timestamp)
 *
 * Consumer Services:
 * - notification-service: Sends email/SMS when order status changes
 * - recommendation-service: Builds user behavior model from order history
 * - analytics-service: Could consume for business intelligence
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
