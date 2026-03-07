package com.shailesh.recommendation.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shailesh.recommendation.repository.RecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * User Change Event Consumer (from Debezium CDC)
 *
 * Consumes user-changes from Kafka (provided by Debezium MySQL CDC).
 *
 * Topic: user-changes (created by Debezium from MySQL binlog)
 * Key: userId (ensures ordering per user)
 *
 * Event Structure (Debezium):
 * {
 *   "before": { old user data },
 *   "after": { new user data },
 *   "change_lsn": "...",
 *   "change_txId": "...",
 *   "tableId": ...,
 *   "source": { source database info },
 *   "op": "c"/"u"/"d" (create/update/delete),
 *   "ts_ms": milliseconds
 * }
 *
 * Processing:
 * 1. Receive CDC event from Debezium
 * 2. Extract operation type (create, update, delete)
 * 3. If delete: remove user's recommendations
 * 4. If update: keep recommendations, maybe re-compute
 *
 * Benefits of CDC:
 * - No changes needed in user-service
 * - Guaranteed event delivery
 * - Complete audit trail
 */
@Service
public class UserChangeEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(UserChangeEventConsumer.class);

    private final RecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper;

    public UserChangeEventConsumer(RecommendationRepository recommendationRepository, ObjectMapper objectMapper) {
        this.recommendationRepository = recommendationRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Consumes CDC events from Kafka
     */
    @KafkaListener(
            topics = "user-changes",
            groupId = "recommendation-service",
            concurrency = "3"
    )
    public void consumeUserChangeEvent(String cdcEventJson, Acknowledgment acknowledgment) {
        try {
            log.debug("Received user change event from Debezium");

            JsonNode event = objectMapper.readTree(cdcEventJson);
            String operation = event.get("op").asText(); // c, u, d

            JsonNode after = event.get("after");
            if (after == null || after.isNull()) {
                after = event.get("before");
            }

            if (after == null || after.isNull()) {
                log.warn("No data in CDC event, skipping");
                return;
            }

            // Extract user ID - adjust field name based on actual schema
            Long userId = null;
            if (after.has("id")) {
                userId = after.get("id").asLong();
            }

            if (userId == null) {
                log.warn("Could not extract user ID from CDC event");
                return;
            }

            log.info("Processing {} event for user: {}", operation, userId);

            if ("d".equals(operation)) {
                // User deleted - clean up recommendations
                var recOpt = recommendationRepository.findByUserId(userId);
                if (recOpt.isPresent()) {
                    recommendationRepository.delete(recOpt.get());
                    log.info("Recommendations deleted for user: {}", userId);
                }
            } else if ("c".equals(operation) || "u".equals(operation)) {
                // User created or updated - just log for now
                log.info("User {} event processed (no action needed at this time)", operation);
            }

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("Error processing CDC event: {}", e.getMessage(), e);
            // Offset is not acknowledged so the message remains for retry/DLQ handling.
        }
    }
}
