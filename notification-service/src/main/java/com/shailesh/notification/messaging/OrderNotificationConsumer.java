package com.shailesh.notification.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Order Event Notification Consumer
 *
 * Consumes order-events and sends notifications to users.
 *
 * Topic: order-events (size: 6 partitions)
 * Group: notification-service
 * Concurrency: 3
 *
 * Flow:
 * 1. Receive order event from Kafka
 * 2. Parse event data (orderId, userId, status, totalAmount)
 * 3. Determine notification type based on status change
 * 4. Send email/SMS/push notification (mocked here)
 * 5. Log notification action
 *
 * Key Ordering:
 * - Events partitioned by userId
 * - All same user's events go to same partition
 * - Single thread processes each partition in order
 * - Guaranteed ordering per user (no race conditions)
 *
 * Example Notifications:
 * - CREATE: "Your order #12345 has been created"
 * - UPDATE (PROCESSING): "Your order is being prepared"
 * - UPDATE (SHIPPED): "Your order has shipped, tracking: XYZ"
 * - UPDATE (DELIVERED): "Your order has been delivered"
 * - CANCEL: "Your order has been canceled, refund issued"
 */
@Service
public class OrderNotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderNotificationConsumer.class);

    private final ObjectMapper objectMapper;

    public OrderNotificationConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "order-events",
            groupId = "notification-service",
            concurrency = "3"
    )
    public void consumeOrderEvent(String orderEventJson, Acknowledgment acknowledgment) {
        try {
            JsonNode event = objectMapper.readTree(orderEventJson);

            Long orderId = Long.parseLong(event.get("orderId").asText());
            Long userId = Long.parseLong(event.get("userId").asText());
            String eventType = event.get("eventType").asText();
            String status = event.get("status").asText();

            log.info("Received order event - Order: {}, User: {}, Type: {}, Status: {}",
                    orderId, userId, eventType, status);

            // Send notification based on event type and status
            String message = buildNotificationMessage(eventType, status, orderId);
            sendNotification(userId, message);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("Error processing order event: {}", e.getMessage(), e);
            // Let the consumer group's error handler decide whether and how to retry.
        }
    }

    /**
     * Build notification message based on order event
     */
    private String buildNotificationMessage(String eventType, String status, Long orderId) {
        return switch (eventType) {
            case "CREATE" -> String.format("Your order #%d has been created", orderId);
            case "UPDATE" -> switch (status) {
                case "PROCESSING" -> String.format("Your order #%d is being prepared", orderId);
                case "SHIPPED" -> String.format("Your order #%d has shipped!", orderId);
                case "DELIVERED" -> String.format("Your order #%d has been delivered", orderId);
                case "CANCELLED" -> String.format("Your order #%d has been cancelled", orderId);
                default -> String.format("Order #%d status changed to %s", orderId, status);
            };
            case "CANCEL" -> String.format("Your order #%d has been cancelled. Refund will be processed.", orderId);
            default -> String.format("Order #%d event: %s", orderId, eventType);
        };
    }

    /**
     * Mock notification sending
     * In production, this would integrate with:
     * - Email service (SendGrid, mailgun, etc.)
     * - SMS service (Twilio, nexmo, etc.)
     * - Push notification (Firebase, OneSignal, etc.)
     */
    private void sendNotification(Long userId, String message) {
        // Simulate sending email
        log.info("📧 SENDING EMAIL to user {}: {}", userId, message);

        // Simulate sending SMS
        log.info("📱 SENDING SMS to user {}: {}", userId, message);

        // Simulate sending push notification
        log.info("🔔 SENDING PUSH NOTIFICATION to user {}: {}", userId, message);

        log.info("✅ Notification sent to user: {}", userId);
    }
}
