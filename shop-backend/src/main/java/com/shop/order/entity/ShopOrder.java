package com.shop.order.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class ShopOrder {
    @EqualsAndHashCode.Include
    @Id private UUID id;
    private UUID userId;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private String comment;
    private UUID shopOrderUuid;
    private UUID crmOrderId;
    private Instant createdAt;
    private Instant updatedAt;
}
