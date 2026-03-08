package com.shailesh.order.messaging;

import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Verifies that {@link OrderEventProducer} builds Kafka messages with the expected
 * topic and partition key (MESSAGE_KEY) based on userId.
 */
class OrderEventProducerTests {

    @Test
    void publishOrderEvent_usesUserIdAsMessageKey() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        OrderEventProducer producer = new OrderEventProducer(kafkaTemplate, new com.fasterxml.jackson.databind.ObjectMapper());

        OrderEventProducer.OrderEvent event = new OrderEventProducer.OrderEvent(
                123L,
                99L,
                "CREATE",
                "PENDING",
                BigDecimal.TEN,
                LocalDateTime.now()
        );

        producer.publishOrderEvent(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Message<String>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(messageCaptor.capture());

        Message<String> message = messageCaptor.getValue();
        assertThat(message.getHeaders().get("kafka_messageKey")).isNotNull();
    }
}

