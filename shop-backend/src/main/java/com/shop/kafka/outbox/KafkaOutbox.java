package com.shop.kafka.outbox;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("kafka_outbox")
public class KafkaOutbox {
    @Id private UUID id;
    private String topic;
    private String messageKey;
    private String payload;
    private String status; // PENDING, PUBLISHED, FAILED
    private Instant createdAt;
    private Instant publishedAt;
    private int attemptCount;
    private String lastError;
}
