package com.shailesh.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service
 *
 * Pure Kafka consumer service that sends notifications.
 *
 * Consumes:
 * - order-events from order-service
 *
 * Processing:
 * 1. Receive order event (CREATE, UPDATE, CANCEL)
 * 2. Send notification (email, SMS, push)
 * 3. Log the notification
 *
 * Consumer Group: notification-service
 * Concurrency: 3 (can consume from 3 partitions simultaneously)
 *
 * This demonstrates:
 * - Event-driven architecture
 * - Multiple consumers of same event stream
 * - Simple stateless processing
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
