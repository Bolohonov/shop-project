package com.shop.kafka.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("pending_crm_tenants")
public class PendingCrmTenant {
    @EqualsAndHashCode.Include
    @Id private UUID id;
    private String email;
    private String tenantSchema;
    private Instant receivedAt;
}
