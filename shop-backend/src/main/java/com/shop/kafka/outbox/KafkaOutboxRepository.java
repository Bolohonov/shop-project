package com.shop.kafka.outbox;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface KafkaOutboxRepository extends CrudRepository<KafkaOutbox, UUID> {
    @Query("SELECT * FROM kafka_outbox WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT :limit FOR UPDATE SKIP LOCKED")
    List<KafkaOutbox> findPendingForUpdate(int limit);

    @Modifying
    @Query("UPDATE kafka_outbox SET status = 'PUBLISHED', published_at = :publishedAt WHERE id = :id")
    void markPublished(UUID id, Instant publishedAt);

    @Modifying
    @Query("UPDATE kafka_outbox SET attempt_count = attempt_count + 1, last_error = :error, status = CASE WHEN attempt_count >= 4 THEN 'FAILED' ELSE status END WHERE id = :id")
    void markAttemptFailed(UUID id, String error);
}
