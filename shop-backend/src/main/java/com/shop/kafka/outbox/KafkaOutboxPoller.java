package com.shop.kafka.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j @Component @RequiredArgsConstructor
public class KafkaOutboxPoller {
    private final KafkaOutboxRepository outboxRepo;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Scheduled(fixedDelayString = "${app.kafka.outbox-poll-interval-ms:5000}")
    @Transactional
    public void poll() {
        List<KafkaOutbox> records = outboxRepo.findPendingForUpdate(50);
        if (records.isEmpty()) return;
        log.debug("Outbox poll: {} pending records", records.size());
        for (KafkaOutbox record : records) {
            try {
                stringKafkaTemplate.send(record.getTopic(), record.getMessageKey(), record.getPayload())
                    .get(10, TimeUnit.SECONDS);
                outboxRepo.markPublished(record.getId(), Instant.now());
                log.debug("Outbox published: id={} topic={}", record.getId(), record.getTopic());
            } catch (Exception ex) {
                log.error("Outbox publish failed: id={} error={}", record.getId(), ex.getMessage());
                outboxRepo.markAttemptFailed(record.getId(),
                    ex.getMessage() != null ? ex.getMessage().substring(0, Math.min(500, ex.getMessage().length())) : "unknown");
            }
        }
    }
}
