package com.shop.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantCreatedEvent {
    private UUID tenantId;
    private String tenantSchema;
    private String adminEmail;
    private Instant activatedAt;
}
