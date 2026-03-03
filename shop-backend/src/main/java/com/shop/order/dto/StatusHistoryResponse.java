package com.shop.order.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class StatusHistoryResponse {
    private String previousStatus;
    private String newStatus;
    private String newStatusLabel;
    private String changedBy;
    private String comment;
    private Instant changedAt;
}
