package com.shailesh.recommendation.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shailesh.recommendation.document.UserRecommendation;
import com.shailesh.recommendation.repository.RecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Order Event Consumer
 *
 * Consumes order-events from Kafka and builds recommendation models.
 *
 * Consumer Group: recommendation-service
 * Topic: order-events
 * Key: userId (ensures ordering per user's orders)
 *
 * Processing:
 * 1. Receive order event (orderId, userId, status, totalAmount)
 * 2. Look up user's recommendation document
 * 3. Update user's purchase history / preferences
 * 4. Save back to MongoDB
 *
 * Partitioning Guarantee:
 * - Same userId always goes to same partition
 * - Single thread/replica processes that partition
 * - Ensures events for a user are processed in order
 * - No race conditions when updating user's recommendations
 */
@Service
public class OrderEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final RecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(RecommendationRepository recommendationRepository, ObjectMapper objectMapper) {
        this.recommendationRepository = recommendationRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Consumes order events
     * Concurrency = 3 means up to 3 threads consuming from partitions
     */
    @KafkaListener(
            topics = "order-events",
            groupId = "recommendation-service",
            concurrency = "3"
    )
    public void consumeOrderEvent(String orderEventJson, Acknowledgment acknowledgment) {
        try {
            log.debug("Received order event for processing");

            JsonNode event = objectMapper.readTree(orderEventJson);
            Long userId = Long.parseLong(event.get("userId").asText());
            String eventType = event.get("eventType").asText();

            log.info("Processing {} event for user: {}", eventType, userId);

            // Lookup or create recommendation for user
            Optional<UserRecommendation> recOpt = recommendationRepository.findByUserId(userId);
            UserRecommendation rec = recOpt.orElse(UserRecommendation.builder()
                    .userId(userId)
                    .recommendedProductIds(new ArrayList<>())
                    .preferredCategories(new ArrayList<>())
                    .generatedAt(LocalDateTime.now())
                    .build());

            // Update recommendations based on order event
            // In reality, this would run ML model or collaborative filtering
            rec.setLastUpdatedAt(LocalDateTime.now());

            recommendationRepository.save(rec);
            log.info("Recommendation updated for user: {}", userId);

            // Manually acknowledge only after successful processing.
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("Error processing order event: {}", e.getMessage(), e);
            // Leave offset unacknowledged so the message can be retried according to
            // the consumer group's error-handling configuration.
        }
    }
}
