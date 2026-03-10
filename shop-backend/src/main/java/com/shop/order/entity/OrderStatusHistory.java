package com.shop.order.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order_status_history")
public class OrderStatusHistory {
    @EqualsAndHashCode.Include
    @Id private UUID id;
    private UUID orderId;
    private String previousStatus;
    private String newStatus;
    private String changedBy;
    private String comment;
    private Instant changedAt;
}
