package com.shop.kafka.outbox;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("kafka_outbox")
public class KafkaOutbox implements Persistable<UUID> {
    @Id private UUID id;
    private String topic;
    private String messageKey;
    private String payload;
    private String status; // PENDING, PUBLISHED, FAILED
    private Instant createdAt;
    private Instant publishedAt;
    private int attemptCount;
    private String lastError;

    @Transient
    private boolean isNew = false;

    @Override
    public boolean isNew() { return isNew; }
}
