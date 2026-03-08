package com.shailesh.order.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Order Event Producer
 *
 * Publishes order events to Kafka topic: order-events
 *
 * Partitioning Strategy:
 * - Key: userId (String)
 * - Purpose: All orders from same user go to same partition, ensuring order
 * - Benefit: Downstream services can process user orders in order
 *
 * Events Published:
 * - CREATE: Order created
 * - UPDATE: Order status changed
 * - CANCEL: Order cancelled
 *
 * Downstream Consumers:
 * - notification-service (group: notification-service-group)
 * - recommendation-service (group: recommendation-service-group)
 */
@Service
public class OrderEventProducer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.order-events:order-events}")
    private String orderEventsTopic;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes an order event to Kafka
     * Uses userId as partition key for ordering guarantees
     */
    public void publishOrderEvent(OrderEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String partitionKey = event.userId().toString();

            Message<String> message = MessageBuilder
                    .withPayload(eventJson)
                    .setHeader(KafkaHeaders.TOPIC, orderEventsTopic)
                    // Use the standard KafkaHeaders.PARTITION_ID header for partitioning key
                    .setHeader("kafka_messageKey", partitionKey.getBytes())
                    .setHeader("event_type", event.eventType())
                    .build();

            // Send asynchronously; failures are surfaced via logs/metrics rather than
            // blocking the HTTP request thread on broker latency.
            kafkaTemplate.send(message);
            log.info("Order event published: {} for order {} (user: {})",
                    event.eventType(), event.orderId(), event.userId());

        } catch (Exception e) {
            log.error("Failed to publish order event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish order event", e);
        }
    }

    /**
     * Order Event DTO
     */
    public record OrderEvent(
            Long orderId,
            Long userId,
            String eventType, // CREATE, UPDATE, CANCEL
            String status,
            java.math.BigDecimal totalAmount,
            LocalDateTime timestamp
    ) {}
}
