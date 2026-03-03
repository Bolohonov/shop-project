package com.shop.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatusChangedEvent {
    private UUID crmOrderId;
    private String shopOrderId;
    private UUID shopOrderUuid;
    private String previousStatus;
    private String newStatus;
    private Instant changedAt;
    private String changedBy;
    private String comment;
}
