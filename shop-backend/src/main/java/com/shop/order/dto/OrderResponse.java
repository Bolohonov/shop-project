package com.shop.order.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private String status;
    private String statusLabel;
    private BigDecimal totalAmount;
    private String comment;
    private UUID shopOrderUuid;
    private List<OrderItemResponse> items;
    private List<StatusHistoryResponse> statusHistory;
    private Instant createdAt;
    private Instant updatedAt;
}
