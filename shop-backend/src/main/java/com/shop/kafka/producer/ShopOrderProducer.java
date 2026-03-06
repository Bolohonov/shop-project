package com.shop.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.config.AppProperties;
import com.shop.kafka.dto.ShopOrderCreatedEvent;
import com.shop.kafka.outbox.KafkaOutbox;
import com.shop.kafka.outbox.KafkaOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class ShopOrderProducer {
    private final KafkaOutboxRepository outboxRepo;
    private final AppProperties props;
    private final ObjectMapper objectMapper;

    public void enqueueOrderCreated(ShopOrderCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String key = event.getShopOrderUuid().toString();
            outboxRepo.save(KafkaOutbox.builder()
                    .id(UUID.randomUUID())
                    .topic(props.getKafka().getTopics().getShopOrderCreated())
                    .messageKey(key)
                    .payload(payload)
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .attemptCount(0)
                    .isNew(true)
                    .build());
            log.info("Outbox enqueued order: shopOrderId={} uuid={}", event.getShopOrderId(), event.getShopOrderUuid());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ShopOrderCreatedEvent", e);
        }
    }
}
